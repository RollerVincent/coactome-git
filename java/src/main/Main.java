package main;

import coactome.*;
import parser.Parser;
import visualization.Visualization;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        FCRA_Network(1e-13, 0.25, -3);

    }

    public static void foldChangeRatioAnalysis(){

        Analysis analysis = new Analysis();
        analysis.setOutputDir("../../results/mouse_differential");
        analysis.setGenes("../../data/experiments/mouse_differential/gene_list.txt");
        analysis.addValues("../../data/experiments/mouse_differential/gene_fc.txt");

        System.out.println("Data initialized starting analysis ...");

        int total = analysis.getGenes().length;
        int count = 1;
        for (Gene gene : analysis.getGenes().all()) {
            analysis.FCRA(gene.id,300, 1000, false);
            System.out.printf("\r%s", "Running FCRA :  " + count + " / " + total + "  (" + gene.id + ")");
            count += 1;
        }


    }


    public static void FCRA_Network(double fdr_cutoff, double mean_cutoff, double varz_cutoff){

        Network network = new Network();

        String dir = "../../results/mouse_differential/fcra";

        Iterator<Path> files = Parser.FileIterator(dir);
        int count = 1;

        while(files.hasNext()){

            String path = files.next().toString();
            String[] split = path.split("/");

            String gene = split[split.length - 2];





            if(gene.startsWith("ENS") && split[split.length - 1].equals("results.tsv.zip")) {

                System.out.printf("\r%s", "Loading :  " + gene + " (" + count + ")  ");
                count += 1;


                if (!network.nodes.containsKey(gene)){
                    network.nodes.put(gene, new Node(gene));
                }

                Node node = network.nodes.get(gene);



                BufferedReader reader = Parser.ZipReader(path);
                if(reader != null) {
                    try {

                        String line;
                        while ((line = reader.readLine()) != null) {

                            if (line.startsWith("#")) {
                                while (line.startsWith("#")) {
                                    line = reader.readLine();
                                }
                                line = reader.readLine();
                            }

                            String[] data = line.split("\t");

                            String other = data[0];


                            float fdr = Float.valueOf(data[2]);
                            float mean = Float.valueOf(data[3]);
                            float varz = Float.valueOf(data[5]);

                            if (fdr <= fdr_cutoff && mean >= mean_cutoff && varz <= varz_cutoff) {
                                if (!network.nodes.containsKey(other)) {
                                    network.nodes.put(other, new Node(other));
                                }

                                Node other_node = network.nodes.get(other);


                                node.links.add(other_node);
                                other_node.links.add(node);
                            }


                        }
                        reader.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


            }



        }
        System.out.println();

        Matrix matrix = network.toMatrix();

        System.out.println("Matrix size is " + matrix.members.length);

        for (int i = 0; i < matrix.size; i++) {
            for (int j = i + 1; j < matrix.size; j++) {
                if (matrix.data[i][j] == 1 && matrix.data[j][i] == 1){
                    matrix.data[j][i] = 0;
                }else if(matrix.data[i][j] == 1){
                    matrix.data[i][j] = 0;
                }else if(matrix.data[j][i] == 1){
                    matrix.data[j][i] = 0;
                }
            }
        }

        for (int i = 0; i < matrix.size; i++) {
            for (int j = i + 1; j < matrix.size; j++) {
                if (matrix.data[i][j] == 0){
                    matrix.data[i][j] = -1;
                }
            }
        }

        System.out.println("Matrix successfully transformed to corresponding format");


        matrix.Save("/Users/vincentroller/Desktop/test_matrix.txt", false);











    }

}
