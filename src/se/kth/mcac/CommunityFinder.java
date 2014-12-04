package se.kth.mcac;

import java.io.IOException;
import java.util.HashMap;
import se.kth.mcac.cd.db.DiffusionBasedCommunityDetector;
import se.kth.mcac.cd.db.MGroup;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.util.CsvConvertor;
import se.kth.mcac.util.ModularityComputer;
import se.kth.mcac.util.SpaceSeparatedConvertor;

/**
 *
 * @author hooman
 */
public class CommunityFinder {

    static final String GRAPH_NAME = "dolphins";
    static final String DEFAULT_FILE_DIR = String.format("/home/hooman/Desktop/dimacs/%s/", GRAPH_NAME);
    static final String FILE_NAME = GRAPH_NAME + ".graph";
    static final int START_ITERATION = 1;
    static final int END_ITERATION = START_ITERATION + 10;
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

        if (FULL_STATS) {
            executeAll();
        } else {

            boolean mgroup = APPLY_MGROUP;
            boolean gc = USE_GARBAGE_COLLECTOR;

            HashMap<StatKeys, Object> stats = execute(mgroup, gc, "");

            print(String.format("**** Results ****\nMax Modularity = %f\nNumber of Communities = %d\nIteration = %d\n "
                    + "AppliedMgroup = %b\nBeforeMgroupModularity = %f\nBeforeMgroupNumCom = %d",
                    (double) stats.get(StatKeys.MAX_MODULARITY),
                    (int) stats.get(StatKeys.NUM_COM),
                    (int) stats.get(StatKeys.ITERATION),
                    (boolean) stats.get(StatKeys.APPLIED_MGROUP),
                    (double) stats.get(StatKeys.BEFORE_MGROUP_MOD),
                    (int) stats.get(StatKeys.BEFORE_MGROUP_NUM_COM)));
        }
    }

    private static void executeAll() throws Exception {

        // No MGroup, No GC
        boolean mgroup = false;
        boolean gc = false;
        HashMap<StatKeys, Object> stats1 = execute(mgroup, gc, "nmng");

        // Apply Mgroup, No GC
        mgroup = true;
        gc = false;
        HashMap<StatKeys, Object> stats2 = execute(mgroup, gc, "mng");

        // No Mgroup, Apply GC
        mgroup = false;
        gc = true;
        HashMap<StatKeys, Object> stats3 = execute(mgroup, gc, "nmg");

        // Apply Mgroup, Apply GC
        mgroup = true;
        gc = true;
        HashMap<StatKeys, Object> stats4 = execute(mgroup, gc, "mg");

        print(String.format("**** NO MGroup, No GC ****\nMax Modularity = %f\nNumber of Communities = %d\nIteration = %d\n"
                + "AppliedMgroup = %b",
                (double) stats1.get(StatKeys.MAX_MODULARITY),
                (int) stats1.get(StatKeys.NUM_COM),
                (int) stats1.get(StatKeys.ITERATION),
                (boolean) stats1.get(StatKeys.APPLIED_MGROUP)));

        print(String.format("**** Applied MGroup, No GC ****\nMax Modularity = %f\nNumber of Communities = %d\nIteration = %d\n"
                + "AppliedMgroup = %b\nBeforeMgroupModularity = %f\nBeforeMgroupNumCom = %d",
                (double) stats2.get(StatKeys.MAX_MODULARITY),
                (int) stats2.get(StatKeys.NUM_COM),
                (int) stats2.get(StatKeys.ITERATION),
                (boolean) stats2.get(StatKeys.APPLIED_MGROUP),
                (double) stats2.get(StatKeys.BEFORE_MGROUP_MOD),
                (int) stats2.get(StatKeys.BEFORE_MGROUP_NUM_COM)));

        print(String.format("**** NO MGroup, Applied GC ****\nMax Modularity = %f\nNumber of Communities = %d\nIteration = %d\n"
                + "AppliedMgroup = %b",
                (double) stats3.get(StatKeys.MAX_MODULARITY),
                (int) stats3.get(StatKeys.NUM_COM),
                (int) stats3.get(StatKeys.ITERATION),
                (boolean) stats3.get(StatKeys.APPLIED_MGROUP)));

        print(String.format("**** Applied MGroup, Applied GC ****\nMax Modularity = %f\nNumber of Communities = %d\nIteration = %d\n"
                + "AppliedMgroup = %b\nBeforeMgroupModularity = %f\nBeforeMgroupNumCom = %d",
                (double) stats4.get(StatKeys.MAX_MODULARITY),
                (int) stats4.get(StatKeys.NUM_COM),
                (int) stats4.get(StatKeys.ITERATION),
                (boolean) stats4.get(StatKeys.APPLIED_MGROUP),
                (double) stats4.get(StatKeys.BEFORE_MGROUP_MOD),
                (int) stats4.get(StatKeys.BEFORE_MGROUP_NUM_COM)));
    }

    private static HashMap<StatKeys, Object> execute(boolean mgroup, boolean gc, String filePrefix) throws Exception {
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
            CsvConvertor.convertAndWrite(g, String.format("%s%s%d", DEFAULT_FILE_DIR, filePrefix, round));

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
                print(String.format("Computation time for MGroup is %d", after - before));
                double temp = modularity;
                modularity = ModularityComputer.compute(g);
                print(String.format("After MGroup Modularity = %f", modularity));
                int newNumCom = g.getNumCommunities();
                print(String.format("Number of Communities %d", newNumCom));
                CsvConvertor.convertAndWrite(g, String.format("%s%s%dmgroup", DEFAULT_FILE_DIR, filePrefix, round));

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
