import os
import subprocess
import sys

JAVA_RUN = "java -cp bin:lib/gearley.jar:src Main "


def main():
    #Clean stage
    subprocess.call(["ant", "clean"])
    #Compilation stage
    cmd = "ant compile"
    compile_process = subprocess.Popen(cmd, shell=True)
    compile_process.wait()
    test_output_dir = os.path.join(os.getcwd(), "pa-2-myoutput")
    test_input_dir = os.path.join(os.getcwd(), "pa-2-input")
    end_msg = "###########################\n"
    for f in os.listdir(test_input_dir):
        print "Run program on: " + f
        with open(os.path.join(test_output_dir, f), 'w+') as out_file:
            if f != "libic.sig":
                cmd = JAVA_RUN + os.path.join(test_input_dir, f)
            else:
                arg_0 = os.path.join(test_input_dir, "empty_prog.ic")
                arg_1 = os.path.join(test_input_dir, f)
                cmd = JAVA_RUN + arg_0 + " -L" + arg_1
            compile_process = subprocess.Popen(cmd, shell=True,
                                               stdout=out_file)
            compile_process.wait()
    with open(os.path.join(os.getcwd(), "diff.log"), 'a') as log_file:
        for f in os.listdir(test_output_dir):
            if f != "libic.sig":
                old_name = ".ic"
            else:
                old_name = ".sig"
            test_out_file = os.path.join(os.getcwd(), "pa-2-output",
                                         f.replace(old_name, ".ast"))
            test_out_my_file = os.path.join(test_output_dir, f)
            diff_msg = "Diff between" + test_out_file + " " + test_out_my_file
            log_file.write(diff_msg + "\n")
            cmd = "diff " + test_out_file + " " + test_out_my_file
            compile_process = subprocess.Popen(cmd, shell=True,
                                               stdout=log_file)
            compile_process.wait()
            log_file.write(end_msg)


if __name__ == '__main__':
    main()