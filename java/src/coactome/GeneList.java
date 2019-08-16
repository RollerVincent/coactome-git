package coactome;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import parser.Parser;
import visualization.Entry;
import visualization.Visualization;

import java.io.BufferedReader;
import java.util.*;

public class GeneList {

    public int length;

    HashMap<String, Gene> map;
    List<Gene> genes;

    public static GeneList fromFile(String file){
        GeneList gl = new GeneList();
        gl.load(file);
        return gl;
    }


    void load(String file){
        length = 0;
        map = new HashMap<>();
        genes = new ArrayList<>();
        BufferedReader br = Parser.Reader(file);
        try {
            String line;
            while ((line = br.readLine()) != null) {
                Gene g =  new Gene(line);
                map.put(line, g);
                genes.add(g);
                length += 1;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public Gene get(String id){
        return map.get(id);
    }


    public Gene get(int index) {
        return genes.get(index);
    }


    public void loadValues(String file){
        BufferedReader br = Parser.Reader(file);
        try {
            String line;
            String[] split;
            Float[] v;
            int v_count;
            while ((line = br.readLine()) != null) {
                split = line.split("\t");
                v = new Float[split.length - 1];
                v_count = 0;
                for (int i = 0; i < v.length; i++) {
                    if(split[i+1].equals("NN")){
                        v[i] = null;
                    }else{
                        v[i] = Float.valueOf(split[i+1]);

                        v_count += 1;
                    }
                }
                get(split[0]).values = v;
                get(split[0]).numDataPoints = v_count;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void setDistributions(Gene target, int minDataPoints, String mode) {
        target.distributions = new HashMap<>();
        if(target.numDataPoints >= minDataPoints) {
            Gene gene;
            List<Double> distribution;
            for (int i = 0; i < length; i++) {
                gene = get(i);
                if (!gene.id.equals(target.id)) {
                    if(gene.numDataPoints >= minDataPoints) {
                        distribution = new ArrayList<>();
                        for (int j = 0; j < target.values.length; j++) {
                            if (target.values[j] != null && gene.values[j] != null) {






                                if (mode.equals("clamped_ratio")) {

                                    if (target.values[j] * gene.values[j] < 0) {
                                        distribution.add(Math.max(1.0 * target.values[j] / gene.values[j], 1.0 * gene.values[j] / target.values[j]));
                                    } else {
                                        distribution.add(Math.min(1.0 * target.values[j] / gene.values[j], 1.0 * gene.values[j] / target.values[j]));
                                    }

                                }

                                else if (mode.equals("ratio")) {

                                    distribution.add(1.0 * target.values[j] / gene.values[j]);


                                }




                            }
                        }
                        if (distribution.size() >= minDataPoints) {
                            Collections.shuffle(distribution);
                            target.distributions.put(gene, distribution);
                        }
                    }
                }
            }
        }
        target.updateParameter();
    }


    public double[] backgroundDistribution(Gene target, int size){
        double[] out = new double[size];
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            int g = r.nextInt(length);
            List<Double> rts = target.distributions.get(get(g));
            if(rts != null && rts.size() > 0){
                int e = r.nextInt(rts.size());
                out[i] = rts.get(e);
            }else{
                i -= 1;
            }
        }
        return out;
    }


    public HashMap<Gene, Double> kolmogorovSmirnov(Gene target, int dataPoints, int backgroundSize){
        HashMap<Gene, Double> out = new HashMap<>();

        double[] bg = backgroundDistribution(target, backgroundSize);
        double[] gd = new double[dataPoints];

        Gene gene;
        List<Double> tmpdistr;

        for (int i = 0; i < length; i++) {
            gene = get(i);
            if(!gene.id.equals(target.id)) {
                tmpdistr = target.distributions.get(gene);
                if (tmpdistr != null && tmpdistr.size() >= dataPoints) {
                    for (int j = 0; j < dataPoints; j++) {
                        gd[j] = tmpdistr.get(j);
                    }
                    KolmogorovSmirnovTest ks = new KolmogorovSmirnovTest();
                    out.put(gene, ks.kolmogorovSmirnovTest(gd, bg));
                }
            }
        }
        return out;
    }


    public HashMap<Gene, Double> varianceTest(Gene target){
        HashMap<Gene, Double> out = new HashMap<>();

        Collection<Double> variances = target.variances.values();
        int size = variances.size();


        double mean = 0;
        for (double v: variances) {
            mean += v;
        }
        mean = mean / size;

        double sd = 0;
        for (double v: variances) {
            sd += Math.pow(v - mean, 2);
        }
        sd = Math.sqrt(sd / size);

        NormalDistribution variance_distribution = new NormalDistribution(mean, sd);

        Gene gene;
        Double v;
        for (int i = 0; i < length; i++) {
            gene = get(i);
            if(!gene.id.equals(target.id)) {
                v = target.variances.get(gene);
                if(v != null) {
                    out.put(gene, variance_distribution.cumulativeProbability(v));
                }
            }
        }
        return out;
    }

    public List<Gene> all(){
        return genes;
    }


    public HashMap<Gene, Double> varianceZscore(Gene target){
        HashMap<Gene, Double> out = new HashMap<>();

        Collection<Double> variances = target.variances.values();
        int size = variances.size();


        double mean = 0;
        for (double v: variances) {
            mean += v;
        }
        mean = mean / size;

        double sd = 0;
        for (double v: variances) {
            sd += Math.pow(v - mean, 2);
        }
        sd = Math.sqrt(sd / size);


        Gene gene;
        Double v;
        for (int i = 0; i < length; i++) {
            gene = get(i);
            if(!gene.id.equals(target.id)) {
                v = target.variances.get(gene);
                if(v != null) {
                    out.put(gene, (v - mean) / sd);
                }
            }
        }
        return out;
    }
}
