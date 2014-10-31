package se.kth.mcac.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;

/**
 *
 * @author hooman
 */
public class CsvConvertor {

    public static final String COMMA = ",";
    public static final String DIRECTED = "directed";

    public void convertAndWrite(Graph g, String outputDir) throws FileNotFoundException {
        try (PrintWriter nodeWriter = new PrintWriter(outputDir + "nodes.csv");
                PrintWriter edgeWriter = new PrintWriter(outputDir + "edges.csv")) {
            nodeWriter.println("Id,Lat,Lon,community");
            edgeWriter.println("Source,Target,Type,Id,BW,RTT,Weight");
            Node[] nodes = g.getNodes();

            for (Node n : nodes) {
                nodeWriter.println(String.format("%s,%f,%f,%d", n.getName(), n.getLat(), n.getLon(), n.getCommunityId()));
                for (Edge e : n.getEdges()) {
                    edgeWriter.println(String.format("%s,%s,%s,%d,%f,%f,%f",
                            e.getSrc(), e.getDst(), DIRECTED, e.getId(), e.getBw(), e.getLatency(), e.getWeight()));
                }
            }
        }
    }

}
