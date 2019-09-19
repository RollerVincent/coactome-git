#git-coactome


##Setup


create root directory e.g.  coactome-root  and clone the repository.
```
cd coactome-root
git clone https://github.com/RollerVincent/coactome-git.git
```

download experimental data and process statistics. This command takes some time.
```
cd git-coactome/preprocessing
python3 data.py -pre mouse_differential.experiments.csv
```
In the entire analysis, mouse_differential can be replaced by human_differential.
The data is saved to coactome-root/data/experiments/mouse_differential
Along the downlowd of the experimental data from [ExpressionAtlas](https://www.ebi.ac.uk/gxa/home), the files gene_list.txt and gene_fc.txt are generated.

Java project located at coactome-root/git-coactome/java.


##Fold Change Ratio Analysis

The fold change ratio analysis (FCRA) detects statistical significance between two genes analysing the log fold change ratio distribution of the genes.

The output files contain several values for each comparision:
* P value (kolmogorov smirnov)
* FDR corrected p value
* Variance 
* Variance z score



