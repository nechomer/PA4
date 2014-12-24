JAVA_PATH="/home/stas/programs/jdk1.7.0_45/bin"

$JAVA_PATH/javac -d ./bin -classpath 'lib/gearley.jar' `find ./ -name "*.java"`
mkdir pa-4-myoutput
for f in $(ls -l pa-4-input/ | grep ^- | awk '{print $9}' | grep "\.ic" | sed 's/.\{3\}$//')
do
	echo "********      Compiling $f      ********"
	$JAVA_PATH/java -classpath 'bin:lib/gearley.jar:src' Main pa-4-input/$f.ic -Lpa-4-input/libic.sig > pa-4-myoutput/$f
	$JAVA_PATH/java -jar lib/3ac-emu.jar -q pa-4-myoutput/$f > pa-4-myoutput/$f.out
	echo "diff -b pa-4-myoutput/$f.out pa-4-output/$f.out "
	diff -b pa-4-myoutput/$f.out pa-4-output/$f.out
done 
