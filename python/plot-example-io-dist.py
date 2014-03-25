#!/usr/bin/python

# Numpy is a library for handling arrays (like data points)
import numpy as np
import sys

# Pyplot is a module within the matplotlib library for plotting
import matplotlib.pyplot as plt

filename=sys.argv[1] if len(sys.argv) > 1 else '../../scc.webpage.io'
sep=sys.argv[2] if len(sys.argv) > 2 else ':'
imgpre=sys.argv[3] if len(sys.argv) > 3 else '../../graphdist'
imgin=imgpre + "-in.png"
imgout=imgpre + "-out.png"

#Open file
f = open(filename,"r")

#Read all content
data = f.read()
f.close()

data = data.split("\n")
#Remove the empty line
#del(data[-1])

#gatering inlink info
inl = []
outl = []
for l in data:
	seg = l.split(sep)
	if len(seg) >= 3:
		inl.append(int(seg[1]))
		outl.append(int(seg[2]))
#inl = [int(round(i/1024.0,0)) for i in inl] #round file size

#new dic
dic = {}

for i in inl:
	if dic.has_key(i):
		dic[i] += 1
	else:
		dic[i] = 1

xinl = [i for i in dic]
yinl = [dic[i] for i in dic]

#plot inlink

plt.grid(True)
plt.xscale('log')
plt.yscale('log')

#plt.title(r'$\int_0^{\infty} t^{x-1} e^{-t} dt$', fontsize=18)
#plt.xlabel(r'$\alpha \sim \Gamma \leftarrow (M_{\odot})$',fontsize=17)
#plt.ylabel(r'Text in Computer Modern font', fontsize=17)

plt.title(r'In link distribution', fontsize=18)
plt.xlabel(r'In Degree',fontsize=14)
plt.ylabel(r'Number of page', fontsize=14)

# Create the plot
#plt.plot(x,y)
#plt.scatter(xinl,yinl)
plt.plot(xinl,yinl,'r.')


# Save the figure in a separate file
plt.savefig(imgin)

# Draw the plot to the screen
plt.show()



#new dic
dic = {}

for i in outl:
	if dic.has_key(i):
		dic[i] += 1
	else:
		dic[i] = 1

xoutl = [i for i in dic]
youtl = [dic[i] for i in dic]

#plot outlink

plt.grid(True)
plt.xscale('log',basex=10)
plt.yscale('log',basey=10)

#plt.title(r'$\int_0^{\infty} t^{x-1} e^{-t} dt$', fontsize=18)
#plt.xlabel(r'$\alpha \sim \Gamma \leftarrow (M_{\odot})$',fontsize=17)
#plt.ylabel(r'Text in Computer Modern font', fontsize=17)

plt.title(r'Out link distribution', fontsize=18)
plt.xlabel(r'Out Degree',fontsize=14)
plt.ylabel(r'Number of page', fontsize=14)

# Create the plot
#plt.plot(x,y)
#plt.scatter(xinl,yinl)
plt.plot(xoutl,youtl,'b.')


# Save the figure in a separate file
plt.savefig(imgout)

# Draw the plot to the screen
plt.show()



