#!/usr/bin/python

# Numpy is a library for handling arrays (like data points)
import numpy as np
import sys
import argparse

# Pyplot is a module within the matplotlib library for plotting
import matplotlib.pyplot as plt


parser = argparse.ArgumentParser(description='Plot graph distribution.')
#parser.add_argument('--sumf', dest='accumulate', action='store_const', const=sum, default=max, help='sum the integers (default: find the max)')
#parser.add_argument('integers', metavar='Ng', type=int, nargs='+',help='an integer for the accumulator')
parser.add_argument('--cuthead', metavar='Nth', type=int, nargs='?', const=1, default=1, help='cut first Nth rows default 1 [const 1]')
parser.add_argument('--cuttail', metavar="Nth", type=int, nargs='?', const=1, default=1, help='cut last  Nth rows default 1 [const 1]')
parser.add_argument('-d', metavar='delim',default=':',help='delimiter default \':\'')
parser.add_argument('-f', metavar='field_no',type=int,default=1,help='filed no start from 1 default 1')
parser.add_argument('-s', metavar='Style', default='b.', help='Style default \'b.\'')
parser.add_argument('-o', metavar='IMG_File', default='dist.png', help='Output png file')
parser.add_argument('-t', metavar='TITLE',default='',help='Title')
parser.add_argument('-xl', metavar='x-label',help='x label')
parser.add_argument('-yl', metavar='y-label',help='y label')
parser.add_argument('-q', metavar='divisor',default=1, type=float, help='Quantization in int (quotient = dividend / divisor) 1 is default')
parser.add_argument('-p', metavar='FP_Nth',default=0,type=int, help='Floating Point FP_Nth (FP_Nth may 0,1,2,...) 0 is default')
parser.add_argument('DataFile',help='Input CSV File')
args = parser.parse_args()

#print vars(args)
title = args.t
ylabel = args.yl
xlabel = args.xl

filename = args.DataFile
sep = args.d
imgname = args.o
fNo = args.f - 1
style = args.s

#Open file
f = open(filename,"r")

#Read all content
data = f.read()
f.close()

data = data.split("\n")
#Remove the empty line
print "cutHead " + str(args.cuthead)
print "cutTail " + str(args.cuttail)
while args.cuttail > 0:
	del(data[-1])
	args.cuttail = args.cuttail - 1
while args.cuthead > 0:
	del(data[0])
	args.cuthead = args.cuthead - 1

#gatering inlink info
inl = [int(round(int(i.split(sep)[fNo])/args.q,0)) for i in data]
#inl = [int(round(i/1024.0,0)) for i in inl] #round file size

#new dic
dic = {}

for i in inl:
	if dic.has_key(i):
		dic[i] += 1
	else:
		dic[i] = 1

x = [i for i in dic]
y = [dic[i] for i in dic]

#plot inlink

plt.grid(True)
plt.xscale('log')
plt.yscale('log')

#plt.title(r'$\int_0^{\infty} t^{x-1} e^{-t} dt$', fontsize=18)
#plt.xlabel(r'$\alpha \sim \Gamma \leftarrow (M_{\odot})$',fontsize=17)
#plt.ylabel(r'Text in Computer Modern font', fontsize=17)

plt.title(title, fontsize=18)
plt.xlabel(xlabel,fontsize=14)
plt.ylabel(ylabel, fontsize=14)

# Create the plot
#plt.plot(x,y)
#plt.scatter(xinl,yinl)
plt.plot(x,y,style)


# Save the figure in a separate file
plt.savefig(imgname)
plt.savefig(imgname, format='png', dpi=600)
plt.savefig(imgname+".svg", format='svg', dpi=300)

# Draw the plot to the screen
plt.show()
