package se.kth.mcac;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import se.kth.mcac.cd.db.DiffusionBasedCommunityDetector;
import se.kth.mcac.cd.db.MGroup;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.util.CsvConvertor;
import se.kth.mcac.util.ModularityComputer;

/**
 *
 * @author hooman
 */
public class CommunityFinder {

    static final String DEFAULT_FILE_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/cd/";
    static final String OUTPUT_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/cd/";
//    static final String FILE_NAME_NODES = "graph-53ad2481.json";
    static final String FILE_NAME_NODES = "avg-graph.csvnodes.csv";
    static final String FILE_NAME_EDGES = "avg-graph.csvedges.csv";
    static final boolean MODULARITY_FOR_WEIGHTED_DIRECTED = true;
    static final boolean EXCLUDE_DISCONNECTED_NODES = true;
    static final float INIT_COLOR_ASSIGNMENT = 1f;
    static final int START_ITERATION = 1;
    static final int END_ITERATION = START_ITERATION + 200;
    static final int INCREMENT_PER_ITERATION = 1;
    static final boolean APPLY_MGROUP = false;
    static final int APPLY_MGROUP_AFTER = 0;
    static final float THRESHOLD = 20;
    static boolean ONLY_MGROUP = false;
    static boolean RESOLVE_SINGLES = true;
    static int MAX_COMMUNITY = 50;
    static HashMap<Integer, Graph> maxs = new HashMap<>();
    static HashMap<Integer, Double> maxValues = new HashMap<>();
    static HashMap<Integer, Integer> maxIterations = new HashMap<>();
    static HashMap<Integer, List<Double>> modularities = new HashMap<>();
    static List<Double> cSizes = new LinkedList<>();
    static HashSet<Integer> selectedIterations = new HashSet<>();
    static boolean SELECTIVE = false;

    public static void main(String[] args) throws IOException, Exception {
        selectedIterations.add(191);//2
        selectedIterations.add(112);//3
        selectedIterations.add(28);//4
        selectedIterations.add(27);//5
        selectedIterations.add(21);//6
        selectedIterations.add(15);//7
        selectedIterations.add(4);//8
        selectedIterations.add(9);//9
        Graph g = new CsvConvertor().convertAndRead(DEFAULT_FILE_DIR + FILE_NAME_NODES, DEFAULT_FILE_DIR + FILE_NAME_EDGES);
        print(String.format("Graph %s, Nodes = %d, Edges = %d", FILE_NAME_NODES, g.size(), g.getNumOfEdges() / 2));
        print(String.format("INIT_COLOR_ASSIGNMENT = %f", INIT_COLOR_ASSIGNMENT));
        for (int i = 2; i <= MAX_COMMUNITY; i++) {
            maxs.put(i, null);
            maxValues.put(i, 0.0);
            maxIterations.put(i, 0);
        }

        start(g);

        Iterator<Map.Entry<Integer, Graph>> iterator = maxs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Graph> entry = iterator.next();
            Graph mg = entry.getValue();
            if (mg != null) {
                print(String.format("Iteration: %d, Max Modularity = %f, Number of Communities = %d",
                        maxIterations.get(entry.getKey()), maxValues.get(entry.getKey()), entry.getKey()));
                CsvConvertor.convertAndWrite(mg, String.format("%s%d.csv", OUTPUT_DIR, entry.getKey()));
            } else {
                print(String.format("Not found Community with size %d", entry.getKey()));
            }
        }

        CsvConvertor.writeModularities(cSizes, OUTPUT_DIR, "", "iteration-size", 1);

