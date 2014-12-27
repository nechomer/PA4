package IC.lir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import IC.BinaryOps;
import IC.ICVoid;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.Call;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.Expression;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.Library;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;
import IC.SymbolTable.Kind;
import IC.SymbolTable.MethodSymbol;
import IC.SymbolTable.SymbolTable;
import IC.Types.TypeTable;
/**
 * Translate the AST to LIR string.
 *  
 * @author Asaf Bruner, Aviv Goll
 */

public class Translator implements Visitor {

	private static int maxLabel = 0;
	private int maxReg = 0;
	
	private String currentClass;
	private Map<String, String> stringMap;
	private String globalTestLabel = null;
	private String globalEndLabel = null;
	
	public Translator(Map<String, String> stringMap) {
		this.stringMap = stringMap;
	}

	private static String generateLabel(String name) {
		return "_" + name + "_" +(++maxLabel);
	}

	private String methodLabel(Method method) {
		return "_" + currentClass + "_" + method.getName() + ":\n";
	}

	private String curMaxReg() {
		return "R" + maxReg;
	}


	@Override
	public Object visit(Program program) {
		
		String lirProgram = runtimeChecksCode;
		for(ICClass icc : program.getClasses()){
			lirProgram += icc.accept(this);
		}
		return lirProgram;
	}

	@Override
	public Object visit(ICClass icClass) {
		String classLir = "";
		
		currentClass = icClass.getName();
		
		for(Method m : icClass.getMethods()){
			classLir += m.accept(this);
		}
		return classLir;
	}

	@Override
	public Object visit(Field field) {
		// Doesn't require visit
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		// Method Label
		String methodName = "\n" + methodLabel(method);
		
		// Method LIR code
		String methodLir = "";
		for ( Statement st : method.getStatements() ) { 
			methodLir += st.accept(this);
		}
		
		// FailSafe for methods without return
		if ( method.getType() instanceof ICVoid )
			methodLir += "Return Rdummy\n";
		
		return methodName + methodLir;
	}



	@Override
	public Object visit(StaticMethod method) {
		// Method Label
		String methodName = methodLabel(method);
		if ( method.isProgramMain() )
			methodName = "\n_ic_main:\n";
		
		// Method LIR code
		String methodLir = "";
		for ( Statement st : method.getStatements() ) { 
			methodLir += st.accept(this);
		}
		
		// FailSafe for methods without return
		if ( method.getType() instanceof ICVoid ) {
			if ( method.isProgramMain() )
				methodLir += "Library __exit(0),Rdummy\n";
			else
				methodLir += "Return Rdummy\n";
		}
		
		return "\n"+methodName + methodLir;
	}

	@Override
	public Object visit(LibraryMethod method) {

		return null;
	}

	@Override
	public Object visit(Formal formal) {
		
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		
		return null;
	}

	@Override
	public Object visit(UserType type) {
		
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		String expReg = curMaxReg();
		String assignemntLir = "" + assignment.getExp().accept(this);
		maxReg++;
		assignment.getVariable().setLvalue(true);
		String lvalLir = "" + assignment.getVariable().accept(this);
		assignemntLir += String.format(lvalLir, expReg);
		
		maxReg--; 
		return assignemntLir;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}

	@Override
	public Object visit(Return returnStatement) {
		if ( returnStatement.hasValue() ) {
			String resReg = curMaxReg() ;
			String result =""+ returnStatement.getValue().accept(this);
			result += "Return " + resReg + "\n";
			return (result);
		}
		else { 
			return "Return Rdummy\n";
		}
	}

	@Override
	public Object visit(If ifStatement) {
		String lir = "";
		String condReg = curMaxReg();
		String falseLabel = generateLabel("false_label");
		String endLabel = generateLabel("end_label");
		
		lir += ifStatement.getCondition().accept(this);
		lir += "Compare 0, " + condReg + "\n";
		lir += "JumpTrue " + falseLabel + "\n";
		lir += ifStatement.getOperation().accept(this);
		if ( ifStatement.hasElse() ) {
			lir += "Jump " + endLabel + "\n";
		}
		lir += falseLabel + ":\n";
		if ( ifStatement.hasElse() ) {
			lir += ifStatement.getElseOperation().accept(this);
			lir += endLabel + ":\n";
		}
		
		
		return lir;
	}

