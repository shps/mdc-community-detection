/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac;

import java.io.FileNotFoundException;
import se.kth.mcac.cd.db.DiffusionBasedCommunityDetector;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.util.CsvConvertor;

/**
 *
 * @author ganymedian
 */
public class TestScenarios {

    static final String OUTPUT_DIR = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/cd/test/";

    public static void main(String[] args) throws FileNotFoundException {
        Graph g = new Graph();
        Node n1 = new Node(0, "GSsalou25");
        Node n2 = new Node(1, "GSsantacatalina59-RS-b9");
        Node n3 = new Node(2, "BCNSantacatalina59Rd");
        Node n4 = new Node(3, "BCNStaCatalina59Nt2e8dd");

        Edge e1 = new Edge(0, n1.getName(), n2.getName(), 3.116f, 6.272f, 0);
        Edge e2 = new Edge(0, n2.getName(), n1.getName(), 4.088f, 7.608f, 0);

        n1.addEdge(e1);
        n2.addEdge(e2);

        Edge e3 = new Edge(0, n2.getName(), n3.getName(), 85f, 0.384615f, 0);
        Edge e4 = new Edge(0, n3.getName(), n2.getName(), 91.307693f, 0.315385f, 0);

        n2.addEdge(e3);
        n3.addEdge(e4);

        Edge e5 = new Edge(0, n3.getName(), n4.getName(), 90.253838f, 0.4f, 0);
        Edge e6 = new Edge(0, n4.getName(), n3.getName(), 91.630775f, 0.319231f, 0);
        n3.addEdge(e5);
        n4.addEdge(e6);
        g.addNodes(n1, n2, n3, n4);

        DiffusionBasedCommunityDetector dc = new DiffusionBasedCommunityDetector();
        int i = 10;
        dc.findCommunities(g, i, false);
        CsvConvertor.convertAndWrite(g, OUTPUT_DIR + String.valueOf(i));
        System.out.println(String.format("Iteration: %d, nCom: %d", i, g.getNumCommunities()));

    }
}
