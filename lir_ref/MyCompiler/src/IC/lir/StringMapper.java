package IC.lir;

import java.util.LinkedHashMap;
import java.util.Map;

import IC.ICVoid;
import IC.LiteralTypes;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
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

/**
 * A visitor that builds the string headlines for LIR.
 */
public class StringMapper implements Visitor {

	private static Map<String, String> stringMap = new LinkedHashMap<>();
	private int stringCounter = 1;
	
	@Override
	public Object visit(Program program) {
		for ( ICClass c : program.getClasses() )
			c.accept(this);
		return null;
	}

	@Override
	public Object visit(ICClass icClass) {
		for ( Method m : icClass.getMethods() )
			m.accept(this);
		return null;
	}

	@Override
	public Object visit(Field field) {
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		for ( Statement s : method.getStatements() )
			s.accept(this);
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		for ( Statement s : method.getStatements() )
			s.accept(this);
		return null;
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
		assignment.getExp().accept(this);
		assignment.getVariable().accept(this);
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		callStatement.getCall().accept(this);
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		if ( returnStatement.hasValue() )
			returnStatement.getValue().accept(this);
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.getCondition().accept(this);
		ifStatement.getOperation().accept(this);
		if ( ifStatement.hasElse() )
			ifStatement.getElseOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		whileStatement.getCondition().accept(this);
		whileStatement.getOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		for ( Statement s : statementsBlock.getStatements() )
			s.accept(this);
		
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		
		if ( localVariable.hasInitValue() )
			localVariable.getInitValue().accept(this);
		
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		if ( location.isExternal() )
			location.getLocation().accept(this);
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		location.getArray().accept(this);
		location.getIndex().accept(this);
		
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		for ( Expression e : call.getArguments() )
			e.accept(this);
		
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		if(call.isExternal())
			call.getLocation().accept(this);
		
		for ( Expression e : call.getArguments() )
			e.accept(this);
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		newArray.getSize().accept(this);
		
		return null;
	}

	@Override
	public Object visit(Length length) {
		
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		if ( literal.getType() == LiteralTypes.STRING ) {
			if ( !stringMap.containsKey(literal.getValue()) )
				stringMap.put("" + literal.getValue(), "str" + (stringCounter ++));
		}
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		expressionBlock.getExpression().accept(this);
		
		return null;
	}

	@Override
	public Object visit(Library library) {
		
		return null;
	}

	@Override
	public Object visit(ICVoid icVoid) {
		
		return null;
	}

	public static Map<String, String> getStringMap() {
		
		return stringMap;
	}

	public static String stringMapText() {
		String result = runtimeStrings;
		for ( String key : stringMap.keySet() ) {
			result += stringMap.get(key) + ": " + key + "\n";
		}
		return result;
	}

	private static final String runtimeStrings = 
			"re_null_ref_str:    \"Runtime Error: Null pointer dereference!\"\n" +
					"re_array_index_str: \"Runtime Error: Array index out of bounds!\"\n" +
					"re_array_alloc_str: \"Runtime Error: Array allocation with non-positive array size!\"\n"+
					"re_zero_div_str:    \"Runtime Error: Division by zero!\"\n";

}
