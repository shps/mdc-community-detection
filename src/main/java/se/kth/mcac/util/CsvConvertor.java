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
            nodeWriter.println("Id, UName,Lat,Lon,community");
            edgeWriter.println("Source,Target,SrcUName, DstUName,Type,Id,BW,RTT,Weight");
            Node[] nodes = g.getNodes();

            for (Node n : nodes) {
                nodeWriter.println(String.format("%s,%s,%f,%f,%d", n.getName(), n.getUName(), n.getLat(), n.getLon(), n.getCommunityId()));
                for (Edge e : n.getEdges()) {
                    edgeWriter.println(String.format("%s,%s,%s,%s,%s,%d,%f,%f,%f",
                            e.getSrc(), e.getDst(), e.getSrcUName(), e.getDstUName(), DIRECTED, e.getId(), e.getBw(), e.getLatency(), e.getWeight()));
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
    public static void writeBootVmOutput(int communityId, HashMap<Node, Float> results, String outputDir, String header) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%sbvresult%d.csv", outputDir, communityId))) {
            writer.println(header);
            Iterator<Entry<Node, Float>> iterator = results.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Node, Float> entry = iterator.next();
                writer.println(String.format("%s,%f", entry.getKey().getName(), entry.getValue()));
            }
        }
    }

    /**
     * Special for the result format of inter-compute nodes latency scenario.
     *
     * @param communityId
     * @param results
     * @param outputDir
     * @param header
     * @throws java.io.FileNotFoundException
     */
    public static void writeLatencyOutput(
            int communityId,
            HashMap<Node, HashMap<Node, Float>> results,
            String outputDir,
            String header) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%slresult%d.csv", outputDir, communityId))) {
            writer.println(header);
            Iterator<Entry<Node, HashMap<Node, Float>>> i1 = results.entrySet().iterator();
            while (i1.hasNext()) {
                Entry<Node, HashMap<Node, Float>> e1 = i1.next();
                Node n1 = e1.getKey();
                Iterator<Entry<Node, Float>> i2 = e1.getValue().entrySet().iterator();
                while (i2.hasNext()) {
                    Entry<Node, Float> e2 = i2.next();
                    Node n2 = e2.getKey();
                    float l = e2.getValue();
                    writer.println(String.format("%s,%s,%f", n1.getName(), n2.getName(), l));
                }
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
