# coding: utf-8
import sys
f=open(sys.argv[1])
for a in f:
    if len(a.split())<8:
        print a,
        continue
    b=a.split()
    b[2]=b[1]
    b.remove(b[5])
    print ' '.join(b)
