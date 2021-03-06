/* *********************************************************************** *
 * project: org.matsim.*
 * PenaltyTravelCost.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.christoph.evacuation.router.util;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.vehicles.Vehicle;

public class PenaltyTravelCost implements TravelDisutility {

	private final TravelDisutility travelCost;
	private final PenaltyCalculator penaltyCalculator;
	
	public PenaltyTravelCost(TravelDisutility travelCost, PenaltyCalculator penaltyCalculator) {
		this.travelCost = travelCost;
		this.penaltyCalculator = penaltyCalculator;
	}
	
	@Override
	public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
		double tc = travelCost.getLinkTravelDisutility(link, time, person, vehicle);
		double penaltyFactor = penaltyCalculator.getPenaltyFactor(link.getId(), time);
		
		return tc * penaltyFactor;
	}
	
	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}