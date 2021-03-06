/* *********************************************************************** *
 * project: org.matsim.*
 * RunEmissionToolOffline.java
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
package playground.jbischoff.taxibus.run.configuration;

import org.matsim.contrib.dvrp.MatsimVrpContext;
import org.matsim.contrib.dvrp.MatsimVrpContextImpl;
import org.matsim.contrib.dvrp.data.VrpData;
import org.matsim.contrib.dvrp.run.VrpLauncherUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;

import playground.jbischoff.taxibus.sim.TaxibusQSimProvider;
import playground.jbischoff.taxibus.sim.TaxibusServiceRoutingModule;
import playground.jbischoff.taxibus.sim.TaxibusTripRouterFactory;

/**
 * @author jbischoff
 *
 */
public class ConfigBasedTaxibusLaunchUtils {
		private MatsimVrpContextImpl context;
		private Controler controler;
		
		
		
		public ConfigBasedTaxibusLaunchUtils(Controler controler) {
			this.controler = controler;
		}
		
	 
	public  void initiateTaxibusses(){
		//this is done exactly once per simulation
		final TaxibusConfigGroup tbcg = (TaxibusConfigGroup) controler.getScenario().getConfig().getModule("taxibusConfig");
      	context = new MatsimVrpContextImpl();
		context.setScenario(controler.getScenario());
		VrpData vrpData = VrpLauncherUtils.initVrpData(context, tbcg.getVehiclesFile());
		
		context.setVrpData(vrpData);	 
           
		controler.addOverridingModule(new AbstractModule(){

			@Override
			public void install() {
				addRoutingModuleBinding("taxibus").toInstance(new TaxibusServiceRoutingModule(controler));
			}
			
		});
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				bindMobsim().toProvider(TaxibusQSimProvider.class);
				bind(MatsimVrpContext.class).toInstance(context);
			}
		});
		
		
		
	} 
}
