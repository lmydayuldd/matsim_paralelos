/* *********************************************************************** *
 * project: org.matsim.*
 * TransitRouterNetworkTravelTimeAndDisutilityVariableWW.java
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

package playground.sergioo.singapore2012.transitRouterVariable;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.TravelTimeCalculatorConfigGroup;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.pt.router.CustomDataManager;
import org.matsim.pt.router.PreparedTransitSchedule;
import org.matsim.pt.router.TransitRouterConfig;
import org.matsim.pt.router.TransitRouterNetworkTravelTimeAndDisutility;
import org.matsim.vehicles.Vehicle;

import playground.sergioo.singapore2012.transitRouterVariable.TransitRouterNetworkWW.TransitRouterNetworkLink;
import playground.sergioo.singapore2012.transitRouterVariable.TransitRouterNetworkWW.TransitRouterNetworkNode;
import playground.sergioo.singapore2012.transitRouterVariable.stopStopTimes.StopStopTime;
import playground.sergioo.singapore2012.transitRouterVariable.vehicleOccupancy.VehicleOccupancy;
import playground.sergioo.singapore2012.transitRouterVariable.waitTimes.WaitTime;

/**
 * TravelTime and TravelDisutility calculator to be used with the transit network used for transit routing.
 *
 * @author sergioo
 */
public class TransitRouterNetworkTravelTimeAndDisutilityWSV extends TransitRouterNetworkTravelTimeAndDisutility implements TravelDisutility {
	
	private static final double NO_SIT_FACTOR = 1.2;
	private static final double SIT_PERCENTAGE = 0.3;
	private Link previousLink;
	private double previousTime;
	private double cachedTravelTime;
	private final Map<Id<Link>, double[]> linkTravelTimes = new HashMap<Id<Link>, double[]>();
	private final Map<Id<Link>, double[]> linkWaitingTimes = new HashMap<Id<Link>, double[]>();
	private final Map<Id<Link>, double[]> linkVehicleOccupancy = new HashMap<Id<Link>, double[]>();
	private final int numSlots;
	private final double timeSlot;
	
	public TransitRouterNetworkTravelTimeAndDisutilityWSV(final TransitRouterConfig config, TransitRouterNetworkWW routerNetwork, WaitTime waitTime, StopStopTime stopStopTime, VehicleOccupancy vehicleOccupancy, TravelTimeCalculatorConfigGroup tTConfigGroup, QSimConfigGroup qSimConfigGroup, PreparedTransitSchedule preparedTransitSchedule) {
		this(config, routerNetwork, waitTime, stopStopTime, vehicleOccupancy, tTConfigGroup, qSimConfigGroup.getStartTime(), qSimConfigGroup.getEndTime(), preparedTransitSchedule);
	}
	public TransitRouterNetworkTravelTimeAndDisutilityWSV(final TransitRouterConfig config, TransitRouterNetworkWW routerNetwork, WaitTime waitTime, StopStopTime stopStopTime, VehicleOccupancy vehicleOccupancy, TravelTimeCalculatorConfigGroup tTConfigGroup, double startTime, double endTime, PreparedTransitSchedule preparedTransitSchedule) {
		super(config, preparedTransitSchedule);
		timeSlot = tTConfigGroup.getTraveltimeBinSize();
		numSlots = (int) ((endTime-startTime)/timeSlot);
		for(TransitRouterNetworkLink link:routerNetwork.getLinks().values())
			if(link.route!=null) {
				double[] times = new double[numSlots];
				double[] occs = new double[numSlots];
				for(int slot = 0; slot<numSlots; slot++) {
					times[slot] = stopStopTime.getStopStopTime(link.fromNode.stop.getStopFacility().getId(), link.toNode.stop.getStopFacility().getId(), startTime+slot*timeSlot);
					occs[slot] = vehicleOccupancy.getVehicleOccupancy(((TransitRouterNetworkNode)link.getFromNode()).stop.getStopFacility().getId(), link.toNode.line.getId(), link.toNode.route.getId(), startTime+slot*timeSlot);
				}
				linkTravelTimes.put(link.getId(), times);
				linkVehicleOccupancy.put(link.getId(), occs);
			}
			else if(link.toNode.route!=null) {
				double[] times = new double[numSlots];
				for(int slot = 0; slot<numSlots; slot++)
					times[slot] = waitTime.getRouteStopWaitTime(link.toNode.line.getId(), link.toNode.route.getId(), link.fromNode.stop.getStopFacility().getId(), startTime+slot*timeSlot);
				linkWaitingTimes.put(link.getId(), times);
			}		
	}
	
