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
package playground.agarwalamit.munich.analysis.modeSwitcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.collections.Tuple;

import playground.agarwalamit.analysis.legMode.distributions.LegModeTravelTimeHandler;

/**
 *This will first find mode switchers and then returns trip times in groups. 
 *<p>
 * @author amit
 */

public class ModeSwitchersTripTime {

	private Logger log = Logger.getLogger(ModeSwitchersTripTime.class);

	private ModeSwitcherInfoCollector modeSwitchInfo;
	
	public static void main(String[] args) {

		String dir = "/Users/amit/Documents/repos/runs-svn/detEval/emissionCongestionInternalization/output/1pct/run9/";
		String runCases[] ={"baseCaseCtd","ei","ci","eci","ei_10"};

		for(String runNr : runCases){
			ModeSwitchersTripTime mstt = new ModeSwitchersTripTime();
			mstt.run(dir+runNr);
			mstt.modeSwitchInfo.writeModeSwitcherTripTimes(dir+runNr);
		}
	}

	public ModeSwitchersTripTime (){
		modeSwitchInfo = new ModeSwitcherInfoCollector();
	}

	public void run (String runCase){

		// data from event files
		String eventsFile_first_it = runCase+"/ITERS/it.1000/1000.events.xml.gz";
		String eventsFile_last_it = runCase+"/ITERS/it.1500/1500.events.xml.gz";

		Map<Id<Person>, List<Tuple<String, Double>>> person2ModeTravelTimes_first_it = getPerson2mode2TripTimes(eventsFile_first_it);
		Map<Id<Person>, List<Tuple<String, Double>>> person2ModeTravelTimes_last_it = getPerson2mode2TripTimes(eventsFile_last_it);

		// start processing
		for(Id<Person> pId : person2ModeTravelTimes_first_it.keySet()){

			if(person2ModeTravelTimes_last_it.containsKey(pId) ) {

				int numberOfLegs = 0; 
				if(person2ModeTravelTimes_last_it.get(pId).size() != person2ModeTravelTimes_first_it.get(pId).size()) {
					//	if person does not have same number of trips as in first iteration

					this.log.warn("Person "+pId+" do not have the same number of trip legs in the two maps. This could be due to stuck and abort event. "
							+ "\n Thus including only minimum legs (removing the common trups) for that person.");
					numberOfLegs  = Math.min(person2ModeTravelTimes_last_it.get(pId).size(),person2ModeTravelTimes_first_it.get(pId).size());

				} else numberOfLegs = person2ModeTravelTimes_last_it.get(pId).size();

				for(int ii=0; ii<numberOfLegs;ii++){

					Tuple<String, Double> first_it_mode = person2ModeTravelTimes_first_it.get(pId).get(ii);
					Tuple<String, Double> last_it_mode = person2ModeTravelTimes_last_it.get(pId).get(ii);

					String firstMode = getTravelMode(first_it_mode.getFirst());
					String lastMode = getTravelMode(last_it_mode.getFirst());
					
					String switchTyp = firstMode.concat("2").concat(lastMode);
					ModeSwitcherType modeSwitchType = ModeSwitcherType.valueOf(switchTyp);
					this.modeSwitchInfo.storeTripTimeInfo(pId, modeSwitchType, new Tuple<Double, Double>(first_it_mode.getSecond(), last_it_mode.getSecond()));
				} 

			} else if(!person2ModeTravelTimes_last_it.containsKey(pId)) {

				this.log.warn("Person "+pId+ "is not present in the last iteration map. This person is thus not included in the results. Probably due to stuck and abort event.");

			} 
		}
	}
	
	private String getTravelMode(String mode){
		if(mode.equals(TransportMode.car)) return "car";
		else return "nonCar";
}

	private Map<Id<Person>, List<Tuple<String, Double>>> getPerson2mode2TripTimes(String eventsFile){

		EventsManager events = EventsUtils.createEventsManager();
		MatsimEventsReader reader = new MatsimEventsReader(events);

		LegModeTravelTimeHandler timeHandler = new LegModeTravelTimeHandler();
		events.addHandler(timeHandler);

		reader.readFile(eventsFile);

		SortedMap<String,Map<Id<Person>,List<Double>>> mode2Person2TripTimes = timeHandler.getLegMode2PesonId2TripTimes();

		Map<Id<Person>, List<Tuple<String, Double>>> person2ModeTravelTimes = new HashMap<>();


		for(String mode : mode2Person2TripTimes.keySet()){
			for (Id<Person> p : mode2Person2TripTimes.get(mode).keySet()){
				for(Double d :mode2Person2TripTimes.get(mode).get(p)){
					Tuple<String, Double> mode2TripTime = new Tuple<String, Double>(mode, d);

					if (person2ModeTravelTimes.containsKey(p)){
						List<Tuple<String, Double>> mode2TripTimeList  = person2ModeTravelTimes.get(p);
						mode2TripTimeList.add(mode2TripTime);
					} else {
						List<Tuple<String, Double>> mode2TripTimeList = new ArrayList<Tuple<String,Double>>();
						mode2TripTimeList.add(mode2TripTime);
						person2ModeTravelTimes.put(p, mode2TripTimeList);
					}

				}
			}
		}
		return person2ModeTravelTimes;
	}
}
