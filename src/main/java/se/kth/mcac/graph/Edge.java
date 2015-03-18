package se.kth.mcac.graph;

/**
 *
 * @author hooman
 */
public class Edge {

    private final long id; // Unique id;
    private String srcId; // source node
    private String dstId; // destination node;
    private String srcUName;
    private String dstUName;
    private float bw; // link bandwidth
    private float latency; // link latency
    private float reliability; // reliability rate
    private float weight = 0; // calculated weight of the edge.

    // connection type
    public Edge(long id, String src, String dst, float bw, float latency, float reliability) {
        this(id, src, dst);
        this.bw = bw;
        this.latency = latency;
        this.reliability = reliability;
    }

    public Edge(long id, String src, String dst) {
        this.id = id;
        this.srcId = src;
        this.dstId = dst;
    }

    public void computeWeight() {
//        this.weight = bw + 1/latency;
        this.weight = 1/latency;
    }

    public float getWeight() {
        if (weight == 0) {
            this.computeWeight();
        }

        return weight;
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
    public String getSrc() {
        return getSrcId();
    }

    /**
     * @return the dst
     */
    public String getDst() {
        return getDstId();
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

    /**
     * @param weight the weight to set
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * @return the srcUName
     */
    public String getSrcUName() {
        return srcUName;
    }

    /**
     * @param srcUName the srcUName to set
     */
    public void setSrcUName(String srcUName) {
        this.srcUName = srcUName;
    }

    /**
     * @return the dstUName
     */
    public String getDstUName() {
        return dstUName;
    }

    /**
     * @param dstUName the dstUName to set
     */
    public void setDstUName(String dstUName) {
        this.dstUName = dstUName;
    }

    /**
     * @return the srcId
     */
    public String getSrcId() {
        return srcId;
    }

    /**
     * @param srcId the srcId to set
     */
    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    /**
     * @return the dstId
     */
    public String getDstId() {
        return dstId;
    }

    /**
     * @param dstId the dstId to set
     */
    public void setDstId(String dstId) {
        this.dstId = dstId;
    }
}
