package se.kth.mcac.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.IllegalFormatException;
import java.util.Random;
import javax.imageio.IIOException;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 * Written with the goal to convert graphs available in DIMACS graph format to
 * Graph object. For more information:
 * http://www.cc.gatech.edu/dimacs10/downloads.shtml
 *
 * @author hooman
 */
public class SpaceSeparatedConvertor {

    private final static String SPACE_DELIMITER = "\\s+";

    /**
     * To convert unweighted undirected graphs.
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Graph convertToGraph(String file) throws FileNotFoundException, IOException, Exception {
        Graph g = new Graph();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Random random = new Random();
            String[] attributes = reader.readLine().split(SPACE_DELIMITER); // the first line includes attributes. [0] vertices [1] edges [2] fmt
            for (int id = 1; id <= Integer.valueOf(attributes[0]); id++) {
                line = reader.readLine();
                String[] connections = line.split(SPACE_DELIMITER);

                Node n1 = g.getNode(String.valueOf(id));
                if (n1 == null) {
                    n1 = new Node(id - 1, String.valueOf(id));
                    g.addNode(n1);
                }

                if (connections[0].equalsIgnoreCase("")) { // The vertex has no connections.
                    continue;
                }

                for (String dst : connections) {
                    Node n2 = g.getNode(dst);
                    if (n2 == null) {
                        n2 = new Node(Integer.valueOf(dst) - 1, dst);
                        g.addNode(n2);
                    }
                    n1.addEdge(new Edge(random.nextLong(), n1.getName(), n2.getName()));
                }
            }

            int v = g.size();
            int e = g.getNumOfEdges() / 2;
            short fmt = Short.valueOf(attributes[2]);
            if (Integer.valueOf(attributes[0]) != v
                    || Integer.valueOf(attributes[1]) != e
                    || fmt != 0) {
                throw new Exception(String.format("The converted graph's attributes "
                        + "are inconsistent with the data in the input file or fmt is not zero: \nV=%d, E=%d, fmt=%d", v, e, fmt));
            }

        }

        return g;
    }
}
