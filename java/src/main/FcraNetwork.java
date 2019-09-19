package main;

import parser.Parser;

public class FcraNetwork {
    public static void main(String[] args) {

        Parser.ArgParser argParser = Parser.ArgParser(args);
        argParser.addOption("-fcra", true, false, "FCRA result directory");
        argParser.addOption("-fdr", true, false, "maximal FDR");
        argParser.addOption("-mean", true, false, "minimal FCR mean");
        argParser.addOption("-varz", true, false, "maximal FCR variance Z score");
        argParser.addOption("-rmc", true, true, "removal cost for clustering");


        // FCRA_Network("../../results/mouse_differential/fcra", 1e-13, 0.25, -3, 50);

        if(argParser.Compile()) {
            int removalCost = 1;
            if(argParser.hasArgument("-rmc")){
                removalCost = Integer.valueOf(argParser.getArgument("-rmc"));
            }
            Main.FCRA_Network(argParser.getArgument("-fcra"), Double.valueOf(argParser.getArgument("-fdr")), Double.valueOf(argParser.getArgument("-mean")), Double.valueOf(argParser.getArgument("-varz")), removalCost);
        }
    }
}
