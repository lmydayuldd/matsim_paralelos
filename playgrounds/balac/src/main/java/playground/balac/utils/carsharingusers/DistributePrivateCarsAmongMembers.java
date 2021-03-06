package playground.balac.utils.carsharingusers;

import java.util.Random;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.*;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

public class DistributePrivateCarsAmongMembers {

	public static void main(String[] args) {

		MutableScenario scenario = (MutableScenario) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		PopulationReader populationReader = new MatsimPopulationReader(scenario);
		MatsimNetworkReader networkReader = new MatsimNetworkReader(scenario);
		networkReader.readFile(args[0]);
		populationReader.readFile(args[1]);
		
		Random rand = MatsimRandom.getRandom();
		
		double x = 650.0 / 1309.0;
		for (Person p : scenario.getPopulation().getPersons().values()) {
			
			if (rand.nextDouble() < x)
			
				PersonUtils.setCarAvail(p, "always");
			else
				PersonUtils.setCarAvail(p, "never");

		}
		
		
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).writeFileV4(args[2] + "/plans_memb_fixedPrivateCars.xml.gz");		

		
		
	}

}
