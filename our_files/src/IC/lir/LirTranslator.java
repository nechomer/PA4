package IC.lir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import IC.BinaryOps;
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

public class LirTranslator implements Visitor {
	
	private Map<String,String> stringsMap;
	private String currClass;
	private String globalTestLabel = null;
	private String globalEndLabel = null;
	
	private int currReg = 0;
	private int currLabel = 0;
	
	
	public LirTranslator(Map<String,String> strMap) {
		this.stringsMap = strMap;
	}

	/**
	 * @param program - The visited program
	 * @return The complete string representation of the program's LIR code, appending each
	 * of its class' LIR code in order
	 */
	@Override
	public Object visit(Program program) {
		String lir = makeLirRuntimeErrorFuncs();
		for (ICClass icClass : program.getClasses()) {
            lir += icClass.accept(this);
        }
		return lir;
	}

	/**
	 * @param icClass - The visited class
	 * @return The string representation of the classe's LIR code, appending each of its method's
	 * LIR code in order
	 */
	@Override
	public Object visit(ICClass icClass) {
		String lir = "";
		currClass = icClass.getName();
        for (Method method : icClass.getMethods()) {
            lir += method.accept(this);        
        }
		return lir;
	}

	/**
	 * @param field - The visited field
	 * @return null, A field doesn't require translation
	 */
	@Override
	public Object visit(Field field) {
		return null;
	}
	
	/**
	 * @param method - The visited method
	 * @return - The string representation of the appended method's statement's LIR code
	 */
	private String visitMethod(Method method) {
		String lir = "";
        for (Statement statement : method.getStatements())
            lir += statement.accept(this); 
        return lir;
	}
	
	/**
	 * @param method - The visited method
	 * @return - The method's label in LIR format
	 */
	private String getMethodLabel(Method method) {
		return "_" + currClass + "_" + method.getName() + ":\n"; 
	}
	
	/**
	 * @param method - The visited virtual method
	 * @return - The string representation of the virtual method's LIR code
	 */
	@Override
	public Object visit(VirtualMethod method) {
		String label = "\n" + getMethodLabel(method);
		String lir = visitMethod(method);
		
		if( method.getType().getName().equals("void"))
				lir += "Return Rdummy\n";
		
		return label +lir;
	}
	
	/**
	 * @param method - The visited static method
	 * @return - The string representation of the static method's LIR code
	 */
	@Override
	public Object visit(StaticMethod method) {
		String label = "\n" + getMethodLabel(method);
		String lir = visitMethod(method);
		
		if( method.getType().getName().equals("void")) {
			//Main method is also static - So it should have a program exit instruction
			//attached at the end of the LIR code
			if(method.getName().equals("main")){
				label = "\n_ic_main:\n";
				lir += "Library __exit(0),Rdummy\n";
			}
			else {
				lir += "Return Rdummy\n";
			}
		}
		
		return label +lir;
	}

	/**
	 * @param method - Library method
	 * @return the empty string, no translation is required for library methods
	 */
	@Override
	public Object visit(LibraryMethod method) {
		return "";
	}

	/**
	 * @param formal
	 * @return the empty string, no translation is required for library methods
	 */
	@Override
	public Object visit(Formal formal) {
		return null;
	}

	/**
	 * @param type
	 * @return the empty string, no translation is required for primitive types
	 */
	@Override
	public Object visit(PrimitiveType type) {
		return null;
	}

	/**
	 * @param type
	 * @return the empty string, no translation is required for user types
	 */
	@Override
	public Object visit(UserType type) {
		return null;
	}

	/**
	 * @param assignment - The visited assignment
	 * @return The assignment's string representation in LIR - Move <assignment>,<variable>
	 */
	@Override
	public Object visit(Assignment assignment) {
		String reg = getNextReg();
		String lirAss = "" + assignment.getAssignment().accept(this);
		currReg++;
		assignment.getVariable().setLhs(true);
		String lirVar = "" + assignment.getVariable().accept(this);
		lirAss += String.format(lirVar, reg);
		currReg--;
		return lirAss;
	}

