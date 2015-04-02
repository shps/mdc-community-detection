package se.kth.mcac.simulation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
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
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.TreeNode;
import se.kth.mcac.util.CsvConvertor;

/**
 *
 * @author hooman
 */
public class Simulation {

    static final String FILE_DIRECTORY = "/home/ganymedian/Desktop/sant-upc/samples/experiments/cd/";
    static final String OUTPUT_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/simbv/";
    static final String SIM_OUTPUT_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/simbv/";
//    static final String NODE_FILE = "geograph-nodes.csv";
//    static final String EDGE_FILE = "geograph-edges.csv";
//    static final String NODE_FILE = "0mgroupnodes.csv";
//    static final String EDGE_FILE = "0mgroupedges.csv";
    static final String NODE_FILE = "avg-graph.csvnodes.csv";
    static final String EDGE_FILE = "avg-graph.csvedges.csv";
    static final int PREFIX = -1;
    static String SIM_FILE_NAME = "sbvs";
    static final int MIN_COMMUNITY_SIZE = 3;
    static final int MAX_COMMUNITY_SIZE = 100;
    static double sumLatency = 0;
    static int totalLatencies = 0;
    static final HashMap<Node, HashMap<Node, Float>> totalLResults = new HashMap<>();
    static final HashMap<Node, HashMap<Node, Float>> pairBwResults = new HashMap<>();
    static final HashMap<Node, HashMap<Node, Float>> pairSharedBwResults = new HashMap<>();
    static final HashMap<Node, Float> totalBwResults = new HashMap<>();
    static final HashMap<Node, Float> totalCachedBwResults = new HashMap<>();
    static final HashMap<Node, Float> totalSharedBwResults = new HashMap<>();
    static int IMAGE_SIZE = 112; // Cloudy (current version, 23 March 2015) is 336MB * 8 = 2688Mb. // Cirros 14MB
    static boolean APPLY_RANDOM_ROUTING = false;
    static float SMART_ROUTING_THRESHOLD = 0;
    static boolean SIMULATE = true;
    static int ROUNDS = 20;
//    static String SIM_FILE_NAME = "sbvs";

    static final HashMap<Node, HashMap<Node, TreeNode>> routingMap = new HashMap<>();
    static final HashMap<Node, HashMap<Node, List<Edge>>> simpleRoutingMap = new HashMap<>();

    static SecureRandom r = new SecureRandom();
    static Graph g;

    public static void main(String[] args) throws IOException {
        if (SIMULATE) {
            simulateScenarios();
        } else {
            calculateScenarios();
        }
    }

