/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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

package playground.johannes.gsv.popsim;

import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import playground.johannes.gsv.synPop.analysis.AnalyzerTask;
import playground.johannes.synpop.data.CommonKeys;
import playground.johannes.synpop.data.Person;

import java.util.Collection;
import java.util.Map;

/**
 * @author johannes
 *
 */
public class AgeIncomeCorrelation extends AnalyzerTask {

	/* (non-Javadoc)
	 * @see playground.johannes.gsv.synPop.analysis.AnalyzerTask#analyze(java.util.Collection, java.util.Map)
	 */
	@Override
	public void analyze(Collection<? extends Person> persons, Map<String, DescriptiveStatistics> results) {
		TDoubleArrayList ages = new TDoubleArrayList();
		TDoubleArrayList incomes = new TDoubleArrayList();
		
		for(Person person : persons) {
			String aStr = person.getAttribute(CommonKeys.PERSON_AGE);
			String iStr = person.getAttribute(CommonKeys.HH_INCOME);
//			String mStr = person.getAttribute(CommonKeys.HH_MEMBERS);
			
//			if(aStr != null && iStr != null && mStr != null) {
			if(aStr != null && iStr != null) {
				double age = Double.parseDouble(aStr);
				double income = Double.parseDouble(iStr);
//				double members = Double.parseDouble(mStr);
				
				ages.add(age);
//				incomes.add(income/members);
				incomes.add(income);
			}
		}

		throw new RuntimeException("Johannes, I commented out the lines below because they did not compile (and if the project does not compile, "
				+ "travis does not even make it into the main and contrib tests).  Sorry ... Kai") ;
		
		/*
		try {
//			TDoubleDoubleHashMap hist = Histogram.createHistogram(ages.toArray(), new LinearDiscretizer(5), false);
			TDoubleDoubleHashMap hist = Histogram.createHistogram(ages.toArray(), new DummyDiscretizer(), false);
			StatsWriter.writeHistogram(hist, "age", "n", getOutputDirectory() + "/age.txt");
			
			hist = Histogram.createHistogram(incomes.toArray(), new LinearDiscretizer(500), false);
//			hist = Histogram.createHistogram(incomes.toArray(), new InterpolatingDiscretizer(incomes.toArray()), false);
			StatsWriter.writeHistogram(hist, "income", "n", getOutputDirectory() + "/income.txt");
			
			StatsWriter.writeScatterPlot(ages, incomes, "age", "income", getOutputDirectory() + "/age.income.txt");
			
			StatsWriter.writeHistogram(Correlations.mean(ages.toArray(), incomes.toArray()), "age", "income", getOutputDirectory() + "/age.income.mean.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

}
