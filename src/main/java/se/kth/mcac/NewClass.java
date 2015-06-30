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

    static final String DEFAULT_FILE_DIR = "/home/ganymedian/Desktop/sant-upc/samples/";
    static final String FILE_NAME = "graph-16march15-1000.json";
    static final boolean EXCLUDE_DISCONNECTED_NODES = true;

    public static void main(String[] args) throws IOException {
//        QmpsuConvertor convertor = new QmpsuConvertor();
        CsvConvertor convertor = new CsvConvertor();
//
        Graph g = convertor.convertAndRead("/home/ganymedian/Desktop/sant-upc/samples/geonodes.csv", "/home/ganymedian/Desktop/sant-upc/samples/edges.csv");
        CsvConvertor.convertAndWriteWithGeoLocation(g, "/home/ganymedian/Desktop/sant-upc/samples/geoedges.csv");

        String outputDir = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/kmeans/";
        String inputDir = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/kmeans/clusters/";
        String nodeFile = inputDir + "geonodes.csv";
        for (int i = 2; i <= 9; i++) {
            CsvConvertor.mergeNodeFileWithCommunityFile(nodeFile, String.format("%s%d.csv", inputDir, i), String.format("%s%d.csv", outputDir, i));
        }
//        Graph g = new QmpsuConvertor().convertToGraph("/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/new-graph-553dde71.json", false, false);
//        CsvConvertor.convertAndWrite(g, "/home/ganymedian/Desktop/new");
    }
}
