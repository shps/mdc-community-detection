package se.kth.mcac.simulation.communitycloud;

import java.util.HashMap;
import org.junit.Test;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.RoutingProtocols;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.TreeNode;

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

        Node n0 = new Node(0, "0");
        n0.setCommunityId(1);
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
        Node n9 = new Node(9, "9");
        Node n10 = new Node(10, "10");

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

        Graph g = new Graph();
        g.addNodes(n0, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10);

        HashMap<Node, TreeNode> routingMap = RoutingProtocolsUtil.findRoutings(n3, g, RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY);
        //n4->n2
        TreeNode rt = routingMap.get(n1);
        assert rt.getN().equals(n3);
        assert rt.getEdges().size() == 1;
        rt = rt.getBranches().get(0);
        assert rt.getN().equals(n4);
        assert rt.getBranches().size() == 1;
        rt = rt.getBranches().get(0);
        assert rt.getN().equals(n1);

        routingMap = RoutingProtocolsUtil.findRoutings(n0, g, RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY);
        //n0->n10
        rt = routingMap.get(n10);
        assert rt.getN().equals(n0);
        assert rt.getBranches().size() == 2;
        TreeNode rt1 = rt.getBranches().get(0);
        TreeNode rt2 = rt.getBranches().get(1);
        assert (rt1.getN().equals(n6) && rt2.getN().equals(n5)) || (rt1.getN().equals(n5) && rt2.getN().equals(n6));
        assert rt1.getBranches().size() == 1;
        assert rt2.getBranches().size() == 1;
        assert rt1.getBranches().get(0).equals(rt2.getBranches().get(0));
        rt = rt1.getBranches().get(0);
        assert rt.getN().equals(n7);
        assert rt.getBranches().size() == 2;
        rt1 = rt.getBranches().get(0);
        rt2 = rt.getBranches().get(1);
        assert (rt1.getN().equals(n8) && rt2.getN().equals(n9)) || (rt1.getN().equals(n9) && rt2.getN().equals(n8));
        assert rt1.getBranches().size() == 1;
        assert rt2.getBranches().size() == 1;
        assert rt1.getBranches().get(0).equals(rt2.getBranches().get(0));
        rt = rt1.getBranches().get(0);
        assert rt.getBranches().isEmpty();
    }

}
