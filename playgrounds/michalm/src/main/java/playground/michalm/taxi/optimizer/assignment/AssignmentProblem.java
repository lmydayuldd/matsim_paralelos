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

package playground.michalm.taxi.optimizer.assignment;

import java.lang.reflect.Array;
import java.util.*;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.*;
import org.matsim.contrib.dvrp.path.*;
import org.matsim.contrib.locationchoice.router.BackwardFastMultiNodeDijkstra;
import org.matsim.core.router.*;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import com.google.common.collect.Iterables;

import playground.michalm.taxi.data.TaxiRequest;
import playground.michalm.taxi.optimizer.*;
import playground.michalm.taxi.optimizer.filter.*;
import playground.michalm.taxi.scheduler.TaxiSchedulerUtils;


public class AssignmentProblem
{
    private static final double NULL_PATH_COST = 48 * 60 * 60; //2 days

    private final TaxiOptimizerConfiguration optimConfig;
    private final MultiNodeDijkstra router;

    private final BackwardFastMultiNodeDijkstra backwardRouter;

    private final RequestFilter requestFilter;
    private final KStraightLineNearestVehicleDepartureFilter vehicleFilter;

    //1800: default
    //666: avg. drive with passenger is around 10 min, all dropoffs are 1 min
    //300: avg. pickup drive is around 5 min
    //420: avg. pickup drive is around 5 min, all pickups are 2 min
    private final double vehPlanningHorizonOversupply = 120;//seconds

    //10: frequency of re-optimization 
    private final double vehPlanningHorizonUndersupply = 30;//seconds

    private VehicleData vData;
    private AssignmentRequestData rData;


    public AssignmentProblem(TaxiOptimizerConfiguration optimConfig, MultiNodeDijkstra router,
            BackwardFastMultiNodeDijkstra backwardRouter)
    {
        this.optimConfig = optimConfig;
        this.router = router;
        this.requestFilter = optimConfig.filterFactory.createRequestFilter();
        this.vehicleFilter = new KStraightLineNearestVehicleDepartureFilter(40);

        this.backwardRouter = backwardRouter;
    }


    public void scheduleUnplannedRequests(SortedSet<TaxiRequest> unplannedRequests)
    {
        if (initDataAndCheckIfSchedulingRequired(unplannedRequests)) {
            RequestPathData[][] pathDataMatrix = createPathDataMatrix();
            double[][] costMatrix = createCostMatrix(pathDataMatrix);
            int[] assignments = new HungarianAlgorithm(costMatrix).execute();
            scheduleRequests(assignments, pathDataMatrix, unplannedRequests);
        }
    }


    private boolean initDataAndCheckIfSchedulingRequired(SortedSet<TaxiRequest> unplannedRequests)
    {
        rData = new AssignmentRequestData(optimConfig, unplannedRequests, 0);//only immediate reqs
        if (rData.dimension == 0) {
            return false;
        }

        int idleVehs = Iterables
                .size(Iterables.filter(optimConfig.context.getVrpData().getVehicles().values(),
                        TaxiSchedulerUtils.createIsIdle(optimConfig.scheduler)));
        
        if (idleVehs < rData.urgentReqCount) {
            vData = new VehicleData(optimConfig, vehPlanningHorizonUndersupply);
        }
        else {
            vData = new VehicleData(optimConfig, vehPlanningHorizonOversupply);
        }

        return vData.dimension > 0;
    }


    private static class RequestPathData
    {
        private Node node;//destination
        private double delay;//at the first and last links
        private Path path;//shortest path
    }


    static int calcPathsForVehiclesCount = 0;
    static int calcPathsForRequestsCount = 0;


