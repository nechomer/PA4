package ic.ast;

import ic.ast.decl.DeclField;
import ic.ast.decl.Parameter;
import ic.ast.decl.DeclClass;
import ic.ast.decl.DeclLibraryMethod;
import ic.ast.decl.PrimitiveType;
import ic.ast.decl.Program;
import ic.ast.decl.DeclStaticMethod;
import ic.ast.decl.ClassType;
import ic.ast.decl.DeclVirtualMethod;
import ic.ast.expr.RefArrayElement;
import ic.ast.expr.BinaryOp;
import ic.ast.expr.Length;
import ic.ast.expr.Literal;
import ic.ast.expr.NewArray;
import ic.ast.expr.NewInstance;
import ic.ast.expr.RefField;
import ic.ast.expr.StaticCall;
import ic.ast.expr.This;
import ic.ast.expr.UnaryOp;
import ic.ast.expr.RefVariable;
import ic.ast.expr.VirtualCall;
import ic.ast.stmt.StmtAssignment;
import ic.ast.stmt.StmtBreak;
import ic.ast.stmt.StmtCall;
import ic.ast.stmt.StmtContinue;
import ic.ast.stmt.StmtIf;
import ic.ast.stmt.LocalVariable;
import ic.ast.stmt.StmtReturn;
import ic.ast.stmt.StmtBlock;
import ic.ast.stmt.StmtWhile;

/**
 * AST visitor interface. Declares methods for visiting each type of AST node.
 * 
 */
public interface Visitor {

	public Object visit(Program program);

	public Object visit(DeclClass icClass);

	public Object visit(DeclField field);

	public Object visit(DeclVirtualMethod method);

	public Object visit(DeclStaticMethod method);

	public Object visit(DeclLibraryMethod method);

	public Object visit(Parameter formal);

	public Object visit(PrimitiveType type);

	public Object visit(ClassType type);

	public Object visit(StmtAssignment assignment);

	public Object visit(StmtCall callStatement);

	public Object visit(StmtReturn returnStatement);

	public Object visit(StmtIf ifStatement);

	public Object visit(StmtWhile whileStatement);

	public Object visit(StmtBreak breakStatement);

	public Object visit(StmtContinue continueStatement);

	public Object visit(StmtBlock statementsBlock);

	public Object visit(LocalVariable localVariable);

	public Object visit(RefVariable location);

	public Object visit(RefField location);

	public Object visit(RefArrayElement location);

	public Object visit(StaticCall call);

	public Object visit(VirtualCall call);

	public Object visit(This thisExpression);

	public Object visit(NewInstance newClass);

	public Object visit(NewArray newArray);

	public Object visit(Length length);

	public Object visit(Literal literal);

	public Object visit(UnaryOp unaryOp);
	public Object visit(BinaryOp binaryOp);

}