        CsvConvertor.writeModularityTrend(modularities, OUTPUT_DIR, "modularity-trend");
//        for (int i = 2; i < 12; i++) {
//            Graph maxGraph = start(g);
//            if (maxGraph != null) {
//                CsvConvertor.convertAndWrite(maxGraph, String.format("%s%d.csv", OUTPUT_DIR, i));
//            } else {
//                print(String.format("*****************No community with size %d", i));
//            }
//        }
    }

    public static void start(Graph g) throws IOException {
        //        SpaceSeparatedConvertor convertor = new SpaceSeparatedConvertor();
//        QmpsuConvertor convertor = new QmpsuConvertor();
//        Graph g = convertor.convertToGraph(DEFAULT_FILE_DIR + FILE_NAME_NODES, true, EXCLUDE_DISCONNECTED_NODES);
//        filterGraph(g, THRESHOLD);
//        print(String.format("After filter Graph %s, Nodes = %d, Edges = %d", FILE_NAME_NODES, g.size(), g.getNumOfEdges() / 2));

//        for (int k = 2; k < 12; k++) {
//        Graph maxGraph = null;
        int maxRound = 0;
        double maxModularity = Double.MIN_VALUE;
        double beforeMgroupModularity = 0;
        boolean appliedMGroup = false;
        int maxNumCom = 0;
        int beforeMgroupNumCom = 0;
        boolean resolveSingles = RESOLVE_SINGLES;
        int mCom = 0;
        double mMod = Double.MIN_VALUE;

        if (ONLY_MGROUP) {
            for (int i = 0; i < g.getNodes().length; i++) {
                g.getNodes()[i].setCommunityId(i);
            }

            MGroup cd2 = new MGroup();
            cd2.findCommunities(g);
            double m = ModularityComputer.compute(g, MODULARITY_FOR_WEIGHTED_DIRECTED);
            print(String.format("After MGroup Modularity = %f", m));
            int newNumCom2 = g.getNumCommunities();
            print(String.format("Number of Communities %d", newNumCom2));
            CsvConvertor.convertAndWrite(g, String.format("%s%dmgroup", OUTPUT_DIR, 0));

            System.exit(0);
        }

        DiffusionBasedCommunityDetector dbcd = new DiffusionBasedCommunityDetector(INIT_COLOR_ASSIGNMENT);
        for (int round = START_ITERATION; round < END_ITERATION; round = round + INCREMENT_PER_ITERATION) {

            long before = System.currentTimeMillis();
            dbcd.findCommunities(g, round, resolveSingles);
            long after = System.currentTimeMillis();
            print(String.format("Iteration: %d, Computation Time: %d", round, after - before));
            int numCom = g.getNumCommunities();
            print(String.format("Communities: %d", numCom));

            before = System.currentTimeMillis();
            double modularity = ModularityComputer.compute(g, MODULARITY_FOR_WEIGHTED_DIRECTED);
            after = System.currentTimeMillis();
            print(String.format("Modularity = %f", modularity));
            print(String.format("Computation time for modularity: %d", after - before));
            cSizes.add((double) numCom);
            List<Double> size;
            if (!modularities.containsKey(numCom)) {
                size = new LinkedList<>();
                modularities.put(numCom, size);
            } else {
                size = modularities.get(numCom);
            }
            size.add(modularity);
//                CsvConvertor.convertAndWrite(g, String.format("%s%d", OUTPUT_DIR, round));

            if (modularity > maxModularity) {
                maxRound = round;
                maxModularity = modularity;
                beforeMgroupModularity = maxModularity;
                maxNumCom = numCom;
                beforeMgroupNumCom = numCom;
                appliedMGroup = false;
            }

//            if (numCom == k && modularity > mMod) {
//                mMod = modularity;
//                mCom = round;
//                maxGraph = g.clone();
//            }
            if (!SELECTIVE) {
                if (maxValues.get(numCom) < modularity) {
                    maxValues.put(numCom, modularity);
                    maxIterations.put(numCom, round);
                    maxs.put(numCom, g.clone());
                }
            } else {
                if (selectedIterations.contains(round)) {
                    maxValues.put(numCom, modularity);
                    maxIterations.put(numCom, round);
                    maxs.put(numCom, g.clone());
                }
            }
            if (APPLY_MGROUP && round > APPLY_MGROUP_AFTER) {
                MGroup cd = new MGroup();
                before = System.currentTimeMillis();
                cd.findCommunities(g);
                after = System.currentTimeMillis();
                System.out.println(String.format("Computation time for MGroup is %d", after - before));
                double temp = modularity;
                modularity = ModularityComputer.compute(g, MODULARITY_FOR_WEIGHTED_DIRECTED);
                print(String.format("After MGroup Modularity = %f", modularity));
                int newNumCom = g.getNumCommunities();
                print(String.format("Number of Communities %d", newNumCom));
                CsvConvertor.convertAndWrite(g, String.format("%s%dmgroup", OUTPUT_DIR, round));

                if (modularity > maxModularity) {
                    maxRound = round;
                    maxModularity = modularity;
                    appliedMGroup = true;
                    beforeMgroupModularity = temp;
                    maxNumCom = newNumCom;
                    beforeMgroupNumCom = numCom;
                }
            }
        }
//
//        print(String.format("Max Modularity = %f, Number of Communities = %d, Iteration = %d, "
//                + "AppliedMgroup = %b, BeforeMgroupModularity = %f, BeforeMgroupNumCom = %d, Your Selected Community = %d, Iteration = %d, Value = %f",
//                maxModularity, maxNumCom, maxRound, appliedMGroup, beforeMgroupModularity, beforeMgroupNumCom, 0, mCom, mMod));
//        }
//        return maxGraph;
    }

    public static void print(String str) {
        System.out.println(str);
    }

    private static void filterGraph(Graph g, float t) {
        for (Node n : g.getNodes()) {
            List<Edge> toRemove = new LinkedList<>();
            for (Edge e : n.getEdges()) {
                if (e.getLatency() > t) {
                    toRemove.add(e);
                }
            }

            for (Edge e : toRemove) {
                Node dst = g.getNode(e.getDst());
                n.removeEdge(e);
                dst.removeEdge(dst.getEdge(e.getSrc()));
                if (dst.getEdges().isEmpty()) {
                    g.remove(dst.getName());
                }
            }

            if (n.getEdges().isEmpty()) {
                g.remove(n.getName());
            }

        }

        for (int i = 0; i < g.getNodes().length; i++) {
            g.getNodes()[i].setId(i);
        }
    }
}
