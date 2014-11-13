package se.kth.mcac.util;

import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class ModularityComputer {

    /**
     * Computes modularity for undirected unweighted graph.
     *
     * @param g
     * @return
     */
    public static float compute(Graph g) {
        float q = 0f; // modularity
        int m = g.getNumOfEdges(); // Assuming the graph is undirected. However, we do not divide it by 2 because we need 2m in the rest of the computation.

        for (Node ni : g.getNodes()) {
            for (Node nj : g.getNodes()) {
                if (ni.getCommunityId() == nj.getCommunityId()) {
                    if (ni.getEdge(nj.getName()) != null) {
                        q += 1;
                    }

                    q -= ni.getDegree() * nj.getDegree() / m;
                }
            }
        }

        return q / m;
    }

}
