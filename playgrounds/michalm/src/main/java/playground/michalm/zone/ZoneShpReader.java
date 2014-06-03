/* *********************************************************************** *
 * project: org.matsim.*
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

package playground.michalm.zone;

import java.io.IOException;
import java.util.*;

import org.matsim.api.core.v01.*;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Polygon;


public class ZoneShpReader
{
    private final Scenario scenario;
    private final Map<Id, Zone> zones;


    public ZoneShpReader(Scenario scenario, Map<Id, Zone> zones)
    {
        this.scenario = scenario;
        this.zones = zones;
    }


    public void readZones(String file)
        throws IOException
    {
        readZones(file, ZoneShpWriter.ID_HEADER);
    }


    public void readZones(String file, String idHeader)
        throws IOException
    {
        ShapeFileReader shpReader = new ShapeFileReader();
        Collection<SimpleFeature> features = shpReader.readFileAndInitialize(file);

        if (features.size() != zones.size()) {
            throw new RuntimeException("Features#: " + features.size() + "; zones#: "
                    + zones.size());
        }

        for (SimpleFeature ft : features) {
            String id = ft.getAttribute(idHeader).toString();
            Zone z = zones.get(scenario.createId(id));
            z.setPolygon((Polygon)ft.getDefaultGeometry());
        }
    }
}