	/**
	 * @param callStatement - The visited call statement
	 * @return - The string LIr representation of the calling statement - Virtual or static
	 */
	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}

	/**
	 * @param returnStatement - The visited return statement
	 * @return The string representation of the return LIR instruction - Return <Reg>
	 */
	@Override
	public Object visit(Return returnStatement) {
		
		if ( returnStatement.hasValue() ) {
			String reg = getNextReg() ;
			String lir = "" + returnStatement.getValue().accept(this);
			lir += "Return " + reg + "\n";
			return lir;
		}
		else { 
			return "Return Rdummy\n";
		}
	}

	/**
	 * @param ifStatement - The visited if statement
	 * @return - The string representation of the LIR if code.
	 * 			 <condition> Compare 0, <conditionReg>
	 * 			 JumpTrue <false unique label>
	 * 			 <operation>
	 *           Jump <end unique label> - For if else statements
	 *           <false unique label>
	 *           <else operation> (If exists)
	 *           <end unique label>
	 */
	@Override
	public Object visit(If ifStatement) {
		String lir = "";
		String condReg = getNextReg();
		String falseLabel = makeNextLirLabel("false_label");
		String endLabel = makeNextLirLabel("end_label");
		
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

	/**
	 * @param whileStatement - The visited while statement
	 * @return The string LIR code for the while loop.
	 * 			<unique test label>
	 * 			<while condition>
	 * 			Compare 0, <ConditionReg>
	 * 			JumpTrue <unique end label>
	 * 			<while operation>
	 * 			Jump <unique test label>
	 * 			<unique end label>
	 * 			
	 */
	@Override
	public Object visit(While whileStatement) {
		String lir = "";
		String condReg = getNextReg();
		
		String testLabel = makeNextLirLabel("test_label");
		String endLabel = makeNextLirLabel("end_label");
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

	/**
	 * @param breakStatement - The visited break statement
	 * @return - A string LIR code for break in a while loop - Jump <unique end label>
	 */
	@Override
	public Object visit(Break breakStatement) {
		return "Jump " + globalEndLabel + "\n";
	}

	/**
	* @param continueStatement - The visited continue statement
	 * @return - A string LIR code for break in a while loop - Jump <unique test label>
	 */
	@Override
	public Object visit(Continue continueStatement) {
		return "Jump " + globalTestLabel + "\n";
	}

	/**
	 * @param statementsBlock - The visited statement block
	 * @return - The string of LIR code for a statement block - 
	 * Each statement is appended in order to the rest
	 */
	@Override
	public Object visit(StatementsBlock statementsBlock) {
		String lir = "";
		
        for (Statement statement : statementsBlock.getStatements())
        	lir += statement.accept(this);
        
		return lir;
	}

	/**
	 * @param localVariable - The visited local variable
	 * @return - The string representation of a LIR instruction for a local variable defintion,
	 * including assigning an initial value - 
	 * <Initial value instruction> (If there's an init value)
	 * Move <Reg>, <local variable>
	 */
	@Override
	public Object visit(LocalVariable localVariable) {
		String lir = "";
		if ( localVariable.hasInitValue() ) {
			String reg = getNextReg();
			lir += localVariable.getInitValue().accept(this);
			lir += "Move " + reg + ", " + localVariable.getName() + "\n";
		} else {
			lir += "Move 0, " + localVariable.getName() + "\n";
		}
		return lir;
	}

	/**
	 * @param location - The visited variable location 
	 * @return - The string representation of the LIR command for variable location
	 * Move this, <Reg1> 
	 * MoveField <Reg2>[.<offset2>] <Reg1>[.<offset1>]
	 * The optional offsets are for a case it is a class' field assigned into another class' field 
	 */
	@Override
	public Object visit(VariableLocation location) {
		String lir = "";
		String resReg = getNextReg();
		//In case it is a local field in a class
		if ( ( ! location.isExternal() )  &&
				 ( location.scope.retrieveIdentifier(location.getName()) instanceof Field  )  ){ // Implicit this
				lir = "";
				resReg = getNextReg();
				
				// allocate object register and evaluate its value
				if ( ! location.isLhs() )
					currReg++;
				String objReg = getNextReg();
				lir += "Move this, " + objReg + "\n";
				
				// get field offset in the dispatch table
				String className = location.scope.getClassOfScope();
				int offset = DispatchTableBuilder.getFieldOffset(className, location.getName());
				
				if ( location.isLhs() ) {//In case of an assignment
					lir += "MoveField %s, " + objReg + "." + offset + "\n";
				} else {//In case it is a direct access to the field, no assignment
					lir += "MoveField " + objReg + "." + offset + ", " + resReg + "\n";
					currReg--;
				}
				
				return lir;
			
			} else if ( location.isExternal() ) {
				lir = "";
				resReg = getNextReg();
				
				// allocate object register and evaluate its value
				if ( ! location.isLhs() )
					currReg++;
				String objReg = getNextReg();
				lir += location.getLocation().accept(this);
				lir += nullPtrCheckStr(objReg);
				
				// get field offset
				String className = location.getcName();
				int offset = DispatchTableBuilder.getFieldOffset(className, location.getName());
				
				if ( location.isLhs() ) {
					lir += "MoveField %s, " + objReg + "." + offset + "\n";
				} else {
					lir += "MoveField " + objReg + "." + offset + ", " + resReg + "\n";
					currReg--;
				}
				
				return lir;
			} else {
				if ( location.isLhs() ) {
					// variable is assignment target
					lir = "Move %s, " + location.getName() + "\n";
					return lir;
				} else {
					// variable is a part of an expression
					lir = "Move " + location.getName() + ", " + getNextReg() + "\n";
					return lir;
				}
			}
	}

	/**
	 * @param location - The visited array location
	 * @return - The string represenation of the LIR array location
	 * MoveArray <Reg2> <Reg1>[index]
	 */
	@Override
	public Object visit(ArrayLocation location) {
		String lir = "";
		String arrReg = "";
		String indexReg = "";
		if ( location.isLhs() ) {//The array location gets an assignment
			
			arrReg = getNextReg();
			lir += location.getArray().accept(this);
			lir += nullPtrCheckStr(arrReg);

			currReg++;
			indexReg = getNextReg();
			lir += location.getIndex().accept(this);
			lir += arrIdxOutOfBoundsCheckStr(arrReg, indexReg);
			
			// create assignment translation
			lir += "MoveArray %s, " + arrReg + "[" + indexReg + "]\n";
			
			currReg--;
		} else {

			String resReg = getNextReg();
			
			currReg++;
			arrReg = getNextReg();
			lir += location.getArray().accept(this);
			lir += nullPtrCheckStr(arrReg);
			
			currReg++;
			indexReg = getNextReg();
			lir += location.getIndex().accept(this);
			lir += arrIdxOutOfBoundsCheckStr(arrReg, indexReg);
			
			// Write result to target register
			lir += "MoveArray " + arrReg + "[" + indexReg + "], " + resReg + "\n"; 
			
			currReg -= 2;
				
		}
		return lir;
	}
	
	/**
	 * @param call - The visited static call
	 * @return - The string representation of the LIR static function call
	 * It may be a library function call or a static function defined in a user class
	 * For user class:
	 * StaticCall <function name in dispatch table>([args registers]), Reg
	 * StaticCall __checkNullRef(a=Reg),<Return reg>
	 * For library call:
	 * Library __<Library function name>(<arguments>),<Return reg>  
	 */
	@Override
	public Object visit(StaticCall call) {
		String lir = "";
		int startMax = currReg;

		String resReg = getNextReg();
		if ( call.getMethod().getType().getName().equals("void") )
			resReg = "Rdummy";
		
		// evaluate all arguments in the function call
		List<String> paramRegs = new ArrayList<>(call.getArguments().size());
		for (Expression argument : call.getArguments()) {
			currReg++;
			paramRegs.add(getNextReg());
			lir += argument.accept(this);
		}
			
		// library function call
		if ( call.getClassName().equals("Library") ) {
			for ( int i = 0; i < paramRegs.size(); i++ ) {
				if (call.getMethod().scope.isStringType(i)) {
					lir +=nullPtrCheckStr(paramRegs.get(i));
				}
			}
			
			lir += "Library __" + call.getName() + "(";
			for ( int i = 0; i < paramRegs.size()-1; i++ )
				lir += paramRegs.get(i) + ",";
			if ( paramRegs.size() > 0 )
				lir += paramRegs.get(paramRegs.size()-1);
		} else {
			lir += "StaticCall _" + call.getClassName() + "_" + call.getName() + "(";
			lir += translateCallArgsToLir(call, paramRegs);
		}

		lir += "), " + resReg + "\n";
			
		currReg = startMax;
		return lir;
	}

	/**
	 * @param call - The visited virtual call
	 * @return - The string representation of the LIR instruction for virtual call
	 * VirtualCall [<Class reg>.]<method offset in dispatch table>(),<Return reg>
	 */
	@Override
	public Object visit(VirtualCall call) {
		String lir = "";
		int startMax = currReg;
		
		String resReg = getNextReg();
		if ( call.getMethod().getType().getName().equals("void") )
			resReg = "Rdummy";
		
		// allocate object register and evaluate its value
		currReg++;
		String objReg = getNextReg();
		if ( call.isExternal() ) {
			lir += call.getLocation().accept(this);
			lir += nullPtrCheckStr(objReg);
		} else {
			lir += "Move this, " + objReg + "\n";
		}
		
		// calculate method offset in the dispatch table
		String className = call.getMethod().scope.getClassOfScope();
		int methodOffset = DispatchTableBuilder.getMethodOffset(className, call.getName());
		
		//evaluate arguments in the function call
		List<String> paramRegs = new ArrayList<>(call.getArguments().size());
		for (Expression argument : call.getArguments()) {
			currReg++;
			paramRegs.add(getNextReg());
			lir += argument.accept(this);
		}
			
		//call the method
		lir += "VirtualCall " + objReg + "." + methodOffset;
		lir += "(" + translateCallArgsToLir(call, paramRegs) + "), " + resReg + "\n";
		
		// pop used registers
		currReg = startMax;
		
		return lir;
	}

	/**
	 * @param call - the static/virtual call
	 * @param paramRegs - The parameter registers list
	 * @return - The string representation of all parameter registers in format of <Reg>=<Defined argument>
	 */
	private String translateCallArgsToLir(Call call, List<String> paramRegs) {
		List<Formal> fl = call.getMethod().getFormals();
		String lir ="";
		for ( int i = 0; i < fl.size() ; i++ ) {
			lir += fl.get(i).getName() + "=" + paramRegs.get(i) + ((i+1==fl.size()) ? "" : ", ");
		}
		return lir;
	}

	/**
	 * @param thisExpression - The visited expression in the "this" format
	 * @return - The string representation of LIR instruction for "this" - Move this, <Reg>
	 * This is for the case we need to access the instance itself
	 */
	@Override
	public Object visit(This thisExpression) {
		String reg = getNextReg();
		String lir = "Move this, " + reg + "\n";
		return lir;
	}

	/**
	 * @param newClass - The visited new class 
	 * @return - The LIR format for new class
	 * Library_allocateObject(<object size>), <Reg>
	 * MoveField <class name in dispatch table> <Reg>.0 
	 */
	@Override
	public Object visit(NewClass newClass) {
		String lir = "";
		String resReg = getNextReg();
		String DVName = DispatchTableBuilder.getDispatchTableName(newClass.getName());
		int sizeOfObject = DispatchTableBuilder.getNumFields(newClass.getName()) *4 + 4;
		lir += "Library __allocateObject(" + sizeOfObject + "), " + resReg + "\n";
		lir += "MoveField " + DVName + ", " + resReg + ".0\n";
		return lir;
	}

	/**
	 * @param newArray - The visited new array
	 * @return - The LIR of new array
	 * Library __allocateArray(<sizeReg>), <Reg>
	 */
	@Override
	public Object visit(NewArray newArray) {
		String lir = "";
		
		//array size
		currReg++;
		String sizeReg = getNextReg();
		lir += newArray.getSize().accept(this);
		lir += arrIdxCheckStr(sizeReg);
		currReg--;
		
		//new array of size (array size)*4
		String resReg = getNextReg();
		lir += "Mul 4, " + sizeReg + "\n";
		lir += "Library __allocateArray(" + sizeReg + "), " + resReg + "\n";		
		
		return lir;
	}

	/**
	 * @param length - The visited array length
	 * @return - The LIR for array length access
	 * ArrayLength<array reg>, <Reg>
	 */
	@Override
	public Object visit(Length length) {
		String lir = "";
		
		//the array
		currReg++;
		String arrReg = getNextReg();
		lir += length.getArray().accept(this);
		lir += nullPtrCheckStr(arrReg);
		currReg--;
		
		//array length
		String resReg = getNextReg();
		lir += "ArrayLength " + arrReg + ", " + resReg + "\n";
		
		return lir;
	}

	/**
	 * @param binaryOp - The visited math binary operation
	 * @return - The LIR for binOp
	 * Can be also a string concatenation in case of a string operand with a + operator
	 * <Operator> <Reg1>, <Reg2> 
	 */
	@Override
	public Object visit(MathBinaryOp binaryOp) {
		String firstReg = getNextReg();
		String binaryLir = "" + binaryOp.getFirstOperand().accept(this);
		
		currReg++;
		String secReg = getNextReg();
		binaryLir += binaryOp.getSecondOperand().accept(this);
		
		// add runtime divide by zero check
		if ( binaryOp.getOperator() == BinaryOps.DIVIDE ) {
			binaryLir += zeroDivCheckStr(secReg);
		}
		
		if ( binaryOp.isStrCat() ) { // add a check for a string concatenation - no 2 null strings
			binaryLir += nullPtrCheckStr(firstReg);
			binaryLir += nullPtrCheckStr(secReg);
			binaryLir += "Library __stringCat(" + firstReg  + "," + secReg + "), " + firstReg + "\n";
		}
		else {
			binaryLir += binaryOp.getOperator().getLirOp()  + " " + secReg + ", " + firstReg  + "\n";
		}
		
		currReg--;
		
		return binaryLir;
	}

	/**
	 * @param binaryOp - The visited logical binary operation
	 * @return - The LIR for logical operation
	 * Calls the methods which handle and, or, comparison for jump labels
	 */
	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		
		int lastReg = currReg;
		
		String binaryLir = "";
		if ( ( binaryOp.getOperator() == BinaryOps.LAND ) ||
				( binaryOp.getOperator() == BinaryOps.LOR ) )
			binaryLir += makeLirLogicalBinaryAndOrCode(binaryOp);
		else
			binaryLir += makeLirComparrisonCode(binaryOp);
		
		currReg = lastReg;
		return binaryLir;
	}
	
		
		
	/**
	 * @param binaryOp - The visited logical comparison
	 * @return - The string LIR to handle the comparison
	 * Move 0, <Result Reg>
	 * Compare <Reg2>,<Reg3>
	 * Jump<compare operation> <logical_op_end label>
	 * Move 1, <Result Reg>
	 * <logical_op_end label>
	 */
	private String makeLirComparrisonCode(LogicalBinaryOp binaryOp) {
		
		String testEnd = makeUniqueJumpLabel("logical_op_end");
		String binaryLir = "";

		String resReg = getNextReg();
		binaryLir += "Move 0, " + resReg + "\n"; // default is false
		currReg++;
		
		
		// generate code to evaluate the 1st operand
		String firstReg = getNextReg();
		binaryLir += binaryOp.getFirstOperand().accept(this);
		
		// generate code to evaluate the 2nd operand
		currReg++;
		String secReg = getNextReg();
		binaryLir += binaryOp.getSecondOperand().accept(this);
		
		binaryLir += "Compare " + firstReg + ", " + secReg + "\n";
		binaryLir += binaryOp.getOperator().getLirOp() + " " + testEnd + "\n";
		binaryLir += "Move 1, " + resReg + "\n";
		binaryLir += testEnd + ":\n";
		
		return binaryLir;
	}

	/**
	 * @param binaryOp - The visited logical and/or
	 * @return - The string LIR to handle the and/or
	 * Compare 0, <Reg1>
	 * For and/or : JumpTrue/JumpFalse <logical_op_end label>
	 * <Eval Reg2>
	 * And/OR <Reg2>, <Reg1>
	 * <logical_op_end label>:
	 */
	private String makeLirLogicalBinaryAndOrCode(LogicalBinaryOp binaryOp) {

		String testEnd = makeUniqueJumpLabel("logical_op_end");
		String binaryLir = "";

		String firstReg = getNextReg();

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
		currReg++;
		binaryLir += binaryOp.getSecondOperand().accept(this);
		
		String secReg = getNextReg();
		
		// Do actual operation and save the result in the 1st register
		binaryLir += binaryOp.getOperator().getLirOp() + " " + secReg + "," + firstReg + "\n";
		
		binaryLir += testEnd + ":\n"; // add a point to jump to in case of lazy eval.

		return binaryLir;
	}

	/**
	 * @param unaryOp - The visited math unary operation 
	 * @return - The LIR representation for the op, in the format
	 * <Evaluate Reg1>
	 * Neg <Reg1>
	 */
	@Override
	public Object visit(MathUnaryOp unaryOp) {
		// Only negation of numeric type expression
		
		String reg = getNextReg();
		String lir = "" + unaryOp.getOperand().accept(this);
				
		lir += "Neg " + reg  + "\n";
		
		return lir;
	}

	/**
	 * @param unaryOp - The visited logical negation
	 * @return - The LIR representation int the format:
	 *  <Evaluate Reg1>
	 *  Xor 1 <Reg>
	 */
	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		// Only negation of boolean type expression
		
		String reg = getNextReg();
		String lir = "" + unaryOp.getOperand().accept(this);
				
		lir += "Xor 1," + reg  + "\n";
		
		return lir;
	}

	/**
	 * @param literal - The visited literal
	 * @return - The LIR format for literal access
	 * Move <value<, <Reg>
	 */
	@Override
	public Object visit(Literal literal) {
		switch ( literal.getType() ) {
		case INTEGER:
			return "Move " + literal.getValue() + ", " + getNextReg() + "\n";
		case NULL:
			return "Move 0, " + getNextReg() + "\n";
		case FALSE:
			return "Move 0, " + getNextReg() + "\n";
		case TRUE:
			return "Move 1, " + getNextReg() + "\n";
		case STRING:
			return "Move " + stringsMap.get(StringsBuilder.formatString(literal.getValue().toString())) + ", " + getNextReg() + "\n";
		}
		return null;
	}

	/**
	 * @param expressionBlock - The visited expresstion block
	 * @return - the LIR representation of an expression block
	 */
	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		
		return expressionBlock.getExpression().accept(this);
	}
	
	/**
	 * @return
	 */
	private String getNextReg() {
		return "R" + (currReg);
	}
	/**
	 * @param label
	 * @return
	 */
	private String makeNextLirLabel(String label) {
		return "_" + label + "_" + (++currLabel);
	}
	/**
	 * @return
	 */
	private static String makeLirRuntimeErrorFuncs(){
		return nullPtrCheckCode + arrIdxOutOfBoundsCheckCode + arrIdxCheckCode + zeroDivCheckCode;
	} 


	/**
	 * 
	 */
	private static final String nullPtrCheckCode = 
	"# Check Null Ptr Reference:\n" +
	"# static void checkNullRef(array a){\n" +
	"# 	if(a == null) {Library.println(...);\n" +
	"# 	Library.exit(1);\n" +
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

	/**
	 * 
	 */
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

	/**
	 * 
	 */
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

	/**
	 * 
	 */
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

	/**
	 * @param reg
	 * @return The LIR representation of null access checking
	 */
	private String nullPtrCheckStr(String reg) {
		return "StaticCall __checkNullRef(a=" + reg + "),Rdummy\n";
	}

	/**
	 * @param arrReg
	 * @param idxReg
	 * @return The LIR representation of an array access - Checks it's a valid idx
	 */
	private String arrIdxOutOfBoundsCheckStr(String arrReg, String idxReg) {
		return "StaticCall __checkArrayAccess(a=" + arrReg + ", i=" + idxReg + "),Rdummy\n";
	}

	/**
	 * @param sizeReg
	 * @return The LIR representation of a new array size - Checks it's a valid size (non negative)
	 */
	private String arrIdxCheckStr(String sizeReg) {
		return "StaticCall __checkSize(n=" + sizeReg + "),Rdummy\n";
	}

	/**
	 * @param intReg
	 * @return The string LIR representation for a zero check of a Reg
	 */
	private String zeroDivCheckStr(String intReg) {
		return "StaticCall __checkZero(b=" + intReg + "),Rdummy\n";
	}
	
	/**
	 * @param name
	 * @return The LIR representation for a label in the program
	 */
	private String makeUniqueJumpLabel(String name) {
		return "_" + name + "_" +(++currLabel);
	}
}
