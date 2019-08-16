package coactome;

import visualization.Visualization;

import java.util.HashMap;

public class Analysis {

    GeneList geneList;
    String outputDir;

    public void setGenes(String file){
        geneList = GeneList.fromFile(file);
    }

    public GeneList getGenes(){
        return geneList;
    }

    public void addValues(String file){
        geneList.loadValues(file); // vge format (value per gene per experiment)
    }

    public void setOutputDir(String dir) {
        outputDir = dir;
    }

    public Gene get(String id){
        return geneList.get(id);
    }

    public Gene get(int i){
        return geneList.get(i);
    }


    public void FCRA(String geneId, int minDataPoints, int backgroundSize, boolean verbose){

        long s = System.nanoTime();

        Gene gene = get(geneId);

        if(gene.numDataPoints >= minDataPoints) {

            geneList.setDistributions(gene, minDataPoints, "clamped_ratio");

            if(gene.distributions.size() > 0){
                gene.setPvalues(geneList.kolmogorovSmirnov(gene, minDataPoints, backgroundSize));

                HashMap<Gene, Double> zScores = geneList.varianceZscore(gene);

                if (verbose) {
                    Visualization.Summary(gene, zScores, minDataPoints, backgroundSize, outputDir + "/fcra/" + gene.id);
                }

                gene.BenjaminiHochbergCorrection();

                TSV tsv = new TSV(gene.distributions.keySet(), "gene");
                tsv.addColumn(gene.pvalues, "pValue");
                tsv.addColumn(gene.fdrs, "fdr");
                tsv.addColumn(gene.means, "mean");
                tsv.addColumn(gene.variances, "variance");
                tsv.addColumn(zScores, "varianceZscore");

                tsv.addComment("FCRA results for gene " + geneId);
                tsv.addComment("p value based on kolmogorov smirnov test on log fold change ratios with background size " + backgroundSize + ".");
                tsv.addComment("genes with less than "+minDataPoints+" common measurements are excluded otherwise "+minDataPoints+" experiments are selected at random.");

                //tsv.save(outputDir + "/fcra/" + gene.id + "/results.tsv");
                tsv.saveAsZip(outputDir + "/fcra/" + gene.id + "/results.tsv");

                if (verbose) {
                    System.out.println("completed in " + (System.nanoTime() - s) / 1000000000.0);
                }

            }

            gene.clear();

        }

    }



}
