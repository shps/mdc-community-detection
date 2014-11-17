package se.kth.mcac.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class CsvConvertor {

    public static final String COMMA = ",";
    public static final String DIRECTED = "directed";

    public void convertAndWrite(Graph g, String outputDir) throws FileNotFoundException {
        try (PrintWriter nodeWriter = new PrintWriter(outputDir + "nodes.csv");
                PrintWriter edgeWriter = new PrintWriter(outputDir + "edges.csv")) {
            nodeWriter.println("Id,Lat,Lon,community");
            edgeWriter.println("Source,Target,Type,Id,BW,RTT,Weight");
            Node[] nodes = g.getNodes();

            for (Node n : nodes) {
                nodeWriter.println(String.format("%s,%f,%f,%d", n.getName(), n.getLat(), n.getLon(), n.getCommunityId()));
                for (Edge e : n.getEdges()) {
                    edgeWriter.println(String.format("%s,%s,%s,%d,%f,%f,%f",
                            e.getSrc(), e.getDst(), DIRECTED, e.getId(), e.getBw(), e.getLatency(), e.getWeight()));
                }
            }
        }
    }

    /**
     * The current version only supports node files with id,modularity class
     * format. and Edge with the format source,target,id.
     *
     * @param nodeFile
     * @param edgeFile
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Graph convertAndRead(String nodeFile, String edgeFile) throws FileNotFoundException, IOException {
        Graph g = new Graph();
        int i = 0; // node id
        try (BufferedReader reader = new BufferedReader(new FileReader(nodeFile))) {
            String line = reader.readLine(); // Skip  the first line.
            while ((line = reader.readLine()) != null) {
                String[] connections = line.split(COMMA);
                Node n = new Node(i, connections[0]);
                n.setCommunityId(Integer.valueOf(connections[1]));
                g.addNode(n);
                i++;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(edgeFile))) {
            String line = reader.readLine(); // Skip  the first line.
            while ((line = reader.readLine()) != null) {
                String[] connections = line.split(COMMA);
                Node n = g.getNode(connections[0]);
                n.addEdge(new Edge(Long.valueOf(connections[2]), connections[0], connections[1]));
            }
        }

        return g;
    }
}
