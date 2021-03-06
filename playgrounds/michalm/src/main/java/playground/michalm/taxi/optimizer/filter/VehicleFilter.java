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

package playground.michalm.taxi.optimizer.filter;

import org.matsim.contrib.dvrp.data.Vehicle;

import playground.michalm.taxi.data.TaxiRequest;


public interface VehicleFilter
{
    VehicleFilter NO_FILTER = new VehicleFilter() {
        public Iterable<Vehicle> filterVehiclesForRequest(Iterable<Vehicle> vehicles,
                TaxiRequest request)
        {
            return vehicles;
        }
    };


    Iterable<Vehicle> filterVehiclesForRequest(Iterable<Vehicle> vehicles, TaxiRequest request);
}
