#!/usr/bin/python

from numpy import *
from pylab import *
from matplotlib import rc, rcParams


x = linspace(0,2*pi,100)
y = sin(x)
z = cos(x)

plot(x,y,label=r'$f(x) = \sin(x)$')
hold(True)
plot(x,z,label=r'$g(x) = \cos(x)$')
title(r'$\int_0^{\infty} t^{x-1} e^{-t} dt$', fontsize=18)
xlabel(r'$\alpha \sim \Gamma \leftarrow (M_{\odot})$',fontsize=17)
ylabel(r'Text in Computer Modern font', fontsize=17)
legend(loc='lower left')
hold(False)
grid(True)

# Save the figure in a separate file
savefig('sine_function_legend.png')

# Draw the plot to the screen
show()