	@Override
	public double getLinkTravelTime(final Link link, final double time, Person person, Vehicle vehicle) {
		previousLink = link;
		previousTime = time;
		TransitRouterNetworkLink wrapped = (TransitRouterNetworkLink) link;
		if (wrapped.route != null)
			//in line link
			cachedTravelTime = linkTravelTimes.get(wrapped.getId())[time/timeSlot<numSlots?(int)(time/timeSlot):(numSlots-1)];
		else if(wrapped.toNode.route!=null)
			//wait link
			cachedTravelTime = linkWaitingTimes.get(wrapped.getId())[time/timeSlot<numSlots?(int)(time/timeSlot):(numSlots-1)];
		else if(wrapped.fromNode.route==null)
			//walking link
			cachedTravelTime = wrapped.getLength()/this.config.getBeelineWalkSpeed();
		else
			//inside link
			cachedTravelTime = 0;
		return cachedTravelTime;
	}
	@Override
	public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle, final CustomDataManager dataManager) {
		boolean cachedTravelDisutility = false; 
		if(previousLink==link && previousTime==time)
			cachedTravelDisutility = true;
		TransitRouterNetworkLink wrapped = (TransitRouterNetworkLink) link;
		if (wrapped.route != null)
			return -(cachedTravelDisutility?cachedTravelTime:linkTravelTimes.get(wrapped.getId())[time/timeSlot<numSlots?(int)(time/timeSlot):(numSlots-1)])*this.config.getMarginalUtilityOfTravelTimePt_utl_s()*linkVehicleOccupancy.get(wrapped.getId())[time/timeSlot<numSlots?(int)(time/timeSlot):(numSlots-1)]>SIT_PERCENTAGE?NO_SIT_FACTOR:1
					- link.getLength() * (this.config.getMarginalUtilityOfTravelDistancePt_utl_m()-2.7726/100000);
		else if (wrapped.toNode.route!=null)
			// it's a wait link
			return -(cachedTravelDisutility?cachedTravelTime:linkWaitingTimes.get(wrapped.getId())[time/timeSlot<numSlots?(int)(time/timeSlot):(numSlots-1)])*this.config.getMarginalUtilityOfWaitingPt_utl_s()
					- this.config.getUtilityOfLineSwitch_utl();
		else if(wrapped.fromNode.route==null)
			// it's a transfer link (walk)
			return -(cachedTravelDisutility?cachedTravelTime:wrapped.getLength()/this.config.getBeelineWalkSpeed())*this.config.getMarginalUtilityOfTravelTimeWalk_utl_s();
		else
			//inside link
			return 0;
	}
	@Override
	public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
		TransitRouterNetworkLink wrapped = (TransitRouterNetworkLink) link;
		if (wrapped.route != null)
			return - linkTravelTimes.get(wrapped.getId())[time/timeSlot<numSlots?(int)(time/timeSlot):(numSlots-1)]*this.config.getMarginalUtilityOfTravelTimePt_utl_s()*linkVehicleOccupancy.get(wrapped.getId())[time/timeSlot<numSlots?(int)(time/timeSlot):(numSlots-1)]>SIT_PERCENTAGE?NO_SIT_FACTOR:1
					- link.getLength() * (this.config.getMarginalUtilityOfTravelDistancePt_utl_m()-2.7726/100000);
		else if (wrapped.toNode.route!=null)
			// it's a wait link
			return - linkWaitingTimes.get(wrapped.getId())[time/timeSlot<numSlots?(int)(time/timeSlot):(numSlots-1)]*this.config.getMarginalUtilityOfWaitingPt_utl_s()
					- this.config.getUtilityOfLineSwitch_utl();
		else if(wrapped.fromNode.route==null)
			// it's a transfer link (walk)
			return -wrapped.getLength()/this.config.getBeelineWalkSpeed()*this.config.getMarginalUtilityOfTravelTimeWalk_utl_s();
		else
			//inside link
			return 0;
	}
	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		return 0;
	}

}
