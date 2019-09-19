import sys
import os
import time
import datetime
import shutil


path = sys.argv[1]

with_cluster = False
clusters = {}
if len(sys.argv) > 2:
	cluster_path = sys.argv[2]
	with_cluster = True

	with open(cluster_path) as f:
		for l in f:
			l = l.split("\t")
			clusters.update({l[0]:int(l[1])})



s = path.split("/")
tmp = ""
for d in s[:-1]:
	tmp += d + "/"

tmp = path[:-4] + "_vis"

if not os.path.exists(tmp):
	os.mkdir(tmp)

with open(path) as file:
	links = 0
	nodes = []
	size = int(next(file).strip())
	for i in range(size):
		nodes.append([next(file).strip(), []])
	for i in range(size - 1):
		l = next(file).strip()
		s = l.split("\t")
		for jj in range(len(s)):
			j = i + 1 + jj
			if int(s[jj]) > 0:
				nodes[i][1].append(nodes[j][0])
				links += 1
	
	if not os.path.exists(tmp + '/js'):
			os.mkdir(tmp + '/js')

	indices = {}
	ind = 0

	with open(tmp + "/js/data.js", "w") as w:
		w.write('data = {"nodes" : [\n')

		for n in nodes[:-1]:
			c = -1
			if with_cluster:
				c = clusters[n[0]]


			w.write('{"id" : "' + n[0] + '", "cluster" : ' + str(c) + '},\n')
			indices.update({n[0]:ind})
			ind += 1

	#		if len(n[1]) > 0:
	#			for l in n[1][:-1]:
	#				w.write("\"" + l + "\",")
	##			w.write("\"" + n[1][-1] + "\"")
	#		w.write("]},\n")

		c = -1
		if with_cluster:
			c = clusters[nodes[-1][0]]

		w.write('{"id" : "' + nodes[-1][0] + '", "cluster" : ' + str(c) + '}\n],\n')
		indices.update({nodes[-1][0]:ind})

		w.write('"links" : [\n')

		links = []
		for n in nodes:
			i = indices[n[0]]
			for l in n[1]:
				j = indices[l]
				links.append([i,j])

		for l in links[:-1]:
			w.write('{"src" : '+ str(l[0]) +', "dst" : '+ str(l[1]) +'},\n')

		if len(links) > 0:
			w.write('{"src" : '+ str(links[-1][0]) +', "dst" : '+ str(links[-1][1]) +'}\n')


		w.write(']\n};')

	

	shutil.copy2('../res/nice.js', tmp + '/js/nice.js')
	shutil.copy2('../res/network_template.html', tmp + '/network.html')







