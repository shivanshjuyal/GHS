import random, sys

numOfNodes = int(sys.argv[1])
edges = []
wtEdges = set()

 

for i in range(numOfNodes-1):
    wt = -1
    while True:
        wt = random.randint(1, 10000000)
        if wt not in wtEdges:
            wtEdges.add(wt)
            break
            
    edges.append([wt, i, i+1])

 

for i in range(numOfNodes):
    for j in range(i+2, numOfNodes):
        if random.randint(1,10) <= 8:
            wt = -1
            while True:
                wt = random.randint(1, 10000000)
                if wt not in wtEdges:
                    wtEdges.add(wt)
                    break
            edges.append([wt, i, j])

 

print(edges)

f = open("testData.txt", "w")
f.write(str(numOfNodes) + "\n")
for i in range(len(edges)):
    if i == len(edges) - 1:
        f.write("(" + str(edges[i][1]) + ", " + str(edges[i][2]) +  ", " +str(edges[i][0]) + ")")
    else:
        f.write("(" + str(edges[i][1]) + ", " + str(edges[i][2]) +  ", " +str(edges[i][0]) + ")\n")
