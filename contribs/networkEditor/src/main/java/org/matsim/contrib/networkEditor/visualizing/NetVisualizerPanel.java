/* *********************************************************************** *
 * project: org.matsim.contrib.networkEditor
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 Daniel Ampuero
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

package org.matsim.contrib.networkEditor.visualizing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.core.utils.io.UncheckedIOException;
import org.matsim.counts.CountsReaderMatsimV1;
import org.matsim.counts.CountsWriter;
import org.matsim.utils.gis.matsim2esri.network.CapacityBasedWidthCalculator;
import org.matsim.utils.gis.matsim2esri.network.FeatureGeneratorBuilderImpl;
import org.matsim.utils.gis.matsim2esri.network.Links2ESRIShape;
import org.matsim.utils.gis.matsim2esri.network.PolygonFeatureGenerator;


/**
 *
 * @author danielmaxx
 */
public class NetVisualizerPanel extends javax.swing.JPanel {

	private NetBlackboard board;
	private NetControls controls;
	private String crs = null;

	/** Creates new form NetVisualizerPanel */
	public NetVisualizerPanel() {
		initComponents();
		initBoard();
	}

	public NetVisualizerPanel(NetworkImpl net) {
		initComponents();
		initBoard();
		setNetToBoard(net);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		controlPanel = new javax.swing.JPanel();
		editorPanel = new javax.swing.JPanel();

		addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
			@Override
			public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
			}
			@Override
			public void ancestorResized(java.awt.event.HierarchyEvent evt) {
				formAncestorResized(evt);
			}
		});
		addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyTyped(java.awt.event.KeyEvent evt) {
				formKeyTyped(evt);
			}
		});

		controlPanel.setMinimumSize(new java.awt.Dimension(234, 383));

		javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
		controlPanel.setLayout(controlPanelLayout);
		controlPanelLayout.setHorizontalGroup(
				controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 234, Short.MAX_VALUE)
				);
		controlPanelLayout.setVerticalGroup(
				controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 470, Short.MAX_VALUE)
				);

		editorPanel.setBackground(java.awt.Color.white);

		javax.swing.GroupLayout editorPanelLayout = new javax.swing.GroupLayout(editorPanel);
		editorPanel.setLayout(editorPanelLayout);
		editorPanelLayout.setHorizontalGroup(
				editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 372, Short.MAX_VALUE)
				);
		editorPanelLayout.setVerticalGroup(
				editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 470, Short.MAX_VALUE)
				);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(editorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(controlPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(editorPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap())
				);
	}// </editor-fold>//GEN-END:initComponents

	private void formAncestorResized(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_formAncestorResized
		// TODO add your handling code here:
		setSize(this.getParent().getSize());
		repaint();
	}//GEN-LAST:event_formAncestorResized

	private void formKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyTyped
		// TODO add your handling code here:     
	}//GEN-LAST:event_formKeyTyped

	private void initBoard() {
		board = new NetBlackboard();
		board.setSize(this.editorPanel.getWidth()-50, this.editorPanel.getHeight()-50);
		this.editorPanel.add(board);

		controls = new NetControls(board);
		controls.setSize(this.controlPanel.getWidth(), this.controlPanel.getHeight()-30);
		controlPanel.add(controls);

		board.setNetControls(controls);
		controls.setNetBlackboard(board);

		this.revalidate();
	}

	public boolean setNetToBoard(NetworkImpl net) {
		//System.out.println("Asignando red:" + net);
		board.setNetwork(net);
		board.repaint();
		return true;
	}

	public boolean loadNetFromFile(String path) {
		Config config = ConfigUtils.createConfig();
		Scenario sc = ScenarioUtils.createScenario(config);
		try {
			new MatsimNetworkReader(sc).readFile(path);
			board.setNetwork((NetworkImpl)sc.getNetwork());
			board.repaint();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}        
		return true;
	}

	public boolean loadCountsFromFile(String path) {
		if(board.net == null)
			return false;
		board.clearCounts();
		CountsReaderMatsimV1 countsReader = new CountsReaderMatsimV1(board.counts);
		try {
			countsReader.parse(path);            
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean saveNetwork(String path) {
		if(board.net == null)
			return false;
		NetworkWriter netWriter = new NetworkWriter(board.net);
		try {
			netWriter.write(path);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean saveNetworkAsESRI(String path) {
		if(board.net == null)
			return false;
		String crs = this.crs;
		if (crs == null) {
			crs = "WGS84"; // better than nothing...
		}
//		String UTM19N_PROJCS = "PROJCS[\"WGS_1984_UTM_Zone_19N\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",-69],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"Meter\",1]]";
//		String crs = UTM19N_PROJCS; // the coordinate reference system to be used.
		FeatureGeneratorBuilderImpl builder = new FeatureGeneratorBuilderImpl(board.net, crs);
		builder.setWidthCoefficient(0.01);
		builder.setFeatureGeneratorPrototype(PolygonFeatureGenerator.class);
		builder.setWidthCalculatorPrototype(CapacityBasedWidthCalculator.class);
		try {
			new Links2ESRIShape(board.net, path, builder).write();
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean saveCounts(String path) {
		if(board.net == null)
			return false;
		if(board.counts == null)
			return false;
		if(board.counts.getName() == null)
			board.counts.setName(board.net.getName()+" Counts\n");
		CountsWriter countWriter = new CountsWriter(board.counts);
		try {
			countWriter.write(path);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean loadNetFromOSM(final String path, final String crs) {
		//REVISAR UTM33N a UTM19N
		//        String UTM19N_PROJCS = "PROJCS[\"WGS_1984_UTM_Zone_19N\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",-69],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"Meter\",1]]";
		//	String crs = UTM19N_PROJCS; // the coordinate reference system to be used.

		this.crs = crs;
		String osm = path;

		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig()) ;
		Network net = sc.getNetwork();

		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, crs);

		OsmNetworkReader onr = new OsmNetworkReader(net,ct); //constructs a new openstreetmap reader
		try {
			onr.parse(osm); //starts the conversion from osm to matsim
		} catch (UncheckedIOException e) {
			e.printStackTrace();
			return false;
		}
		//at this point we already have a matsim network...
		new NetworkCleaner().run(net); //but may be there are isolated (not connected) links. The network cleaner removes those links
		board.setNetwork((NetworkImpl)net);
		board.repaint();
		return true;
	}

	public void cleanNetwork() {
		board.cleanNetwork();
		board.repaint();
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel controlPanel;
	private javax.swing.JPanel editorPanel;
	// End of variables declaration//GEN-END:variables

}
