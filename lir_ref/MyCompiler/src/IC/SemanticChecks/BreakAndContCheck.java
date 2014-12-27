package IC.SemanticChecks;

import IC.ICVoid;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
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
 * A visitor implementation that checks that a break or continue statements
 * appear only in the body of while loops
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public class BreakAndContCheck implements Visitor{
	

	@Override
	public Object visit(Program program) {
		for (ICClass icClass : program.getClasses())
			icClass.accept(this);
		return null;
	}

	@Override
	public Object visit(ICClass icClass) {
		for (Method method : icClass.getMethods())
			method.accept(this);
		return null;
	}

	@Override
	public Object visit(Field field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		for (Statement statement : method.getStatements())
			statement.accept(this);
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		for (Statement statement : method.getStatements())
			statement.accept(this);
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) {
		for (Statement statement : method.getStatements())
			statement.accept(this);
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(UserType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.getOperation().accept(this);
		if(ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		breakError(breakStatement.getLine(), 
				"break can only appear inside loops");
		return null;
	}



	@Override
	public Object visit(Continue continueStatement) {
		breakError(continueStatement.getLine(), 
				"continue can only appear inside loops");
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		for (Statement statement : statementsBlock.getStatements())
			statement.accept(this);
		
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Length length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Library library) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ICVoid icVoid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void breakError(int line, String string) {
		System.err.println("semantic error at line " + line + ": " + string);
		System.exit(0);
	}
	

}
