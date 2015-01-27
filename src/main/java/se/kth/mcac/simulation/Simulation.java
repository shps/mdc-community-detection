package se.kth.mcac.simulation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.simulation.communitycloud.OpenStackUtil;
import se.kth.mcac.simulation.communitycloud.OpenStackUtil.SelectionStrategy;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.RoutingProtocols;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.TreeNode;
import se.kth.mcac.util.CsvConvertor;

/**
 *
 * @author hooman
 */
public class Simulation {

    static final String FILE_DIRECTORY = "/home/ganymedian/Desktop/sant-upc/";
//    static final String NODE_FILE = "nsinglecommunity.csv";
//    static final String EDGE_FILE = "esinglecommunity.csv";
    static final String NODE_FILE = "125nodes.csv";
    static final String EDGE_FILE = "125edges.csv";
    static final int MIN_COMMUNITY_SIZE = 3;
    static final int MAX_COMMUNITY_SIZE = 100;

    public static void main(String[] args) throws IOException {
        CsvConvertor convertor = new CsvConvertor();
        Graph g = convertor.convertAndRead(FILE_DIRECTORY + NODE_FILE, FILE_DIRECTORY + EDGE_FILE);
        print(String.format("Graph %s, %s, Nodes = %d, Edges = %d", NODE_FILE, EDGE_FILE, g.size(), g.getNumOfEdges() / 2));

        HashMap<Integer, HashMap<String, Node>> communities = g.getCommunities();
        Iterator<Entry<Integer, HashMap<String, Node>>> iterator = communities.entrySet().iterator();
        HashMap<Node, HashMap<Node, TreeNode>> routingMap = new HashMap<>();
        for (Node n : g.getNodes()) {
            routingMap.put(n, RoutingProtocolsUtil.findRoutings(n, g, RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY));
        }
        HashMap<Integer, HashMap<Node, Float>> results = new HashMap<>();
        while (iterator.hasNext()) {
            Entry<Integer, HashMap<String, Node>> entry = iterator.next();
            results.put(entry.getKey(), execute(entry.getKey(), entry.getValue(), routingMap, true));
        }

        // TODO: print results
    }

    public static HashMap<Node, Float> execute(
            int communityId,
            HashMap<String, Node> nodes,
            HashMap<Node, HashMap<Node, TreeNode>> routingMap,
            boolean printResult) throws FileNotFoundException {
        HashMap<Node, Float> results = new HashMap<>();
        if (nodes.size() < MIN_COMMUNITY_SIZE || nodes.size() > MAX_COMMUNITY_SIZE) {
            print(String.format("ID: %d, Inappropriate community size %d", communityId, nodes.size()));
            return results;
        }

        print(String.format("**** Boot VM execution for community %d, Size: %d ****", communityId, nodes.size()));

        // assign the Openstack rolls to the nodes.
        Node controller = nodes.get(OpenStackUtil.selectController(
                SelectionStrategy.BETWEENNESS_CENTRALITY,
                nodes,
                routingMap)); // Openstack Controler
        Node dbmq = nodes.get(OpenStackUtil.selectDbmq(
                SelectionStrategy.BETWEENNESS_CENTRALITY,
                controller,
                nodes,
                routingMap,
                controller)); // database and message queue
        print(String.format("Controller: %s, DBMQ: %s", controller.getName(), dbmq.getName()));
        Node[] computes = new Node[nodes.size() - 2];
        Iterator<Entry<String, Node>> iterator = nodes.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Node n = iterator.next().getValue();
            if (!n.equals(controller) && !n.equals(dbmq)) {
                computes[i] = n;
                i++;
            }
        }

        for (Node n : computes) {
            float t = bootVM(controller, dbmq, n, routingMap);
            print(String.format("Total latency: %f", t));
            results.put(n, t);
        }

        if (printResult) {
            CsvConvertor.writeOutput(
                    communityId,
                    results,
                    FILE_DIRECTORY,
                    String.format("Controller: %s, DBMQ: %s", controller.getName(), dbmq.getName()));
        }

        return results;
    }

    private static float bootVM(
            Node controller,
            Node dbmq,
            Node compute,
            HashMap<Node, HashMap<Node, TreeNode>> routingMap) {

        // compute controller-dbmq communication cost in terms of the latency
        print(String.format("Compute Node: %s", compute.getName()));

        float controllerDbmqLatency = computeLatency(routingMap.get(controller).get(dbmq));
        float dbmqControllerLatency = computeLatency(routingMap.get(dbmq).get(controller));
        float computeDbmqLatency = computeLatency(routingMap.get(compute).get(dbmq));
        float dbmqComputeLatency = computeLatency(routingMap.get(dbmq).get(compute));
        float computeControllerLatency = computeLatency(routingMap.get(compute).get(controller));
        float controllerComputeLatency = computeLatency(routingMap.get(controller).get(compute));

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

    private static float computeLatency(TreeNode head) {

        if (head.getBranches().isEmpty()) {
            return 0;
        }

        return head.getEdges().get(0).getLatency() + computeLatency(head.getBranches().get(0));
    }

    private static void print(String message) {
        System.out.println(message);
    }

    private static void mergeAllcommunities(Graph g) {
        //TODO make the graph to have one big community. Filter out all the disconnected nodes. Disconnected nodes can be a single node community.
    }
}
