package se.kth.mcac;

import java.io.IOException;
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

    static final String DEFAULT_FILE_DIR = "/home/hooman/Desktop/dimacs/jazz/";
    static final String FILE_NAME = "jazz.graph";
    static final int START_ITERATION = 1;
    static final int END_ITERATION = START_ITERATION + 10;
    static final int INCREMENT_PER_ITERATION = 1;
    static final boolean APPLY_MGROUP = false;
    static final int APPLY_MGROUP_AFTER = 0;

    public static void main(String[] args) throws IOException, Exception {

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

            if (APPLY_MGROUP && round > APPLY_MGROUP_AFTER) {
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

        print(String.format("Max Modularity = %f, Number of Communities = %d, Iteration = %d, "
                + "AppliedMgroup = %b, BeforeMgroupModularity = %f, BeforeMgroupNumCom = %d",
                maxModularity, maxNumCom, maxRound, appliedMGroup, beforeMgroupModularity, beforeMgroupNumCom));
    }

    public static void print(String str) {
        System.out.println(str);
    }
}