	@Override
	public Object visit(While whileStatement) {
		String lir = "";
		String condReg = curMaxReg();
		
		String testLabel = generateLabel("test_label");
		String endLabel = generateLabel("end_label");
		String oldTestLabel = globalTestLabel;
		String oldEndLabel = globalEndLabel;
		globalTestLabel = testLabel;
		globalEndLabel = endLabel;
		
		lir += testLabel + ":\n";
		lir += whileStatement.getCondition().accept(this);
		lir += "Compare 0, " + condReg + "\n";
		lir += "JumpTrue " + endLabel + "\n";
		lir += whileStatement.getOperation().accept(this);
		lir += "Jump " + testLabel + "\n";
		lir += endLabel + ":\n";
		
		globalTestLabel = oldTestLabel;
		globalEndLabel = oldEndLabel;
		
		return lir;
	}

	@Override
	public Object visit(Break breakStatement) {		
		return "Jump " + globalEndLabel + "\n";
	}

	@Override
	public Object visit(Continue continueStatement) {
		return "Jump " + globalTestLabel + "\n";
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		String lir = "";
		for ( Statement s : statementsBlock.getStatements() ) {
			lir += s.accept(this);
		}
		return lir;
	}

	@Override	
	public Object visit(LocalVariable localVariable) {
		String lir = "";
		if ( localVariable.hasInitValue() ) {
			// Evaluate the init value
			lir += localVariable.getInitValue().accept(this);
			lir += "Move " + curMaxReg() + ", " + localVariable.getUID() + "\n";
		} else {
			lir += "Move 0, " + localVariable.getUID() + "\n";
		}
		return lir;
	}

	private String getFieldClass(SymbolTable enclosingScope, String fieldName) {
		SymbolTable parent = enclosingScope;
		while ( parent.lookin(fieldName) == null ) {
			parent = parent.getParentSymbolTable();
		}
		return parent.getId();
	}
	
	@Override
	public Object visit(VariableLocation location) {
		
		if ( ( ! location.isExternal() )  &&
			 ( location.getEnclosingScope().lookup(location.getName()).
					getKind() == Kind.FIELD )  ){ // Implicit this
			String lir = "";
			String resReg = curMaxReg();
			
			// allocate object register and evaluate its value
			if ( ! location.isLvalue() )
				maxReg++;
			String objReg = curMaxReg();
			lir += "Move this, " + objReg + "\n";
			
			// get field offset
			String className = getFieldClass(location.getEnclosingScope(), location.getName());
			int offset = DVCreator.getFieldOffset(className, location.getName());
			
			if ( location.isLvalue() ) {
				lir += "MoveField %s, " + objReg + "." + offset + "\n";
			} else {
				lir += "MoveField " + objReg + "." + offset + ", " + resReg + "\n";
				maxReg--;
			}
			
			return lir;
		
		} else if ( location.isExternal() ) {
			String lir = "";
			String resReg = curMaxReg();
			
			// allocate object register and evaluate its value
			if ( ! location.isLvalue() )
				maxReg++;
			String objReg = curMaxReg();
			lir += location.getLocation().accept(this);
			lir += nullCheckStr(objReg);
			
			// get field offset
			String className = location.getLocation().getTypeScope().getId();
			int offset = DVCreator.getFieldOffset(className, location.getName());
			
			if ( location.isLvalue() ) {
				lir += "MoveField %s, " + objReg + "." + offset + "\n";
			} else {
				lir += "MoveField " + objReg + "." + offset + ", " + resReg + "\n";
				maxReg--;
			}
			
			return lir;
		} else {
			if ( location.isLvalue() ) {
				// variable is assignment target
				String lir = "Move %s, " + location.getLirName() + "\n";
				return lir;
			} else {
				// variable is a part of an expression
				String lir = "Move " + location.getLirName() + ", " + curMaxReg() + "\n";
				return lir;
			}
		}
	}

	@Override
	public Object visit(ArrayLocation location) {
		if ( location.isLvalue() ) {
			
			String lir = "";
			
			// array to R_T
			String arrReg = curMaxReg();
			lir += location.getArray().accept(this);
			lir += nullCheckStr(arrReg);

			// index to R_T+1
			maxReg++;
			String indexReg = curMaxReg();
			lir += location.getIndex().accept(this);
			lir += arrayCheckStr(arrReg, indexReg);
			
			// create assignment translation
			lir += "MoveArray %s, " + arrReg + "[" + indexReg + "]\n";
			
			maxReg--;
			return lir;
		} else {
			String lir = "";

			// Set R_T to contain the result
			String resReg = curMaxReg();
			
			// array to R_T+1
			maxReg++;
			String arrReg = curMaxReg();
			lir += location.getArray().accept(this);
			lir += nullCheckStr(arrReg);
			
			// index to R_T+2
			maxReg++;
			String indexReg = curMaxReg();
			lir += location.getIndex().accept(this);
			lir += arrayCheckStr(arrReg, indexReg);
			
			// Write result to target register
			lir += "MoveArray " + arrReg + "[" + indexReg + "], " + resReg + "\n"; 
			
			maxReg -= 2;
			
			return lir;
			
		}
	}

