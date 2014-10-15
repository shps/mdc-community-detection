
package se.kth.mcac;

import java.io.IOException;
import se.kth.mcac.cd.db.DiffusionBasedCommunityDetector;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.util.CsvConvertor;
import se.kth.mcac.util.QmpsuConvertor;

/**
 *
 * @author hooman
 */
public class CommunityDetector {
    
    private static final String DEFAULT_FILE_DIR = "/home/hooman/Desktop/";
    
    
    public static void main(String[] args) throws IOException
    {
        QmpsuConvertor convertor = new QmpsuConvertor();
        Graph g = convertor.convertToGraph(DEFAULT_FILE_DIR + "graph-5434e0f1.json");
        se.kth.mcac.cd.CommunityDetector detector = new DiffusionBasedCommunityDetector();
        detector.findCommunities(g);
        CsvConvertor csvc = new CsvConvertor();
        csvc.convert(g, DEFAULT_FILE_DIR);
    }
}
