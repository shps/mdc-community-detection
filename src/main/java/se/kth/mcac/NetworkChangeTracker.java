package se.kth.mcac;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.util.CsvConvertor;
import se.kth.mcac.util.QmpsuConvertor;

/**
 *
 * @author ganymedian
 */
public class NetworkChangeTracker {

    static final String DEFAULT_FILE_DIR = "/home/ganymedian/Desktop/sant-upc/samples/";
    static final String FILE_NAME = "graph-16march15-1000.json";
    static final boolean EXCLUDE_DISCONNECTED_NODES = true;
    static final String DELIMITER = ":";
    static final int FILE_RANGE = 24;
    static final boolean DROP_BAD_EDGES = true;

    public static void main(String[] args) throws IOException {

        HashMap<String, LinkedList<Edge>> eChanges = new HashMap<>();
        HashMap<String, Node> nodes = new HashMap<>();

        for (int i = 1; i <= 26; i++) {
            System.out.println(i);
            QmpsuConvertor convertor = new QmpsuConvertor();
            Graph g = convertor.convertToGraph(DEFAULT_FILE_DIR + String.valueOf(i) + ".json", false, EXCLUDE_DISCONNECTED_NODES);
            for (Node n : g.getNodes()) {
                nodes.put(n.getUName(), n);
                for (Edge e : n.getEdges()) {
                    String key = e.getSrcUName() + DELIMITER + e.getDstUName();
                    LinkedList<Edge> es;
                    if (eChanges.containsKey(key)) {
                        es = eChanges.get(key);
                    } else {
                        es = new LinkedList<>();
                        eChanges.put(key, es);
                    }

                    es.add(e);
                }
            }
        }

        Graph g = buildGraph(nodes, eChanges);
        checkGraph(g);
        CsvConvertor.convertAndWrite(g, DEFAULT_FILE_DIR + "avg-graph.csv");

        try (PrintWriter nodeWriter = new PrintWriter(DEFAULT_FILE_DIR + "changes.csv")) {
            Iterator<Map.Entry<String, LinkedList<Edge>>> iterator = eChanges.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, LinkedList<Edge>> entry = iterator.next();
                String key = entry.getKey();
                LinkedList<Edge> es = entry.getValue();
                StringBuilder sl = new StringBuilder();
                StringBuilder sb = new StringBuilder();
                for (Edge e : es) {
                    sl.append(e.getLatency()).append(",");
                    sb.append(e.getBw()).append(",");
                }

                nodeWriter.println(String.format("%s, latency, %s, bw, %s", key, sl.toString(), sb.toString()));
            }
        }
    }

    private static Graph buildGraph(HashMap<String, Node> nodes, HashMap<String, LinkedList<Edge>> eChanges) {
        Graph g = new Graph();
        Iterator<Map.Entry<String, LinkedList<Edge>>> iterator = eChanges.entrySet().iterator();
        while (iterator.hasNext()) {
            int bwCounter = 0;
            float bwSum = 0;
            int lCounter = 0;
            float lSum = 0;
            Map.Entry<String, LinkedList<Edge>> entry = iterator.next();
            String key = entry.getKey();
            LinkedList<Edge> es = entry.getValue();
            if (key.equalsIgnoreCase("UPCc6-65ab:UPC-CN-D6-116-4a5c")) {
                System.out.println();
            }
            for (Edge e : es) {
                if (e.getBw() > 0) {
                    bwSum += e.getBw();
                    bwCounter++;
                }

                if (e.getLatency() > 0) {
                    lSum += e.getLatency();
                    lCounter++;
                }
            }

            Edge e = es.getFirst();
            float avgBw = bwSum / (float) bwCounter;
            float avgL = lSum / (float) lCounter;
            if (Float.isNaN(avgL) || Float.isNaN(avgBw)) {
                System.out.println(String.format("Dropped edge %s:%s.", e.getSrcUName(), e.getDstUName()));
                continue;
            }
            e.setBw(avgBw);
            e.setLatency(avgL);
            e.setSrcId(e.getSrcUName());
            e.setDstId(e.getDstUName());
            
            Node n;
            if (g.containsNode(e.getSrcUName())) {
                n = g.getNode(e.getSrcUName());
            } else {
                Node oldNode = nodes.get(e.getSrcUName());
                n = new Node(oldNode.getId(), oldNode.getUName());  // since the names are not unique cross files. But the uname is.
                n.setLat(oldNode.getLat());
                n.setLon(oldNode.getLon());
                n.setUName(oldNode.getUName());
                g.addNode(n);
            }
            n.addEdge(e);
        }

        return g;
    }

    private static void checkGraph(Graph g) {
        for (Node n : g.getNodes())
        {
            for (Edge e : n.getEdges())
            {
                Node dst = g.getNode(e.getDst());
                if (dst.getEdge(n.getName()) == null)
                {
                    n.removeEdge(e);
                    System.out.println(String.format("Single Connection from %s to %s is dropped!!", n.getName(), dst.getName()));
                }
            }
        }
    }
}
