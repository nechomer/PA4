package ic.ast;

import ic.ast.Node;
import ic.ast.Visitor;
import ic.ast.decl.DeclField;
import ic.ast.decl.Parameter;
import ic.ast.decl.DeclClass;
import ic.ast.decl.DeclLibraryMethod;
import ic.ast.decl.DeclMethod;
import ic.ast.decl.PrimitiveType;
import ic.ast.decl.Program;
import ic.ast.decl.DeclStaticMethod;
import ic.ast.decl.ClassType;
import ic.ast.decl.DeclVirtualMethod;
import ic.ast.expr.RefArrayElement;
import ic.ast.expr.BinaryOp;
import ic.ast.expr.Expression;
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
import ic.ast.stmt.Statement;
import ic.ast.stmt.StmtBlock;
import ic.ast.stmt.StmtWhile;

/**
 * Pretty printing visitor - travels along the AST and prints info about each
 * node, in human-readable form.
 */
public class PrettyPrint implements Visitor {

	private int depth = 0; // current distance from the root

	private void indent(StringBuffer output, Node node) {
		output.append("\n");
		String lineNumber = (node == null) ? "" : "[" + node.getLine() + "]";
		output.append(String.format("%5s ", lineNumber));
		for (int i = 0; i < depth; ++i)
			output.append("    ");
		output.append("+-- ");
	}

	public Object visit(Program program) {
		StringBuffer output = new StringBuffer();

		for (DeclClass icClass : program.getClasses())
			output.append(icClass.accept(this));
		return output.toString();
	}

