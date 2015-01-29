/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation.communitycloud;

import java.util.HashMap;
import java.util.Objects;
import org.junit.Test;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.simulation.communitycloud.OpenStackUtil.SelectionStrategy;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.TreeNode;

/**
 *
 * @author ganymedian
 */
public class OpenStackUtilTest {

    Graph g = null;
    HashMap<Node, HashMap<Node, TreeNode>> routingMap = null;
    HashMap<Integer, HashMap<String, Node>> communities = null;

    public OpenStackUtilTest() {
        Node n0 = new Node(0, "0");
        n0.setCommunityId(2);
        Node n1 = new Node(1, "1");
        n1.setCommunityId(1);
        Node n2 = new Node(2, "2");
        n2.setCommunityId(1);
        Node n3 = new Node(3, "3");
        n3.setCommunityId(1);
        Node n4 = new Node(4, "4");
        n4.setCommunityId(1);
        Node n5 = new Node(5, "5");
        n5.setCommunityId(2);
        Node n6 = new Node(6, "6");
        n6.setCommunityId(2);
        Node n7 = new Node(7, "7");
        n7.setCommunityId(2);
        Node n8 = new Node(8, "8");
        n8.setCommunityId(2);
        Node n9 = new Node(9, "9");
        n9.setCommunityId(2);
        Node n10 = new Node(10, "10");
        n10.setCommunityId(2);

        Edge e01 = new Edge(1, n0.getName(), n1.getName());
        e01.setLatency(10);
        Edge e02 = new Edge(2, n0.getName(), n2.getName(), 1, 1, 1);
        Edge e05 = new Edge(5, n0.getName(), n5.getName(), 1, 1, 1);
        Edge e06 = new Edge(6, n0.getName(), n6.getName(), 1, 1, 1);
        n0.addEdges(e01, e02, e05, e06);

        Edge e10 = new Edge(10, n1.getName(), n0.getName(), 1, 1, 1);
        Edge e13 = new Edge(13, n1.getName(), n3.getName(), 1, 1, 1);
        Edge e14 = new Edge(14, n1.getName(), n4.getName(), 1, 1, 1);
        n1.addEdges(e10, e13, e14);

        Edge e20 = new Edge(20, n2.getName(), n0.getName());
        e20.setLatency(10);
        Edge e24 = new Edge(24, n2.getName(), n4.getName(), 1, 1, 1);
        Edge e26 = new Edge(26, n2.getName(), n6.getName(), 1, 1, 1);
        n2.addEdges(e20, e24, e26);

        Edge e31 = new Edge(31, n3.getName(), n1.getName());
        e31.setLatency(10);
        Edge e34 = new Edge(34, n3.getName(), n4.getName(), 1, 1, 1);
        n3.addEdges(e31, e34);

        Edge e41 = new Edge(41, n4.getName(), n1.getName(), 1, 1, 1);
        Edge e42 = new Edge(42, n4.getName(), n2.getName(), 1, 1, 1);
        Edge e43 = new Edge(43, n4.getName(), n3.getName(), 1, 1, 1);
        n4.addEdges(e41, e42, e43);

        Edge e50 = new Edge(50, n5.getName(), n0.getName(), 1, 1, 1);
        Edge e57 = new Edge(57, n5.getName(), n7.getName(), 1, 1, 1);
        n5.addEdges(e50, e57);

        Edge e60 = new Edge(60, n6.getName(), n0.getName(), 1, 1, 1);
        Edge e62 = new Edge(62, n6.getName(), n2.getName(), 1, 1, 1);
        Edge e67 = new Edge(67, n6.getName(), n7.getName(), 1, 1, 1);
        n6.addEdges(e60, e62, e67);

        Edge e75 = new Edge(75, n7.getName(), n5.getName(), 1, 1, 1);
        Edge e76 = new Edge(76, n7.getName(), n6.getName(), 1, 1, 1);
        Edge e78 = new Edge(78, n7.getName(), n8.getName(), 1, 1, 1);
        Edge e79 = new Edge(79, n7.getName(), n9.getName(), 1, 1, 1);
        n7.addEdges(e75, e76, e78, e79);

        Edge e810 = new Edge(810, n8.getName(), n10.getName(), 1, 1, 1);
        Edge e87 = new Edge(87, n8.getName(), n7.getName(), 1, 1, 1);
        n8.addEdges(e810, e87);

        Edge e97 = new Edge(97, n9.getName(), n7.getName(), 1, 1, 1);
        Edge e910 = new Edge(910, n9.getName(), n10.getName(), 1, 1, 1);
        n9.addEdges(e97, e910);

        Edge e108 = new Edge(108, n10.getName(), n8.getName(), 1, 1, 1);
        Edge e109 = new Edge(109, n10.getName(), n9.getName(), 1, 1, 1);
        n10.addEdges(e108, e109);

        g = new Graph();
        g.addNodes(n0, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10);
        communities = g.getCommunities();

        routingMap = new HashMap<>();
        for (Node n : g.getNodes()) {
            routingMap.put(n, RoutingProtocolsUtil.findRoutings(n, g, RoutingProtocolsUtil.RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY));
        }
    }

    @Test
    public void testComputeBetweennessCentralityScores() {
        HashMap<String, Float> scores = OpenStackUtil.computeBetweennessCentralityScores(communities.get(1), routingMap);
        assert scores.get("1") == 0;
        assert scores.get("2") == 0;
        assert scores.get("3") == 0;
        assert scores.get("4") == 4.5;

        scores = OpenStackUtil.computeBetweennessCentralityScores(communities.get(2), routingMap);
        assert scores.get("7") == 20;

        assert scores.get("9") == 4.0 && Objects.equals(scores.get("9"), scores.get("8"));
    }

    /**
     * Test of selectController method, of class OpenStackUtil.
     */
    @Test
    public void testSelectNode() {
        Node n1 = g.getNode(OpenStackUtil.selectController(SelectionStrategy.BETWEENNESS_CENTRALITY, communities.get(1), routingMap));
        Node n2 = g.getNode(OpenStackUtil.selectController(SelectionStrategy.BETWEENNESS_CENTRALITY, communities.get(1), routingMap, n1));
        Node n3 = g.getNode(OpenStackUtil.selectController(SelectionStrategy.BETWEENNESS_CENTRALITY, communities.get(1), routingMap, n1, n2));
        Node n4 = g.getNode(OpenStackUtil.selectController(SelectionStrategy.BETWEENNESS_CENTRALITY, communities.get(1), routingMap, n1, n2, n3));

        assert n1.getId() == 4;
        assert n2.getId() == 1;
        assert n3.getId() == 2;
        assert n4.getId() == 3;

        n1 = g.getNode(OpenStackUtil.selectController(SelectionStrategy.BETWEENNESS_CENTRALITY, communities.get(2), routingMap));
        assert n1.getId() == 7;
    }

}
