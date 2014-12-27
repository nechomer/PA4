package IC.SemanticChecks;

import java.util.LinkedList;
import java.util.List;

import IC.BinaryOps;
import IC.ICVoid;
import IC.UnaryOps;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.BinaryOp;
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
import IC.AST.UnaryOp;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;
import IC.SymbolTable.Kind;
import IC.SymbolTable.Symbol;
import IC.SymbolTable.SymbolTable;
import IC.Types.ArrayType;
import IC.Types.MethodType;
import IC.Types.ClassType;
import IC.Types.Type;
import IC.Types.TypeTable;

/**
 * A visitor implementation to check that all the static types match in the program
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public class TypeCheck implements Visitor {

	private List<SymbolTable> scopeStack = new LinkedList<>();

	private SymbolTable libraryTable = null;
	
	@Override
	public Object visit(Program program) {
		setLibraryTable(program.getLibrary().getEnclosingScope());
		for (ICClass icClass : program.getClasses())
			icClass.accept(this);
		return null;
	}

	@Override
	public Object visit(ICClass icClass) {
		pushScope(icClass.getEnclosingScope());
		
		for (Method method : icClass.getMethods()){
			method.accept(this);
		}
		popScope();
		return null;
	}

	@Override
	public Object visit(Field field) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		pushScope(method.getEnclosingScope());
		for (Statement statement : method.getStatements())
			statement.accept(this);
		popScope();
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		pushScope(method.getEnclosingScope());
		for (Statement statement : method.getStatements())
			statement.accept(this);
		popScope();
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(UserType type) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		Type lht = (Type) assignment.getVariable().accept(this);
		Type rht = (Type) assignment.getExp().accept(this);
		if (!rht.subtypeof(lht))
			typeError(assignment.getLine(), "Can't assign " + rht.getName()
					+ " to " + lht.getName());
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}

	@Override
	public Object visit(Return returnStatement) {
		Symbol retSym = returnStatement.getEnclosingScope().lookup("$ret");
		
		if(returnStatement.getValue() == null){ //empty return statement
			if(retSym != null){
				typeError(returnStatement.getLine(), "empty return error");
				return null;
			}
			else{
				return null;
			}
		}
		
		if(retSym == null) { //void function
			if(returnStatement.getValue() != null){
				typeError(returnStatement.getLine(), "void function cant have return values");
				return null;
			}
		}
		Type funcRetType = retSym.getType();
		
		
		Type retExpType = (Type) returnStatement.getValue().accept(this);
		Symbol sm = returnStatement.getEnclosingScope().lookup("$ret");
		if(sm == null){
			typeError(returnStatement.getLine(), "void functions can't have a return statement");
		}
		
		if(! retExpType.subtypeof(funcRetType) )
			typeError(returnStatement.getLine(), "return type needs to be of type " + funcRetType);
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		if (ifStatement.getCondition().accept(this) != TypeTable.boolType)
			typeError(ifStatement.getLine(),
					"if statment must have boolean type as a condition");
		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		if (whileStatement.getCondition().accept(this) != TypeTable.boolType)
			typeError(whileStatement.getLine(),
					"while statment must have boolean type as a condition");
		whileStatement.getOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		// been here...
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		// been here...
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		pushScope(statementsBlock.getEnclosingScope());
		for (Statement statement : statementsBlock.getStatements())
			statement.accept(this);
		popScope();
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		Type varType = null;
		try {
			varType = TypeTable.astType(localVariable.getType());
		} catch (SemanticError e) {
			typeError(e);
		}
		if ( localVariable.hasInitValue() ) { 
			Type initValType = (Type) localVariable.getInitValue().accept(this);
			if ( ! initValType.subtypeof(varType) )
				typeError(localVariable.getLine(), "Can't initialize a variable of type "
						+ varType.getName() + " with value of type " + initValType.getName() );
		}
		return varType;
	}

	@Override
	public Object visit(VariableLocation location) {
		Symbol locSym;
		if (location.isExternal()) {
			Type leftType = (Type) location.getLocation().accept(this);
			SymbolTable leftSymTab = TypeTable.getClassSymTab(leftType
					.getName());
			locSym = leftSymTab.lookup(location.getName()); // will not be null
															// because of scope
															// check
		} else {
			locSym = location.getEnclosingScope().lookup(location.getName());// will not be
																	// null
																	// because
																	// of scope
																	// check
			location.setLirName(locSym.getUid());
		}
		return locSym.getType();
	}

	@Override
	public Object visit(ArrayLocation location) {
		Object arrType = location.getArray().accept(this);
		if ( ! (arrType instanceof ArrayType ) )
			typeError(location.getLine(), "Not an array");
		if ( location.getIndex().accept(this) != TypeTable.intType ) {
			typeError(location.getLine(), "Array index must be an integer expression");
		}
		return ((ArrayType) arrType).getElemType();
	}

	@Override
	public Object visit(StaticCall call) {
		
		MethodType mt;
		if ( call.getClassName().equals("Library")) {
			mt = (MethodType) getLibraryTable().lookup(call.getName()).getType();
		}
		else {
		// Scope checking assures us that this will work.
			mt = (MethodType) TypeTable.getClassSymTab(call.getClassName())
					.lookup(call.getName()).getType();
		}
		
		checkMethodType(call, mt);
		
		if ( mt.getReturnType().subtypeof(TypeTable.voidType) )
			call.setReturningVoid(true);
		return mt.getReturnType();
	}

	private SymbolTable getLibraryTable() {
		return libraryTable;
	}

	public void setLibraryTable(SymbolTable libraryTable) {
		this.libraryTable = libraryTable;
	}

	@Override
	public Object visit(VirtualCall call) {
		Symbol callSym = null;
		if ( call.isExternal() ) {
			Type locType = (Type) call.getLocation().accept(this);
			SymbolTable locSymTab = TypeTable.getClassSymTab(locType.getName());
			callSym = locSymTab.lookup(call.getName());
		} else {
			callSym = getCurrentScope().lookup(call.getName());
		}
		if ( callSym.getKind() != Kind.METHOD )
			typeError(call.getLine(), call.getName() + " is not a method.");
		MethodType mt = (MethodType) callSym.getType();
		
		checkMethodType(call, mt);
		
		if ( mt.getReturnType().subtypeof(TypeTable.voidType) )
			call.setReturningVoid(true);
		return mt.getReturnType();
	}

	/**
	 * @param call
	 * @param mt
	 */ 
	private void checkMethodType(Call call, MethodType mt) {
		Type[] pts = mt.getParamTypes();
		List<Expression> args = call.getArguments();
		if ( pts.length != args.size() )
			typeError(call.getLine(), "Expected " + pts.length + " arguments. found: " + args.size()
					+ " in call to " + call.getName());
		for ( int i = 0; i < pts.length; i++ ){
			Type argType = (Type) args.get(i).accept(this);
			if ( ! argType.subtypeof(pts[i]) )
				typeError(call.getLine(), "Can't use " + argType.getName() + " instead of " + pts[i].getName()
						+ " as argument no. " + (i+1) + " in call to " + call.getName());
		}
	}

	@Override
	public Object visit(This thisExpression) {
		Symbol thisSym = getCurrentScope().lookup("this");
		if ( thisSym == null ){
			typeError(thisExpression.getLine(), "'this' is undefined here.");
		}
		return thisSym.getType();
	}

	@Override
	public Object visit(NewClass newClass) {
		ClassType classType = TypeTable.getUserType(newClass.getName());
		
		if(classType == null){
			typeError(newClass.getLine(), "unresolved type " + newClass.getName());
		}
		
		return classType;
	}

	@Override
	public Object visit(NewArray newArray) {
		if ( newArray.getSize().accept(this) != TypeTable.intType )
			typeError(newArray.getLine(), "Size of array must be an integer expression");
		Type arrType = null;
		try {
			arrType = TypeTable.astType( newArray.getType() );
		} catch (SemanticError e) {
			typeError(e);
		}
		
		return TypeTable.arrayType(arrType, 1);
	}

	@Override
	public Object visit(Length length) {
		Type t = (Type) length.getArray().accept(this);
		if(t instanceof ArrayType){
			return TypeTable.intType;
		}
		else{
			typeError(length.getLine(), "cant perform .length operation on " + t.getName());
			return null;
		}
		 
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		Type t1 = (Type) binaryOp.getFirstOperand().accept(this);
		Type t2 = (Type) binaryOp.getSecondOperand().accept(this);

		if (binaryOp.getOperator() == BinaryOps.PLUS) {
			if (t1 == TypeTable.intType && t2 == TypeTable.intType) {
				return TypeTable.intType;
			} else if (t1 == TypeTable.stringType && t2 == TypeTable.stringType) {
				binaryOp.setStrCat(true);
				return TypeTable.stringType;
			} else {
				binaryOpError(binaryOp, t1, t2);
			}
		} else {
			if (t1 == TypeTable.intType && t2 == TypeTable.intType) {
				if (binaryOp.getOperator() == BinaryOps.DIVIDE
						|| binaryOp.getOperator() == BinaryOps.MINUS
						|| binaryOp.getOperator() == BinaryOps.MOD
						|| binaryOp.getOperator() == BinaryOps.MULTIPLY) {
					return TypeTable.intType;
				} else {
					binaryOpError(binaryOp, t1, t2);
				}
			} else {
				binaryOpError(binaryOp, t1, t2);
			}
		}

		return null;
	}

	private void binaryOpError(BinaryOp binaryOp, Type t1, Type t2) {
		typeError(binaryOp.getLine(), "cant use "
				+ binaryOp.getOperator().name() + " on " + t1.getName()
				+ " and " + t2.getName());
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		Type t1 = (Type) binaryOp.getFirstOperand().accept(this);
		Type t2 = (Type) binaryOp.getSecondOperand().accept(this);

		switch ( binaryOp.getOperator() ) {
		// integer only comparisons
		case GT:
		case GTE:
		case LT:
		case LTE:
			if ((t1 != TypeTable.intType) || (t2 != TypeTable.intType))
				binaryOpError(binaryOp, t1, t2);
			break;
			
		// boolean only operations
		case LAND:
		case LOR:
			if ((t1 != TypeTable.boolType) || (t2 != TypeTable.boolType))
				binaryOpError(binaryOp, t1, t2);
			break;
		
		// reference or primitive value comparison
		case EQUAL:
		case NEQUAL:
			if ( ! (t1.subtypeof(t2) || t2.subtypeof(t1)) )
				binaryOpError(binaryOp, t1, t2);
			break;
		
		// Should never get here
		default:
			binaryOpError(binaryOp, t1, t2);
			break;
		}
		
		// logical operation always return a boolean.
		return TypeTable.boolType;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		Type t = (Type) unaryOp.getOperand().accept(this);
		if( ( unaryOp.getOperator() == UnaryOps.UMINUS ) &&
			( t == TypeTable.intType                   )    ) {
			return TypeTable.intType;
		}
		else{
			unaryOpError(unaryOp, t);
		}
		return null;
	}

	private void unaryOpError(UnaryOp unaryOp, Type t) {
		typeError(unaryOp.getLine(), "cant use "
				+ unaryOp.getOperator().name() + " on " + t.getName());
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		Type t = (Type) unaryOp.getOperand().accept(this);
		if( ( unaryOp.getOperator() == UnaryOps.LNEG ) &&
			( t == TypeTable.boolType                )    ) {
			return TypeTable.boolType;
		}
		else{
			unaryOpError(unaryOp, t);
		}
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		return TypeTable.literalType(literal.getType());
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		return expressionBlock.getExpression().accept(this);
	}

	@Override
	public Object visit(Library library) {
		// been here...
		return null;
	}

	@Override
	public Object visit(ICVoid icVoid) {
		// been here..
		return null;
	}

	private void typeError(int line, String msg) {
		System.err.println("Semantic error at line " + line + ": " + msg);
		System.exit(1);
	}

	private void typeError(SemanticError e) {
		System.err.println(e.getMessage());
		System.exit(1);
	}

	private void popScope() {
		scopeStack.remove(0);
	}

	private void pushScope(SymbolTable scope) {
		scopeStack.add(0, scope);
	}

	public SymbolTable getCurrentScope() {
		return scopeStack.get(0);
	}

}
