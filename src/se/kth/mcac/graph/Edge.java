package se.kth.mcac.graph;

/**
 *
 * @author hooman
 */
public class Edge {

    private final long id; // Unique id;
    private final long srcId; // source node
    private final long dstId; // destination node;
    private float bw; // link bandwidth
    private float latency; // link latency
    private float reliability; // reliability rate

    // connection type
    public Edge(long id, long src, long dst, float bw, float latency, float reliability) {
        this(id, src, dst);
        this.bw = bw;
        this.latency = latency;
        this.reliability = reliability;
    }

    public Edge(long id, long src, long dst) {
        this.id = id;
        this.srcId = src;
        this.dstId = dst;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the src
     */
    public long getSrc() {
        return srcId;
    }

    /**
     * @return the dst
     */
    public long getDst() {
        return dstId;
    }

    /**
     * @return the bw
     */
    public float getBw() {
        return bw;
    }

    /**
     * @param bw the bw to set
     */
    public void setBw(float bw) {
        this.bw = bw;
    }

    /**
     * @return the latency
     */
    public float getLatency() {
        return latency;
    }

    /**
     * @param latency the latency to set
     */
    public void setLatency(float latency) {
        this.latency = latency;
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

}
