package se.kth.mcac.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
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

    public Graph convertToGraph(String file) throws FileNotFoundException, IOException {
        Graph g = new Graph();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int id = 1;
            Random random = new Random();
            reader.readLine(); // Skip the first line.
            while ((line = reader.readLine()) != null) {
                String[] connections = line.split(SPACE_DELIMITER);

                Node n1 = g.getNode(String.valueOf(id));
                if (n1 == null) {
                    n1 = new Node(id - 1, connections[1]);
                    g.addNode(n1);
                    id++;
                }

                for (int i = 0; i < connections.length; i++) {
                    Node n2 = g.getNode(connections[i]);
                    if (n2 == null) {
                        n2 = new Node(Integer.valueOf(connections[i]) - 1, connections[i]);
                        g.addNode(n2);
                    }

                    n1.addEdge(new Edge(random.nextLong(), n1.getName(), n2.getName()));
                    n2.addEdge(new Edge(random.nextLong(), n2.getName(), n1.getName()));
                }
            }
        }

        return g;
    }
}
