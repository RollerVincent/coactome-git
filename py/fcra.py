import sys
import zipfile
import glob
import os
import subprocess



mode = sys.argv[1]

if mode == '-analysis':
    if len(sys.argv) < 5:
        print('\nusage:\n        python3 fcra.py -analysis d mm bg')
        print('\n        d     data name')
        print('        mm    minimum common measurements')
        print('        bg    background distribution size\n')
    else:
        source_folder = '../../data/experiments/' + sys.argv[2]
        gene_list = source_folder + "/gene_list.txt"
        gene_fc = source_folder + "/gene_fc.txt"
        min_measurements = sys.argv[3]
        background_size = sys.argv[4]

        proc = subprocess.Popen(['java', '-jar', '../jar/FCRA.jar', '-o', source_folder, '-g', gene_list, '-fc', gene_fc, '-min', min_measurements, '-bg', background_size], stdout=subprocess.PIPE)
        for line in proc.stdout:
            print(line.rstrip().decode())

if mode == '-network':

    

    if len(sys.argv) < 8:
        print('\nusage:\n        python3 fcra.py -network d fdr mean varz rmc bdr')
        print('\n        d       data name')
        print('        fdr     maximal FDR')
        print('        mean    minimal FCR mean')
        print('        varz    maximal FCR variance Z score')
        print('        rmc     removal cost for clustering, default is 1')
        print('        bdr     include only bidirectional significant links [true|false]\n')
    else:
        fcra = '../../results/' + sys.argv[2] + '/fcra'
        fdr = sys.argv[3]
        mean = sys.argv[4]
        varz = sys.argv[5]
        rmc = sys.argv[6]
        bdr = sys.argv[7]

        print("\nExtracting network")

        proc = subprocess.Popen(['java', '-jar', '../jar/FcraNetwork.jar', '-fcra', fcra, '-fdr', fdr, '-mean', mean, '-varz', varz, '-rmc', rmc, '-bidirectional', bdr], stdout=subprocess.PIPE)
        for line in proc.stdout:
            print(line.rstrip().decode())

        
        print("\nInitializing clustering with TransClust\n")

        bidirectional = ""
        if bdr == 'true':
            bidirectional = "_bidirectional"

        i = fcra + "/networks/data/network_fdr_" + fdr + "_mean_" + mean + "_varz_" + varz + "_cost_" + rmc + bidirectional + "/matrix.txt"
        o = fcra + "/networks/data/network_fdr_" + fdr + "_mean_" + mean + "_varz_" + varz + "_cost_" + rmc + bidirectional + "/clusters.txt"

        proc = subprocess.Popen(['java', '-jar', '../jar/TransClust.jar', '-i', i,
                                                                          '-o', o], stdout=subprocess.PIPE)
        for line in proc.stdout:
            print(line.rstrip().decode())




        print("\nStarting visulization preprocessing")

        proc = subprocess.Popen(['java', '-jar', '../jar/ForceDirection.jar', 
                                                                             '-network', i,
                                                                             '-clustering', o,
                                                                             '-attributes', '../res/' + sys.argv[2] + '_go.txt.gz',
                                                                             '-go',
                                                                             '-fpp', '0.0001'
                                                                          ], stdout=subprocess.PIPE)
        for line in proc.stdout:
            print(line.rstrip().decode())

if mode == '-cluster':
    if len(sys.argv) < 3:
        print('\nusage:\n        python3 fcra.py -cluster i')
        print('\n        i       input matrix\n')
        
    else:
        i = sys.argv[2]
        oo = ''
        for s in i.split('/')[:-1]:
            oo += s + '/'
        o = oo + 'clusters.txt'

        print("\nInitializing clustering with TransClust")

        proc = subprocess.Popen(['java', '-jar', '../jar/TransClust.jar', '-i', i,
                                                                          '-o', o], stdout=subprocess.PIPE)
        for line in proc.stdout:
            print(line.rstrip().decode())

        
if mode == '-visualization':
    if len(sys.argv) < 5:
        print('\nusage:\n        python3 fcra.py -visualization d i f')
        print('\n        d       data name')
        print('        i       input matrix')
        print('        f       maximal force per particle\n')
        
    else:
        i = sys.argv[3]
        oo = ''
        for s in i.split('/')[:-1]:
            oo += s + '/'
        o = oo + 'clusters.txt'

        proc = subprocess.Popen(['java', '-jar', '../jar/ForceDirection.jar', 
                                                                             '-network', i,
                                                                             '-clustering', o,
                                                                             '-attributes', '../res/' + sys.argv[2] + '_go.txt.gz',
                                                                             '-go',
                                                                             '-fpp', sys.argv[4]
                                                                          ], stdout=subprocess.PIPE)
        for line in proc.stdout:
            print(line.rstrip().decode())