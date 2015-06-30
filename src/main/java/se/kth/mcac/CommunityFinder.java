package se.kth.mcac;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import se.kth.mcac.cd.db.DiffusionBasedCommunityDetector;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.util.CsvConvertor;
import se.kth.mcac.util.ModularityComputer;

/**
 *
 * @author hooman
 */
public class CommunityFinder {

    static final String DEFAULT_FILE_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/cd/";
    static final String OUTPUT_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/cd/results/";
    static final String FILE_NAME_NODES = "avg-graph.csvnodes.csv";
    static final String FILE_NAME_EDGES = "avg-graph.csvedges.csv";
    
    static final boolean MODULARITY_FOR_WEIGHTED_DIRECTED = true;
    static final boolean EXCLUDE_DISCONNECTED_NODES = true;
    static final int START_ITERATION = 1;
    static final int END_ITERATION = START_ITERATION + 1500;
    static final int INCREMENT_PER_ITERATION = 1;
    static final float THRESHOLD = 20;
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
//        Graph g = new QmpsuConvertor().convertToGraph("/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/new-graph-553dde71.json", true, true);
        print(String.format("Graph %s, Nodes = %d, Edges = %d", FILE_NAME_NODES, g.size(), g.getNumOfEdges()));
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
    }

    public static void start(Graph g) throws IOException {
        double maxModularity = Double.MIN_VALUE;
        boolean resolveSingles = RESOLVE_SINGLES;

        DiffusionBasedCommunityDetector dbcd = new DiffusionBasedCommunityDetector();
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

            if (modularity > maxModularity) {
                maxModularity = modularity;
            }

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
        }
    }

    public static void print(String str) {
        System.out.println(str);
    }
}
