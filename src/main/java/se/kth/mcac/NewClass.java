package se.kth.mcac;

import java.io.IOException;
import se.kth.mcac.graph.Graph;
import se.kth.mcac.util.CsvConvertor;
import se.kth.mcac.util.ModularityComputer;
import se.kth.mcac.util.TabSeparatedConvertor;

/**
 *
 * @author hooman
 */
public class NewClass {

    public static void main(String[] args) throws IOException {
        
//        TabSeparatedConvertor tabCon = new TabSeparatedConvertor();
//        Graph g = tabCon.convertToGraph("/home/hooman/Desktop/gephi results/Cit-HepTh.txt");
        String nodeFile = "/home/ganymedian/Desktop/sant-upc/1nodes.csv";
        String edgeFile = "/home/ganymedian/Desktop/sant-upc/1edges.csv";
        CsvConvertor convertor = new CsvConvertor();
//        convertor.convertAndWrite(g, String.format("%s%d", "/home/hooman/Desktop/gephi results/", 1000));
        Graph g = convertor.convertAndRead(nodeFile, edgeFile);
        System.out.println(ModularityComputer.compute(g));
    }
}