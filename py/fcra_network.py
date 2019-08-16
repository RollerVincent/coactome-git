import sys
import zipfile
import glob
import os

path = sys.argv[1]

fdr_cutoff = float(sys.argv[2])
varZ_cutoff = float(sys.argv[3])
mean_cutoff = float(sys.argv[4])

results = glob.glob(path + '/fcra/*')

total = str(len(results))
count = 1

if not os.path.exists(path + '/fcra/networks'):
    os.mkdir(path + '/fcra/networks')
if not os.path.exists(path + '/fcra/networks/js'):
    os.mkdir(path + '/fcra/networks/js')

with open(path + '/fcra/networks/js/network_'+ str(fdr_cutoff) +'_'+ str(varZ_cutoff) +'_'+ str(mean_cutoff) +'.js', 'w') as w:

    w.write('graph_data = {"nodes" : [\n')
        
        
    
    
    
    
    act1 = False
    act2 = False


    for r in results:
        print('processing:  ' + str(count) + '/' + total, end = '\r')
        count += 1
        act2 = False
        gene = r.split('/')[-1]
        if gene.startswith('ENS'):
            if act1:
                w.write(',\n')
            w.write('{"id" : "' + gene + '", "links" : [')
            p = r + '/results.tsv.zip'
            with zipfile.ZipFile(p) as z:
                with z.open('results.tsv') as f:
                    l = next(f).decode()
                    while l.startswith('#'):
                        l = next(f).decode()
                    for l in f:
                        l = l.decode().strip().split('\t')

                        g = l[0]
                        fdr = l[2]
                        mean = l[3]
                        variancesZscore = l[5]

                        if float(fdr) <= fdr_cutoff and float(variancesZscore) <= varZ_cutoff and float(mean) >= mean_cutoff:
                            if act2:
                                w.write(', ')
                            w.write('"' + g + '"')
                            act2 = True
            w.write(']}')
            act1 = True
            
                            
			
    w.write(']};')
