import os
import sys
import time
from glob import glob
from subprocess import *




ProgFileList = glob("input/*.ic")
for Progfile in ProgFileList:
	fname = str(Progfile).split('\\')[-1:][0]
	f1 = open('outputast/'+fname+'.out', 'w+')
	args = ['PA4.jar', Progfile,"-Linput/Library/libic.sig", "-print-ast"]
	process = Popen(['java', '-jar']+list(args), stdout=f1, stderr=f1)
	time.sleep(2)
	f1.close
	
