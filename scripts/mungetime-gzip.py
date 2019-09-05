import numpy as np
import sys
import gzip

from collections import defaultdict
buckets=defaultdict(list)
with gzip.open(sys.argv[1],'rt') as f:
    for line in f:
        if line.startswith('[broker]'):
            fields=line.strip().split(" ")
            arrival_time = float(fields[1])*1000 #sec to ms
            complt_time = float(fields[2]) #ms
            arrival_bucket=(arrival_time + complt_time)//1000 #to seconds
            buckets[arrival_bucket].append(complt_time)

w=[]
for i in range(0, len(buckets)):
    w.clear()
    for j in range(i-min(i, 30), i): #30 sec moving windows
        if j in buckets:
            w += buckets[j]
    print(0 if not w else np.percentile(w, 95))
