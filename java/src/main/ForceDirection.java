package main;

import parser.Parser;

public class ForceDirection {

    public static void main(String[] args) {


        Parser.ArgParser argParser = Parser.ArgParser(args);
        argParser.addOption("-network", true, false, "Network file path");
        argParser.addOption("-clustering", true, true, "Clustering file path");
        argParser.addOption("-attributes", true, true, "Attributes file path");
        argParser.addOption("-go", false, true, "enables GO mode (gene ontology)");
        argParser.addOption("-fpp", true, false, "FPP cutoff (force per particle)");

        if(argParser.Compile()){
            /*Main.ForceDirection("../../results/mouse_differential/fcra/networks/data/network_fdr_1.0E-13_mean_0.25_varz_-3.0_cost_50/matrix.txt",
                    "../../results/mouse_differential/fcra/networks/data/network_fdr_1.0E-13_mean_0.25_varz_-3.0_cost_50/clusters.txt",
                    "../res/mouse_go.txt.gz",
                    "go",
                    0.0001);*/



            String mode = "";
            if(argParser.hasArgument("-go")){
                mode = "go";
            }
            String clustering = "";
            if(argParser.hasArgument("-clustering")){
                clustering = argParser.getArgument("-clustering");
            }
            String attributes = "";
            if(argParser.hasArgument("-attributes")){
                attributes = argParser.getArgument("-attributes");
            }

            Main.ForceDirection(argParser.getArgument("-network"), clustering, attributes, mode, Double.valueOf(argParser.getArgument("-fpp")));
        }






    }


}
