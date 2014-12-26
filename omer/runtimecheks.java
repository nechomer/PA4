private static String runtimeErrorFuncs(){
	return nullPtrCheckCode + arrIdxOutOfBoundsCheckCode + arrIdxCheckCode + zeroDivCheckCode;
} 


private static final String nullPtrCheckCode = 
"# Check Null Ptr Reference:\n" +
"# static void checkNullRef(array a){\n" +
"# 	if(a == null) {Library.println(...);\n" +
"# 	Library.exit(1);" +
"# 	}\n" +
"# }\n" +
"__checkNullRef:\n" +
"	Move a, R0\n" +
"	Compare 0, R0\n" +
"	JumpTrue _error1\n" +
"	Return Rdummy\n" +
"_error1:\n" +
"	Library __println(str_err_null_ptr_ref),Rdummy\n" +
"	Library __exit(1),Rdummy\n" + 
"\n";

private static final String arrIdxOutOfBoundsCheckCode = 
"# Check Array Index Out Of Bounds:\n" +
"# static void checkArrayAccess(array a, index i) {\n" +
"# 	if (i<0 || i>=a.length) {\n" +
"# 	Library.println(\"Runtime Error\");\n" +
"# 	}\n" +
"# }\n" +
"__checkArrayAccess:\n" +
"	Move i, R0\n" +
"	Compare 0, R0\n" +
"	JumpL _error2\n" +
"	ArrayLength a, R0\n" +
"	Compare i, R0\n" +
"	JumpLE _error2" +
"	Return Rdummy\n" +
"_error2:\n" +
"	Library __println(str_err_arr_out_of_bounds),Rdummy\n" +
"	Library __exit(1),Rdummy\n" + 
"\n";

private static final String arrIdxCheckCode = 
"# Check Array Allocation Is Not With Negative Number:\n" +
"# static void checkSize(size n) {\n" +
"# 	if (n<0) Library.println(\"Runtime Error\");\n" +
"# }\n" +
"__checkSize:\n" +
"	Move n, R0\n" +
"	Compare 0, R0\n" +
"	JumpLE _error3\n" +
"	Return Rdummy\n" +
"_error3:\n" +
"	Library __println(str_err_neg_arr_size),Rdummy\n" +
"	Library __exit(1),Rdummy\n" + 
"\n";

private static final String zeroDivCheckCode = 
"# Check Division By Zero:\n" +
"# static void checkZero(value b) {\n" +
"# 	if (b == 0) Library.println(\"Runtime Error\");\n" +
"# }\n" +
"__checkZero:\n" +
"	Move b, R0\n" +
"	Compare 0, R0" +
"	JumpTrue _error4\n" +
"	Return Rdummy\n" +
"_error4:\n" +
"	Library __println(str_err_div_by_zero),Rdummy\n" +
"	Library __exit(1),Rdummy\n" + 
"\n";

private String nullPtrCheckStr(String reg) {
	return "StaticCall __checkNullRef(a=" + reg + "),Rdummy\n";
}

private String arrIdxOutOfBoundsCheckStr(String arrReg, String idxReg) {
	return "StaticCall __checkArrayAccess(a=" + arrReg + ", i=" + idxReg + "),Rdummy\n";
}

private String arrIdxCheckStr(String sizeReg) {
	return "StaticCall __checkSize(n=" + sizeReg + "),Rdummy\n";
}

private String zeroDivCheckStr(String intReg) {
	return "StaticCall __checkZero(b=" + intReg + "),Rdummy\n";
}