    private static void simulateScenarios() throws IOException {
        CsvConvertor convertor = new CsvConvertor();
        g = convertor.convertAndRead(FILE_DIRECTORY + NODE_FILE, FILE_DIRECTORY + EDGE_FILE);
        print(String.format("Graph %s, %s, Nodes = %d, Edges = %d", NODE_FILE, EDGE_FILE, g.size(), g.getNumOfEdges()));

        HashMap<Integer, HashMap<String, Node>> communities = g.getCommunities();
        Iterator<Entry<Integer, HashMap<String, Node>>> iterator = communities.entrySet().iterator();

        for (Node n : g.getNodes()) {
            routingMap.put(n, RoutingProtocolsUtil.findRoutings(n, g, RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY));
        }

        int nComputes = g.getNodes().length - (g.getNumCommunities() * 2);
        // Assign OpenStack roles to nodes.
        HashMap<Integer, Node> comController = new HashMap<>();
        HashMap<Integer, Node> comDbmq = new HashMap<>();
        Node[] computes = new Node[nComputes];
        int i = 0;
        while (iterator.hasNext()) {
            Entry<Integer, HashMap<String, Node>> entry = iterator.next();
            HashMap<String, Node> nodes = entry.getValue();
            Node controller = nodes.get(OpenStackUtil.selectController(
                    SelectionStrategy.BETWEENNESS_CENTRALITY,
                    nodes,
                    routingMap, g)); // Openstack Controler
            Node dbmq = nodes.get(OpenStackUtil.selectDbmq(
                    SelectionStrategy.BETWEENNESS_CENTRALITY,
                    controller,
                    nodes,
                    routingMap, g,
                    controller));
            comController.put(entry.getKey(), controller);
            comDbmq.put(entry.getKey(), dbmq);
            Iterator<Entry<String, Node>> iterator2 = nodes.entrySet().iterator();
            while (iterator2.hasNext()) {
                Node n = iterator2.next().getValue();
                if (!n.equals(controller) && !n.equals(dbmq)) {
                    computes[i] = n;
                    i++;
                }
            }
        }

        // Update Betweenness Centrality of the nodes
        OpenStackUtil.resetBcs(g);
        for (Node n : computes) {
            Node controller = comController.get(n.getCommunityId());
            OpenStackUtil.updateBcsOnNodes(controller, n, routingMap.get(controller).get(n), g);
        }
        
        CsvConvertor.writeBetweennessOutPut(PREFIX, g, OUTPUT_DIR, "", "bc");

        for (Node n : computes) {
            Node controller = comController.get(n.getCommunityId());
            Node dbmq = comDbmq.get(n.getCommunityId());
            float t = bootVM(controller, dbmq, n, true, false);
            print(String.format("Total BootVM Time: %f", t));
            totalSharedBwResults.put(n, t);
        }

        // Run simulation for different number of parallel boot VMs.
        int rounds = ROUNDS; // Number of rounds
        float[][] total = new float[nComputes][rounds];
        for (int c = 1; c <= nComputes; c++) {
            float[][] results = new float[rounds][c];
            for (int k = 0; k < rounds; k++) {
                ArrayList<Integer> list = new ArrayList<>(computes.length);
                for (int j = 0; j < computes.length; j++) {
                    list.add(j);
                }
                Collections.shuffle(list);
                Node[] targets = new Node[c];
                for (int j = 0; j < c; j++) {
                    targets[j] = computes[list.get(j)];
                }

                // Update Betweenness Centrality of the nodes
                OpenStackUtil.resetBcs(g);
                for (Node n : targets) {
                    Node controller = comController.get(n.getCommunityId());
                    OpenStackUtil.updateBcsOnNodes(controller, n, routingMap.get(controller).get(n), g);
                }

                // Compute BootVM time for the selected compute nodes.
                float[] bootTimes = new float[c];
                for (int j = 0; j < targets.length; j++) {
                    Node compute = targets[j];
                    Node controller = comController.get(compute.getCommunityId());
                    Node dbmq = comDbmq.get(compute.getCommunityId());
                    bootTimes[j] = bootVM(controller, dbmq, compute, true, false);
                }

                results[k] = bootTimes;
            }

            CsvConvertor.writeRandomBootVMTime(c, results, SIM_OUTPUT_DIR, "", SIM_FILE_NAME);
            for (int j = 0; j < rounds; j++) {
                float sum = 0;
                for (int k = 0; k < c; k++) {
                    sum += results[j][k];
                }
                total[c - 1][j] = sum / c; // Compute mean.
            }
        }

        CsvConvertor.writeBootVmOutput(
                PREFIX,
                totalSharedBwResults,
                OUTPUT_DIR,
                "pbv",
                "");

        CsvConvertor.writeAverageRandomBootVMTime(PREFIX, total, SIM_OUTPUT_DIR, "", SIM_FILE_NAME);
    }

