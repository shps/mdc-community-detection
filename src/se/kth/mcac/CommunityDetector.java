
package se.kth.mcac;

import java.io.IOException;
import se.kth.mcac.util.QmpsuConvertor;

/**
 *
 * @author hooman
 */
public class CommunityDetector {
    
    private static final String DEFAULT_FILE_DIR = "/home/hooman/Desktop/graph-5434e0f1.json";
    
    
    public static void main(String[] args) throws IOException
    {
        QmpsuConvertor convertor = new QmpsuConvertor();
        convertor.convertToGraph(DEFAULT_FILE_DIR);
    }
}
