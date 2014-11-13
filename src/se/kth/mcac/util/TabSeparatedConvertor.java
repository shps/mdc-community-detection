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
 * Written with the goal to convert graphs available in snap.stanford to Graph
 * object.
 *
 * @author hooman
 */
public class TabSeparatedConvertor {

    private final static String SPACE_DELIMITER = "\\s+";

    public Graph convertToGraph(String file) throws FileNotFoundException, IOException {
        Graph g = new Graph();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        int id = 0;
        Random random = new Random();
        while ((line = reader.readLine()) != null) {
            String[] connections = line.split(SPACE_DELIMITER);
            if (connections[0].equals(SPACE_DELIMITER)) {
                continue;
            }

            Node n1 = g.getNode(connections[0]);
            Node n2 = g.getNode(connections[1]);
            if (n1 == null) {
                n1 = new Node(id, connections[0]);
                g.addNode(n1);
                id++;
            }
            
            if (n2 == null) {
                n2 = new Node(id, connections[1]);
                g.addNode(n2);
                id++;
            }
            
            n1.addEdge(new Edge(random.nextLong(), n1.getName(), n2.getName()));
            n2.addEdge(new Edge(random.nextLong(), n2.getName(), n1.getName()));
        }
        
        reader.close();

        return g;
    }

}
