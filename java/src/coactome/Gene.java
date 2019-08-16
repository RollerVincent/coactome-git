package coactome;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class Gene {

    public String id;

    public Float[] values;
    public int numDataPoints;

    public HashMap<Gene, List<Double>> distributions;
    public HashMap<Gene, Double> means;
    public HashMap<Gene, Double> variances;
    public HashMap<Gene, Double> pvalues;
    public HashMap<Gene, Double> fdrs;




    public Gene(String id){
        this.id = id;
    }

    public void updateParameter(){
        means = new HashMap<>();
        variances = new HashMap<>();
        for (Gene gene: distributions.keySet()) {
            List<Double> distr = distributions.get(gene);
            int size = distr.size();
            if(size > 1) {
                double mean = 0;
                for (double v : distr) {
                    mean += v;
                }
                mean /= distr.size();
                double variance = 0;
                for (double v : distr) {
                    variance += Math.pow(v - mean, 2);
                }
                variance /= (distr.size() - 1);
                means.put(gene, mean);
                variances.put(gene, variance);
            }
        }
    }

    public void BenjaminiHochbergCorrection(){
        HashMap<Gene, Double> tmp = new HashMap<>();
        fdrs = pvalues
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        double mfdr = Double.MAX_VALUE;
        int n = fdrs.size();
        double k=n;
        double fdrk;
        for(Gene gene: fdrs.keySet()){
            fdrk = fdrs.get(gene)*(n/k);
            if(fdrk<mfdr){
                mfdr=fdrk;
            }else{
                fdrk=mfdr;
            }
            tmp.put(gene,fdrk);
            k-=1;
        }
        fdrs = tmp;
    }

    public void setPvalues(HashMap<Gene, Double> pvalues){
        this.pvalues = pvalues;
    }

    public void combinePvalues(HashMap<Gene, Double> pvalues){
        for (Gene gene: pvalues.keySet()) {
            this.pvalues.put(gene, (this.pvalues.get(gene) + pvalues.get(gene)) / 2.0);
        }
    }

    public void clear(){
        distributions = null;
        means = null;
        variances = null;
        pvalues = null;
        fdrs = null;
    }

}
