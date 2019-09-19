package main;

import parser.Parser;

public class FCRA {
    public static void main(String[] args) {

        Parser.ArgParser argParser = Parser.ArgParser(args);
        argParser.addOption("-o", true, false, "Output directory");
        argParser.addOption("-g", true, false, "Path to gene_list.txt");
        argParser.addOption("-fc", true, false, "Path to gene_fc.txt");
        argParser.addOption("-min", true, false, "Minimum of common measurments");
        argParser.addOption("-bg", true, false, "Background size (kolmogorov smirnov)");


        if(argParser.Compile()) {
            Main.foldChangeRatioAnalysis(   argParser.getArgument("-o"),
                                            argParser.getArgument("-g"),
                                            argParser.getArgument("-fc"),
                                            Integer.valueOf(argParser.getArgument("-min")),
                                            Integer.valueOf(argParser.getArgument("-bg")));
        }
    }
}
