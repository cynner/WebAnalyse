#!/usr/bin/python

# Numpy is a library for handling arrays (like data points)
import sys
import argparse

# Pyplot is a module within the matplotlib library for plotting

from numpy import *
from pylab import *
#from matplotlib import rc, rcParams
import matplotlib.pyplot as plt


parser = argparse.ArgumentParser(description='Plot graph distribution.')
#parser.add_argument('--sumf', dest='accumulate', action='store_const', const=sum, default=max, help='sum the integers (default: find the max)')
#parser.add_argument('integers', metavar='Ng', type=int, nargs='+',help='an integer for the accumulator')
parser.add_argument('-d', metavar='delim',default=':',help='delimiter default \':\'')
parser.add_argument('-f', metavar='field_no',type=int,default=3,help='filed no start from 1 default 2')
parser.add_argument('-s', metavar='Style', default='b.', help='Style default \'b.\'')
parser.add_argument('-o', metavar='IMG_File', default='graph/indist.png', help='Output png file')
parser.add_argument('-t', metavar='TITLE',default='In-Degree Distribution',help='Title')
parser.add_argument('-xl', metavar='x-label',default='In-Degree',help='x label')
parser.add_argument('-yl', metavar='y-label',default='Number of pages',help='y label')
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
del(data[-1])

#gatering inlink info
inl = [int(i.split(sep)[fNo]) for i in data]
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

#plt.title(r'$\int_0^{\infty} t^{x-1} e^{-t} dt$', fontsize=18)
#plt.xlabel(r'$\alpha \sim \Gamma \leftarrow (M_{\odot})$',fontsize=17)
#plt.ylabel(r'Text in Computer Modern font', fontsize=17)
plt.grid(True)
plt.xscale('log')
plt.yscale('log')
plt.title(title, fontsize=24)
plt.xlabel(xlabel,fontsize=22)
plt.ylabel(ylabel, fontsize=22)
plt.tick_params(axis='both', which='major', labelsize=20)
plt.tick_params(axis='both', which='minor', labelsize=20)
plt.tight_layout()

# Create the plot
#plt.plot(x,y)
#plt.scatter(xinl,yinl)
plt.plot(x,y,style)

# Save the figure in a separate file
plt.savefig(imgname, format='png', dpi=300)
plt.savefig(imgname+".pdf", format='pdf', dpi=300)

# Draw the plot to the screen
plt.show()
