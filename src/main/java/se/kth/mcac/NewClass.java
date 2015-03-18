package se.kth.mcac;

import java.io.IOException;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.util.CsvConvertor;
import se.kth.mcac.util.QmpsuConvertor;

/**
 *
 * @author hooman
 */
public class NewClass {

    static final String DEFAULT_FILE_DIR = "/home/ganymedian/Desktop/sant-upc/";
    static final String FILE_NAME = "graph-16march15-1000.json";
    static final boolean EXCLUDE_DISCONNECTED_NODES = true;

    public static void main(String[] args) throws IOException {
        QmpsuConvertor convertor = new QmpsuConvertor();
        Graph g = convertor.convertToGraph(DEFAULT_FILE_DIR + FILE_NAME, false, EXCLUDE_DISCONNECTED_NODES);
        CsvConvertor.convertAndWrite(g, DEFAULT_FILE_DIR + FILE_NAME);   
    }
}
