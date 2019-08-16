# git-coactome


* Setup


create root directory e.g.  coactome-root  and clone the repository
```
cd coactome-root
git clone https://github.com/RollerVincent/coactome-git.git
```

download experimental data and process statistics
```
cd git-coactome/preprocessing
python3 data.py -pre mouse_differential.experiments.csv
```

The last command takes several minutes.
The data is saved to coactome-root/data/experiments

Java project located at coactome-root/git-coactome/java