	@Override
	public Object visit(StaticCall call) {
		String lir = "";
		int startMax = maxReg;

		String resReg = curMaxReg();
		if ( call.isReturningVoid() )
			resReg = "Rdummy";
		
		// R_T+1, R_T+2, ...   <--   evaluate arguments
		List<String> paramRegs = new ArrayList<>(call.getArguments().size());
		for ( Expression exp : call.getArguments() ) {
			maxReg++;
			paramRegs.add(curMaxReg());
			lir += exp.accept(this);
		}
			
		// R_T <- call the method
		if ( call.getClassName().equals("Library") ) {
			lir += "Library __" + call.getName() + "(";
			for ( int i = 0; i < paramRegs.size()-1; i++ )
				lir += paramRegs.get(i) + ",";
			if ( paramRegs.size() > 0 )
				lir += paramRegs.get(paramRegs.size()-1);
		} else {
			lir += "StaticCall _" + call.getClassName() + "_" + call.getName() + "(";
			lir += callArgString(call, paramRegs);
		}

		lir += "), " + resReg + "\n";
			
		maxReg = startMax;
		return lir;
	}

	private String callArgString(Call call, List<String> paramRegs) {
		String lir = "";
		String className = getCallClass(call);
		MethodSymbol mSym = (MethodSymbol) (TypeTable.getClassSymTab(className)
				.lookup(call.getName()));
		List<Formal> fl = mSym.getFormals();
		for ( int i = 0; i < fl.size()-1 ; i++ ) 
			lir += fl.get(i).getLirName() + "=" + paramRegs.get(i) + ", ";
		if ( fl.size() > 0 )
			lir += fl.get(fl.size()-1).getLirName() + "=" + paramRegs.get(fl.size()-1);
		return lir;
	}

	private String getCallClass(Call call) {
		if ( call instanceof StaticCall )
			return ((StaticCall) call).getClassName();
		else {
			VirtualCall vcall = (VirtualCall) call;
			if ( vcall.isExternal() ) {
				Expression loc = vcall.getLocation();
				return loc.getTypeScope().getId();
			} else {
				SymbolTable parent = call.getEnclosingScope();
				while ( parent.getKind() != Kind.CLASS )
					parent = parent.getParentSymbolTable();
				return parent.getId();
			}
		}
	}

	@Override
	public Object visit(VirtualCall call) {
		String lir = "";
		int startMax = maxReg;
		
		String resReg = curMaxReg();
		if ( call.isReturningVoid() )
			resReg = "Rdummy";
		
		// allocate object register and evaluate its value
		maxReg++;
		String objReg = curMaxReg();
		if ( call.isExternal() ) {
			lir += call.getLocation().accept(this);
			lir += nullCheckStr(objReg);
		} else {
			lir += "Move this, " + objReg + "\n";
		}
		
		// calculate method offset
		String className = getCallClass(call);
		int methodOffset = DVCreator.getMethodOffset(className, call.getName());
		
		// R_T+2, R_T+3, ...   <--   evaluate arguments
		List<String> paramRegs = new ArrayList<>(call.getArguments().size());
		for ( Expression exp : call.getArguments() ) {
			maxReg++;
			paramRegs.add(curMaxReg());
			lir += exp.accept(this);
		}
			
		// R_T <- call the method
		lir += "VirtualCall " + objReg + "." + methodOffset;
		lir += "(" + callArgString(call, paramRegs) + "), " + resReg + "\n";
		
		// pop used registers
		maxReg = startMax;
		
		return lir;
	}

	@Override
	public Object visit(This thisExpression) {
		String resReg = curMaxReg();
		String lir = "Move this, " + resReg + "\n";
		return lir;
	}

