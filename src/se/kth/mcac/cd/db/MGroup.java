package se.kth.mcac.cd.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import se.kth.mcac.cd.Community;
import se.kth.mcac.cd.CommunityDetector;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class MGroup implements CommunityDetector {

    @Override
    public void findCommunities(Graph g) {
        ArrayList<Community> coms = new ArrayList<>(Arrays.asList(getCommunities(g)));

        int k = coms.size();
        int numCom = k;
        double a = g.getSumOfWeights();

        // TODO: Optimization in terms of computation time by consuming more memory.
        while (numCom > 1) {
            // Assumes that community ids are integers starting from zero to k-1.
//            double[][] deltas = new double[k][k];
            int maxi = 0, maxj = 0;
            double maxQ = Double.MIN_VALUE;
            for (int i = 0; i < coms.size(); i++) {
                for (int j = i + 1; j < coms.size(); j++) {
                    Community c1 = coms.get(i);
                    Community c2 = coms.get(j);
                    double deltaQ = computeDeltaQ(a, c1, c2, g);
//                    deltas[c1.getId()][c2.getId()] = deltaQ;
                    if (deltaQ > maxQ) {
                        maxQ = deltaQ;
                        maxi = i;
                        maxj = j;
                    }
                }
            }

            if (maxQ > 0) {
                Community c1 = coms.get(maxi);
                Community c2 = coms.get(maxj);
                c1.addNodes(c2.getNodes());
                coms.remove(maxj);
                numCom--;
            } else {
                break;
            }
        }
    }

    private double computeDeltaQ(double a, Community c1, Community c2, Graph g) {
        double epq = computeE(a, c1, c2);
        double eqp = computeE(a, c2, c1);
        double apbq = computeAQ(a, c1) * computeBQ(a, c2, g);
        double aqbp = computeAQ(a, c2) * computeBQ(a, c1, g);

        return epq + eqp - apbq - aqbp;
    }

    private double computeE(double a, Community p, Community q) {
        double e = 0;

        Node[] pNodes = p.getNodes();
        for (Node n : pNodes) {
            for (Edge edge : n.getEdges()) {
                if (q.getNode(edge.getDst()) != null) // If the destination node is in c2
                {
                    e += edge.getWeight();
                }
            }
        }

        return e / a;
    }

    private double computeAQ(double a, Community q) {
        double aq = 0;

        Node[] pNodes = q.getNodes();
        for (Node n : pNodes) {
            for (Edge edge : n.getEdges()) {
                aq += edge.getWeight();
            }
        }

        return aq / a;
    }

    /**
     * This method assumes that there exist links in both directions between two
     * nodes.
     *
     * @param a
     * @param q
     * @param g
     * @return
     */
    private double computeBQ(double a, Community q, Graph g) {
        double bq = 0;

        Node[] pNodes = q.getNodes();
        for (Node n : pNodes) {
            for (Edge edge : n.getEdges()) {
                Node dst = g.getNode(edge.getDst());
                bq += dst.getEdge(n.getName()).getWeight();
            }
        }

        return bq / a;
    }

    /**
     * Get communities. This method is O(number of vertices).
     *
     * @return
     */
    private Community[] getCommunities(Graph g) {
        Map<Integer, Community> temp = new HashMap<>();
        for (Node n : g.getNodes()) {
            Community c = temp.get(n.getCommunityId());
            if (c == null) {
                c = new Community(n.getCommunityId());
                temp.put(c.getId(), c);
            }
            c.addNode(n);
        }

        // Sets community Ids are from 0 to number of communities
        Community[] communities = new Community[temp.size()];
        int id = 0;
        for (Community c : temp.values()) {
            c.setId(id);
            communities[c.getId()] = c;
            id++;
        }

        return communities;
    }

}
