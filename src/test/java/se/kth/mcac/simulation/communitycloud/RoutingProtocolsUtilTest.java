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
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.RoutingProtocols;

/**
 *
 * @author ganymedian
 */
public class RoutingProtocolsUtilTest {

    public RoutingProtocolsUtilTest() {
    }

    /**
     * Test of findRoutings method, of class RoutingProtocolsUtil.
     */
    @Test
    public void testFindRoutings() {

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
        n8.setCommunityId(3);

        Edge e12 = new Edge(12, n1.getName(), n2.getName());
        e12.setLatency(10);
        Edge e13 = new Edge(13, n1.getName(), n3.getName());
        Edge e16 = new Edge(16, n1.getName(), n6.getName());
        Edge e17 = new Edge(17, n1.getName(), n7.getName());
        n1.addEdges(e12, e13, e16, e17);

        Edge e21 = new Edge(21, n2.getName(), n1.getName());
        Edge e24 = new Edge(24, n2.getName(), n4.getName());
        Edge e25 = new Edge(25, n2.getName(), n5.getName());
        n2.addEdges(e21, e24, e25);

        Edge e31 = new Edge(31, n3.getName(), n1.getName());
        e31.setLatency(10);
        Edge e35 = new Edge(35, n3.getName(), n5.getName());
        Edge e37 = new Edge(37, n3.getName(), n7.getName());
        n3.addEdges(e31, e35, e37);

        Edge e42 = new Edge(42, n4.getName(), n2.getName());
        e42.setLatency(10);
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

        Graph g = new Graph();
        g.addNodes(n1, n2, n3, n4, n5, n6, n7, n8);

        HashMap<Node, List<Edge>> routingMap = RoutingProtocolsUtil.findRoutings(n1, g, RoutingProtocols.SIMPLE_SHORTEST_PATH);

        // Check the correctness of the routing map
        //n1->n2
        List<Edge> path = routingMap.get(n2);
        assert path.size() == 1;
        assert path.get(0).equals(e12);
        //n1->n3
        path = routingMap.get(n3);
        assert path.size() == 1;
        assert path.get(0).equals(e13);
        //n1->n4
        path = routingMap.get(n4);
        assert path.size() == 2;
        assert path.get(0).equals(e12) && path.get(1).equals(e24);
        //n1->n5
        path = routingMap.get(n5);
        assert path.size() == 2;
        assert (path.get(0).equals(e12) && path.get(1).equals(e25)) || (path.get(0).equals(e13) && path.get(1).equals(e35));
        //n1->n1
        path = routingMap.get(n1);
        assert path.isEmpty();
        
        routingMap = RoutingProtocolsUtil.findRoutings(n4, g, RoutingProtocols.SIMPLE_SHORTEST_PATH);
        //n4->n2
        path = routingMap.get(n2);
        assert path.size() == 1;
        assert path.get(0).equals(e42);
        //n4->n5
        path = routingMap.get(n5);
        assert path.size() == 1;
        assert path.get(0).equals(e45);
        //n4->n1
        path = routingMap.get(n1);
        assert path.size() == 2;
        assert path.get(0).equals(e42) && path.get(1).equals(e21);
        //n4->n3
        path = routingMap.get(n3);
        assert path.size() == 2;
        assert path.get(0).equals(e45) && path.get(1).equals(e53);
        
        routingMap = RoutingProtocolsUtil.findRoutings(n1, g, RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY);
        path = routingMap.get(n2);
        assert path.size() == 3;
        assert path.get(0).equals(e13) && path.get(1).equals(e35) && path.get(2).equals(e52);
        //n1->n3
        path = routingMap.get(n3);
        assert path.size() == 1;
        assert path.get(0).equals(e13);
        //n1->n4
        path = routingMap.get(n4);
        assert path.size() == 3;
        assert path.get(0).equals(e13) && path.get(1).equals(e35) && path.get(2).equals(e54);
        //n1->n5
        path = routingMap.get(n5);
        assert path.size() == 2;
        assert path.get(0).equals(e13) && path.get(1).equals(e35);
        //n1->n1
        path = routingMap.get(n1);
        assert path.isEmpty();
        
        routingMap = RoutingProtocolsUtil.findRoutings(n4, g, RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY);
        //n4->n2
        path = routingMap.get(n2);
        assert path.size() == 2;
        assert path.get(0).equals(e45) && path.get(1).equals(e52);
        //n4->n5
        path = routingMap.get(n5);
        assert path.size() == 1;
        assert path.get(0).equals(e45);
        //n4->n1
        path = routingMap.get(n1);
        assert path.size() == 3;
        assert path.get(0).equals(e45) && path.get(1).equals(e52) && path.get(2).equals(e21);
        //n4->n3
        path = routingMap.get(n3);
        assert path.size() == 2;
        assert path.get(0).equals(e45) && path.get(1).equals(e53);
    }

}
