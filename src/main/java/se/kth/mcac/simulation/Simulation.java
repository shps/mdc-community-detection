package se.kth.mcac.simulation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import se.kth.mcac.util.ModularityComputer;

/**
 *
 * @author hooman
 */
public class Simulation {

    static final String EXPERIMENT_NAME = "cd";
    static boolean GEPHI_INPUT = false;
    static final String FILE_DIRECTORY = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/" + EXPERIMENT_NAME + "/";
    static final String OUTPUT_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/results/" + EXPERIMENT_NAME + "/";
    static final String SIM_OUTPUT_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/results/" + EXPERIMENT_NAME + "/";
    static int MAX_ROUND = 9;
    static int MIN_ROUND = 2;
    static final String EDGE_FILE = "edges.csv";
    static String SIM_FILE_NAME = "sbvs";
    static final int MIN_COMMUNITY_SIZE = 1;
    static final int MAX_COMMUNITY_SIZE = Integer.MAX_VALUE;
    static double sumLatency = 0;
    static int totalLatencies = 0;
    static HashMap<Node, HashMap<Node, Float>> pairBwResults;
    static HashMap<Node, HashMap<Node, Float>> pairSharedBwResults;
    static HashMap<Node, Float> totalBwResults;
    static HashMap<Node, Float> totalCachedBwResults;
    static HashMap<Node, Double> totalSharedBwResults;

    static List[] latencies;
    static List[] minBws;
    static List[] minSharedBws;
    static List[] inLatencies;
    static List[] outLatencies;
    static List[] inBw;
    static List[] outBw;
    static List<Double>[] communitySizes;
    static double[][] bcs;
    static int IMAGE_SIZE = 112; // Cloudy (current version, 23 March 2015) is 336MB * 8 = 2688Mb. // Cirros 14MB
    static boolean APPLY_RANDOM_ROUTING = false;
    static float SMART_ROUTING_THRESHOLD = 0;
    static boolean SIMULATE = false;
    static int ROUNDS = 1000;

    static HashMap<Node, HashMap<Node, TreeNode>> routingMap = new HashMap<>();
    static HashMap<Node, HashMap<Node, List<Edge>>> simpleRoutingMap = new HashMap<>();
    static List<Double> modularities = new LinkedList<>();

    static SecureRandom r = new SecureRandom();
    static Graph g;

    private static void init() {
        pairBwResults = new HashMap<>();
        pairSharedBwResults = new HashMap<>();
        totalBwResults = new HashMap<>();
        totalCachedBwResults = new HashMap<>();
        totalSharedBwResults = new HashMap<>();

        routingMap = new HashMap<>();
        simpleRoutingMap = new HashMap<>();
        sumLatency = 0;
        totalLatencies = 0;
    }

