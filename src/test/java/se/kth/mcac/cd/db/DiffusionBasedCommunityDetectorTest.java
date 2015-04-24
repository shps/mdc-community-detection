//package se.kth.mcac.cd.db;
//
//import se.kth.mcac.graph.Edge;
//import se.kth.mcac.graph.Graph;
//import se.kth.mcac.graph.Node;
//
///**
// *
// * @author hooman
// */
//public class DiffusionBasedCommunityDetectorTest {
//
//    public DiffusionBasedCommunityDetectorTest() {
//    }
//
//    /**
//     * Test of findCommunities method, of class DiffusionBasedCommunityDetector.
//     */
//    public void testFindCommunities() {
//
//        Graph g = new Graph();
//        Node n1 = new Node(0, "0");
//        Node n2 = new Node(1, "1");
//        Node n3 = new Node(2, "2");
//        Node n4 = new Node(3, "3");
//        Edge e1 = new Edge(0, n1.getName(), n2.getName());
//        Edge e2 = new Edge(1, n1.getName(), n3.getName());
//        Edge e3 = new Edge(2, n1.getName(), n4.getName());
//        n1.addEdge(e1);
//        n1.addEdge(e2);
//        n1.addEdge(e3);
//        Edge e4 = new Edge(4, n2.getName(), n1.getName());
//        n2.addEdge(e4);
//        Edge e5 = new Edge(5, n3.getName(), n1.getName());
//        Edge e6 = new Edge(6, n3.getName(), n4.getName());
//        n3.addEdge(e5);
//        n3.addEdge(e6);
//        Edge e7 = new Edge(7, n4.getName(), n1.getName());
//        Edge e8 = new Edge(8, n4.getName(), n3.getName());
//        n4.addEdge(e7);
//        n4.addEdge(e8);
//
//        g.addNode(n1);
//        g.addNode(n2);
//        g.addNode(n3);
//        g.addNode(n4);
//        
//        DiffusionBasedCommunityDetector detector = new DiffusionBasedCommunityDetector();
//        detector.findCommunities(g, 2);
//        
//        assert n1.getCommunityId() == 0;
//        assert n2.getCommunityId() == 0;
//        assert n3.getCommunityId() == 0;
//        assert n4.getCommunityId() == 0;
//    }
//
//}
