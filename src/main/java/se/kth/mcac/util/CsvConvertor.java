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
            nodeWriter.println("Id,UName,Lat,Lon,community");
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
     * @param fileName
     * @param header
     * @throws java.io.FileNotFoundException
     */
    public static void writeBootVmOutput(int communityId, HashMap<Node, Float> results, String outputDir, String fileName, String header) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%s%s%d.csv", outputDir, fileName, communityId))) {
            if (!header.isEmpty()) {
                writer.println(header);
            }
            Iterator<Entry<Node, Float>> iterator = results.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Node, Float> entry = iterator.next();
//                writer.println(String.format("%s,%f", entry.getKey().getName(), entry.getValue()));
                writer.println(String.format("%f", entry.getValue()));
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
     * @param fileName
     * @throws java.io.FileNotFoundException
     */
    public static void writePairOutput(
            int communityId,
            HashMap<Node, HashMap<Node, Float>> results,
            String outputDir,
            String header,
            String fileName) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%s%s%d.csv", outputDir, fileName, communityId))) {
            if (!header.isEmpty()) {
                writer.println(header);
            }
            Iterator<Entry<Node, HashMap<Node, Float>>> i1 = results.entrySet().iterator();
            while (i1.hasNext()) {
                Entry<Node, HashMap<Node, Float>> e1 = i1.next();
                Node n1 = e1.getKey();
                Iterator<Entry<Node, Float>> i2 = e1.getValue().entrySet().iterator();
                while (i2.hasNext()) {
                    Entry<Node, Float> e2 = i2.next();
                    Node n2 = e2.getKey();
                    float l = e2.getValue();
//                    writer.println(String.format("%s,%s,%f", n1.getName(), n2.getName(), l));
                    writer.println(String.format("%f", l));
                }
            }
        }
    }

    public static void writeRandomBootVMTime(
            int index,
            float[][] results,
            String outputDir,
            String header,
            String fileName) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%s%s%d.csv", outputDir, fileName, index))) {
            if (!header.isEmpty()) {
                writer.println(header);
            }

            int c = results[0].length;
            for (int i = 0; i < c; i++) {
                for (int j = 0; j < results.length; j++) {
                    writer.print(String.format("%f,", results[j][i]));
                }
                writer.println();
            }
        }
    }
    
    public static void writeAverageRandomBootVMTime(
            int index,
            float[][] results,
            String outputDir,
            String header,
            String fileName) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%s%s%d.csv", outputDir, fileName, index))) {
            if (!header.isEmpty()) {
                writer.println(header);
            }

            int c = results.length;
            for (int i = 0; i < c; i++) {
                for (int j = 0; j < results[i].length; j++) {
                    writer.print(String.format("%f,", results[i][j]));
                }
                writer.println();
            }
        }
    }

    public static void writeBetweennessOutPut(
            int communityId,
            Graph g,
            String outputDir,
            String header,
            String fileName) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%s%s%d.csv", outputDir, fileName, communityId))) {
            if (!header.isEmpty()) {
                writer.println(header);
            }

            for (Node n : g.getNodes()) {
                writer.println(String.format("%s,%f", n.getName(), n.getBc()));
            }
//            Iterator<Entry<Node, HashMap<Node, Float>>> i1 = results.entrySet().iterator();
//            while (i1.hasNext()) {
//                Entry<Node, HashMap<Node, Float>> e1 = i1.next();
//                Node n1 = e1.getKey();
//                Iterator<Entry<Node, Float>> i2 = e1.getValue().entrySet().iterator();
//                while (i2.hasNext()) {
//                    Entry<Node, Float> e2 = i2.next();
//                    Node n2 = e2.getKey();
//                    float l = e2.getValue();
////                    writer.println(String.format("%s,%s,%f", n1.getName(), n2.getName(), l));
//                    writer.println(String.format("%f", l));
//                }
//            }
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
                n.setLat(Double.parseDouble(connections[2]));
                n.setLon(Double.parseDouble(connections[3]));
                n.setCommunityId(Integer.valueOf(connections[4]));
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
                        new Edge(Long.valueOf(connections[5]),
                                connections[0], connections[1],
                                Float.parseFloat(connections[6]),
                                Float.parseFloat(connections[7]),
                                0));
            }
        }

        return g;
    }
}