	@Override
	public Object visit(NewClass newClass) {
		String lir = "";
		String resReg = curMaxReg();
		String DVName = DVCreator.getDVName(newClass.getName());
		int sizeOfObject = DVCreator.getNumFields(newClass.getName()) *4 + 4;
		lir += "Library __allocateObject(" + sizeOfObject + "), " + resReg + "\n";
		lir += "MoveField " + DVName + ", " + resReg + ".0\n";
		return lir;
	}

	@Override
	public Object visit(NewArray newArray) {
		String lir = "";
		
		// R_T+1 <- array size
		maxReg++;
		String sizeReg = curMaxReg();
		lir += newArray.getSize().accept(this);
		lir += sizeCheckStr(sizeReg);
		maxReg--;
		
		// R_T <- new array of size (R_T+1)*4
		String resReg = curMaxReg();
		lir += "Mul 4, " + sizeReg + "\n";
		lir += "Library __allocateArray(" + sizeReg + "), " + resReg + "\n";		
		
		return lir;
	}

	@Override
	public Object visit(Length length) {
		
		String lir = "";
		
		// R_T+1 <- the array
		maxReg++;
		String arrReg = curMaxReg();
		lir += length.getArray().accept(this);
		lir += nullCheckStr(arrReg);
		maxReg--;
		
		// R_T <- array length
		String resReg = curMaxReg();
		lir += "ArrayLength " + arrReg + ", " + resReg + "\n";
		
		return lir;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		String firstReg = curMaxReg();
		String binaryLir = "" + binaryOp.getFirstOperand().accept(this);
		
		maxReg++;
		String secReg = curMaxReg();
		binaryLir += binaryOp.getSecondOperand().accept(this);
		
		// add runtime divide by zero check
		if ( binaryOp.getOperator() == BinaryOps.DIVIDE ) {
			binaryLir += zeroCheckStr(secReg);
		}
		
		if ( binaryOp.isStrCat() ) {
			binaryLir += "Library __stringCat(" + firstReg  + "," + secReg + "), " + firstReg + "\n";
		}
		else {
			binaryLir += binaryOp.getOperator().getLirOp()  + " " + secReg + ", " + firstReg  + "\n";
		}
		
		maxReg--;
		
		return binaryLir;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		
		int oldMaxReg = maxReg;
		
		String binaryLir = "";
		if ( ( binaryOp.getOperator() == BinaryOps.LAND ) ||
				( binaryOp.getOperator() == BinaryOps.LOR ) )
			binaryLir += andOrCode(binaryOp);
		else
			binaryLir += comparrisonCode(binaryOp);
		
		maxReg = oldMaxReg;
		return binaryLir;
	}
	
		
		
	private String comparrisonCode(LogicalBinaryOp binaryOp) {
		
		String testEnd = generateLabel("logical_op_end");
		String binaryLir = "";

		String resReg = curMaxReg();
		binaryLir += "Move 0, " + resReg + "\n"; // default is false
		maxReg++;
		
		
		// generate code to evaluate the 1st operand
		String firstReg = curMaxReg();
		binaryLir += binaryOp.getFirstOperand().accept(this);
		
		// generate code to evaluate the 2nd operand
		maxReg++;
		String secReg = curMaxReg();
		binaryLir += binaryOp.getSecondOperand().accept(this);
		
		binaryLir += "Compare " + firstReg + ", " + secReg + "\n";
		binaryLir += binaryOp.getOperator().getLirOp() + " " + testEnd + "\n";
		binaryLir += "Move 1, " + resReg + "\n";
		binaryLir += testEnd + ":\n";
		
		return binaryLir;
	}

	private String andOrCode(LogicalBinaryOp binaryOp) {

		String testEnd = generateLabel("logical_op_end");
		String binaryLir = "";

		String firstReg = curMaxReg();

		// generate code to evaluate the 1st operand
		binaryLir += binaryOp.getFirstOperand().accept(this);
		
		
		if ( binaryOp.getOperator() == BinaryOps.LAND ) { // lazy "&&" evaluation
			binaryLir += "Compare 0, " + firstReg + "\n" +
					"JumpTrue " + testEnd + "\n" ;
		} else if ( binaryOp.getOperator() == BinaryOps.LOR ) { // lazy "||" evaluation
			binaryLir += "Compare 0, " + firstReg + "\n" +
					"JumpFalse " + testEnd + "\n";
		}
		
		// generate code to evaluate the 2nd operand
		maxReg++;
		binaryLir += binaryOp.getSecondOperand().accept(this);
		
		String secReg = curMaxReg();
		
		// Do actual operation and save the result in the 1st register
		binaryLir += binaryOp.getOperator().getLirOp() + " " + secReg + "," + firstReg + "\n";
		
		binaryLir += testEnd + ":\n"; // add a point to jump to in case of lazy eval.

		return binaryLir;
	}




