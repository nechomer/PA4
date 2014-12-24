REM call genlex.cmd
dir /s /B src\*.java > sources.txt
javac -d bin -cp lib\gearley.jar @sources.txt
call do_diffs.cmd > test_results.txt 2>&1