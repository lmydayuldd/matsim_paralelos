/* *********************************************************************** *
 * project: org.matsim.*
 * DgLiveOtfIvtch
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package playground.dgrether.activityendtimes;

import org.matsim.core.controler.Controler;

import playground.dgrether.DgPaths;



/**
 * @author dgrether
 *
 */
public class EndTimesStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String config = DgPaths.STUDIESDG + "activityEndTimeConfig.xml";
		
		
//		OTFVisController controller = new OTFVisController(config);
		Controler controller = new Controler(config);
		controller.setOverwriteFiles(true);
		controller.addControlerListener(new CalculateActivityEndTimesControllerListener());
		controller.run();
	}

}