	@Override
	public Object visit(MathUnaryOp unaryOp) {
		// Only negation of numeric type expression
		
		String reg = curMaxReg();
		String lir = "" + unaryOp.getOperand().accept(this);
				
		lir += "Neg " + reg  + "\n";
		
		return lir;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		// Only negation of boolean type expression
		
		String reg = curMaxReg();
		String lir = "" + unaryOp.getOperand().accept(this);
				
		lir += "Xor 1," + reg  + "\n";
		
		return lir;
	}

	@Override
	public Object visit(Literal literal) {
		switch ( literal.getType() ) {
		case INTEGER:
			return "Move " + literal.getValue() + ", " + curMaxReg() + "\n";
		case NULL:
			return "Move 0, " + curMaxReg() + "\n";
		case FALSE:
			return "Move 0, " + curMaxReg() + "\n";
		case TRUE:
			return "Move 1, " + curMaxReg() + "\n";
		case STRING:
			return "Move " + stringMap.get(literal.getValue()) + ", " + curMaxReg() + "\n";
		}
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		
		return expressionBlock.getExpression().accept(this);
	}

	@Override
	public Object visit(Library library) {
		
		return null;
	}

	@Override
	public Object visit(ICVoid icVoid) {
		
		return null;
	}

	private static final String runtimeChecksCode = 
			"__checkNullRef:" + "\n" +
			"	# a is a reference" + "\n" +
			"	Move a, R0" + "\n" +
			"	Compare 0, R0" + "\n" +
			"	JumpTrue _rc_error_label1" + "\n" +
			"	Return Rdummy" + "\n" +
			"_rc_error_label1:" + "\n" +
			"	Library __println(re_null_ref_str),Rdummy" + "\n" +
			"	Library __exit(1),Rdummy" + "\n" +
			"" + "\n" +
			"__checkArrayAccess:" + "\n" +
			"	# a is an arry" + "\n" +
			"	# i is an index register" + "\n" +
			"	ArrayLength a, R0" + "\n" +
			"	Compare i, R0" + "\n" +
			"	JumpLE _rc_error_label2" + "\n" +
			"	Move i, R0" + "\n" +
			"	Compare 0, R0" + "\n" +
			"	JumpL _rc_error_label2" + "\n" +
			"	Return Rdummy" + "\n" +
			"_rc_error_label2:" + "\n" +
			"	Library __println(re_array_index_str),Rdummy" + "\n" +
			"	Library __exit(1),Rdummy" + "\n" +
			"" + "\n" +
			"__checkSize:" + "\n" +
			"	# n is an array length" + "\n" +
			"	Move n, R0" + "\n" +
			"	Compare 0, R0" + "\n" +
			"	JumpLE _rc_error_label3" + "\n" +
			"	Return Rdummy" + "\n" +
			"_rc_error_label3:" + "\n" +
			"	Library __println(re_array_alloc_str),Rdummy" + "\n" +
			"	Library __exit(1),Rdummy" + "\n" +
			"" + "\n" +
			"__checkZero:" + "\n" +
			"	# x is a value that should no be zero" + "\n" +
			"	Move x, R0" + "\n" +
			"	Compare 0, R0" + "\n" +
			"	JumpTrue _rc_error_label4" + "\n" +
			"	Return Rdummy" + "\n" +
			"_rc_error_label4:" + "\n" +
			"	Library __println(re_zero_div_str),Rdummy" + "\n" +
			"	Library __exit(1),Rdummy" + "\n";
	
	private String nullCheckStr(String refReg) {
		return "StaticCall __checkNullRef(a=" + refReg + "),Rdummy\n";
	}

	private String arrayCheckStr(String arrReg, String indexReg) {
		return "StaticCall __checkArrayAccess(a=" + arrReg + ", i=" + indexReg + "),Rdummy\n";
	}

	private String sizeCheckStr(String sizeReg) {
		return "StaticCall __checkSize(n=" + sizeReg + "),Rdummy\n";
	}

	private String zeroCheckStr(String valReg) {
		return "StaticCall __checkZero(x=" + valReg + "),Rdummy\n";
	}
}
