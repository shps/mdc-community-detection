package se.kth.mcac.cd;

import se.kth.mcac.graph.Graph;

/**
 *
 * @author hooman
 */
public interface CommunityDetector {

    /**
     * Finds the communities of a given graph. The algorithm depends to the
     * implementation of the community detector. It sets the community id to each
     * node and it depends to the caller how to use these ids.
     *
     * @param graph
     */
    public void findCommunities(Graph graph);
}