    public static void calculateScenarios() throws FileNotFoundException, IOException {
        CsvConvertor convertor = new CsvConvertor();
        g = convertor.convertAndRead(FILE_DIRECTORY + NODE_FILE, FILE_DIRECTORY + EDGE_FILE);
        print(String.format("Graph %s, %s, Nodes = %d, Edges = %d", NODE_FILE, EDGE_FILE, g.size(), g.getNumOfEdges()));

        HashMap<Integer, HashMap<String, Node>> communities = g.getCommunities();
        Iterator<Entry<Integer, HashMap<String, Node>>> iterator = communities.entrySet().iterator();
        Iterator<Entry<Integer, HashMap<String, Node>>> iterator2 = communities.entrySet().iterator();
        for (Node n : g.getNodes()) {
            routingMap.put(n, RoutingProtocolsUtil.findRoutings(n, g, RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY));
        }

        for (Node n : g.getNodes()) {
            simpleRoutingMap.put(n, RoutingProtocolsUtil.findRoutingsSimple(n, g));
        }

        while (iterator2.hasNext()) {
            Entry<Integer, HashMap<String, Node>> entry = iterator2.next();
            OpenStackUtil.computeBetweennessCentralityScores(entry.getValue(), routingMap, g, true);
        }

        HashMap<Integer, HashMap<Node, Float>> results = new HashMap<>();
        HashMap<String, Float> bcNodes;
        while (iterator.hasNext()) {
            Entry<Integer, HashMap<String, Node>> entry = iterator.next();
            results.put(entry.getKey(), execute(entry.getKey(), entry.getValue(), false));
        }

        CsvConvertor.writeBetweennessOutPut(PREFIX, g, OUTPUT_DIR, "", "bc");

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
                "ft");
        CsvConvertor.writePairOutput(
                PREFIX,
                pairSharedBwResults,
                OUTPUT_DIR,
                "",
                "pft");
        CsvConvertor.writeBootVmOutput(
                PREFIX,
                totalSharedBwResults,
                OUTPUT_DIR,
                "pbv",
                "");

        CsvConvertor.writeBootVmOutput(
                PREFIX,
                totalBwResults,
                OUTPUT_DIR,
                "bv",
                "");
        CsvConvertor.writeBootVmOutput(
                PREFIX,
                totalCachedBwResults,
                OUTPUT_DIR,
                "cbv",
                "");

        print(String.format("Average Latency for %d comparisons %f", totalLatencies, sumLatency / (double) totalLatencies));

