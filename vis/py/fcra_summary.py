import sys
import os
import shutil
import zipfile



path = sys.argv[1]

genes = []
pvalues = []
fdrs = []
means = []
variances = []
variancesZscore = []

with zipfile.ZipFile(path) as z:
	with z.open(path.split('/')[-1][:-4]) as f:
		l = next(f).decode()
		while l.startswith('#'):
			l = next(f).decode()
		for l in f:
			l = l.decode().strip().split('\t')
			genes.append(l[0])
			pvalues.append(l[1])
			fdrs.append(l[2])
			means.append(l[3])
			variances.append(l[4])
			variancesZscore.append(l[5])


	

js = ''
for s in path.split('/')[:-2]:
	js += s + '/'
js += 'summary'

if not os.path.exists(js):
	os.mkdir(js)

js += '/js'

if not os.path.exists(js):
	os.mkdir(js)

js += '/'
with open(js + path.split('/')[-2] + '_data.js', 'w') as w:
	w.write('fcra = {\n')

	w.write('"id":"' + path.split('/')[-2] + '",\n')
	
	w.write('"genes":[\n')
	for x in genes[:-1]:
		w.write('"' + x + '"' + ', ')
	w.write('"' + genes[-1] + '"')
	w.write('],\n')

	w.write('"pvalues":[\n')
	for x in pvalues[:-1]:
		w.write(x + ', ')
	w.write(pvalues[-1])
	w.write('],\n')

	w.write('"fdrs":[\n')
	for x in fdrs[:-1]:
		w.write(x + ', ')
	w.write(fdrs[-1])
	w.write('],\n')

	w.write('"means":[\n')
	for x in means[:-1]:
		w.write(x + ', ')
	w.write(means[-1])
	w.write('],\n')

	w.write('"variances":[\n')
	for x in variances[:-1]:
		w.write(x + ', ')
	w.write(variances[-1])
	w.write('],\n')

	w.write('"variancesZscore":[\n')
	for x in variancesZscore[:-1]:
		w.write(x + ', ')
	w.write(variancesZscore[-1])
	w.write(']\n')
	
	w.write('};\n')


#shutil.copy2('../res/nice.js', js + 'nice.js')

with open('../res/fcra_summary_template.html') as t:
	with open(js[:-3] + path.split('/')[-2] + '.html', 'w') as w:
		for l in t:
			l = l.split('*+*')
			if len(l) == 1:
				w.write(l[0])
			else:
				w.write(l[0] + path.split('/')[-2] + l[1])





#shutil.copy2('../res/fcra_summary_template.html', js[:-3] + path.split('/')[-2] + '.html')
