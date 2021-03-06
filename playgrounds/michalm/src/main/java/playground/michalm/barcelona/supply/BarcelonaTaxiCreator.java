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

package playground.michalm.barcelona.supply;

import org.matsim.api.core.v01.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.*;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.utils.geometry.geotools.MGC;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.prep.PreparedPolygon;

import playground.michalm.barcelona.BarcelonaZones;
import playground.michalm.zone.util.RandomPointUtils;


public class BarcelonaTaxiCreator
    implements VehicleCreator
{
    private static final int PAXPERCAR = 4;

    private final NetworkImpl network;
    private final PreparedPolygon preparedPolygon;

    private int currentVehicleId = 0;


    public BarcelonaTaxiCreator(Scenario scenario)
    {
        network = (NetworkImpl)scenario.getNetwork();
        preparedPolygon = new PreparedPolygon(BarcelonaZones.readAgglomerationArea());
    }


    @Override
    public Vehicle createVehicle(double t0, double t1)
    {
        Id<Vehicle> vehId = Id.create("taxi" + currentVehicleId++, Vehicle.class);
        Point p = RandomPointUtils.getRandomPointInGeometry(preparedPolygon);
        Link link = network.getNearestLinkExactly(MGC.point2Coord(p));
        return new VehicleImpl(vehId, link, PAXPERCAR, Math.round(t0), Math.round(t1));
    }
}