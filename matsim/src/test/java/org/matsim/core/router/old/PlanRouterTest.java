/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * PlanRouterTest.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2014 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package org.matsim.core.router.old;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Injector;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.*;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeAndDisutility;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutilityFactory;
import org.matsim.core.router.util.DijkstraFactory;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.facilities.Facility;
import org.matsim.vehicles.Vehicle;

import java.util.Arrays;
import java.util.List;

public class PlanRouterTest {

    @Test
    public void passesVehicleFromOldPlan() {
        final Config config = ConfigUtils.loadConfig("test/scenarios/equil/config.xml");
        config.plans().setInputFile("test/scenarios/equil/plans1.xml");
        final Scenario scenario = ScenarioUtils.loadScenario(config);
        Injector injector = Injector.createInjector(scenario.getConfig(), new AbstractModule() {
            @Override
            public void install() {
                install(new TripRouterModule());
                bind(Scenario.class).toInstance(scenario);
                addTravelTimeBinding("car").toInstance(new FreespeedTravelTimeAndDisutility(config.planCalcScore()));
                addTravelDisutilityFactoryBinding("car").toInstance(new OnlyTimeDependentTravelDisutilityFactory());
            }
        });
        TripRouter tripRouter = injector.getInstance(TripRouter.class);
        PlanRouter testee = new PlanRouter(tripRouter);
        Plan plan = scenario.getPopulation().getPersons().get(Id.createPersonId(1)).getSelectedPlan();
        Id<Vehicle> vehicleId = Id.create(1, Vehicle.class);
        ((NetworkRoute) TripStructureUtils.getLegs(plan).get(0).getRoute()).setVehicleId(vehicleId);
        testee.run(plan);
        Assert.assertEquals("Vehicle Id transferred to new Plan", vehicleId, ((NetworkRoute) TripStructureUtils.getLegs(plan).get(0).getRoute()).getVehicleId());
    }

    @Test
    public void keepsVehicleIfTripRouterUsesOneAlready() {
        final Config config = ConfigUtils.loadConfig("test/scenarios/equil/config.xml");
        config.plans().setInputFile("test/scenarios/equil/plans1.xml");
        final Scenario scenario = ScenarioUtils.loadScenario(config);
        final DijkstraFactory leastCostAlgoFactory = new DijkstraFactory();
        final OnlyTimeDependentTravelDisutilityFactory disutilityFactory = new OnlyTimeDependentTravelDisutilityFactory();
        final FreeSpeedTravelTime travelTime = new FreeSpeedTravelTime();

        Plan plan = scenario.getPopulation().getPersons().get(Id.createPersonId(1)).getSelectedPlan();
        Id<Vehicle> oldVehicleId = Id.create(1, Vehicle.class);
        ((NetworkRoute) TripStructureUtils.getLegs(plan).get(0).getRoute()).setVehicleId(oldVehicleId);

        // A trip router which provides vehicle ids by itself.
        final Id<Vehicle> newVehicleId = Id.create(2, Vehicle.class);
        Injector injector = Injector.createInjector(scenario.getConfig(), new AbstractModule() {
            @Override
            public void install() {
                install(AbstractModule.override(Arrays.asList(new TripRouterModule()), new AbstractModule() {
                    @Override
                    public void install() {
                        bind(Scenario.class).toInstance(scenario);
                        addTravelTimeBinding("car").toInstance(new FreespeedTravelTimeAndDisutility( config.planCalcScore() ));
                        addTravelDisutilityFactoryBinding("car").toInstance(new OnlyTimeDependentTravelDisutilityFactory());
                    }
                }));
            }
        });
        TripRouter tripRouter = injector.getInstance(TripRouter.class);
        RoutingModule routingModule = new RoutingModule() {
            @Override
            public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime, Person person) {
                List<? extends PlanElement> trip = DefaultRoutingModules.createNetworkRouter("car", scenario.getPopulation().getFactory(), 
                		scenario.getNetwork(), 
                		leastCostAlgoFactory.createPathCalculator(scenario.getNetwork(), disutilityFactory.createTravelDisutility(travelTime, config.planCalcScore()), travelTime)
                		).calcRoute(fromFacility, toFacility, departureTime, person);
                ((NetworkRoute) TripStructureUtils.getLegs(trip).get(0).getRoute()).setVehicleId(newVehicleId);
                return trip;
            }

            @Override
            public StageActivityTypes getStageActivityTypes() {
                return EmptyStageActivityTypes.INSTANCE;
            }
        };
        tripRouter.setRoutingModule("car", routingModule);

        PlanRouter testee = new PlanRouter(tripRouter);
        testee.run(plan);
        Assert.assertEquals("Vehicle Id from TripRouter used", newVehicleId, ((NetworkRoute) TripStructureUtils.getLegs(plan).get(0).getRoute()).getVehicleId());
    }

}