    public static void main(String[] args) throws IOException {
        if (EXPERIMENT_NAME.equalsIgnoreCase("m")) {
            GEPHI_INPUT = true;
        }
        latencies = new List[MAX_ROUND - 1];
        minBws = new List[MAX_ROUND - 1];
        minSharedBws = new List[MAX_ROUND - 1];
        inLatencies = new List[MAX_ROUND - 1];
        outLatencies = new List[MAX_ROUND - 1];
        inBw = new List[MAX_ROUND - 1];
        outBw = new List[MAX_ROUND - 1];
        communitySizes = new List[MAX_ROUND - 1];
        bcs = new double[11][52];
        for (int i = MIN_ROUND; i <= MAX_ROUND; i++) {
            String nodeFile = String.format("%s%d.csv", FILE_DIRECTORY, i);
            CsvConvertor convertor = new CsvConvertor();
            if (GEPHI_INPUT) {
                g = convertor.convertAndReadGephi(nodeFile, FILE_DIRECTORY + EDGE_FILE);
            } else {
                g = convertor.convertAndRead(nodeFile, FILE_DIRECTORY + EDGE_FILE);
            }
            init();
            print(String.format("Graph %s, %s, Nodes = %d, Edges = %d", nodeFile, EDGE_FILE, g.size(), g.getNumOfEdges()));

            if (SIMULATE) {
                simulateScenarios(i);
            } else {
                latencies[i - MIN_ROUND] = new LinkedList();
                minBws[i - MIN_ROUND] = new LinkedList();
                minSharedBws[i - MIN_ROUND] = new LinkedList();
                inLatencies[i - MIN_ROUND] = new LinkedList();
                outLatencies[i - MIN_ROUND] = new LinkedList();
                inBw[i - MIN_ROUND] = new LinkedList();
                outBw[i - MIN_ROUND] = new LinkedList();
                communitySizes[i - MIN_ROUND] = new LinkedList();
                calculateScenarios(i);
            }
        }

        if (!SIMULATE) {
            StringBuilder sb = new StringBuilder();
            for (int i = MIN_ROUND; i <= MAX_ROUND; i++) {
                sb.append(i).append(",");
            }
            CsvConvertor.writeModularities(modularities, OUTPUT_DIR, "", "modularities", MIN_ROUND);
            CsvConvertor.writeTotalLatencies(latencies, OUTPUT_DIR, "total-latencies", sb.toString());
            CsvConvertor.writeTotalLatencies(minBws, OUTPUT_DIR, "total-bws", sb.toString());
            CsvConvertor.writeTotalLatencies(minSharedBws, OUTPUT_DIR, "total-shared-bws", sb.toString());
            CsvConvertor.writeTotalLatencies(inLatencies, OUTPUT_DIR, "inLatencies", sb.toString());
            CsvConvertor.writeTotalLatencies(outLatencies, OUTPUT_DIR, "outLatencies", sb.toString());
            CsvConvertor.writeTotalLatencies(inBw, OUTPUT_DIR, "inBw", sb.toString());
            CsvConvertor.writeTotalLatencies(outBw, OUTPUT_DIR, "outBw", sb.toString());
            CsvConvertor.writeTotalLatencies(communitySizes, OUTPUT_DIR, "community-sizes", sb.toString());
            CsvConvertor.writeSizes(inLatencies, outLatencies, OUTPUT_DIR, "link-sizes", sb.toString(), MIN_ROUND);
            CsvConvertor.writeTotalResults(bcs, OUTPUT_DIR, "", "total-bcs");
        }
    }

    private static void simulateScenarios(int round) throws IOException {

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

        CsvConvertor.writeBetweennessOutPut(round, g, OUTPUT_DIR, "", "bbc");

        for (Node n : computes) {
            Node controller = comController.get(n.getCommunityId());
            Node dbmq = comDbmq.get(n.getCommunityId());
            double t = bootVM(controller, dbmq, n, true, false);
            print(String.format("Total BootVM Time: %f", t));
            totalSharedBwResults.put(n, t);
        }

        // Run simulation for different number of parallel boot VMs.
        int rounds = ROUNDS; // Number of rounds
        double[][] total = new double[nComputes][rounds];
        for (int c = 1; c <= nComputes; c++) {
            double[][] results = new double[rounds][c];
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
                double[] bootTimes = new double[c];
                for (int j = 0; j < targets.length; j++) {
                    Node compute = targets[j];
                    Node controller = comController.get(compute.getCommunityId());
                    Node dbmq = comDbmq.get(compute.getCommunityId());
                    bootTimes[j] = bootVM(controller, dbmq, compute, true, false);
                }

                results[k] = bootTimes;
            }

//            CsvConvertor.writeRandomBootVMTime(c, results, SIM_OUTPUT_DIR, "", SIM_FILE_NAME);
            for (int j = 0; j < rounds; j++) {
                float sum = 0;
                for (int k = 0; k < c; k++) {
                    sum += results[j][k];
                }
                total[c - 1][j] = sum / c; // Compute mean.
            }
        }

        CsvConvertor.writeBootVmOutput(
                round,
                totalSharedBwResults,
                OUTPUT_DIR,
                "simpbv",
                "");