    private RequestPathData[][] createPathDataMatrix()
    {
        RequestPathData[][] pathDataMatrix = (RequestPathData[][])Array
                .newInstance(RequestPathData.class, vData.dimension, rData.dimension);

        if (rData.dimension > vData.dimension) {
            calcPathsForVehicles(pathDataMatrix);
            calcPathsForVehiclesCount++;
        }
        else {
            calcPathsForRequests(pathDataMatrix);
            calcPathsForRequestsCount++;
        }

        if ( (calcPathsForRequestsCount + calcPathsForVehiclesCount) % 100 == 0) {
            System.err.println("PathsForRequests = " + calcPathsForRequestsCount
                    + " PathsForVehicles = " + calcPathsForVehiclesCount);
            System.err.println("reqs = " + rData.dimension + " vehs = " + vData.dimension
                    + " idleVehs = " + vData.idleCount);
        }

        return pathDataMatrix;
    }


    private void calcPathsForVehicles(RequestPathData[][] pathDataMatrix)
    {
        for (int v = 0; v < vData.dimension; v++) {
            VehicleData.Entry departure = vData.entries.get(v);
            Node fromNode = departure.link.getToNode();

            Map<Id<Node>, InitialNode> reqInitialNodes = new HashMap<>();
            Map<Id<Node>, Path> pathsToReqNodes = new HashMap<>();

            Iterable<TaxiRequest> filteredReqs = requestFilter
                    .filterRequestsForVehicle(rData.requests, departure.vehicle);

            for (TaxiRequest req : filteredReqs) {
                int r = rData.reqIdx.get(req.getId());
                RequestPathData pathData = pathDataMatrix[v][r] = new RequestPathData();
                Link reqLink = req.getFromLink();

                if (departure.link == reqLink) {
                    //hack: we are basically there (on the same link), so let's pretend reqNode == fromNode
                    pathData.node = fromNode;
                    pathData.delay = 0;
                }
                else {
                    pathData.node = reqLink.getFromNode();
                    //simplified, but works for taxis, since pickup trips are short (about 5 mins)
                    pathData.delay = 1 + reqLink.getFreespeed(departure.time);
                }

                if (!reqInitialNodes.containsKey(pathData.node.getId())) {
                    InitialNode newInitialNode = new InitialNode(pathData.node, 0, 0);
                    reqInitialNodes.put(pathData.node.getId(), newInitialNode);
                }
            }

            ImaginaryNode toNodes = router.createImaginaryNode(reqInitialNodes.values());
            Path path = router.calcLeastCostPath(fromNode, toNodes, departure.time, null, null);
            Node bestReqNode = path.nodes.get(path.nodes.size() - 1);
            pathsToReqNodes.put(bestReqNode.getId(), path);

            //get paths for all remaining endNodes 
            for (InitialNode i : reqInitialNodes.values()) {
                Node reqNode = i.node;
                if (reqNode.getId() != bestReqNode.getId()) {
                    path = router.constructPath(fromNode, reqNode, departure.time);
                    pathsToReqNodes.put(reqNode.getId(), path);
                }
            }

            for (TaxiRequest req : filteredReqs) {
                int r = rData.reqIdx.get(req.getId());
                RequestPathData pathData = pathDataMatrix[v][r];
                pathData.path = pathsToReqNodes.get(pathData.node.getId());
            }
        }
    }


