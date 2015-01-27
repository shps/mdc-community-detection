/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.mcac.simulation;

import java.io.FileNotFoundException;
import java.util.HashMap;
import org.junit.Test;
import se.kth.mcac.graph.Edge;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.graph.Node;
import se.kth.mcac.simulation.communitycloud.OpenStackUtil;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil;
import se.kth.mcac.simulation.communitycloud.RoutingProtocolsUtil.TreeNode;

/**
 *
 * @author ganymedian
 */
public class SimulationTest {

    public SimulationTest() {
    }

    /**
     * Test of main method, of class Simulation.
     *
     * @throws java.io.FileNotFoundException
     */
    @Test
    public void testExecute() throws FileNotFoundException {
        Node n1 = new Node(0, "0"); // DBMQ node
        n1.setCommunityId(1);
        Node n2 = new Node(1, "1"); // Controller node
        n2.setCommunityId(1);
        Node n3 = new Node(2, "2"); // Compute node
        n3.setCommunityId(1);

        Edge e12 = new Edge(0, n1.getName(), n2.getName());
        e12.setLatency(3);
        n1.addEdge(e12);
        Edge e21 = new Edge(1, n2.getName(), n1.getName());
        e21.setLatency(4);
        Edge e23 = new Edge(3, n2.getName(), n3.getName());
        e23.setLatency(1);
        n2.addEdges(e21, e23);
        Edge e32 = new Edge(4, n3.getName(), n2.getName());
        e32.setLatency(2);
        n3.addEdge(e32);
        Graph g = new Graph();
        g.addNodes(n1, n2, n3);
        HashMap<Node, HashMap<Node, TreeNode>> routingMap = new HashMap<>();
        for (Node n : g.getNodes()) {
            routingMap.put(n, RoutingProtocolsUtil.findRoutings(n, g, RoutingProtocolsUtil.RoutingProtocols.SHORTEST_PATH_BASED_ON_LATENCY));
        }
        HashMap<Node, Float> results = Simulation.execute(1, g.getCommunities().get(1), routingMap, false);
        assert results.size() == 1;
        float t = OpenStackUtil.computeBootVMLatency(
                e21.getLatency(),
                e23.getLatency(),
                e12.getLatency(),
                e12.getLatency() + e23.getLatency(),
                e32.getLatency() + e21.getLatency(),
                e32.getLatency());
        assert results.get(n3) == t;
    }

}
