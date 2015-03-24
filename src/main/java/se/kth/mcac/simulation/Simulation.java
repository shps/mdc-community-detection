package se.kth.mcac.simulation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import se.kth.mcac.graph.Edge;
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

    static final String FILE_DIRECTORY = "/home/ganymedian/Desktop/sant-upc/samples/experiments/cd/";
    static final String OUTPUT_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/";
    static final String NODE_FILE = "0mgroupnodes.csv";
    static final String EDGE_FILE = "0mgroupedges.csv";
//    static final String NODE_FILE = "avg-graph.csvnodes.csv";
//    static final String EDGE_FILE = "avg-graph.csvedges.csv";
    static final int PREFIX = -2;
    static final int MIN_COMMUNITY_SIZE = 3;
    static final int MAX_COMMUNITY_SIZE = 100;
    static double sumLatency = 0;
    static int totalLatencies = 0;
    static final HashMap<Node, HashMap<Node, Float>> totalLResults = new HashMap<>();
    static final HashMap<Node, HashMap<Node, Float>> pairBwResults = new HashMap<>();
    static final HashMap<Node, Float> totalBwResults = new HashMap<>();
    static int IMAGE_SIZE = 2688; // Cloudy (current version, 23 March 2015) is 336MB * 8 = 2688Mb.

    public static void main(String[] args) throws IOException {
        CsvConvertor convertor = new CsvConvertor();
        Graph g = convertor.convertAndRead(FILE_DIRECTORY + NODE_FILE, FILE_DIRECTORY + EDGE_FILE);
        print(String.format("Graph %s, %s, Nodes = %d, Edges = %d", NODE_FILE, EDGE_FILE, g.size(), g.getNumOfEdges()));

        HashMap<Integer, HashMap<String, Node>> communities = g.getCommunities();
        Iterator<Entry<Integer, HashMap<String, Node>>> iterator = communities.entrySet().iterator();
        HashMap<Node, HashMap<Node, TreeNode>> routingMap = new HashMap<>();
        for (Node n : g.getNodes()) {
            routingMap.put(n, RoutingProtocolsUtil.findRoutings(n, g, RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY));
        }
        HashMap<Integer, HashMap<Node, Float>> results = new HashMap<>();
        while (iterator.hasNext()) {
            Entry<Integer, HashMap<String, Node>> entry = iterator.next();
//            if (entry.getKey() == 11 || entry.getKey() == 42 || entry.getKey() == 40 || entry.getKey() == 21) {
            results.put(entry.getKey(), execute(entry.getKey(), entry.getValue(), routingMap, false));
//            } else {
//                Iterator<Entry<String, Node>> it = entry.getValue().entrySet().iterator();
//                System.err.println(String.format("****** Community %d", entry.getKey()));
//                while (it.hasNext()) {
//                    Node n = it.next().getValue();
//                    System.err.println(String.format("Connections for node %s", n.getName()));
//                    for (Edge e : n.getEdges()) {
//                        System.err.println(String.format("%s -> %s\tBW = %f, L = %f", e.getSrc(), e.getDst(), e.getBw(), e.getLatency()));
//                        Edge dst = g.getNode(e.getDst()).getEdge(e.getSrc());
//                        System.err.println(String.format("%s -> %s\tBW = %f, L = %f", dst.getSrc(), dst.getDst(), dst.getBw(), dst.getLatency()));
//                    }
//                }
//            }
        }

        CsvConvertor.writePairOutput(
                PREFIX,
                totalLResults,
                OUTPUT_DIR,
                "",
                "lresult");
        CsvConvertor.writePairOutput(
                PREFIX,
                pairBwResults,
                OUTPUT_DIR,
                "",
                "pairbvresult");
        CsvConvertor.writeBootVmOutput(
                PREFIX,
                totalBwResults,
                OUTPUT_DIR,
                "");

        print(String.format("Average Latency for %d comparisons %f", totalLatencies, sumLatency / (double) totalLatencies));

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
            print(String.format("Total BootVM Time: %f", t));
            totalBwResults.put(n, t);
            results.put(n, t);
        }

        if (printResult) {
            CsvConvertor.writeBootVmOutput(
                    communityId,
                    results,
                    OUTPUT_DIR,
                    String.format("Controller: %s, DBMQ: %s", controller.getName(), dbmq.getName()));
        }

        print(String.format("**** Intra-DC nodes latency for community %d, Size: %d ****", communityId, nodes.size()));
        HashMap<Node, HashMap<Node, Float>> lResults = new HashMap<>();
        for (Node n1 : nodes.values()) {
            lResults.put(n1, new HashMap<Node, Float>());
            totalLResults.put(n1, new HashMap<Node, Float>());
            for (Node n2 : nodes.values()) {
                if (!n1.equals(n2)) {
                    float l = computeLatency(routingMap.get(n1).get(n2));
                    totalLatencies++;
                    sumLatency += l;
                    print(String.format("%s\t%s\t%f", n1.getName(), n2.getName(), l));
                    lResults.get(n1).put(n2, l);
                    totalLResults.get(n1).put(n2, l);
                }
            }
        }

        if (printResult) {
            CsvConvertor.writePairOutput(
                    communityId,
                    lResults,
                    OUTPUT_DIR,
                    String.format("Controller: %s, DBMQ: %s", controller.getName(), dbmq.getName()), "lresult");
        }

        print(String.format("**** Intra-DC nodes File Transfer for community %d, Size: %d ****", communityId, nodes.size()));
        for (Node n1 : nodes.values()) {
            pairBwResults.put(n1, new HashMap<Node, Float>());
            for (Node n2 : nodes.values()) {
                if (!n1.equals(n2)) {
                    float l = computeLatency(routingMap.get(n1).get(n2));
                    Edge minBwEdge = findMinBw(routingMap.get(n1).get(n2));
                    float minBw = minBwEdge.getBw();
                    float bvTime = (IMAGE_SIZE / minBw) + (l / 1000);
                    print(String.format("%s\t%s\t%f", n1.getName(), n2.getName(), bvTime));
                    pairBwResults.get(n1).put(n2, bvTime);
                }
            }
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

        float l = OpenStackUtil.computeBootVMLatency(
                controllerDbmqLatency,
                controllerComputeLatency,
                dbmqControllerLatency,
                dbmqComputeLatency,
                computeDbmqLatency,
                computeControllerLatency);

//        Edge minBwEdge = findMinBw(routingMap.get(controller).get(compute));
//        float minBw = minBwEdge.getBw();
//        float bvTime = ((IMAGE_SIZE / minBw) + controllerComputeLatency) + (l / 1000);

        return l;
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

    private static Edge findMinBw(TreeNode head) {
        float minBw = Float.MAX_VALUE;
        Edge minBwEdge = null;
        while (!head.getBranches().isEmpty()) {
            Edge e = head.getEdges().get(0);
            if (e.getBw() < minBw) {
                minBw = e.getBw();
                minBwEdge = e;
            }

            head = head.getBranches().get(0);
        }

        return minBwEdge;
    }
}
