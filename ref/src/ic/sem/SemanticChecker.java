package ic.sem;

import java.util.Stack;

import javax.xml.datatype.DatatypeConfigurationException;

import ic.ast.Visitor;
import ic.ast.decl.ClassType;
import ic.ast.decl.DeclClass;
import ic.ast.decl.DeclField;
import ic.ast.decl.DeclLibraryMethod;
import ic.ast.decl.DeclMethod;
import ic.ast.decl.DeclStaticMethod;
import ic.ast.decl.DeclVirtualMethod;
import ic.ast.decl.Parameter;
import ic.ast.decl.PrimitiveType;
import ic.ast.decl.PrimitiveType.DataType;
import ic.ast.decl.Program;
import ic.ast.decl.Type;
import ic.ast.expr.BinaryOp;
import ic.ast.expr.Length;
import ic.ast.expr.Literal;
import ic.ast.expr.NewArray;
import ic.ast.expr.NewInstance;
import ic.ast.expr.RefArrayElement;
import ic.ast.expr.RefField;
import ic.ast.expr.RefVariable;
import ic.ast.expr.StaticCall;
import ic.ast.expr.This;
import ic.ast.expr.UnaryOp;
import ic.ast.expr.VirtualCall;
import ic.ast.stmt.LocalVariable;
import ic.ast.stmt.Statement;
import ic.ast.stmt.StmtAssignment;
import ic.ast.stmt.StmtBlock;
import ic.ast.stmt.StmtBreak;
import ic.ast.stmt.StmtCall;
import ic.ast.stmt.StmtContinue;
import ic.ast.stmt.StmtIf;
import ic.ast.stmt.StmtReturn;
import ic.ast.stmt.StmtWhile;
import ic.sem.ScopeNode.ScopeType;

public class SemanticChecker implements Visitor {

	private boolean static_scope, hasReturn, isLibrary;
	private Stack<Boolean> loop, cond_block;
	private Type currMethodType;

	public SemanticChecker() {
		// TODO Auto-generated constructor stub
		loop = new Stack<>();
		cond_block = new Stack<>();
		this.isLibrary = false;
	}

	@Override
	public Object visit(Program program) {
		int main_cnt = 0;
		for (DeclClass c : program.getClasses()) {
			if (c.getName().equals("Library")) {
				this.isLibrary = true;
			}
			c.accept(this);
			this.isLibrary = false;
			DeclMethod m = c.scope.getMethod("main");
			if (m == null) {
				continue;
			} else {
				if (main_cnt > 0) {
					throw new SemanticException(m,
							" Found more than one main in the file");
				}
				if (!m.getType().getDisplayName().equals("void")) {
					throw new SemanticException(m,
							" Main method should have 'void' return type");
				}
				ic.ast.decl.Type args = m.scope.getParameter("args");
				if (args == null) {
					throw new SemanticException(m,
							" Argument for main method should be 'string[] args'");
				} else if (!args.getDisplayName().equals("string")) {
					throw new SemanticException(m,
							" Argument for main method should be 'string[] args'");
				} else if (args.getArrayDimension() != 1) {
					throw new SemanticException(m,
							" Argument for main method should be 'string[] args'");
				}
				main_cnt++;
			}
		}
		return null;
	}

	@Override
	public Object visit(DeclClass icClass) {
		for (DeclMethod m : icClass.getMethods()) {
			m.accept(this);
		}
		for (DeclField f : icClass.getFields()) {
			f.accept(this);
		}

		return null;
	}

	@Override
	public Object visit(DeclField field) {
		if (field.getType() instanceof PrimitiveType) {
			return null;
		}
		Object c = field.scope.lookupId(field.getType().getDisplayName());
		if (c == null) {
			throw new SemanticException(field, field.getType().getDisplayName()
					+ " not found in type table");
		}
		return null;
	}

	@Override
	public Object visit(DeclVirtualMethod method) {
		this.static_scope = false;
		this.currMethodType = method.getType();
		this.hasReturn = false;
		if (method.getType().getDisplayName().equals("void")) {
			this.hasReturn = true;
		}
		for (Statement s : method.getStatements()) {
			s.accept(this);
		}
		checkParams(method);
		if (!this.hasReturn) {
			throw new SemanticException(method,
					" no return statement in non void method");
		}
		return null;
	}

	@Override
	public Object visit(DeclStaticMethod method) {
		this.static_scope = true;
		this.currMethodType = method.getType();
		for (Statement s : method.getStatements()) {
			s.accept(this);
		}
		checkParams(method);
		return null;
	}

