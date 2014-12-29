import os
import sys
from glob import glob
from subprocess import *


ProgFileList = glob("input/*.ic")
for Progfile in ProgFileList:
	fname = str(Progfile).split('\\')[-1:][0]
	f1 = open('output/'+fname+'.lir', 'w+')
	args = ['PA4.jar', Progfile,"-Linput/Library/libic.sig", "-print-lir"]
	process = Popen(['java', '-jar']+list(args), stdout=f1, stderr=f1)
	f1.close
	
ProgFileList = glob("output/*.lir")
for Progfile in ProgFileList:
	fname = str(Progfile).split('\\')[-1:][0]
	f1 = open('lir/'+fname+'.txt', 'w+')
	f1.readline();
	f1.readline();
	args = ['microLIRFull.jar', Progfile]
	process = Popen(['java', '-jar']+list(args), stdout=f1, stderr=f1)
	f1.close
	

