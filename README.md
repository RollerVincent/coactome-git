# git-coactome


* Setup


create root directory e.g.  coactome-root  and clone the repository.
```
cd coactome-root
git clone https://github.com/RollerVincent/coactome-git.git
```

download experimental data and process statistics.
Thiscommand takes some time.
```
cd git-coactome/preprocessing
python3 data.py -pre mouse_differential.experiments.csv
```
instead of mouse_differential ,  human_differential can be used.

The data is saved to coactome-root/data/experiments.

Along the experimental data from ExpressionAtlas, the files gene_list.txt and gene_fc.txt can be found.

Java project located at coactome-root/git-coactome/java.


* Fold Change Ratio Analysis

The fold change ratio analysis (FCRA) detects statistical significance between two genes analysing the log fold change ratio distribution of the genes.

The output files contain several values for each comparision:

p value

fdr

variance 

variance z score



