import os
import sys
import time
from glob import glob
from subprocess import *



ProgFileList = glob("input/*.lir")
for Progfile in ProgFileList:
	os.remove(Progfile)

ProgFileList = glob("input/*.ic")
for Progfile in ProgFileList:
	#fname = str(Progfile).split('\\')[-1:][0]
	#f1 = open('output/'+fname+'.txt', 'w+')
	args = ['PA4.jar', Progfile,"-Linput/Library/libic.sig", "-print-lir"]
	process = Popen(['java', '-jar']+list(args), stdout=PIPE, stderr=PIPE)
	time.sleep(2)
	#f1.close
	
time.sleep(5)	

ProgFileList = glob("input/*.lir")
for Progfile in ProgFileList:
	fname = str(Progfile).split('\\')[-1:][0]
	f1 = open('output/'+fname+'.txt', 'w+')
	args = ['microLIRFull.jar', Progfile]
	process = Popen(['java', '-jar']+list(args), stdout=f1, stderr=f1)
	f1.close
	

