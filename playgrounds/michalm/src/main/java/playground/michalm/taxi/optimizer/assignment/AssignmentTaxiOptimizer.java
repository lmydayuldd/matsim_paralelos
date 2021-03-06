/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.michalm.taxi.optimizer.assignment;

import java.util.*;

import org.matsim.contrib.dvrp.data.Requests;
import org.matsim.contrib.locationchoice.router.*;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.router.*;
import org.matsim.core.router.util.RoutingNetwork;

import playground.michalm.taxi.data.TaxiRequest;
import playground.michalm.taxi.optimizer.*;


public class AssignmentTaxiOptimizer
    extends AbstractTaxiOptimizer
{
    private final MultiNodeDijkstra router;

    private final BackwardFastMultiNodeDijkstra backwardRouter;


    public AssignmentTaxiOptimizer(TaxiOptimizerConfiguration optimConfig)
    {
        super(optimConfig, new TreeSet<TaxiRequest>(Requests.ABSOLUTE_COMPARATOR), true);

        router = new MultiNodeDijkstra(optimConfig.context.getScenario().getNetwork(),
                optimConfig.travelDisutility, optimConfig.travelTime, true);

        FastRouterDelegateFactory fastRouterFactory = new ArrayFastRouterDelegateFactory();
        RoutingNetwork routingNetwork = new InverseArrayRoutingNetworkFactory(null)
                .createRoutingNetwork(optimConfig.context.getScenario().getNetwork());
        backwardRouter = new BackwardFastMultiNodeDijkstra(routingNetwork,
                optimConfig.travelDisutility, optimConfig.travelTime, null, fastRouterFactory,
                true);
    }

    
    @Override
    public void notifyMobsimBeforeSimStep(@SuppressWarnings("rawtypes") MobsimBeforeSimStepEvent e)
    {
        if (e.getSimulationTime() % 10 == 0) {
            super.notifyMobsimBeforeSimStep(e);
        }
    }

    protected void scheduleUnplannedRequests()
    {
        new AssignmentProblem(optimConfig, router, backwardRouter)
                .scheduleUnplannedRequests((SortedSet<TaxiRequest>)unplannedRequests);
    }
}
