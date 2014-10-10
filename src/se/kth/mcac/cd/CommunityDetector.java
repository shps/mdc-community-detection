package se.kth.mcac.cd;

import java.util.List;
import se.kth.mcac.graph.Graph;

/**
 *
 * @author hooman
 */
public interface CommunityDetector {

    /**
     * Finds the communities of a given graph. The algorithm depends to the
     * implementation of the community detector.
     *
     * @param graph
     * @return
     */
    public List<Community> findCommunities(Graph graph);
}
