#!/usr/bin/python

# Numpy is a library for handling arrays (like data points)
import numpy as np
import sys
import argparse
import codecs
from operator import itemgetter

parser = argparse.ArgumentParser(description='Plot graph distribution.')
#parser.add_argument('--sumf', dest='accumulate', action='store_const', const=sum, default=max, help='sum the integers (default: find the max)')
#parser.add_argument('integers', metavar='Ng', type=int, nargs='+',help='an integer for the accumulator')
parser.add_argument('-ch','--cuthead', metavar='Nth', type=int, nargs='?', const=1, default=1, help='cut first Nth rows default 1 [const 1]')
parser.add_argument('-ct','--cuttail', metavar="Nth", type=int, nargs='?', const=1, default=1, help='cut last  Nth rows default 1 [const 1]')
parser.add_argument('--nodisplay', action='store_true', help='no gui')
parser.add_argument('-d', metavar='delim', default=':', help='delimiter default \':\'')
parser.add_argument('-f', metavar='field_no',type=int,default=1,help='filed no start from 1 default 1')
parser.add_argument('-s', metavar='Style', default='b.', help='Style default \'b.\'')
parser.add_argument('-t', metavar='TITLE',default='',help='Title')
parser.add_argument('-xl', metavar='x-label',help='x label')
parser.add_argument('-yl', metavar='y-label',help='y label')
parser.add_argument('-q', metavar='divisor', default=1, type=float, help='Quantization in int (quotient = dividend / divisor) 1 is default')
#parser.add_argument('-p', metavar='FP_Nth', default=0, type=int, help='Floating Point FP_Nth (FP_Nth may 0,1,2,...) 0 is default')
parser.add_argument('-m', '--map', dest='map', metavar='map_file', help='Mapping file')
parser.add_argument('-o', metavar='img_file', help='Output png file', required=True)
parser.add_argument('--skipcount', action='store_true', help='Read result and show')
parser.add_argument('DataFile',help='Input CSV File')
args = parser.parse_args()


# Pyplot is a module within the matplotlib library for plotting
import matplotlib as mpl
if args.nodisplay:
	mpl.use('Agg')
import matplotlib.pyplot as plt
mpl.rcParams['font.size'] = 20.0

#REMOVE BACKGROUND
frame = plt.getp(plt.gca(), 'frame')
frame.set_visible(False)

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
f = codecs.open(filename,"r",encoding='utf-8')

#Read all content
data = f.read()
f.close()

data = data.split("\n")

if not args.skipcount:
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
	data = [i.split(sep)[fNo] for i in data]
	#inl = [int(round(i/1024.0,0)) for i in inl] #round file size

	#new dic
	dic = {}

	for i in data:
		if dic.has_key(i):
			dic[i] += 1
		else:
			dic[i] = 1

	if args.map != None :
		#Open file
		f = codecs.open(args.map, "r", encoding='utf-8')
		mdata = f.read()
		f.close()
		mdata = mdata.split("\n")
		for i in mdata:
			strs = i.split(sep)
			if len(strs) > 1:
				dic[strs[1]] = dic.pop(strs[0])
				#dic[strs[1]] = dic[strs[0]]
				#del dic[strs[0]]
else:
	dic = {}
	for i in data:
		strs = i.split(sep)
		if len(strs) == 2:
			dic[strs[0]] = float(strs[1])

sdic = sorted(dic.items(), key=itemgetter(1), reverse=True)

#Crossing
cdic = []
if len(sdic) % 2 == 1:
	cdic += [sdic[0]]
	del sdic[0]

l=len(sdic)
for i in range(l):
	cdic += [sdic[((i%2)*(i/2)) + ((1-i%2)*(l-(i/2)-1))]]
print cdic

xdata = [i[0] for i in cdic]
y = [i[1]/args.q for i in cdic]
x = range(len(xdata))

#PERCENT PRINT
SUM = sum(dic.itervalues())
percent = [float(i[1]) * 100/SUM for i in cdic]
for i in x:
	xdata[i] = xdata[i] + ' (%.2f%%) ' %(percent[i])
#-------------

# The slices will be ordered and plotted counter-clockwise.
cmap = plt.cm.prism
colors = cmap(np.linspace(0., 1., len(y)))
explode = (0, 0, 0, 0) # only "explode" the 2nd slice (i.e. 'Hogs')

#plt.pie(y, explode=explode, labels=xdata, colors=colors, autopct='%1.2f%%', pctdistance=0.7, labeldistance=1.1)
#plt.pie(y, labels=xdata, pctdistance=0.7, labeldistance=1.1)
# Set aspect ratio to be equal so that pie is drawn as a circle.
plt.axis('equal')
#plt.xticks( x,  xdata, rotation=0)
pie_wedge_collection = plt.pie(y, colors=colors, labels=xdata, labeldistance=1.05);

for pie_wedge in pie_wedge_collection[0]:
	    pie_wedge.set_edgecolor('white')




plt.grid(True)
plt.title(title, fontsize=24)
#plt.xlabel(xlabel,fontsize=22)
#plt.ylabel(ylabel, fontsize=16)
#plt.tick_params(axis='both', which='major', labelsize=15)
#plt.tick_params(axis='both', which='minor', labelsize=15)
plt.tight_layout()

# Save the figure in a separate file
plt.savefig(imgname)
plt.savefig(imgname, format='png', dpi=600)
plt.savefig(imgname+".svg", format='svg', dpi=300)


if not args.nodisplay:
	plt.show()

