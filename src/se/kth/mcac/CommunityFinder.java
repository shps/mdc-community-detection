package se.kth.mcac;

import java.io.IOException;
import se.kth.mcac.cd.CommunityDetector;
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

    static final String DEFAULT_FILE_DIR = "/home/hooman/Desktop/dimacs/netscience/";
    static final String FILE_NAME = "netscience.graph";
    static final float INIT_COLOR_ASSIGNMENT = 1f;
    static final int START_ITERATION = 100;
    static final int END_ITERATION = START_ITERATION + 100;
    static final int INCREMENT_PER_ITERATION = 50;
    static final boolean APPLY_MGROUP = true;
    static final int APPLY_MGROUP_AFTER = 100;

    public static void main(String[] args) throws IOException, Exception {

        SpaceSeparatedConvertor convertor = new SpaceSeparatedConvertor();
        Graph g = convertor.convertToGraph(DEFAULT_FILE_DIR + FILE_NAME);
        print(String.format("Graph Nodes = %d, Edges = %d", g.size(), g.getNumOfEdges() / 2));
        print(String.format("INIT_COLOR_ASSIGNMENT = %f", INIT_COLOR_ASSIGNMENT));

        int maxRound = 0;
        float maxModularity = Float.MIN_VALUE;
        boolean appliedMGroup = false;

        DiffusionBasedCommunityDetector dbcd = new DiffusionBasedCommunityDetector(INIT_COLOR_ASSIGNMENT);
        for (int round = START_ITERATION; round < END_ITERATION; round = round + INCREMENT_PER_ITERATION) {

            long before = System.currentTimeMillis();
            dbcd.findCommunities(g, round);
            long after = System.currentTimeMillis();
            print(String.format("Computation time for %d round is %d", round, after - before));
            print(String.format("Number of Communities %d", g.getNumCommunities()));

            before = System.currentTimeMillis();
            float modularity = ModularityComputer.compute(g);
            after = System.currentTimeMillis();
            print(String.format("Modularity = %f", modularity));
            print(String.format("Computation time for modularity is %d", after - before));
            CsvConvertor.convertAndWrite(g, String.format("%s%d", DEFAULT_FILE_DIR, round));

            if (modularity > maxModularity) {
                maxRound = round;
                maxModularity = modularity;
                appliedMGroup = false;
            }

            if (APPLY_MGROUP && round > APPLY_MGROUP_AFTER) {
                MGroup cd = new MGroup();
                before = System.currentTimeMillis();
                cd.findCommunities(g);
                after = System.currentTimeMillis();
                System.out.println(String.format("Computation time for MGroup is %d", after - before));

                modularity = ModularityComputer.compute(g);
                print(String.format("After MGroup Modularity = %f", modularity));
                print(String.format("Number of Communities %d", g.getNumCommunities()));
                CsvConvertor.convertAndWrite(g, String.format("%s%dmgroup", DEFAULT_FILE_DIR, round));

                if (modularity > maxModularity) {
                    maxRound = round;
                    maxModularity = modularity;
                    appliedMGroup = true;
                }
            }
        }

        print(String.format("Max Modularity = %f, Iteration = %d, AppliedMgroup = %b", maxModularity, maxRound, appliedMGroup));
    }

    public static void print(String str) {
        System.out.println(str);
    }
}
