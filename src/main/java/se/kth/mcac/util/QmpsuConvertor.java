package se.kth.mcac.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class QmpsuConvertor {

    public static final String ADJACENCIES = "adjacencies";
    public static final String DATA = "data";
    public static final String NODE_TO = "nodeTo";
    public static final String ID = "id";
    public static final String BW = "bw";
    public static final String RTT = "rtt";
    public static final String NODE_NAME = "id";
    public static final String UNAME = "name";
    public Random random = new Random();

    private final List<Node> noLocationNodes = new LinkedList<>();
    private final List<Edge> noBwEdges = new LinkedList<>();
    private final List<Edge> noRttEdges = new LinkedList<>();

    private static final float DEFAULT_BW = 20f;
    private static final float DEFAULT_RTT = 2f;

    private int nFullInfo;
    private int nMissingInfo;

    /**
     * Converts QMPSU's JSON file to Graph object.
     *
     * @param jsonFile
     * @param applyCorrections
     * @param excludeDisconnectedNodes
     * @return
     * @throws IOException
     */
    public Graph convertToGraph(String jsonFile, boolean applyCorrections, boolean excludeDisconnectedNodes) throws IOException {
        nFullInfo = 0;
        nMissingInfo = 0;
        Graph graph = new Graph();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readValue(new File(jsonFile), JsonNode.class);
        extractGraph(rootNode, graph);
        if (applyCorrections) {
            generateMissingData(graph);
        }
        setEdgesUNames(graph);

        if (excludeDisconnectedNodes) {
            excludeDisconnectedNodes(graph);
            //Correct Ids.
            Node[] nodes = graph.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].setId(i);
            }
        }

        System.out.println(String.format("Edges with full info = %d, Edges with missing info = %d", nFullInfo, nMissingInfo));
        System.out.println(String.format("Graph size = %d", graph.size()));

        return graph;
    }

    private void extractGraph(JsonNode rootNode, Graph graph) {
        int n = rootNode.get(0).get("adjacencies").size();
        int id = 0; // assigns an unique id starting from 0 to the nodes.
        for (int i = 1; i <= n; i++) {
            JsonNode jNode = rootNode.get(i);
            String name = jNode.get(NODE_NAME).textValue();
            Node node = new Node(id, name);
            String uName = jNode.get(UNAME).textValue();
            node.setUName(uName);
            graph.addNode(node);
            setNode(jNode, node);
            id++;
        }
    }

    private void setNode(JsonNode jNode, Node node) {
        if (jNode.get("x") != null) {
            double x = jNode.get("x").doubleValue();
            double y = jNode.get("y").doubleValue();
            node.setLat(y);
            node.setLon(x);
            //TODO: Set resources and reliability
        } else {
            System.out.println(String.format("node %s: no position is available.", jNode.get(NODE_NAME)));
            noLocationNodes.add(node);
        }

        boolean fullInfo = true;

        JsonNode adjs = jNode.get(ADJACENCIES);
        if (adjs == null) {
            System.err.println(String.format("Node %s has no connections.", node.getName()));
            return;
        }

        for (int j = 0; j < adjs.size(); j++) {
            String dst = adjs.get(j).get(NODE_TO).textValue();
            Edge edge = new Edge(random.nextLong(), node.getName(), dst);
            edge.setSrcUName(node.getUName());
            node.addEdge(edge);
            if (adjs.get(j).get(DATA).get(BW) != null) {
                edge.setBw(adjs.get(j).get(DATA).get(BW).floatValue());
            } else {
                System.out.println(String.format("Edge %s->%s does not contain BW information.", edge.getSrc(), edge.getDst()));
                noBwEdges.add(edge);
                fullInfo = false;
            }

            if (adjs.get(j).get(DATA).get(RTT) != null) {
                edge.setLatency(adjs.get(j).get(DATA).get(RTT).floatValue());
            } else {
                System.out.println(String.format("Edge %s->%s does not contain RTT information.", edge.getSrc(), edge.getDst()));
                noRttEdges.add(edge);
                fullInfo = false;
            }

            if (fullInfo) {
                nFullInfo++;
            } else {
                nMissingInfo++;
            }
            // TODO: Reliability
        }
    }

    private void generateMissingData(Graph graph) {
        // TODO: generate positions
        // Create missing edges
        Node[] nodes = graph.getNodes();
        for (Node n : nodes) {
            List<Edge> edges = n.getEdges();
            for (Edge e : edges) {
                Node dst = graph.getNode(e.getDst());
                if (dst.getEdge(e.getSrc()) == null) {
                    Edge newEdge = new Edge(random.nextLong(), dst.getName(), e.getSrc());
                    newEdge.setBw(e.getBw());
                    newEdge.setLatency(e.getLatency());
                    dst.addEdge(newEdge);
                }
            }
        }

        for (Edge e : noBwEdges) {
            Edge otherEdge = graph.getNode(e.getDst()).getEdge(e.getSrc());
            if (otherEdge.getBw() != 0) {
                e.setBw(otherEdge.getBw());
            } else {
                e.setBw(DEFAULT_BW);
                otherEdge.setBw(DEFAULT_BW);
            }
        }

        for (Edge e : noRttEdges) {
            Edge otherEdge = graph.getNode(e.getDst()).getEdge(e.getSrc());
            if (otherEdge.getLatency() != 0) {
                e.setLatency(otherEdge.getLatency());
            } else {
                e.setLatency(DEFAULT_RTT);
                otherEdge.setLatency(DEFAULT_RTT);
            }
        }
    }

    private void excludeDisconnectedNodes(Graph graph) {
        for (Node n : graph.getNodes()) {
            if (n.getEdges().isEmpty()) {
                System.out.println(String.format("Node %s is a disconnected node and is removed!", n.getName()));
                graph.remove(n.getName());
            }
        }
    }

    /**
     * @return the nFullInfo
     */
    public int getnFullInfo() {
        return nFullInfo;
    }

    /**
     * @param nFullInfo the nFullInfo to set
     */
    public void setnFullInfo(int nFullInfo) {
        this.nFullInfo = nFullInfo;
    }

    /**
     * @return the nMissingInfo
     */
    public int getnMissingInfo() {
        return nMissingInfo;
    }

    /**
     * @param nMissingInfo the nMissingInfo to set
     */
    public void setnMissingInfo(int nMissingInfo) {
        this.nMissingInfo = nMissingInfo;
    }

    private void setEdgesUNames(Graph graph) {
        for (Node src : graph.getNodes()) {
            for (Edge e : src.getEdges()) {
                e.setSrcUName(src.getUName());
                try {
                    e.setDstUName(graph.getNode(e.getDst()).getUName());
                } catch (NullPointerException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }

    public class Position {

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        private double x, y;

        /**
         * @return the x
         */
        public double getX() {
            return x;
        }

        /**
         * @param x the x to set
         */
        public void setX(double x) {
            this.x = x;
        }

        /**
         * @return the y
         */
        public double getY() {
            return y;
        }

        /**
         * @param y the y to set
         */
        public void setY(double y) {
            this.y = y;
        }
    }
}