	@Override
	public Object visit(DeclLibraryMethod method) {
		if (!this.isLibrary) {
			throw new SemanticException(method,
					" Library methods should be defined only in Library class");
		}
		this.static_scope = true;
		this.currMethodType = method.getType();
		for (Statement s : method.getStatements()) {
			s.accept(this);
		}
		checkParams(method);
		return null;
	}

	@Override
	public Object visit(Parameter formal) {
		return formal.getType();
	}

	@Override
	public Object visit(PrimitiveType type) {
		return type;
	}

	@Override
	public Object visit(ClassType type) {
		return type;
	}

	@Override
	public Object visit(StmtAssignment assignment) {
		Type a = (Type) assignment.getVariable().accept(this);
		Type b = (Type) assignment.getAssignment().accept(this);

		checkAssignment(a, b, assignment);

		return a;
	}

	@Override
	public Object visit(StmtCall callStatement) {
		return callStatement.getCall().accept(this);

	}

	@Override
	public Object visit(StmtReturn returnStatement) {
		// TODO compare return type and method type
		if (returnStatement.hasValue()) {
			Type t = (Type) returnStatement.getValue().accept(this);
			if (this.currMethodType.getDisplayName().equals(t.getDisplayName())) {
				if (this.loop.empty() || !this.loop.peek().booleanValue())
					this.hasReturn = true;
				return this.currMethodType;
			} else
				throw new SemanticException(returnStatement,
						"Return statement is not of type "
								+ this.currMethodType.getDisplayName());
		} else {
			if (this.currMethodType.getDisplayName().equals("void")) {
				if (this.loop.empty() || !this.loop.peek().booleanValue())
					this.hasReturn = true;
				return this.currMethodType;
			} else
				throw new SemanticException(returnStatement,
						"Return statement is not of type "
								+ this.currMethodType.getDisplayName());

		}
	}