        CsvConvertor.writeTotalResults(total, SIM_OUTPUT_DIR, "", String.format("%s%d", SIM_FILE_NAME, round));
    }

    public static void calculateScenarios(int round) throws FileNotFoundException, IOException {

        modularities.add(ModularityComputer.compute(g, true));
        for (Node n : g.getNodes()) {
            for (Edge e : n.getEdges()) {
                if (n.getCommunityId() == g.getNode(e.getDst()).getCommunityId()) {
                    inLatencies[round - MIN_ROUND].add(e.getLatency());
                    inBw[round - MIN_ROUND].add(e.getBw());
                } else {
                    outLatencies[round - MIN_ROUND].add(e.getLatency());
                    outBw[round - MIN_ROUND].add(e.getBw());
                }
            }
        }

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

        int i = 0;
        for (Node n : g.getNodes()) {
            bcs[round - MIN_ROUND][i] = n.getBc();
            i++;
        }

        while (iterator.hasNext()) {
            Entry<Integer, HashMap<String, Node>> entry = iterator.next();
            communitySizes[round - MIN_ROUND].add((double) entry.getValue().size());
            execute(entry.getKey(), entry.getValue(), false, round);
        }

//        CsvConvertor.writeBetweennessOutPut(round, g, OUTPUT_DIR, "", "bc");
//        CsvConvertor.writePairOutput(
//                round,
//                totalLResults,
//                OUTPUT_DIR,
//                "",
//                "lresult");
//        CsvConvertor.writePairOutput(
//                round,
//                pairBwResults,
//                OUTPUT_DIR,
//                "",
//                "ft");
//        CsvConvertor.writePairOutput(
//                round,
//                pairSharedBwResults,
//                OUTPUT_DIR,
//                "",
//                "pft");
//        CsvConvertor.writeBootVmOutput(
//                round,
//                totalSharedBwResults,
//                OUTPUT_DIR,
//                "pbv",
//                "");
//
//        CsvConvertor.writeBootVmOutput(
//                round,
//                totalBwResults,
//                OUTPUT_DIR,
//                "bv",
//                "");
//        CsvConvertor.writeBootVmOutput(
//                round,
//                totalCachedBwResults,
//                OUTPUT_DIR,
//                "cbv",
//                "");
        print(String.format("Average Latency for %d comparisons %f", totalLatencies, sumLatency / (double) totalLatencies));

        // TODO: print results
    }

    public static void execute(
            int communityId,
            HashMap<String, Node> nodes,
            boolean printResult, int round) throws FileNotFoundException {
        if (nodes.size() <= MIN_COMMUNITY_SIZE || nodes.size() > MAX_COMMUNITY_SIZE) {
            print(String.format("ID: %d, Inappropriate community size %d", communityId, nodes.size()));
            return;
        }

//        print(String.format("**** Boot VM execution for community %d, Size: %d ****", communityId, nodes.size()));
//
//        // assign the Openstack rolls to the nodes.
//        Node controller = nodes.get(OpenStackUtil.selectController(
//                SelectionStrategy.BETWEENNESS_CENTRALITY,
//                nodes,
//                routingMap, g)); // Openstack Controler
//        Node dbmq = nodes.get(OpenStackUtil.selectDbmq(
//                SelectionStrategy.BETWEENNESS_CENTRALITY,
//                controller,
//                nodes,
//                routingMap, g,
//                controller)); // database and message queue
//        print(String.format("Controller: %s, DBMQ: %s", controller.getName(), dbmq.getName()));
//        Node[] computes = new Node[nodes.size() - 2];
//        Iterator<Entry<String, Node>> iterator = nodes.entrySet().iterator();
//        int i = 0;
//        while (iterator.hasNext()) {
//            Node n = iterator.next().getValue();
//            if (!n.equals(controller) && !n.equals(dbmq)) {
//                computes[i] = n;
//                i++;
//            }
//        }
//
//        for (Node n : computes) {
//            float t = bootVM(controller, dbmq, n, false, false);
//            print(String.format("Total BootVM Time: %f", t));
//            totalBwResults.put(n, t);
//        }
//
//        for (Node n : computes) {
//            float t = bootVM(controller, dbmq, n, false, true);
//            totalCachedBwResults.put(n, t);
//        }
//
//        for (Node n : computes) {
//            float t = bootVM(controller, dbmq, n, true, false);
//            print(String.format("Total BootVM Time: %f", t));
//            totalSharedBwResults.put(n, t);
//        }
        print(String.format("**** Intra-DC nodes latency for community %d, Size: %d ****", communityId, nodes.size()));
        HashMap<Node, HashMap<Node, Double>> lResults = new HashMap<>();
        for (Node n1 : nodes.values()) {
            lResults.put(n1, new HashMap<Node, Double>());
//            totalLResults.put(n1, new HashMap<Node, Float>());
            for (Node n2 : nodes.values()) {
                if (!n1.equals(n2)) {
                    TreeNode path = getPath(n1, n2);
                    double l = computeLatency(path);
                    latencies[round - MIN_ROUND].add(l);
                    totalLatencies++;
                    sumLatency += l;
                    print(String.format("%s\t%s\t%f", n1.getName(), n2.getName(), l));
                    lResults.get(n1).put(n2, l);
                    minBws[round - MIN_ROUND].add(findMinBw(path, false).getBw());
                    minSharedBws[round - MIN_ROUND].add(findMinBw(path, true).getSharedBw());
//                    totalLResults.get(n1).put(n2, l);
                }
            }
        }

//        if (printResult) {
//            CsvConvertor.writePairOutput(
//                    communityId,
//                    lResults,
//                    OUTPUT_DIR,
//                    String.format("Controller: %s, DBMQ: %s", controller.getName(), dbmq.getName()), "lresult");
//        }
//        print(String.format("**** Intra-DC nodes File Transfer for community %d, Size: %d ****", communityId, nodes.size()));
//        for (Node n1 : nodes.values()) {
//            pairBwResults.put(n1, new HashMap<Node, Float>());
//            for (Node n2 : nodes.values()) {
//                if (!n1.equals(n2)) {
//                    TreeNode p = getPath(n1, n2);
//                    float l = computeLatency(p);
//                    Edge minBwEdge = findMinBw(p, false);
//                    float minBw = minBwEdge.getBw();
//                    float bvTime = (IMAGE_SIZE / minBw) + (l / 1000);
//                    print(String.format("%s\t%s\t%f", n1.getName(), n2.getName(), bvTime));
//                    pairBwResults.get(n1).put(n2, bvTime);
//                }
//            }
//        }
//        print(String.format("**** Intra-DC concurrent nodes File Transfer for community %d, Size: %d ****", communityId, nodes.size()));
//        for (Node n1 : nodes.values()) {
//            pairSharedBwResults.put(n1, new HashMap<Node, Float>());
//            for (Node n2 : nodes.values()) {
//                if (!n1.equals(n2)) {
//                    TreeNode p = getPath(n1, n2);
//                    float l = computeLatency(p);
//                    Edge minBwEdge = findMinBw(p, true);
//                    float minBw = minBwEdge.getSharedBw();
//                    float ftTime = (IMAGE_SIZE / minBw) + (l / 1000);
//                    pairSharedBwResults.get(n1).put(n2, ftTime);
//                }
//            }
//        }
    }

    private static double bootVM(
            Node controller,
            Node dbmq,
            Node compute, boolean sharedBw, boolean cached) {

        // compute controller-dbmq communication cost in terms of the latency
        print(String.format("Compute Node: %s", compute.getName()));

        double controllerDbmqLatency = computeLatency(getPath(controller, dbmq));
        double dbmqControllerLatency = computeLatency(getPath(dbmq, controller));
        double computeDbmqLatency = computeLatency(getPath(compute, dbmq));
        double dbmqComputeLatency = computeLatency(getPath(dbmq, compute));
        double computeControllerLatency = computeLatency(getPath(compute, controller));
        TreeNode ccp = getPath(controller, compute);
        double controllerComputeLatency = computeLatency(ccp);

        double l = OpenStackUtil.computeBootVMLatency(
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
        double minBw;
        if (sharedBw) {
            minBw = minBwEdge.getSharedBw();
        } else {
            minBw = minBwEdge.getBw();
        }
        double bvTime = ((IMAGE_SIZE / minBw) + (controllerComputeLatency / 1000)) + (l / 1000);
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

    private static double computeLatency(TreeNode head) {

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
        double minBw = Double.MAX_VALUE;
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