	public Object visit(DeclClass icClass) {
		StringBuffer output = new StringBuffer();
		
		indent(output, icClass);
		output.append("Declaration of class: " + icClass.getName());
		if (icClass.hasSuperClass())
			output.append(", subclass of " + icClass.getSuperClassName());
		depth++;
		for (DeclField field : icClass.getFields())
			output.append(field.accept(this));
		for (DeclMethod method : icClass.getMethods())
			output.append(method.accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(PrimitiveType type) {
		StringBuffer output = new StringBuffer();

		indent(output, type);
		output.append("Primitive data type: ");
		if (type.getArrayDimension() > 0)
			output.append(type.getArrayDimension() + "-dimensional array of ");
		output.append(type.getDisplayName());
		return output.toString();
	}

	public Object visit(ClassType type) {
		StringBuffer output = new StringBuffer();

		indent(output, type);
		output.append("User-defined data type: ");
		if (type.getArrayDimension() > 0)
			output.append(type.getArrayDimension() + "-dimensional array of ");
		output.append(type.getDisplayName());
		return output.toString();
	}

	public Object visit(DeclField field) {
		StringBuffer output = new StringBuffer();

		indent(output, field);
		output.append("Declaration of field: " + field.getName());
		++depth;
		output.append(field.getType().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(DeclLibraryMethod method) {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of library method: " + method.getName());
		depth++;
		output.append(method.getType().accept(this));
		for (Parameter formal : method.getFormals())
			output.append(formal.accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(Parameter formal) {
		StringBuffer output = new StringBuffer();

		indent(output, formal);
		output.append("Parameter: " + formal.getName());
		++depth;
		output.append(formal.getType().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(DeclVirtualMethod method) {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of virtual method: " + method.getName());
		depth++;
		output.append(method.getType().accept(this));
		for (Parameter formal : method.getFormals())
			output.append(formal.accept(this));
		for (Statement statement : method.getStatements())
			output.append(statement.accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(DeclStaticMethod method) {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of static method: " + method.getName());
		depth++;
		output.append(method.getType().accept(this));
		for (Parameter formal : method.getFormals())
			output.append(formal.accept(this));
		for (Statement statement : method.getStatements())
			output.append(statement.accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(StmtAssignment assignment) {
		StringBuffer output = new StringBuffer();

		indent(output, assignment);
		output.append("Assignment statement");
		depth++;
		output.append(assignment.getVariable().accept(this));
		output.append(assignment.getAssignment().accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(StmtCall callStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, callStatement);
		output.append("Method call statement");
		++depth;
		output.append(callStatement.getCall().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(StmtReturn returnStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, returnStatement);
		output.append("Return statement");
		if (returnStatement.hasValue())
			output.append(", with return value");
		if (returnStatement.hasValue()) {
			++depth;
			output.append(returnStatement.getValue().accept(this));
			--depth;
		}
		return output.toString();
	}

	public Object visit(StmtIf ifStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, ifStatement);
		output.append("If statement");
		if (ifStatement.hasElse())
			output.append(", with else branch");
		depth++;
		output.append(ifStatement.getCondition().accept(this));
		output.append(ifStatement.getOperation().accept(this));
		if (ifStatement.hasElse())
			output.append(ifStatement.getElseOperation().accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(StmtWhile whileStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, whileStatement);
		output.append("While statement");
		depth++;
		output.append(whileStatement.getCondition().accept(this));
		output.append(whileStatement.getOperation().accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(StmtBreak breakStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, breakStatement);
		output.append("Break statement");
		return output.toString();
	}

	public Object visit(StmtContinue continueStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, continueStatement);
		output.append("Continue statement");
		return output.toString();
	}

	public Object visit(StmtBlock statementsBlock) {
		StringBuffer output = new StringBuffer();

		indent(output, statementsBlock);
		output.append("Block of statements");
		depth++;
		for (Statement statement : statementsBlock.getStatements())
			output.append(statement.accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(LocalVariable localVariable) {
		StringBuffer output = new StringBuffer();

		indent(output, localVariable);
		output.append("Declaration of local variable: "
				+ localVariable.getName());
		if (localVariable.isInitialized()) {
			output.append(", with initial value");
			++depth;
		}
		++depth;
		output.append(localVariable.getType().accept(this));
		if (localVariable.isInitialized()) {
			output.append(localVariable.getInitialValue().accept(this));
			--depth;
		}
		--depth;
		return output.toString();
	}

	public Object visit(RefVariable location) {
		StringBuffer output = new StringBuffer();

		indent(output, location);
		output.append("Reference to variable: " + location.getName());
		return output.toString();
	}

	public Object visit(RefField location) {
		StringBuffer output = new StringBuffer();

		indent(output, location);
		output.append("Reference to field: " + location.getField() + ", of");
		++depth;
		output.append(location.getObject().accept(this));
		--depth;
		return output.toString();
	}
	
	public Object visit(RefArrayElement location) {
		StringBuffer output = new StringBuffer();

		indent(output, location);
		output.append("Reference to array");
		depth++;
		output.append(location.getArray().accept(this));
		output.append(location.getIndex().accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(StaticCall call) {
		StringBuffer output = new StringBuffer();

		indent(output, call);
		output.append("Call to static method: " + call.getMethod()
				+ ", in class " + call.getClassName());
		depth++;
		for (Expression argument : call.getArguments())
			output.append(argument.accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(VirtualCall call) {
		StringBuffer output = new StringBuffer();

		indent(output, call);
		output.append("Call to virtual method: " + call.getMethod());
		depth++;
		if (call.hasExplicitObject()) {
			output.append(", on object");
			output.append(call.getObject().accept(this));
		}
		else {
			output.append(", of 'this'");
		}
		for (Expression argument : call.getArguments())
			output.append(argument.accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(This thisExpression) {
		StringBuffer output = new StringBuffer();

		indent(output, thisExpression);
		output.append("Reference to 'this' instance");
		return output.toString();
	}

	public Object visit(NewInstance newClass) {
		StringBuffer output = new StringBuffer();

		indent(output, newClass);
		output.append("Instantiation of class: " + newClass.getName());
		return output.toString();
	}

	public Object visit(NewArray newArray) {
		StringBuffer output = new StringBuffer();

		indent(output, newArray);
		output.append("Array allocation");
		depth++;
		output.append(newArray.getType().accept(this));
		output.append(newArray.getSize().accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(Length length) {
		StringBuffer output = new StringBuffer();

		indent(output, length);
		output.append("Reference to array length");
		++depth;
		output.append(length.getArray().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(BinaryOp binaryOp) {
		StringBuffer output = new StringBuffer();

		indent(output, binaryOp);
		output.append(String.format("Binary operation: %s (%s)",
				binaryOp.getOperator().getDescription(), binaryOp.getOperator()));
		depth++;
		output.append(binaryOp.getFirstOperand().accept(this));
		output.append(binaryOp.getSecondOperand().accept(this));
		depth--;
		return output.toString();
	}

	public Object visit(UnaryOp unaryOp) {
		StringBuffer output = new StringBuffer();

		indent(output, unaryOp);
		output.append(String.format("Unary operation: %s (%s)",
				unaryOp.getOperator().getDescription(), unaryOp.getOperator()));
		++depth;
		output.append(unaryOp.getOperand().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(Literal literal) {
		StringBuffer output = new StringBuffer();

		Object value = literal.getValue();
		indent(output, literal);
		output.append(literal.getType() + ": "
				+ (literal.getType() == PrimitiveType.DataType.STRING ? 
						stringRepr((String)value) : value));
		return output.toString();
	}
	
	private String stringRepr(String s) {
		if (s == null) return "null";
		StringBuffer sb = new StringBuffer();
		for (char c : s.toCharArray()) {
			switch (c) {
			case '\t': sb.append("\\t"); break;
			case '\n': sb.append("\\n"); break;
			case '\\': sb.append("\\\\"); break;
			default: sb.append(c);
			}
		}
		return "''" + sb.toString() + "''";
	}

}