	@Override
	public Object visit(StmtIf ifStatement) {
		// TODO check that every branch has return!
		Type cond = (Type) ifStatement.getCondition().accept(this);
		if (!cond.getDisplayName().equals("boolean")) {
			throw new SemanticException(ifStatement,
					"Non boolean condition for if statement");
		}
		this.cond_block.push(true);
		ifStatement.getOperation().accept(this);
		this.cond_block.pop();
		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().accept(this);
		}
		return null;
	}

	@Override
	public Object visit(StmtWhile whileStatement) {
		Type cond = (Type) whileStatement.getCondition().accept(this);
		if (cond == null) {
			return null;
		}
		if (!cond.getDisplayName().equals("boolean")) {
			throw new SemanticException(whileStatement,
					"Non boolean condition for while statement");
		}
		this.loop.push(true);
		if (whileStatement.getOperation() != null) {
			whileStatement.getOperation().accept(this);
		}
		this.loop.pop();
		return null;
	}

	@Override
	public Object visit(StmtBreak breakStatement) {
		// TODO Auto-generated method stub
		if (this.loop.empty() || !this.loop.peek().booleanValue())
			throw new SemanticException(breakStatement,
					"Use of 'break' statement outside of loop not allowed");
		return null;
	}

	@Override
	public Object visit(StmtContinue continueStatement) {
		// TODO Auto-generated method stub
		if (this.loop.empty() || !this.loop.peek().booleanValue())
			throw new SemanticException(continueStatement,
					"Use of 'continue' statement outside of loop not allowed");
		return null;
	}

	@Override
	public Object visit(StmtBlock statementsBlock) {
		// TODO Auto-generated method stub
		for (Statement s : statementsBlock.getStatements()) {
			s.accept(this);
		}
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		// TODO Auto-generated method stub
		if (localVariable.isInitialized()) {
			Type init = (Type) localVariable.getInitialValue().accept(this);
			if (init == null) {
				return null;
			}
			checkAssignment(localVariable.getType(), init, localVariable);
		}
		return localVariable.getType();
	}

	@Override
	public Object visit(RefVariable location) {
		// TODO Auto-generated method stub
		Object variable = location.scope.lookupId(location.getName());
		if (variable == null) {
			throw new SemanticException(location, location.getName()
					+ " not found in symbol table");
		} else if (variable instanceof DeclField) {
			if (static_scope == true)
				throw new SemanticException(location,
						"Use of field inside static method is not allowed");
			return ((DeclField) variable).getType();
		}

		return (Type) variable;
	}

	@Override
	public Object visit(RefField location) {
		// TODO Auto-generated method stub
		Type ctype = (Type) location.getObject().accept(this);
		DeclClass c = (DeclClass) location.scope.lookupId(ctype
				.getDisplayName());
		DeclField field = c.scope.getField(location.getField());
		if (field == null) {
			throw new SemanticException(location, location.getField()
					+ " doesn't exist in " + c.getName());
		}
		return field.getType();
	}

	@Override
	public Object visit(RefArrayElement location) {
		// TODO Auto-generated method stub
		Object type = location.getIndex().accept(this);
		if (type instanceof String) {
			throw new SemanticException(location, " index should be integer");
		} else if (type instanceof PrimitiveType) {
			PrimitiveType ptype = (PrimitiveType) type;
			if (!ptype.getDisplayName().equals("int")) {
				throw new SemanticException(location,
						" index should be integer");
			}
		}
		Type result = (Type) location.getArray().accept(this);
		return reduceDimByOne(result);
	}

	@Override
	public Object visit(StaticCall call) {

		Object c = call.scope.lookupId(call.getClassName());
		if (!(c instanceof DeclClass)) {
			throw new SemanticException(call, call.getClassName()
					+ " class doesn't exist");
		}
		DeclMethod method = ((DeclClass) c).scope.getMethod(call.getMethod());
		if (method == null) {
			throw new SemanticException(call, "Method " + call.getMethod()
					+ " doesn't exist");
		}
		if (call.getArguments().size() != method.getFormals().size()) {
			throw new SemanticException(call,
					"Invalid number of arguments for "
							+ ((DeclClass) c).getName() + "."
							+ call.getMethod());
		}
		if (method instanceof DeclVirtualMethod)
			throw new SemanticException(call, " called method isn't static");
		for (int i = 0; i < call.getArguments().size(); i++) {
			Type t = (Type) call.getArguments().get(i).accept(this);
			Type formal = method.getFormals().get(i).getType();
			if (!t.getDisplayName().equals(formal.getDisplayName())) {
				if (formal instanceof ClassType && t instanceof ClassType) {
					DeclClass classA = (DeclClass) call.scope.lookupId(t
							.getDisplayName());
					DeclClass classB = (DeclClass) call.scope.lookupId(formal
							.getDisplayName());
					if (!isSubClass(classB.scope, classA.scope)) {
						throw new SemanticException(call, "Method "
								+ ((DeclClass) c).getName() + "."
								+ call.getMethod()
								+ " is not applicable for the arguments given");
					}
				} else {

					throw new SemanticException(call, "Method "
							+ ((DeclClass) c).getName() + "."
							+ call.getMethod()
							+ " is not applicable for the arguments given");
				}
			}
		}
		return method.getType();
	}

	@Override
	public Object visit(VirtualCall call) {
		Object m = null;
		String class_name = null;
		if (call.getObject() == null) {
			m = call.scope.lookupId(call.getMethod());
			class_name = lookupClassScopeName(call.scope);
			if (m == null || !(m instanceof DeclMethod)) {
				throw new SemanticException(call, call.getMethod()
						+ " not found in symbol table");
			}
			if (this.static_scope == true && m instanceof DeclVirtualMethod) {

				throw new SemanticException(call,
						" Calling a local virtual method from inside a static method is not allowed");
			}

		} else {
			Type class_type = (Type) call.getObject().accept(this);
			if (class_type instanceof PrimitiveType) {
				throw new SemanticException(call,
						" Primitive type has no methods");
			}
			Object c = call.scope.lookupId(class_type.getDisplayName());
			m = ((DeclClass) c).scope.lookupId(call.getMethod());
			class_name = ((DeclClass) c).getName();
			if (m == null || !(m instanceof DeclMethod)) {
				throw new SemanticException(call, "Method " + class_name + "."
						+ call.getMethod() + " not found in type table");
			}

		}
		if (call.getArguments().size() != ((DeclMethod) m).getFormals().size()) {
			throw new SemanticException(call,
					"Invalid number of arguments for method "
							+ call.getMethod());
		}

		for (int i = 0; i < call.getArguments().size(); i++) {
			Type t = (Type) call.getArguments().get(i).accept(this);
			Type formal = ((DeclMethod) m).getFormals().get(i).getType();

			if (!formal.getDisplayName().equals(t.getDisplayName())) {
				if (formal instanceof ClassType && t instanceof ClassType) {
					DeclClass classA = (DeclClass) call.scope.lookupId(t
							.getDisplayName());
					DeclClass classB = (DeclClass) call.scope.lookupId(formal
							.getDisplayName());
					if (!isSubClass(classB.scope, classA.scope)) {
						throw new SemanticException(call, "Method "
								+ class_name + "." + call.getMethod()
								+ " is not applicable for the arguments given");
					}
				} else {
					throw new SemanticException(call, "Method " + class_name
							+ "." + call.getMethod()
							+ " is not applicable for the arguments given");
				}
			}
		}
		return ((DeclMethod) m).getType();
	}

	@Override
	public Object visit(This thisExpression) {
		if (this.static_scope == true) {
			throw new SemanticException(thisExpression,
					" Use of 'this' expression inside static method is not allowed");
		}
		return new ClassType(thisExpression.getLine(),
				lookupClassScopeName(thisExpression.scope));
	}

	@Override
	public Object visit(NewInstance newClass) {
		// TODO Auto-generated method stub
		Object c = newClass.scope.lookupId(newClass.getName());
		if (c == null) {
			throw new SemanticException(newClass, newClass.getName()
					+ " not found in symbol table");
		}
		return new ClassType(newClass.getLine(), newClass.getName());
	}

	@Override
	public Object visit(NewArray newArray) {
		// TODO Auto-generated method stub
		Type size = (Type) newArray.getSize().accept(this);
		if (!size.getDisplayName().equals("int")) {
			throw new SemanticException(newArray, " size should be int");
		}
		return newArray.getType();
	}

	@Override
	public Object visit(Length length) {
		// TODO Auto-generated method stub
		length.getArray().accept(this);
		return new PrimitiveType(length.getLine(), DataType.INT);
	}

	@Override
	public Object visit(Literal literal) {
		// TODO Auto-generated method stub
		return new PrimitiveType(literal.getLine(), literal.getType());
	}

	@Override
	public Object visit(UnaryOp unaryOp) {
		// TODO Auto-generated method stub
		Type operand = (Type) unaryOp.getOperand().accept(this);
		switch (operand.getDisplayName()) {
		case "int":
			if (!unaryOp.getOperator().getDescription().equals("negate")
					|| operand.getArrayDimension() != 0) {
				throw new SemanticException(unaryOp, " type mismatch");
			}
			return operand;
		case "boolean":
			if (!unaryOp.getOperator().getDescription().equals("logical not")
					|| operand.getArrayDimension() != 0) {
				throw new SemanticException(unaryOp, " type mismatch");
			}
			return operand;
		default:
			throw new SemanticException(unaryOp, " type mismatch");
		}
	}

	@Override
	public Object visit(BinaryOp binaryOp) {
		Type a = (Type) binaryOp.getFirstOperand().accept(this);
		Type b = (Type) binaryOp.getSecondOperand().accept(this);
		switch (binaryOp.getOperator().getDescription()) {
		case "add":
			if (a.getDisplayName().equals("int")
					&& b.getDisplayName().equals("int")
					&& a.getArrayDimension() == 0 && b.getArrayDimension() == 0) {
				return a;
			} else if (a.getDisplayName().equals("string")
					&& b.getDisplayName().equals("string")
					&& a.getArrayDimension() == 0 && b.getArrayDimension() == 0) {
				return a;
			} else {
				throw new SemanticException(binaryOp, "Type mismatch: "
						+ a.getDisplayName() + " " + binaryOp.getOperator()
						+ " " + b.getDisplayName());
			}
		case "subtract":
		case "multiply":
		case "divide":
		case "modulo":
			if (a.getDisplayName().equals("int")
					&& b.getDisplayName().equals("int")
					&& a.getArrayDimension() == 0 && b.getArrayDimension() == 0) {
				return a;
			} else {
				throw new SemanticException(binaryOp, "Type mismatch: "
						+ a.getDisplayName() + " " + binaryOp.getOperator()
						+ " " + b.getDisplayName());
			}
		case "logical and":
		case "logical or":
			if (a.getDisplayName().equals("boolean")
					&& b.getDisplayName().equals("boolean")
					&& a.getArrayDimension() == 0 && b.getArrayDimension() == 0) {
				return a;
			} else {
				throw new SemanticException(binaryOp, "Type mismatch: "
						+ a.getDisplayName() + " " + binaryOp.getOperator()
						+ " " + b.getDisplayName());
			}
		case "less than":
		case "less than or equal to":
		case "greater than":
		case "greater than or equal to":
			if (a.getDisplayName().equals("int")
					&& b.getDisplayName().equals("int")
					&& a.getArrayDimension() == 0 && b.getArrayDimension() == 0) {
				return new PrimitiveType(binaryOp.getLine(), DataType.BOOLEAN);

			} else {
				throw new SemanticException(binaryOp,
						"Invalid logical binary op (" + binaryOp.getOperator()
								+ ") on non-integer expression");
			}
		case "equals":
		case "not equals":
			if (a.getDisplayName().equals(b.getDisplayName())) {
				return new PrimitiveType(binaryOp.getLine(), DataType.BOOLEAN);
			} else if ((a.getDisplayName().equals("void") && b instanceof ClassType)
					|| (b.getDisplayName().equals("void") && a instanceof ClassType)) {
				return new PrimitiveType(binaryOp.getLine(), DataType.BOOLEAN);
			} else if (b instanceof ClassType && a instanceof ClassType) {
				DeclClass classA = (DeclClass) binaryOp.scope.lookupId(a
						.getDisplayName());
				DeclClass classB = (DeclClass) binaryOp.scope.lookupId(b
						.getDisplayName());
				if (!isSubClass(classA.scope, classB.scope)
						&& !isSubClass(classB.scope, classA.scope)) {
					throw new SemanticException(binaryOp, "Type mismatch: "
							+ a.getDisplayName() + " " + binaryOp.getOperator()
							+ " " + b.getDisplayName());
				}
				return new PrimitiveType(binaryOp.getLine(), DataType.BOOLEAN);
			} else {
				throw new SemanticException(binaryOp, "Type mismatch: "
						+ a.getDisplayName() + " " + binaryOp.getOperator()
						+ " " + b.getDisplayName());
			}
		}
		return null;
	}

	private String lookupClassScopeName(ScopeNode node) {
		while (node.getType() != ScopeType.Class) {
			node = node.getParent();
		}
		return node.getName();

	}

	private boolean isSubClass(ScopeNode classA, ScopeNode subClassA) {
		if (classA.getName().equals(subClassA.getName()))
			return true;
		while (true) {
			ScopeNode eClass = subClassA.getParent();
			if (eClass.getType() == ScopeType.Global) {
				break;
			}
			if (eClass.getName().equals(classA.getName())) {
				return true;
			}
		}
		return false;
	}

	private void checkParams(DeclMethod method) {
		for (Parameter s : method.getFormals()) {
			if (s.getType() instanceof PrimitiveType) {
				continue;
			}
			Object c = method.scope.lookupId(s.getType().getDisplayName());
			if (c == null) {
				throw new SemanticException(method, s.getType()
						.getDisplayName() + " not found in type table");
			}
		}
	}

	private void checkAssignment(Type a, Type b, ic.ast.Node assignment) {
		if (!a.getDisplayName().equals(b.getDisplayName())) {
			if (a instanceof ClassType && b instanceof ClassType) {
				DeclClass classA = (DeclClass) assignment.scope.lookupId(a
						.getDisplayName());
				DeclClass classB = (DeclClass) assignment.scope.lookupId(b
						.getDisplayName());
				if (isSubClass(classA.scope, classB.scope)) {
					return;
				}
			} else if (a instanceof ClassType && b instanceof PrimitiveType) {
				if (b.getDisplayName().equals("void")) {
					return;
				}
			} else if (a instanceof PrimitiveType && a.getArrayDimension() > 0) {
				if (b.getDisplayName().equals("void")) {
					return;
				}
			} else if (a instanceof PrimitiveType) {
				if (a.getDisplayName().equals("string")
						&& b.getDisplayName().equals("void")) {
					return;
				}
			}
			throw new SemanticException(assignment,
					"Invalid assignment of type " + b.getDisplayName()
							+ " to variable of type " + a.getDisplayName());
		}

	}

	public Type reduceDimByOne(Type t) {
		if (t instanceof PrimitiveType) {
			PrimitiveType pt = (PrimitiveType) t;
			PrimitiveType ret = new PrimitiveType(pt.getLine(), pt.getType());
			while (ret.getArrayDimension() < pt.getArrayDimension() - 1) {
				ret.incrementDimension();
			}
			return ret;
		} else {
			ClassType ct = (ClassType) t;
			ClassType ret = new ClassType(t.getLine(), t.getDisplayName());
			while (ret.getArrayDimension() < ct.getArrayDimension() - 1) {
				ret.incrementDimension();
			}
			return ret;
		}

	}

}