        // TODO: print results
    }

    public static HashMap<Node, Float> execute(
            int communityId,
            HashMap<String, Node> nodes,
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
                routingMap, g)); // Openstack Controler
        Node dbmq = nodes.get(OpenStackUtil.selectDbmq(
                SelectionStrategy.BETWEENNESS_CENTRALITY,
                controller,
                nodes,
                routingMap, g,
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
            float t = bootVM(controller, dbmq, n, false, false);
            print(String.format("Total BootVM Time: %f", t));
            totalBwResults.put(n, t);
            results.put(n, t);
        }

        for (Node n : computes) {
            float t = bootVM(controller, dbmq, n, false, true);
            totalCachedBwResults.put(n, t);
            results.put(n, t);
        }

        for (Node n : computes) {
            float t = bootVM(controller, dbmq, n, true, false);
            print(String.format("Total BootVM Time: %f", t));
            totalSharedBwResults.put(n, t);
        }

        if (printResult) {
            CsvConvertor.writeBootVmOutput(
                    communityId,
                    results,
                    OUTPUT_DIR,
                    "bv",
                    String.format("Controller: %s, DBMQ: %s", controller.getName(), dbmq.getName()));
        }

        print(String.format("**** Intra-DC nodes latency for community %d, Size: %d ****", communityId, nodes.size()));
        HashMap<Node, HashMap<Node, Float>> lResults = new HashMap<>();
        for (Node n1 : nodes.values()) {
            lResults.put(n1, new HashMap<Node, Float>());
            totalLResults.put(n1, new HashMap<Node, Float>());
            for (Node n2 : nodes.values()) {
                if (!n1.equals(n2)) {
                    float l = computeLatency(getPath(n1, n2));
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
                    TreeNode p = getPath(n1, n2);
                    float l = computeLatency(p);
                    Edge minBwEdge = findMinBw(p, false);
                    float minBw = minBwEdge.getBw();
                    float bvTime = (IMAGE_SIZE / minBw) + (l / 1000);
                    print(String.format("%s\t%s\t%f", n1.getName(), n2.getName(), bvTime));
                    pairBwResults.get(n1).put(n2, bvTime);
                }
            }
        }

        print(String.format("**** Intra-DC concurrent nodes File Transfer for community %d, Size: %d ****", communityId, nodes.size()));
        for (Node n1 : nodes.values()) {
            pairSharedBwResults.put(n1, new HashMap<Node, Float>());
            for (Node n2 : nodes.values()) {
                if (!n1.equals(n2)) {
                    TreeNode p = getPath(n1, n2);
                    float l = computeLatency(p);
                    Edge minBwEdge = findMinBw(p, true);
                    float minBw = minBwEdge.getSharedBw();
                    float ftTime = (IMAGE_SIZE / minBw) + (l / 1000);
                    pairSharedBwResults.get(n1).put(n2, ftTime);
                }
            }
        }

        return results;
    }

    private static float bootVM(
            Node controller,
            Node dbmq,
            Node compute, boolean sharedBw, boolean cached) {

        // compute controller-dbmq communication cost in terms of the latency
        print(String.format("Compute Node: %s", compute.getName()));

        float controllerDbmqLatency = computeLatency(getPath(controller, dbmq));
        float dbmqControllerLatency = computeLatency(getPath(dbmq, controller));
        float computeDbmqLatency = computeLatency(getPath(compute, dbmq));
        float dbmqComputeLatency = computeLatency(getPath(dbmq, compute));
        float computeControllerLatency = computeLatency(getPath(compute, controller));
        TreeNode ccp = getPath(controller, compute);
        float controllerComputeLatency = computeLatency(ccp);

        float l = OpenStackUtil.computeBootVMLatency(
                controllerDbmqLatency,
                controllerComputeLatency,
                dbmqControllerLatency,
                dbmqComputeLatency,
                computeDbmqLatency,
                computeControllerLatency);

        if (cached) {
            return l;
        }

        Edge minBwEdge = findMinBw(ccp, sharedBw);
        float minBw;
        if (sharedBw) {
            minBw = minBwEdge.getSharedBw();
        } else {
            minBw = minBwEdge.getBw();
        }
        float bvTime = ((IMAGE_SIZE / minBw) + (controllerComputeLatency / 1000)) + (l / 1000);
        return bvTime;
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

    private static Edge findMinBw(TreeNode head, boolean sharedBw) {
        float minBw = Float.MAX_VALUE;
        Edge minBwEdge = null;
        while (!head.getBranches().isEmpty()) {
            Edge e = head.getEdges().get(0);
            if (!sharedBw && e.getBw() < minBw) {
                minBw = e.getBw();
                minBwEdge = e;
            } else if (sharedBw && e.getSharedBw() < minBw) {
                minBw = e.getSharedBw();
                minBwEdge = e;
            }

            head = head.getBranches().get(0);
        }

        return minBwEdge;
    }

    private static TreeNode getPath(Node n1, Node n2) {
        if (!APPLY_RANDOM_ROUTING || r.nextFloat() < SMART_ROUTING_THRESHOLD) {
            return routingMap.get(n1).get(n2);
        } else {
            TreeNode root = new TreeNode(n1.getId(), n1.getName());
            TreeNode t = root;
            List<Edge> path = simpleRoutingMap.get(n1).get(n2);
            for (Edge e : path) {
                Node n = g.getNode(e.getDst());
                TreeNode t2 = new TreeNode(n.getId(), n.getName());
                t.addNode(t2, e);
                t = t2;
            }

            return root;
        }
    }

}
