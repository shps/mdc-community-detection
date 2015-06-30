package se.kth.mcac.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ganymedian
 */
public class GmlConvertor {

    static String nodeOutput = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/guifi/gmlnodes.csv";
    static String edgeOutput = "/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/guifi/gmledges.csv";

    public void convert() throws SAXException, IOException, ParserConfigurationException {
        PrintWriter nodeWriter = new PrintWriter(nodeOutput);
        nodeWriter.println("id,name,type,status,lon,lat");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new File("/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/guifi/nodes"));
        doc.getDocumentElement().normalize();
        NodeList dnodes = doc.getElementsByTagName("dnodes");
        for (int i = 0; i < dnodes.getLength(); i++) {
            NodeList infos = dnodes.item(i).getChildNodes();
            nodeWriter.print(infos.item(3).getFirstChild().getNodeValue());
            nodeWriter.write(",");
            nodeWriter.print(infos.item(5).getFirstChild().getNodeValue());
            nodeWriter.write(",");
            nodeWriter.print(infos.item(7).getFirstChild().getNodeValue());
            nodeWriter.write(",");
            nodeWriter.print(infos.item(9).getFirstChild().getNodeValue());
            nodeWriter.write(",");
            nodeWriter.println(infos.item(1).getFirstChild().getChildNodes().item(0).getFirstChild().getNodeValue());
        }
        nodeWriter.flush();
        nodeWriter.close();

        PrintWriter edgeWriter = new PrintWriter(edgeOutput);
        edgeWriter.println("source,target,srcName,dstName,KMS,link-type,status");
        doc = dBuilder.parse(new File("/home/ganymedian/Desktop/sant-upc/samples/experiments/simulation/final/samples/guifi/links"));
        doc.getDocumentElement().normalize();
        NodeList dlinks = doc.getElementsByTagName("dlinks");
        for (int i = 0; i < dlinks.getLength(); i++) {
            NodeList infos = dlinks.item(i).getChildNodes();
            edgeWriter.print(infos.item(1).getFirstChild().getNodeValue());
            edgeWriter.write(",");
            edgeWriter.print(infos.item(5).getFirstChild().getNodeValue());
            edgeWriter.write(",");
            edgeWriter.print(infos.item(3).getFirstChild().getNodeValue());
            edgeWriter.write(",");
            edgeWriter.print(infos.item(7).getFirstChild().getNodeValue());
            edgeWriter.write(",");
            edgeWriter.print(infos.item(9).getFirstChild().getNodeValue());
            edgeWriter.write(",");
            edgeWriter.print(infos.item(11).getFirstChild().getNodeValue());
            edgeWriter.write(",");
            edgeWriter.println(infos.item(13).getFirstChild().getNodeValue());
        }
        edgeWriter.flush();
        edgeWriter.close();
    }

    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
        GmlConvertor convertor = new GmlConvertor();
        convertor.convert();
    }
}
