package se.kth.mcac.util;

import se.kth.mcac.graph.Edge;
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
     * @param weightedDirected
     * @return
     */
    public static double compute(Graph g, boolean weightedDirected) {
        if (!weightedDirected) {
            return computeUnWeightedUndirected(g);
        } else {
            return computeWeightedDirected(g);
        }
    }

    /**
     * Computes modularity for undirected unweighted graph.
     *
     * @param g
     * @return
     */
    private static double computeUnWeightedUndirected(Graph g) {
        double q = 0; // modularity
        double m = (double) g.getNumOfEdges(); // Assuming the graph is undirected. However, we do not divide it by 2 because we need 2m in the rest of the computation.

        for (Node ni : g.getNodes()) {
            for (Node nj : g.getNodes()) {
                if (ni.getCommunityId() == nj.getCommunityId()) {
                    double a = 0;
                    if (ni.getEdge(nj.getName()) != null) {
                        a = 1;
                    }

                    q += a - ((double) ni.getDegree() * (double) nj.getDegree() / m);
                }
            }
        }

        return q / m;
    }

    private static double computeWeightedDirected(Graph g) {
        double q = 0;
        double m = g.getSumOfWeights();

        for (Node ni : g.getNodes()) {
            for (Node nj : g.getNodes()) {
                if (ni.getCommunityId() == nj.getCommunityId()) {
                    double a = 0;
                    Edge e = ni.getEdge(nj.getName());
                    if (e != null) {
                        a = (double) e.getWeight();
                    }

                    q += a - (ni.getSumOfWeights() * getSumOfInboundWeights(nj, g) / m);
                }
            }
        }

        return q / m;
    }

    private static double getSumOfInboundWeights(Node n, Graph g) {
        double sum = 0;
        for (Edge e : n.getEdges()) {
            Node dst = g.getNode(e.getDst());
            Edge inEdge = dst.getEdge(n.getName());
            sum += (double) inEdge.getWeight();
        }

        return sum;
    }

}
