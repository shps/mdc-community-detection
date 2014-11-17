package se.kth.mcac;

import java.io.IOException;
import se.kth.mcac.cd.CommunityDetector;
import se.kth.mcac.cd.db.DiffusionBasedCommunityDetector;
import se.kth.mcac.cd.db.MGroup;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.util.CsvConvertor;
import se.kth.mcac.util.ModularityComputer;
import se.kth.mcac.util.ObjectSizeCalculator;
import se.kth.mcac.util.QmpsuConvertor;
import se.kth.mcac.util.SpaceSeparatedConvertor;
import se.kth.mcac.util.TabSeparatedConvertor;

/**
 *
 * @author hooman
 */
public class CommunityFinder {

    private static final String DEFAULT_FILE_DIR = "/home/hooman/Desktop/dimacs/";

    public static void main(String[] args) throws IOException {
        float initialColorAssignment = 0.005f;
//        QmpsuConvertor convertor = new QmpsuConvertor();
//        Graph g = convertor.convertToGraph(DEFAULT_FILE_DIR + "graph-5434e0f1.json");
//        TabSeparatedConvertor convertor = new TabSeparatedConvertor();
        SpaceSeparatedConvertor convertor = new SpaceSeparatedConvertor();
        Graph g = convertor.convertToGraph("/home/hooman/Desktop/dimacs/as-22july06/as-22july06.graph");
        System.out.println(String.format("Graph Nodes = %d, Edges = %d", g.size(), g.getNumOfEdges() / 2));
//        Graph g = convertor.convertToGraph("/home/hooman/Desktop/diffusion results/guifi.json");
        DiffusionBasedCommunityDetector primaryDetector = new DiffusionBasedCommunityDetector(initialColorAssignment);
        for (int round = 1; round < 100; round = round + 10) {
            long before = System.currentTimeMillis();
            primaryDetector.findCommunities(g, round);
            long after = System.currentTimeMillis();
            System.out.println(String.format("Computation time for %d round is %d", round, after - before));
//        CommunityDetector secondaryDetector = new MGroup();
//        secondaryDetector.findCommunities(g);
            System.out.println(String.format("Number of Communities %d", g.getNumCommunities()));
            
            before = System.currentTimeMillis();
            float modularity = ModularityComputer.compute(g);
            after = System.currentTimeMillis();
            System.out.println(String.format("Modularity = %f", modularity));
            System.out.println(String.format("Computation time for modularity is %d", after - before));
            CsvConvertor csvc = new CsvConvertor();
            csvc.convertAndWrite(g, String.format("%s%d", DEFAULT_FILE_DIR, round));
        }
    }
}
