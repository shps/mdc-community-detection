/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation.communitycloud;

import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.simulation.communitycloud.OpenStackUtil.SelectionStrategy;

/**
 *
 * @author ganymedian
 */
public class OpenStackUtilTest {

    Graph g = null;
    HashMap<Node, HashMap<Node, List<Edge>>> routingMap = null;

    public OpenStackUtilTest() {
        Node n1 = new Node(0, "0");
        n1.setCommunityId(1);
        Node n2 = new Node(1, "1");
        n2.setCommunityId(1);
        Node n3 = new Node(2, "2");
        n3.setCommunityId(1);
        Node n4 = new Node(3, "3");
        n4.setCommunityId(1);
        Node n5 = new Node(4, "4");
        n5.setCommunityId(1);
        Node n6 = new Node(5, "5");
        n6.setCommunityId(2);
        Node n7 = new Node(6, "6");
        n7.setCommunityId(2);
        Node n8 = new Node(7, "7");
        n8.setCommunityId(2);

        Edge e12 = new Edge(12, n1.getName(), n2.getName());
        Edge e13 = new Edge(13, n1.getName(), n3.getName());
        Edge e16 = new Edge(16, n1.getName(), n6.getName());
        Edge e17 = new Edge(17, n1.getName(), n7.getName());
        n1.addEdges(e12, e13, e16, e17);

        Edge e21 = new Edge(21, n2.getName(), n1.getName());
        Edge e24 = new Edge(24, n2.getName(), n4.getName());
        Edge e25 = new Edge(25, n2.getName(), n5.getName());
        n2.addEdges(e21, e24, e25);

        Edge e31 = new Edge(31, n3.getName(), n1.getName());
        Edge e35 = new Edge(35, n3.getName(), n5.getName());
        Edge e37 = new Edge(37, n3.getName(), n7.getName());
        n3.addEdges(e31, e35, e37);

        Edge e42 = new Edge(42, n4.getName(), n2.getName());
        Edge e45 = new Edge(45, n4.getName(), n5.getName());
        n4.addEdges(e42, e45);

        Edge e52 = new Edge(52, n5.getName(), n2.getName());
        Edge e53 = new Edge(53, n5.getName(), n3.getName());
        Edge e54 = new Edge(54, n5.getName(), n4.getName());
        n5.addEdges(e52, e53, e54);

        Edge e61 = new Edge(61, n6.getName(), n1.getName());
        n6.addEdge(e61);

        Edge e71 = new Edge(71, n7.getName(), n1.getName());
        Edge e73 = new Edge(73, n7.getName(), n3.getName());
        n7.addEdges(e71, e73);

        g = new Graph();
        g.addNodes(n1, n2, n3, n4, n5, n6, n7, n8);
        HashMap<Integer, HashMap<String, Node>> communities = g.getCommunities();

        routingMap = new HashMap<>();
        for (Node n : g.getNodes()) {
            if (n.getCommunityId() == 1) {
                routingMap.put(n, RoutingProtocolsUtil.findRoutings(n, communities.get(1), RoutingProtocolsUtil.RoutingProtocols.SIMPLE_SHORTEST_PATH));
            }
        }
    }

    @Test
    public void testComputeBetweennessCentralityScores() {
        HashMap<String, Integer> scores = OpenStackUtil.computeBetweennessCentralityScores(routingMap);
        assert scores.get("0") == 2;
        assert scores.get("1") == 4;
        assert scores.get("2") == 0;
        assert scores.get("3") == 0;
        assert scores.get("4") == 2;
    }

    /**
     * Test of selectNode method, of class OpenStackUtil.
     */
    @Test
    public void testSelectNode() {
        Node n1 = g.getNode(OpenStackUtil.selectNode(SelectionStrategy.BETWEENNESS_CENTRALITY, routingMap));
        Node n2 = g.getNode(OpenStackUtil.selectNode(SelectionStrategy.BETWEENNESS_CENTRALITY, routingMap, n1));
        Node n3 = g.getNode(OpenStackUtil.selectNode(SelectionStrategy.BETWEENNESS_CENTRALITY, routingMap, n1, n2));
        Node n4 = g.getNode(OpenStackUtil.selectNode(SelectionStrategy.BETWEENNESS_CENTRALITY, routingMap, n1, n2, n3));
        Node n5 = g.getNode(OpenStackUtil.selectNode(SelectionStrategy.BETWEENNESS_CENTRALITY, routingMap, n1, n2, n3, n4));

        assert n1.getId() == 1;
        assert n2.getId() == 0;
        assert n3.getId() == 4;
        assert n4.getId() == 2;
        assert n5.getId() == 3;
    }

}
