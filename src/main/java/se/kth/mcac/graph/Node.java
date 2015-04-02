package se.kth.mcac.graph;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author hooman
 */
public class Node {

    private String name; // Unique node name in the network.
    private String uName;
    private int id; // Internal unique id assigned to each node starting from 0, to simplify the semantic and overhead of algorithms.
    private int communityId; // Community name
    private final List<Edge> edges;
    private double lat, lon;
    private Resource resource;
    private float reliability; // reliability rate
    private float bc = 0; // betweenness centrality

    public Node(int id, String name, double lat, double lon, float reliability, Resource resource) {
        this(id, name);
        this.lat = lat;
        this.lon = lon;
        this.reliability = reliability;
        this.resource = resource;
    }

    public Node(int id, String name) {
        this.id = id;
        this.name = name;
        communityId = -1;
        edges = new LinkedList<>();
    }

    /**
     * Adds a new edge to the edge lists.
     *
     * @param e
     */
    public void addEdge(Edge e) {
        edges.add(e);
    }

    public boolean removeEdge(Edge e) {
        return edges.remove(e);
    }

    /**
     *
     * @param es
     */
    public void addEdges(Edge... es) {
        edges.addAll(Arrays.asList(es));
    }

    /**
     * Returns the degree of this node.
     *
     * @return
     */
    public int getDegree() {
        return edges.size();
    }

    /**
     * returns sum of the weights. If the graph is directed, this only returns
     * the sum of the outbound edges.
     *
     * @return
     */
    public double getSumOfWeights() {
        double sum = 0;
        for (Edge e : edges) {
            sum += e.getWeight();
        }

        return sum;
    }

    /**
     * @return the name
     */
    public int getId() {
        return id;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Finds the connecting edge to the nodeId and returns null if there is no
     * connection. O(k)
     *
     * @param nodeId
     * @return
     */
    public Edge getEdge(String nodeId) {
        for (Edge e : edges) {
            if (e.getDst().equalsIgnoreCase(nodeId)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Node && (((Node) obj).getName() == null ? this.getName() == null : ((Node) obj).getName().equals(this.getName()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    /**
     * @return the lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    public double getLon() {
        return lon;
    }

    /**
     * @param lon the lon to set
     */
    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * @return the resource
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * @return the reliability
     */
    public float getReliability() {
        return reliability;
    }

    /**
     * @param reliability the reliability to set
     */
    public void setReliability(float reliability) {
        this.reliability = reliability;
    }

    /**
     * @return the communityId
     */
    public int getCommunityId() {
        return communityId;
    }

    /**
     * @param communityId the communityId to set
     */
    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the uName
     */
    public String getUName() {
        return uName;
    }

    /**
     * @param uName the uName to set
     */
    public void setUName(String uName) {
        this.uName = uName;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the bc
     */
    public float getBc() {
        return bc;
    }

    /**
     * @param bc the bc to set
     */
    public void setBc(float bc) {
        this.bc = bc;
    }

    public class Resource {

        private final int memory, cpu, storage;

        public Resource(int memory, int cpu, int storage) {
            this.memory = memory;
            this.cpu = cpu;
            this.storage = storage;
        }

        /**
         * @return the memory
         */
        public int getMemory() {
            return memory;
        }

        /**
         * @return the cpu
         */
        public int getCpu() {
            return cpu;
        }

        /**
         * @return the storage
         */
        public int getStorage() {
            return storage;
        }
    }
}
