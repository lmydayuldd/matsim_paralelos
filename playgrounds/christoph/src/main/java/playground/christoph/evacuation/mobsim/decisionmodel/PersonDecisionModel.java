/* *********************************************************************** *
 * project: org.matsim.*
 * PersonDecisionModel.java
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

package playground.christoph.evacuation.mobsim.decisionmodel;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;

/**
 * 
 * @author cdobler
 */
public interface PersonDecisionModel extends DecisionModel {

	public void runModel(Person person);
	
	public void runModel(Population population);
}
