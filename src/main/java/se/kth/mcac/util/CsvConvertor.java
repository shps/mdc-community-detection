package se.kth.mcac.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
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

    public static void convertAndWrite(Graph g, String outputDir) throws FileNotFoundException {
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
     * Special for the result format of bootVM scenario.
     *
     * @param communityId
     * @param results
     * @param outputDir
     * @param header
     * @throws java.io.FileNotFoundException
     */
    public static void writeOutput(int communityId, HashMap<Node, Float> results, String outputDir, String header) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%sresult%d.csv", outputDir, communityId))) {
            writer.println(header);
            Iterator<Entry<Node, Float>> iterator = results.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Node, Float> entry = iterator.next();
                writer.println(String.format("%s,%f", entry.getKey().getName(), entry.getValue()));
            }
        }
    }

//    /**
//     * The current version only supports node files with id,modularity class
//     * format. and Edge with the format source,target,id.
//     *
//     * @param nodeFile
//     * @param edgeFile
//     * @return
//     * @throws FileNotFoundException
//     * @throws IOException
//     */
//    public Graph convertAndRead(String nodeFile, String edgeFile) throws FileNotFoundException, IOException {
//        Graph g = new Graph();
//        int i = 0; // node id
//        try (BufferedReader reader = new BufferedReader(new FileReader(nodeFile))) {
//            String line = reader.readLine(); // Skip  the first line.
//            while ((line = reader.readLine()) != null) {
//                String[] connections = line.split(COMMA);
//                Node n = new Node(i, connections[0]);
//                n.setCommunityId(Integer.valueOf(connections[1]));
//                g.addNode(n);
//                i++;
//            }
//        }
//
//        try (BufferedReader reader = new BufferedReader(new FileReader(edgeFile))) {
//            String line = reader.readLine(); // Skip  the first line.
//            while ((line = reader.readLine()) != null) {
//                String[] connections = line.split(COMMA);
//                Node n = g.getNode(connections[0]);
//                n.addEdge(new Edge(Long.valueOf(connections[2]), connections[0], connections[1]));
//            }
//        }
//
//        return g;
//    }
    /**
     * This method is to read the output of the CommunityFinder class with the
     * same file format.
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
                n.setLat(Double.parseDouble(connections[1]));
                n.setLon(Double.parseDouble(connections[2]));
                n.setCommunityId(Integer.valueOf(connections[3]));
                g.addNode(n);
                i++;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(edgeFile))) {
            String line = reader.readLine(); // Skip  the first line.
            while ((line = reader.readLine()) != null) {
                String[] connections = line.split(COMMA);
                Node n = g.getNode(connections[0]);
                n.addEdge(
                        new Edge(Long.valueOf(connections[3]),
                                connections[0], connections[1],
                                Float.parseFloat(connections[4]),
                                Float.parseFloat(connections[5]),
                                0));
            }
        }

        return g;
    }
}
