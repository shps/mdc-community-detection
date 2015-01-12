/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.simulation.communitycloud.OpenStackUtil;
import se.kth.mcac.simulation.communitycloud.OpenStackUtil.SelectionStrategy;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.RoutingProtocols;
import se.kth.mcac.util.CsvConvertor;

/**
 *
 * @author hooman
 */
public class Simulation {

    static final String FILE_DIRECTORY = "/home/ganymedian/Desktop/sant-upc/";
    static final String NODE_FILE = "85nodes.csv";
    static final String EDGE_FILE = "85edges.csv";
    static final int MIN_COMMUNITY_SIZE = 3;
    static final int MAX_COMMUNITY_SIZE = 100;

    public static void main(String[] args) throws IOException {
        CsvConvertor convertor = new CsvConvertor();
        Graph g = convertor.convertAndRead(FILE_DIRECTORY + NODE_FILE, FILE_DIRECTORY + EDGE_FILE);
        print(String.format("Graph %s, %s, Nodes = %d, Edges = %d", NODE_FILE, EDGE_FILE, g.size(), g.getNumOfEdges() / 2));

        HashMap<Integer, HashMap<String, Node>> communities = g.getCommunities();
        Iterator<Entry<Integer, HashMap<String, Node>>> iterator = communities.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, HashMap<String, Node>> entry = iterator.next();
            execute(entry.getKey(), entry.getValue());
        }

    }

    public static float execute(int communityId, HashMap<String, Node> nodes) {
        if (nodes.size() < MIN_COMMUNITY_SIZE || nodes.size() > MAX_COMMUNITY_SIZE) {
            print(String.format("ID: %d, Inappropriate community size %d", communityId, nodes.size()));
            return -1;
        }

        print(String.format("**** Boot VM execution for community %d, Size: %d ****", communityId, nodes.size()));
        HashMap<Node, HashMap<Node, List<Edge>>> routingMap = new HashMap<>();
        Iterator<Entry<String, Node>> iterator = nodes.entrySet().iterator();
        while (iterator.hasNext()) {
            Node n = iterator.next().getValue();
            routingMap.put(n, RoutingProtocolsUtil.findRoutings(n, nodes, RoutingProtocols.SIMPLE_SHORTEST_PATH));
        }

        // assign the Openstack rolls to the nodes.
        Node controller = nodes.get(OpenStackUtil.selectNode(SelectionStrategy.BETWEENNESS_CENTRALITY, routingMap)); // Openstack Controler
        Node dbmq = nodes.get(OpenStackUtil.selectNode(SelectionStrategy.BETWEENNESS_CENTRALITY, routingMap, controller)); // database and message queue
        print(String.format("Controller: %s, DBMQ: %s", controller.getName(), dbmq.getName()));
        Node[] computes = new Node[nodes.size() - 2];
        iterator = nodes.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Node n = iterator.next().getValue();
            if (!n.equals(controller) && !n.equals(dbmq)) {
                computes[i] = n;
                i++;
            }
        }

        float t = bootVM(controller, dbmq, computes, routingMap);

        print(String.format("Total latency: %f", t));
        
        return t;
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
    private static float bootVM(
            Node controller,
            Node dbmq,
            Node[] computes,
            HashMap<Node, HashMap<Node, List<Edge>>> routingMap) {

        // compute controller-dbmq communication cost in terms of the latency
        List<Edge> controllerDbmq = routingMap.get(controller).get(dbmq);
        List<Edge> dbmqController = routingMap.get(dbmq).get(controller);
        Node compute = selectComputeNode(computes);
        print(String.format("Compute Node: %s", compute.getName()));
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
        float t = OpenStackUtil.computeBootVMLatency(
                controllerDbmqLatency,
                controllerComputeLatency,
                dbmqControllerLatency,
                dbmqComputeLatency,
                computeDbmqLatency,
                computeControllerLatency);

        return t;
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

    private static void print(String message) {
        System.out.println(message);
    }

    private static void mergeAllcommunities(Graph g) {
        //TODO make the graph to have one big community. Filter out all the disconnected nodes. Disconnected nodes can be a single node community.
    }
}
