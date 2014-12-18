package se.kth.mcac.cd;

import java.util.List;
import se.kth.mcac.graph.Graph;

/**
 *
 * @author hooman
 */
public class SGroup implements CommunityDetector {

    @Override
    public void findCommunities(Graph g) {
        float[][] relMatrix = computeRelevanceMatrix(g);
        List<Community> communities = OBOGroup(g, relMatrix);
        communities = MGroup(communities);
    }

    private float[][] computeRelevanceMatrix(Graph g) {
        float[][] relMatrix = new float[g.size()][g.size()];
        float[][] normMatrix = computeNormalizedGraphLaplacian(g);

        return relMatrix;
    }

    private float[][] computeNormalizedGraphLaplacian(Graph g) {
        float[][] normGraph = new float[g.size()][g.size()];
        return normGraph;
    }

    private float[][] BLin(float[][] w, float[] e) {
        float[][] r = new float[w.length][w.length];

        return r;
    }

    private List<Community> OBOGroup(Graph g, float[][] relMatrix) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<Community> MGroup(List<Community> communities) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
