package playground.boescpa.baseline.preparation;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.misc.Counter;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesReaderMatsimV1;

/**
 * If an activity has no facility assigned, the closest facility offering this activity will be assigned.
 *
 * @author boescpa
 */
public class FacilityAdder {

    public static void main(final String[] args) {
        final String pathToInputPopulation = args[0];
        final String pathToInputFacilities = args[1];
        final String pathToOutputPopulation = args[2];

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        MatsimPopulationReader plansReader = new MatsimPopulationReader(scenario);
        plansReader.readFile(pathToInputPopulation);
        Population population = scenario.getPopulation();
        FacilitiesReaderMatsimV1 facilitiesReader = new FacilitiesReaderMatsimV1(scenario);
        facilitiesReader.readFile(pathToInputFacilities);
        ActivityFacilities facilities = scenario.getActivityFacilities();

        addFacilitiesToPopulation(population, facilities);

        PopulationWriter writer = new PopulationWriter(population);
        writer.write(pathToOutputPopulation);
    }

    protected static void addFacilitiesToPopulation(final Population population, final ActivityFacilities facilities) {
        Counter counter = new Counter(" person # ");
        for (Person p : population.getPersons().values()) {
            counter.incCounter();
            if (p.getSelectedPlan() != null) {
                for (PlanElement pe : p.getSelectedPlan().getPlanElements()) {
                    if (pe instanceof ActivityImpl) {
                        ActivityImpl act = (ActivityImpl) pe;
                        if  (act.getFacilityId() == null) {
                            addFacilityToActivity(act, facilities);
                        }
                    }
                }
            }
        }
    }

    private static void addFacilityToActivity(ActivityImpl act, ActivityFacilities facilities) {
        Coord actCoord = act.getCoord();
        String actType = act.getType();

        ActivityFacility facility = getClosestFacility(actCoord, actType, facilities);

        act.setCoord(facility.getCoord());
        act.setFacilityId(facility.getId());
    }

    private static ActivityFacility getClosestFacility(Coord actCoord, String actType, ActivityFacilities facilities) {
        double distanceToSelectedFacility = Double.MAX_VALUE;
        ActivityFacility selectedFacility = null;

        for (ActivityFacility facility : facilities.getFacilities().values()) {
            if (facility.getActivityOptions().containsKey(actType)) {
                double distanceToCurrentFacility = CoordUtils.calcDistance(actCoord, facility.getCoord());
                if (distanceToCurrentFacility < distanceToSelectedFacility) {
                    distanceToSelectedFacility = distanceToCurrentFacility;
                    selectedFacility = facility;
                    if (distanceToSelectedFacility < 100) {
                        break;
                    }
                }
            }
        }

        return selectedFacility;
    }


}
