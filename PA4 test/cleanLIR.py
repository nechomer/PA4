import os
import sys
import time
from glob import glob
from subprocess import *



ProgFileList = glob("input/*.lir")
for Progfile in ProgFileList:
	os.remove(Progfile)
	
# ProgFileList = glob("inputref/*.lir")
# for Progfile in ProgFileList:
	# os.remove(Progfile)
	
ProgFileList = glob("output/*.txt")
for Progfile in ProgFileList:
	os.remove(Progfile)
	
# ProgFileList = glob("outputref/*.txt")
# for Progfile in ProgFileList:
	# os.remove(Progfile)
	


