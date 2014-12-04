package se.kth.mcac;

import java.io.IOException;
import java.util.HashMap;
import se.kth.mcac.cd.db.DiffusionBasedCommunityDetector;
import se.kth.mcac.cd.db.MGroup;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.util.CsvConvertor;
import se.kth.mcac.util.ModularityComputer;
import se.kth.mcac.util.SpaceSeparatedConvertor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author hooman
 */
public class CommunityFinder {

    static final String GRAPH_NAME = "dolphins";
    static final String DEFAULT_FILE_DIR = String.format("/home/hooman/Desktop/dimacs/%s/", GRAPH_NAME);
    static final String FILE_NAME = GRAPH_NAME + ".graph";
    static final int START_ITERATION = 1;
    static final int END_ITERATION = START_ITERATION + 50;
    static final int INCREMENT_PER_ITERATION = 1;
    static final boolean APPLY_MGROUP = true;
    static final int APPLY_MGROUP_AFTER = 0;
    static final boolean COLOR_STATISTICS = false;
    static final boolean USE_GARBAGE_COLLECTOR = true;
    static final boolean FULL_STATS = true;

    // Stats Keys
    enum StatKeys {

        MAX_MODULARITY, NUM_COM, ITERATION, APPLIED_MGROUP, BEFORE_MGROUP_MOD,
        BEFORE_MGROUP_NUM_COM,
    }

    public static void main(String[] args) throws IOException, Exception {

        boolean mgroup = APPLY_MGROUP;
        boolean gc = USE_GARBAGE_COLLECTOR;

        HashMap<StatKeys, Object> stats = execute(mgroup, gc);

        int maxRound = (int) stats.get(StatKeys.ITERATION);
        double maxModularity = (double) stats.get(StatKeys.MAX_MODULARITY);
        double beforeMgroupModularity = (double) stats.get(StatKeys.BEFORE_MGROUP_MOD);
        boolean appliedMGroup = (boolean) stats.get(StatKeys.APPLIED_MGROUP);
        int maxNumCom = (int) stats.get(StatKeys.NUM_COM);
        int beforeMgroupNumCom = (int) stats.get(StatKeys.BEFORE_MGROUP_NUM_COM);

        print(String.format("Max Modularity = %f, Number of Communities = %d, Iteration = %d, "
                + "AppliedMgroup = %b, BeforeMgroupModularity = %f, BeforeMgroupNumCom = %d",
                maxModularity, maxNumCom, maxRound, appliedMGroup, beforeMgroupModularity, beforeMgroupNumCom));
    }

    private static HashMap<StatKeys, Object> executeAll() throws Exception {
        return null;
    }

    private static HashMap<StatKeys, Object> execute(boolean mgroup, boolean gc) throws Exception {
        SpaceSeparatedConvertor convertor = new SpaceSeparatedConvertor();
        Graph g = convertor.convertToGraph(DEFAULT_FILE_DIR + FILE_NAME);
        print(String.format("Graph Nodes = %d, Edges = %d", g.size(), g.getNumOfEdges() / 2));

        int maxRound = 0;
        double maxModularity = Float.MIN_VALUE;
        double beforeMgroupModularity = 0;
        boolean appliedMGroup = false;
        int maxNumCom = 0;
        int beforeMgroupNumCom = 0;

        DiffusionBasedCommunityDetector dbcd = new DiffusionBasedCommunityDetector();
        dbcd.setColorStats(COLOR_STATISTICS).setGc(gc);
        for (int round = START_ITERATION; round < END_ITERATION; round = round + INCREMENT_PER_ITERATION) {

            long before = System.currentTimeMillis();
            dbcd.findCommunities(g, round);
            long after = System.currentTimeMillis();
            print(String.format("Computation time for %d round is %d", round, after - before));
            int numCom = g.getNumCommunities();
            print(String.format("Number of Communities %d", numCom));

            before = System.currentTimeMillis();
            double modularity = ModularityComputer.compute(g);
            after = System.currentTimeMillis();
            print(String.format("Modularity = %f", modularity));
            print(String.format("Computation time for modularity is %d", after - before));
            CsvConvertor.convertAndWrite(g, String.format("%s%d", DEFAULT_FILE_DIR, round));

            if (modularity > maxModularity) {
                maxRound = round;
                maxModularity = modularity;
                beforeMgroupModularity = maxModularity;
                maxNumCom = numCom;
                beforeMgroupNumCom = numCom;
                appliedMGroup = false;
            }

            if (mgroup && round > APPLY_MGROUP_AFTER) {
                MGroup cd = new MGroup();
                before = System.currentTimeMillis();
                cd.findCommunities(g);
                after = System.currentTimeMillis();
                System.out.println(String.format("Computation time for MGroup is %d", after - before));
                double temp = modularity;
                modularity = ModularityComputer.compute(g);
                print(String.format("After MGroup Modularity = %f", modularity));
                int newNumCom = g.getNumCommunities();
                print(String.format("Number of Communities %d", newNumCom));
                CsvConvertor.convertAndWrite(g, String.format("%s%dmgroup", DEFAULT_FILE_DIR, round));

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

        HashMap<StatKeys, Object> stats = new HashMap<>();
        stats.put(StatKeys.ITERATION, maxRound);
        stats.put(StatKeys.MAX_MODULARITY, maxModularity);
        stats.put(StatKeys.BEFORE_MGROUP_MOD, beforeMgroupModularity);
        stats.put(StatKeys.APPLIED_MGROUP, appliedMGroup);
        stats.put(StatKeys.NUM_COM, maxNumCom);
        stats.put(StatKeys.BEFORE_MGROUP_NUM_COM, beforeMgroupNumCom);

        return stats;
    }

    private static void print(String str) {
        System.out.println(str);
    }
}
