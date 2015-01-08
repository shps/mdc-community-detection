/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.simulation.communitynetwork.RoutingProtocolsUtil;
import se.kth.mcac.simulation.communitynetwork.RoutingProtocolsUtil.RoutingProtocols;

/**
 *
 * @author hooman
 */
public class Simulation {

    public static void main(String[] args) {
        Graph g = null;

        HashMap<Integer, HashMap<String, Node>> communities = g.getCommunities();
        Iterator<Map.Entry<Integer, HashMap<String, Node>>> iterator = communities.entrySet().iterator();
        while (iterator.hasNext()) {
            execute(iterator.next().getValue());
        }

    }

    private static void execute(HashMap<String, Node> nodes) {
        HashMap<Node, HashMap<Node, List<Edge>>> routingMap = new HashMap<>();
        Iterator<Map.Entry<String, Node>> iterator = nodes.entrySet().iterator();
        while (iterator.hasNext()) {
            Node n = iterator.next().getValue();
            routingMap.put(n, RoutingProtocolsUtil.findRoutings(n, nodes, RoutingProtocols.SIMPLE_SHORTEST_PATH));
        }

        //TODO: find controller, dbqm and compute nodes
        Node controller = null; // Openstack Controler
        Node dbmq = null; // database and message queue
        Node[] computes = new Node[nodes.size() - 2];

        bootVM(controller, dbmq, computes, routingMap);
    }

    /**
     * BootVM simplified scenario:
     *
     * 1- controller - dbmq (taking token) 2- controller - dbmq (token
     * validation) 3- controller - dbmq (initial db entry for the new instance)
     * 4- controller - dbmq (nova-controller put message) 5- controller - dbmq
     * (scheduler pick message) 6- controller - dbmq (scheduler query database)
     * 7- controller - dbmq (scheduler update database) 8- controller - dbmq
     * (scheduler put launch message) 9- compute - dbmq (compute pick message)
     * 10- compute - dbqm (compute put instance info request message) 11-
     * controller - dbmq (conductor pick message) 12- controller - dbmq
     * (conductor query database) 13- controller - dbmq (conductor put instance
     * info message) 14- compute - dbmq (compute picks the instance info) 15-
     * compute - controller (compute asks for the image) 16- controller - dbmq
     * (glance validate the token) 17- controller - compute (sends the image
     * metadata) 18- compute - controller (download image) 19- compute -
     * controller (ask for the network configuration) 20- controller - dbmq
     * (token validation) 21- controller - compute (sends the network info) 22-
     * compute - controller (ask for the volume) 23- controller - dbmq (token
     * validation) 24- controller - compute (sends volume info)
     *
     */
    private static void bootVM(
            Node controller,
            Node dbmq,
            Node[] computes,
            HashMap<Node, HashMap<Node, List<Edge>>> routingMap) {

        // compute controller-dbmq communication cost in terms of the latency
        List<Edge> controllerDbmq = routingMap.get(controller).get(dbmq);
        List<Edge> dbmqController = routingMap.get(dbmq).get(controller);
        Node compute = selectComputeNode(computes);
        List<Edge> computeDbmq = routingMap.get(compute).get(dbmq);
        List<Edge> dbmqCompute = routingMap.get(dbmq).get(compute);
        List<Edge> computeController = routingMap.get(compute).get(controller);
        List<Edge> controllerCompute = routingMap.get(controller).get(compute);

        float controllerDbmqLatency = computeLatency(controllerDbmq);
        float dbmqControllerLatency = computeLatency(dbmqController);
        float computeDbmqLatency = computeLatency(computeDbmq);
        float dbmqComputeLatency = computeLatency(dbmqCompute);
        float computeControllerLatency = computeLatency(computeController);
        float controllerComputeLatency = computeLatency(controllerCompute);

        //14 controller-dbmq communications
        // 3 compute-dbmq communications
        // 7 compute-controller communications
        float t = 14 * (controllerDbmqLatency + dbmqControllerLatency)
                + 3 * (computeDbmqLatency + dbmqComputeLatency)
                + 7 * (computeControllerLatency + controllerComputeLatency);
    }

    /**
     * Selects a compute node in a uniformly random fashion.
     *
     * @param computes
     * @return
     */
    private static Node selectComputeNode(Node[] computes) {
        Random r = new Random();
        return computes[r.nextInt(computes.length)];
    }

    private static float computeLatency(List<Edge> paths) {
        float l = 0;

        for (Edge e : paths) {
            l = l + e.getLatency();
        }

        return l;
    }
}
