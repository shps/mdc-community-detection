package se.kth.mcac.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author hooman
 */
public class Node {

    private final String name; // Unique node name in the network.
    private final int id;
    private int communityId; // Community name
    private final List<Edge> edges;
    private double lat, lon;
    private Resource resource;
    private float reliability; // reliability rate

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
     * @return a list of edges including the new edge.
     */
    public List<Edge> addEdge(Edge e) {
        edges.add(e);
        return edges;
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
