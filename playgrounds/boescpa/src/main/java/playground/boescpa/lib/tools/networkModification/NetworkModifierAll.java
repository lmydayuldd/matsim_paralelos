/*
 * *********************************************************************** *
 * project: org.matsim.*                                                   *
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
 * *********************************************************************** *
 */

package playground.boescpa.lib.tools.networkModification;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordUtils;

/**
 * Implementation of AbstractNetworkModifier...
 * The present setting makes all links close to Zurich HB untravelable...
 *
 * @author boescpa
 */
public class NetworkModifierAll extends AbstractNetworkModifier {

	public NetworkModifierAll(Coord center) {
		super(center);
	}

	public static void main(String[] args) {
		AbstractNetworkModifier networkModifier = new NetworkModifierAll(new Coord(682952.0, 247797.0)); // A bit south of HB Zurich...);
		networkModifier.run(args);
	}

	@Override
	public boolean isLinkAffected(Link link) {
		return CoordUtils.calcDistance(link.getFromNode().getCoord(), center) <= radius ||
				CoordUtils.calcDistance(link.getToNode().getCoord(), center) <= radius ||
				CoordUtils.calcDistance(link.getCoord(), center) <= radius;
	}
}
