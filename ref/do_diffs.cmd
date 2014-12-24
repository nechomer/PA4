@ECHO off
mkdir pa-3-myoutput
mkdir pa-3-myoutput\lib
del pa-3-myoutput\*.ic.out
FOR %%a in (pa-3-input/*.ic) do (
   echo ********      TESTING %%a      ********
	java -cp bin;lib\gearley.jar;src Main pa-3-input/%%a > pa-3-myoutput/%%a.out
	echo.
	echo diff -b pa-3-myoutput/%%a.out pa-3-output/%%~na.sym
	diff -b pa-3-myoutput/%%a.out pa-3-output/%%~na.sym
   echo =================================================================
)

FOR %%a in (pa-3-input/lib/*.ic) do (
   echo ********      TESTING with Library %%a      ********
	java -cp bin;lib\gearley.jar;src Main pa-3-input/lib/%%a -Lpa-3-input/libic.sig > pa-3-myoutput/lib/%%a.out
	echo.
	echo diff -b pa-3-myoutput/lib/%%a.out pa-3-output/lib/%%~na.sym
	diff -b pa-3-myoutput/lib/%%a.out pa-3-output/lib/%%~na.sym
   echo =================================================================
)

echo ********      TESTING Library      ********
	java -cp bin;lib\gearley.jar;src Main pa-3-input/empty_prog.ic -Lpa-3-input/libic.sig > pa-3-myoutput/libic.ic.out
	echo.
	echo diff -b pa-3-myoutput/libic.ic.out pa-3-output/libic.sym
	diff -b pa-3-myoutput/libic.ic.out pa-3-output/libic.sym
echo =================================================================