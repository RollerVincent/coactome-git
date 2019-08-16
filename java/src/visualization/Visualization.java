package visualization;

import coactome.Gene;
import parser.Parser;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Visualization {

    public static void distributions(Gene gene, String description, boolean withPvalueOnly, double pvalueCutoff){
        String name = gene.id + "_d_" + description;
        List<Entry> entries = new ArrayList<>();
        for (Gene other: gene.distributions.keySet()) {

            if (!withPvalueOnly || gene.pvalues.get(other) != null) {
                Entry e = new Entry(other.id);
                e.setX(gene.means.get(other));
                e.setY(gene.variances.get(other));
                if (withPvalueOnly && gene.pvalues.get(other) < pvalueCutoff) {
                    e.setGroup("rgb(200,100,100)");
                } else {
                    e.setGroup("rgb(100,100,100)");
                }
                entries.add(e);
            }

        }
        saveJS(name, entries);
        //plotJS(name, new String[]{"plot.scatter(plot_data, 1, 'group', 0.3)"});
        plotJS(name, new String[]{"plot.histogram2D(plot_data, 100, 100)"});
        showPlot(name);
    }

    public static void distributions(Gene gene, String description){
        distributions(gene, description, false, 0);

    }

    public static void pvalues(Gene gene, String description){
        String name = gene.id + "_pvd_" + description;
        List<Double> pvalues = new ArrayList<>();
        for (double pv: gene.pvalues.values()) {
            pvalues.add(pv);
        }
        Histogram(pvalues, 20, name);
    }

    public static void variances(Gene gene, String description){
        String name = gene.id + "_vd_" + description;
        List<Double> variances = new ArrayList<>();
        for (double v: gene.variances.values()) {
            variances.add(v);
        }
        Histogram(variances, 100, name);
    }

    public static void means(Gene gene, String description){
        String name = gene.id + "_md_" + description;
        List<Double> means = new ArrayList<>();
        for (double m: gene.means.values()) {
            means.add(m);
        }
        Histogram(means, 100, name);
    }

    public static void Histogram(List<Double> values, int bins, String name){
        List<Entry> entries = new ArrayList<>();
        for (Double v: values) {
            Entry e = new Entry("null");
            e.setX(v);
            e.setY(0);
            e.setGroup("g");
            entries.add(e);
        }
        saveJS(name, entries);
        plotJS(name, new String[]{"plot.histogram(plot_data, " + bins + ")"});
        showPlot(name);
    }

    public static void Cumulative(List<Double> values, String name){
        Collections.sort(values);
        List<Entry> entries = new ArrayList<>();
        double x = 0;
        for (Double v: values) {
            Entry e = new Entry("null");
            e.setX(v);
            e.setY(x);
            x+=1;
            e.setGroup("g");
            entries.add(e);
        }
        saveJS(name, entries);
        plotJS(name, new String[]{"plot.scatter(plot_data, 2)"});
        showPlot(name);
    }

    public static void Scatter(List<Entry> entries, String name){
        saveJS(name, entries);
        plotJS(name, new String[]{"plot.scatter(plot_data, 2)"});
        showPlot(name);
    }




    public static void Summary(Gene gene, HashMap<Gene, Double> zscores, int minData, int background_size, String dir){


        List<Entry> entries = new ArrayList<>();
        for (Gene other: gene.distributions.keySet()) {

            if (gene.pvalues.get(other) != null) {
                Entry e = new Entry(other.id);
                e.setX(gene.means.get(other));
                e.setY(gene.variances.get(other));
                e.setGroup("" + gene.pvalues.get(other) + ";" + zscores.get(other));
                entries.add(e);
            }

        }
        saveJS(dir + "/js/data.js", entries, true);

        Parser.Copy("../vis/res/summary_template.html", dir + "/summary.html");
        Parser.Copy("../vis/res/nice.js", dir + "/js/nice.js");

        BufferedWriter bw = Parser.Writer(dir + "/js/meta.js");
        try{

            bw.write("var gene_id = '" + gene.id + "';\n");
            bw.write("var gene_partner_count = '" + entries.size() + "';\n");
            bw.write("var minimum_data_points = '" + minData + "';\n");
            bw.write("var kolmogorov_smirnov_background = '" + background_size + "';\n");

            bw.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    static void saveJS(String name, List<Entry> entries) {
        saveJS(name, entries, false);
    }

    static void saveJS(String name, List<Entry> entries, boolean raw){
        String w = null;
        if(!raw){
            name = "../vis/js/" + name + ".js";
        }
        BufferedWriter bw = Parser.Writer(name);
        try{
            bw.write("plot_data = [\n");
            for (Entry e: entries) {
                if(w != null){
                    bw.write(w + ",\n");
                }
                w = "\t{\"id\":\"" + e.id + "\", \"x\":" + e.x + ", \"y\":" + e.y + ", \"group\":\"" + e.group + "\"}";
            }
            bw.write(w + "\n];");
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void plotJS(String name, String[] content){

        String js = "../js/" + name + ".js";
        BufferedWriter bw = Parser.Writer("../vis/html/" + name + ".html");
        BufferedReader br = Parser.Reader("../vis/res/template.html");

        int mode = 0;
        try{
            String line;
            String[] split;
            while((line = br.readLine()) != null){
                split = line.split("°§°");
                if(split.length == 2){
                    if(mode == 0){
                        bw.write(split[0] + js + split[1] + '\n');
                        mode += 1;
                    }else{
                        bw.write(split[0]);
                        for (String c: content) {
                            bw.write(c+";\n        ");
                        }
                        bw.write(split[1] + '\n');
                    }
                }else{
                    bw.write(line + '\n');
                }
            }
            bw.close();
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void showPlot(String name){
        File htmlFile = new File("../vis/html/" + name + ".html");
        try {
            Desktop.getDesktop().browse(htmlFile.toURI());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
