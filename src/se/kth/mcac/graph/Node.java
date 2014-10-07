package se.kth.mcac.graph;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author hooman
 */
public class Node {

    private final long id; // Unique Vertex ID
    private final List<Edge> edges;
    private float lat, lon;
    private Resource resource;
    private float reliability; // reliability rate

    public Node(long id, float lat, float lon, float reliability, Resource resource) {
        this(id);
        this.lat = lat;
        this.lon = lon;
        this.reliability = reliability;
        this.resource = resource;
    }

    public Node(long id) {
        this.id = id;
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
     * @return the id
     */
    public long getId() {
        return id;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Node && ((Node) obj).id == this.id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    /**
     * @return the lat
     */
    public float getLat() {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(float lat) {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    public float getLon() {
        return lon;
    }

    /**
     * @param lon the lon to set
     */
    public void setLon(float lon) {
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