    //TODO does not support adv reqs
    private void calcPathsForRequests(RequestPathData[][] pathDataMatrix)
    {
        double currTime = optimConfig.context.getTime();

        for (int r = 0; r < rData.dimension; r++) {
            TaxiRequest req = rData.requests.get(r);
            Link toLink = req.getFromLink();
            Node toNode = toLink.getFromNode();

            Map<Id<Node>, InitialNode> vehInitialNodes = new HashMap<>();
            Map<Id<Node>, Path> pathsFromVehNodes = new HashMap<>();

            Iterable<VehicleData.Entry> filteredVehs = vehicleFilter
                    .filterVehiclesForRequest(vData.entries, req);

            for (VehicleData.Entry departure : filteredVehs) {
                int v = departure.idx;
                RequestPathData pathData = pathDataMatrix[v][r] = new RequestPathData();

                if (departure.link == toLink) {
                    //hack: we are basically there (on the same link), so let's pretend vehNode == toNode
                    pathData.node = toNode;
                    pathData.delay = 0;
                }
                else {
                    pathData.node = departure.link.getToNode();
                    //simplified, but works for taxis, since pickup trips are short (about 5 mins)
                    pathData.delay = 1 + toLink.getFreespeed(departure.time);
                }

                if (!vehInitialNodes.containsKey(pathData.node.getId())) {
                    InitialNode newInitialNode = new InitialNode(pathData.node, 0, 0);
                    vehInitialNodes.put(pathData.node.getId(), newInitialNode);
                }
            }

            ImaginaryNode fromNodes = backwardRouter.createImaginaryNode(vehInitialNodes.values());
            Path path = backwardRouter.calcLeastCostPath(toNode, fromNodes, currTime, null, null);
            Node bestVehNode = path.nodes.get(path.nodes.size() - 1);
            pathsFromVehNodes.put(bestVehNode.getId(), path);

            //get paths for all remaining endNodes 
            for (InitialNode i : vehInitialNodes.values()) {
                Node vehNode = i.node;
                if (vehNode.getId() != bestVehNode.getId()) {
                    path = backwardRouter.constructPath(toNode, vehNode, currTime);
                    pathsFromVehNodes.put(vehNode.getId(), path);
                }
            }

            for (VehicleData.Entry departure : filteredVehs) {
                int v = departure.idx;
                RequestPathData pathData = pathDataMatrix[v][r];
                pathData.path = pathsFromVehNodes.get(pathData.node.getId());
            }
        }
    }


    private double[][] createCostMatrix(RequestPathData[][] pathDataMatrix)
    {
        boolean reduceTP = doReduceTP();
        double[][] costMatrix = new double[vData.dimension][rData.dimension];

        for (int v = 0; v < vData.dimension; v++) {
            VehicleData.Entry departure = vData.entries.get(v);

            for (int r = 0; r < rData.dimension; r++) {
                RequestPathData pathData = pathDataMatrix[v][r];

                double travelTime = pathData == null ? //
                        NULL_PATH_COST : // no path (too far away)
                        pathData.delay + pathData.path.travelTime;

                double pickupBeginTime = Math.max(rData.requests.get(r).getT0(),
                        departure.time + travelTime);

                costMatrix[v][r] = reduceTP ? //
                        //this will work different than (B) at oversupply -> will reduce T_P and fairness
                        pickupBeginTime - departure.time : // T_P

                		//(A) more fairness, lower throughput
                		//this will work different than than (B) at undersupply -> will reduce unfairness and throughput 
                		//pickupBeginTime - rData.requests.get(r).getT0(); // all T_W

                		//(B)less fairness, higher throughput
                		pickupBeginTime;// remaining T_W (probably win-win situation)
            }
        }

        return costMatrix;
    }


    private boolean doReduceTP()
    {
        switch (optimConfig.goal) {
            case MIN_PICKUP_TIME:
                return true;

            case MIN_WAIT_TIME:
                return false;

            case DEMAND_SUPPLY_EQUIL:
                return rData.urgentReqCount > vData.idleCount;

            default:
                throw new IllegalStateException();
        }
    }


    private void scheduleRequests(int[] assignments, RequestPathData[][] pathDataMatrix,
            SortedSet<TaxiRequest> unplannedRequests)
    {
        for (int v = 0; v < assignments.length; v++) {
            int r = assignments[v];

            if (r == -1 || //no request assigned
                    r >= rData.dimension) {// non-existing (dummy) request assigned
                continue;
            }

            VehicleData.Entry departure = vData.entries.get(v);
            TaxiRequest req = rData.requests.get(r);
            RequestPathData pathData = pathDataMatrix[v][r];

            VrpPathWithTravelData vrpPath = pathData == null ? //
                    VrpPaths.calcAndCreatePath(departure.link, req.getFromLink(), departure.time,
                            router, optimConfig.travelTime)
                    : VrpPaths.createPath(departure.link, req.getFromLink(), departure.time,
                            pathData.path, optimConfig.travelTime);

            optimConfig.scheduler.scheduleRequest(departure.vehicle, req, vrpPath);
            unplannedRequests.remove(req);
        }
    }
}
