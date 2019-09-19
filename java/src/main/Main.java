package main;

import coactome.*;
import force_direction.Particle;
import force_direction.ParticleSystem;
import force_direction.Simulation;
import parser.Parser;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {



    

    }

    public static void foldChangeRatioAnalysis(String out, String gene_list, String gene_fc, int minDataPoints, int backgroundSize){

        Analysis analysis = new Analysis();

        //analysis.setOutputDir("../../results/mouse_differential");
        analysis.setOutputDir(out);

        //analysis.setGenes("../../data/experiments/mouse_differential/gene_list.txt");
        analysis.setGenes(gene_list);

        //analysis.addValues("../../data/experiments/mouse_differential/gene_fc.txt");
        analysis.addValues(gene_fc);

        System.out.println("Data initialized starting analysis ...");

        int total = analysis.getGenes().length;
        int count = 1;
        for (Gene gene : analysis.getGenes().all()) {
            // minDataPoints 300    backgroundSize 1000
            analysis.FCRA(gene.id, minDataPoints, backgroundSize, false);
            System.out.printf("\r%s", "Running FCRA :  " + count + " / " + total + "  (" + gene.id + ")");
            count += 1;
        }


    }

    public static void FCRA_Network(String fcra, double fdr_cutoff, double mean_cutoff, double varz_cutoff, int removalCost){

        Network network = new Network();

        Iterator<Path> files = Parser.FileIterator(fcra);
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


        network.setDoubles();
        network.removeUndoubled();

        Matrix matrix = network.toMatrix(true);


        boolean verified = true;
        for (int i = 0; i < matrix.size; i++) {
            for (int j = i + 1; j < matrix.size; j++) {
                if (matrix.data[i][j] != matrix.data[j][i]){
                    System.out.println("Failed verifying matrix symmetry  (" + matrix.data[i][j] + " vs " + matrix.data[j][i] + ")");
                    verified = false;
                }
            }
        }
        if(verified){
            System.out.println("Successfully verified matrix symmetry");
        }





        int link_count = 0;
        for (int i = 0; i < matrix.size; i++) {
            for (int j = i + 1; j < matrix.size; j++) {
                if (matrix.data[i][j] == 0){
                    matrix.data[i][j] = -1;
                }else{
                    link_count += 1;
                    matrix.data[i][j] = removalCost;
                }
            }
        }

        System.out.println("Matrix successfully transformed to corresponding format with");
        System.out.println("size " + matrix.size + " and "+ link_count + " positiv entries");








        matrix.Save(fcra + "/networks/data/network_fdr_" + fdr_cutoff + "_mean_" + mean_cutoff + "_varz_" + varz_cutoff + "_cost_" + removalCost + "/matrix.txt", false);











    }

    public static void ForceDirection(String network, String clustering, String attributes, String mode, double fppCutoff){


        ParticleSystem system = ParticleSystem.FromNetworkFile(network);

        System.out.println(system);

        if(!clustering.equals("")){
            system.loadClustering(clustering);
        }

        system.setSizes("degree");
       // system.setSizes(3);

        Simulation simulation = new Simulation();
        simulation.setSystem(system);




        simulation.run(fppCutoff);


        simulation.correctCollisions();


        if(mode.equals("go")){
            Mapping mapping = Mapping.GO(attributes);


            system.setAttributes(mapping);

            simulation.GoTerms = attributes;
        }


        String o = "";
        String[] s = network.split("/");
        for (int i = 0; i < s.length - 1; i++) {
            o += s[i] + "/";
        }


        simulation.save(o+"js/data.js");

        Parser.Copy("../vis/res/network_template.html", o+"network.html");
        Parser.Copy("../vis/res/nice.js", o+"js/nice.js");

        System.out.println("\nSuccessfully saved network visualization to  " + o + "network.html");


    }



}
