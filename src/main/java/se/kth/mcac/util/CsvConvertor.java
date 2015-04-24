package se.kth.mcac.util;

import com.sun.org.apache.bcel.internal.generic.AALOAD;
import com.sun.xml.internal.bind.v2.model.util.ArrayInfoUtil;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
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

    public static void convertAndWrite(Graph g, String fileName) throws FileNotFoundException {
        try (PrintWriter nodeWriter = new PrintWriter(fileName) //                ;
                //                PrintWriter edgeWriter = new PrintWriter(outputDir + "edges.csv")
                ) {
            nodeWriter.println("Id,UName,Lat,Lon,community");
//            edgeWriter.println("Source,Target,SrcUName, DstUName,Type,Id,BW,RTT,Weight");
            Node[] nodes = g.getNodes();

            for (Node n : nodes) {
                nodeWriter.println(String.format("%s,%s,%f,%f,%d", n.getName(), n.getUName(), n.getLat(), n.getLon(), n.getCommunityId()));
//                for (Edge e : n.getEdges()) {
//                    edgeWriter.println(String.format("%s,%s,%s,%s,%s,%d,%f,%f,%f",
//                            e.getSrc(), e.getDst(), e.getSrcUName(), e.getDstUName(), DIRECTED, e.getId(), e.getBw(), e.getLatency(), e.getWeight()));
//                }
            }
        }
    }

    public static void convertAndWriteWithGeoLocation(Graph g, String fileName) throws FileNotFoundException {
        try (
                //                PrintWriter nodeWriter = new PrintWriter(fileName);
                PrintWriter edgeWriter = new PrintWriter(fileName)) {
//            nodeWriter.println("Id,UName,Lat,Lon,community");
            edgeWriter.println("Source,Target,SrcUName, DstUName,srcLat,srcLon,dstLat,dstLon,Type,Id,BW,RTT,Weight");
            Node[] nodes = g.getNodes();

            for (Node n : nodes) {
//                nodeWriter.println(String.format("%s,%s,%f,%f,%d", n.getName(), n.getUName(), n.getLat(), n.getLon(), n.getCommunityId()));
                for (Edge e : n.getEdges()) {
                    Node dst = g.getNode(e.getDst());
                    edgeWriter.println(String.format("%s,%s,%s,%s,%f,%f,%f,%f,%s,%d,%f,%f,%f",
                            e.getSrc(), e.getDst(), e.getSrcUName(), e.getDstUName(), n.getLat(), n.getLon(), dst.getLat(), dst.getLon(),
                            DIRECTED, e.getId(), e.getBw(), e.getLatency(), e.getWeight()));
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
    public static void writeBootVmOutput(int communityId, HashMap<Node, Double> results, String outputDir, String fileName, String header) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%s%s%d.csv", outputDir, fileName, communityId))) {
            if (!header.isEmpty()) {
                writer.println(header);
            }
            Iterator<Entry<Node, Double>> iterator = results.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Node, Double> entry = iterator.next();
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

    public static void writeTotalResults(
            double[][] results,
            String outputDir,
            String header,
            String fileName) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%s%s.csv", outputDir, fileName))) {
            if (!header.isEmpty()) {
                writer.println(header);
            }

            int c = results.length;
            int r = results[0].length;
            for (int j = 0; j < r; j++) {
                for (int i = 0; i < c; i++) {
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

    public static void writeModularities(
            List<Double> list,
            String outputDir,
            String header,
            String fileName, int offset) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%s%s.csv", outputDir, fileName))) {
            if (!header.isEmpty()) {
                writer.println(header);
            }

            int i = 0;
            for (double l : list) {
                writer.println(String.format("%d, %f", offset + i, l));
                i++;
            }
        }
    }

    public static void writeTotalLatencies(List<Double>[] latencies, String outputDir, String fileName, String header) throws FileNotFoundException {

        String splitter = ",";
        try (PrintWriter writer = new PrintWriter(String.format("%s%s.csv", outputDir, fileName)); //                PrintWriter writer2 = new PrintWriter(String.format("%s%s-size.csv", outputDir, fileName))
                PrintWriter writer2 = new PrintWriter(String.format("%s%s-errors.csv", outputDir, fileName))) {
            writer.println(header);

            Iterator[] iterators = new Iterator[latencies.length];
            for (int i = 0; i < latencies.length; i++) {
                iterators[i] = latencies[i].iterator();
//                writer2.print(i + offset);
//                writer2.println(String.format(",%d", latencies[i].size()));
                double[] ms = ArrayUtils.toPrimitive(latencies[i].toArray(new Double[latencies[i].size()]));
                double mean = new Mean().evaluate(ms);
                double stderror = new StandardDeviation().evaluate(ms) / Math.sqrt(ms.length);
                writer2.println(String.format("%d,%f,%f", i + 2, mean, stderror));
            }

            boolean finished = false;
            while (!finished) {
                finished = true;
                for (Iterator i : iterators) {
                    if (i.hasNext()) {
                        writer.print(i.next());
                        finished = false;
                    }
                    writer.print(splitter);
                }
                writer.println();
            }
        }
    }

    public static void writeSizes(List[] l1, List[] l2, String outputDir, String fileName, String header, int offset) throws FileNotFoundException {
        try (PrintWriter writer2 = new PrintWriter(String.format("%s%s.csv", outputDir, fileName))) {
            for (int i = 0; i < l1.length; i++) {
                writer2.println(String.format("%d,%d, %d", i + offset, l1[i].size(), l2[i].size()));
            }
        }
    }

    public static void mergeNodeFileWithCommunityFile(String nFile, String cFile, String outputFileName) throws FileNotFoundException, IOException {
        int i = 0; // node id
        LinkedList<Node> nodes = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(nFile))) {
            String line = reader.readLine(); // Skip  the first line.
            while ((line = reader.readLine()) != null) {
                String[] connections = line.split(COMMA);
                Node n = new Node(i, connections[0]);
                n.setLat(Double.parseDouble(connections[2]));
                n.setLon(Double.parseDouble(connections[3]));
                n.setCommunityId(Integer.valueOf(connections[4]));
                nodes.add(n);
                i++;
            }
        }

        LinkedList<Integer> cs = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(cFile))) {
            String line = reader.readLine(); // Skip  the first line.
            while ((line = reader.readLine()) != null) {
                String[] connections = line.split(COMMA);
                cs.add(Integer.parseInt(connections[1]));
            }
        }

        if (nodes.size() != cs.size()) {
            System.out.println("Wrong node sizes.");
            System.exit(-1);
        }

        for (i = 0; i < nodes.size(); i++) {
            nodes.get(i).setCommunityId(cs.get(i));
        }

        try (PrintWriter nodeWriter = new PrintWriter(outputFileName)) {
            nodeWriter.println("Id,UName,Lat,Lon,community");
            for (Node n : nodes) {
                nodeWriter.println(String.format("%s,%s,%f,%f,%d", n.getName(), n.getUName(), n.getLat(), n.getLon(), n.getCommunityId()));
            }
        }
    }

    public static void writeModularityTrend(HashMap<Integer, List<Double>> modularities, String outputDir, String fileName) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format("%s%s.csv", outputDir, fileName));
                PrintWriter writer2 = new PrintWriter(String.format("%s%s-errors.csv", outputDir, fileName))) {
            Iterator<Entry<Integer, List<Double>>> iterator = modularities.entrySet().iterator();
            String splitter = ",";
            StringBuilder header = new StringBuilder();
            Iterator[] iterators = new Iterator[modularities.size()];
            int j = 0;
            while (iterator.hasNext()) {
                Entry<Integer, List<Double>> entry = iterator.next();
                iterators[j] = entry.getValue().iterator();
                j++;
                header.append(entry.getKey()).append(splitter);

                double[] ms = ArrayUtils.toPrimitive(entry.getValue().toArray(new Double[entry.getValue().size()]));
                double mean = new Mean().evaluate(ms);
                double stderror = new StandardDeviation().evaluate(ms) / Math.sqrt(ms.length);
                writer2.println(String.format("%d,%f,%f", entry.getKey(), mean, stderror));
//                writer2.print(i + offset);
//                writer2.println(String.format(",%d", latencies[i].size()));
            }
            writer.println(header.toString());
            boolean finished = false;
            while (!finished) {
                finished = true;
                for (Iterator i : iterators) {
                    if (i.hasNext()) {
                        writer.print(i.next());
                        finished = false;
                    }
                    writer.print(splitter);
                }
                writer.println();
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
    public Graph convertAndReadGephi(String nodeFile, String edgeFile) throws FileNotFoundException, IOException {
        Graph g = new Graph();
        int i = 0; // node id
        try (BufferedReader reader = new BufferedReader(new FileReader(nodeFile))) {
            String line = reader.readLine(); // Skip  the first line.
            while ((line = reader.readLine()) != null) {
                String[] connections = line.split(COMMA);
                Node n = new Node(i, connections[0]);
                n.setLat(Double.parseDouble(connections[3]));
                n.setLon(Double.parseDouble(connections[4]));
                n.setCommunityId(Integer.valueOf(connections[5]));
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
