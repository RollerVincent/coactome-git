import sys
import os
import requests
import glob

def download(file):
	if not os.path.exists('../../data/experiments'): os.makedirs('../../data/experiments')
	with open(file) as f:
		experiments = next(f).strip().split(',')
		print("Downloading "+ str(len(experiments)) +" experiments :")
		count = 0
		if not os.path.exists('../../data/experiments/' + file.split('.')[0]): os.makedirs('../../data/experiments/' + file.split('.')[0])
		for e in experiments:
			if not os.path.exists('../../data/experiments/' + file.split('.')[0] + '/' + e): os.makedirs('../../data/experiments/' + file.split('.')[0] + '/' + e)
			r = requests.get('https://www.ebi.ac.uk/gxa/experiments-content/' + e + '/resources/ExperimentDownloadSupplier.Microarray/query-results')
			if not r.content.startswith(b'# Expression'):
				r = requests.get('https://www.ebi.ac.uk/gxa/experiments-content/' + e + '/resources/ExperimentDownloadSupplier.RnaSeqDifferential/tsv')
			with open('../../data/experiments/' + file.split('.')[0] + '/' + e + '/results.tsv', 'wb') as w:
				w.write(r.content)
			count += 1
			print(str(count) + ' / ' + str(len(experiments)) + '\r', end = '')

def saveGeneList(source):
    
    print("extracting gene list...")
    
    genes = set()
    files = glob.glob('../../data/experiments/' + source + '/*/*.tsv')
    for file in files:
        with open(file) as f:
            try:
                l = next(f).strip()
                while l.startswith('#'):
                    l = next(f).strip()
                for l in f:
                    gene = l.split('\t')[0]
                    if not gene.startswith("E"):
                        print("skipping experiment " + file.split('/')[-2] + " probably the results are compressed (.zip)")
                        os.remove(file)
                        os.rmdir('../../data/experiments/' + source + '/' + file.split('/')[-2])
                        break
                    genes.add(gene)
            except Exception as e:
                print(e)
                print("Error in experiment :  " + file.split('/')[-2] +'  DELETING...')
                os.remove(file)
                os.rmdir('../../data/experiments/' + source + '/' + file.split('/')[-2])
    with open('../../data/experiments/' + source + '/gene_list.txt', 'w') as w:
        for g in genes:
            w.write(g + '\n')

    print("done.\n")

def saveFoldChanges(source):
    
    print("extracting fold changes...")
    
    genes = {}
    with open('../../data/experiments/' + source + '/gene_list.txt') as f:
        for l in f:
            genes.update({l.strip() : []})

    files = glob.glob('../../data/experiments/' + source + '/*/*.tsv')

    index = 0
    for file in files:
        with open(file) as f:
            l = next(f).strip()
            while l.startswith('#'):
                l = next(f).strip()
            fc_indices = []
            for i, s in enumerate(l.split('\t')):
                if s.split('.')[-1] == 'foldChange':
                    fc_indices.append(i)
            nn = ['NN' for i in range(len(fc_indices))]
            for gene in genes:
                genes[gene] = genes[gene] + nn

            for l in f:
                l = l.strip().split('\t')
                if l[0] not in genes:
                    genes.update({l[0], []})
                for i, fci in enumerate(fc_indices):
                    if len(l) >= fci + 1:
                        genes[l[0]][index + i] = l[fci] if l[fci] != '' else 'NN'

            index += len(nn)

    with open('../../data/experiments/' + source + '/gene_fc.txt', 'w') as w:
        for g in genes:
            o = ''
            for fc in genes[g]: o += fc + '\t'
            w.write(g + '\t' + o + '\n')

    print("done.\n")

mode = sys.argv[1]

if mode == '-d': download(sys.argv[2])

elif mode == '-fc': saveFoldChanges(sys.argv[2])

elif mode == '-g': saveGeneList(sys.argv[2])

elif mode == '-pre':
    download(sys.argv[2])
    print("\n")
    saveGeneList(sys.argv[2].split('.')[0])
    saveFoldChanges(sys.argv[2].split('.')[0])


