import sys




if sys.argv[1] == "-exp":
    exp = sys.argv[2]
    cut = 2
    if exp.startswith('E-GEOD'):
        cut = 3
    des = sys.argv[3]
    organism = sys.argv[4]
    elements = []
    data = {}
    with open(exp) as f:
        d = next(f)
        elements = d.strip().split("\t")[cut:]

        for l in f:
            l = l.strip().split("\t")
            data.update({l[0]:[float(x) for x in l[cut:]]})

        
    
    print(len(data))

    mapping = {}
    with open("../res/ensembl_to_ncbi_"+organism+".txt") as f:
        next(f)
        for l in f:
            l = l.strip().split("\t")
            if len(l) > 1:
                mapping.update({l[0]:l[1]})

    design = {}
    factors = set()
    with open(des) as f:
        v = next(f).strip().split('\t')
        fi = None
        for i,x in enumerate(v):
            if x.startswith('Factor Value['):
                fi = i
        for l in f:
            l = l.split('\t')
            design.update({l[0]:l[fi]})
            factors.add(l[fi])
    print('Comparision :  ' + str(factors))
    print('Samples :  ' + str(len(design)))

    with open(exp.split('.')[0] + '_gf_formatted.txt', 'w') as w:

        mapped = 0
        w.write('label\t')
        for gene in data:
            if gene in mapping:
                w.write(mapping[gene] + '\t')
                mapped += 1
        w.write('\n')

        for i, e in enumerate(elements):
            w.write(design[e] + '\t')
            for gene in data:
                if gene in mapping:
                    w.write(str(data[gene][i]) + '\t')
            w.write('\n')


    print('Mapped :  ' + str(mapped))


    






else:
    net = sys.argv[1]

    organism = sys.argv[2]

    linkValue = net.split('_')[-1].split('/')[0]

    mapping = {}
    with open("../res/ensembl_to_ncbi_"+organism+".txt") as f:
        next(f)
        for l in f:
            l = l.strip().split("\t")
            if len(l) > 1:
                mapping.update({l[0]:l[1]})

    print()

    print('Mapping entries :  ' + str(len(mapping)))


    nodes = []
    links = []

    with open(net) as f:
        n = int(next(f))
        print('Total nodes :  ' + str(n))

        for i in range(n):
            id = next(f).strip()
            nodes.append(id)

        for i in range(n):
            l =  next(f).split('\t')
            for j in range(len(l)):
                if l[j] == linkValue:
                    links.append([i, j])

    print('Total links :  ' + str(len(links)))    

    mapped_nodes= []
    for n in nodes:
        if n in mapping:
            mapped_nodes.append(mapping[n])
    print('Mapped nodes :  ' + str(len(mapped_nodes)))
    print('Node loss :  ' + str(len(nodes) - len(mapped_nodes)))


    with open(net[:-4] + '_grandforest_format.txt', 'w') as w:
        for link in links:
            a = nodes[link[0]]
            b = nodes[link[1]]
            if a in mapping and b in mapping:
                w.write(mapping[a] + ',' + mapping[b] + '\n')


    print()

