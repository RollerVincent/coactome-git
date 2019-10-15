import sys
import zipfile
import glob
import os
import subprocess

mode = sys.argv[1]
if mode == '-js':
    path = sys.argv[2]

    assays = []

    with open(path) as f:
        l = next(f)
        while l.startswith('#'):
                l = next(f)
        l = l.strip().split("\t")
        cut = 2
        if l[2] == "Design Element":
            cut = 3

        assays = l[cut:]
        print(assays)
        assayData = []
        for a in assays:
            assayData.append({})

        for l in f:
            l = l.strip().split("\t")
            for i,v in enumerate(l[cut:]):
                if v != "":
                    assayData[i].update({l[0]:v})
    

    with open(path[:-len(path.split('/')[-1])] + "experiment.js", "w") as w:
        w.write("experiment = {\n")
        for i,a in enumerate(assays[:-1]):
            w.write("\t\""+a+"\" : {\n")

            first = True
            for g in assayData[i]:
                if first:
                    w.write("\t\t\""+g+"\" : " + assayData[i][g])
                    first = False
                else:
                    w.write(",\n\t\t\""+g+"\" : " + assayData[i][g])
                

            w.write("\n\t},\n")

        w.write("\t\""+assays[-1]+"\" : {\n")
        first = True
        for g in assayData[-1]:
            if first:
                w.write("\t\t\""+g+"\" : " + assayData[-1][g])
                first = False
            else:
                w.write(",\n\t\t\""+g+"\" : " + assayData[-1][g])
        w.write("\n\t}\n};")

            
            
