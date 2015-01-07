package IC.SemanticChecks;

import java.util.Stack;

import IC.DataTypes;
import IC.LiteralTypes;
import IC.AST.*;
import IC.SemanticChecks.FrameScope.ScopeType;

public class SemanticChecker implements Visitor {

	// TODO comment on fields
	private boolean static_scope, hasReturn, isLibrary;
	private Stack<Boolean> whileLoop;
	private Type currMethodType;


	public SemanticChecker() {
		whileLoop = new Stack<>();
		this.isLibrary = false;
	}

	/**Visit the program and validate each of its classes, also verify there's one valid main method in the program
	 * 
	 * @param program - The main program node
	 * @return null
	 */
	@Override
	public Object visit(Program program) {
		int main_cnt = 0;
		for (ICClass c : program.getClasses()) {
			if (c.getName().equals("Library")) {
				this.isLibrary = true;
			}
			c.accept(this);
			this.isLibrary = false;
			Method m = c.scope.getMethod("main");
			if (m == null) {
				continue;
			} else {
				if (main_cnt > 0) {
					throw new SemanticException(m,
							" Found more than one main in the file");
				}
				if (!m.getType().getName().equals("void")) {
					throw new SemanticException(m,
							" Main returns a non-void type! ");
				}
				Type args = m.scope.getFormal("args");
				if (args == null) {
					throw new SemanticException(m,
							" Argument for main method isn't a string of arguments!");
				} else if (!args.getName().equals("string")) {
					throw new SemanticException(m,
							" Argument for main method isn't a string of arguments!");
				} else if (args.getDimension() != 1) {
					throw new SemanticException(m,
							" Argument for main method isn't a string of arguments!");
				}
				if (!(m instanceof StaticMethod)) {
					throw new SemanticException(m,
							" Main method is not static! ");
				}
				main_cnt++;
			}
		}
		if (main_cnt == 0) {
			throw new SemanticException(program,
					" No Main method in file!");
		}
		return null;
	}

	/**Verify the class's members - All fields & methods
	 * @param icClass - The class visited
	 * @return - null
	 */
	@Override
	public Object visit(ICClass icClass) {
		for (Method m : icClass.getMethods()) {
			m.accept(this);
		}
		for (Field f : icClass.getFields()) {
			f.accept(this);
		}

		return null;
	}

	/**Verifies the field is of a valid type. If the type isn't primitive and doesn't appear in the field type table
	 * @param field - The visited field
	 * @return null
	 */
	@Override
	public Object visit(Field field) {
		if (field.getType() instanceof PrimitiveType) {
			return null;
		}
		Object c = field.scope.retrieveIdentifier(field.getType().getName());
		if (c == null) {
			throw new SemanticException(field, field.getType().getName()
					+ " not found in type table");
		}
		return null;
	}

	/**Verifies the virtual method - The return value and the arguments are all of valid types, 
	 * the method has a return statement if declared, all statement are visited successfully
	 * @param method - The visited method
	 * @return null
	 */
	@Override
	public Object visit(VirtualMethod method) {
		this.static_scope = false;
		this.currMethodType = method.getType();
		this.hasReturn = false;
		if (method.getType().getName().equals("void")) {
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

	/**Verifies the static method - The return value and the arguments are all of valid types, 
	 * the method has a return statement if declared, all statement are visited successfully
	 * @param method - The visited method
	 * @return null
	 */
	@Override
	public Object visit(StaticMethod method) {
		this.static_scope = true;
		this.currMethodType = method.getType();
		this.hasReturn = false;
		if (method.getType().getName().equals("void")) {
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

	/**
	 * This function is intended for library method checking - Verifies all parameters 
	 * in the method declaration are predefined or primitive. Since library methods return a
	 * primitive/void, the return type isn't checks since it was verified by the parser.
	 * 
	 * @param method - A library method to be checked
	 * @return - null
	 */
	@Override
	public Object visit(LibraryMethod method) {
		if (!this.isLibrary) {
			throw new SemanticException(method,
					" Library methods should be defined only in Library class");
		}
		this.static_scope = true;
		this.currMethodType = method.getType();
		checkParams(method);
		return null;
	}

	/**
	 * @param formal
	 * @return
	 */
	@Override
	public Object visit(Formal formal) {
		return formal.getType();
	}

	/**
	 * @param type
	 * @return
	 */
	@Override
	public Object visit(PrimitiveType type) {
		return type;
	}

	/**
	 * @param type
	 * @return
	 */
	@Override
	public Object visit(UserType type) {
		return type;
	}

	/**Verify the assignment - The LHS & RHS are of valid types, and the assignment rules follow (compareTypes verifies the rules)
	 * @param assignment - The assignment visited
	 * @return the type of the assignment's LHS
	 */
	@Override
	public Object visit(Assignment assignment) {
		Type a = (Type) assignment.getVariable().accept(this);
		Type b = (Type) assignment.getAssignment().accept(this);

		compareTypes(a, b, assignment);

		return a;
	}

	/**
	 * @param callStatement
	 * @return
	 */
	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);

	}

	/**Verifies the return statement - The returned variable is of the same type of the method return declaration,
	 * or follows one of the valid terms
	 * @param returnStatement - The returnStatement visited
	 * @return the returned variable's type
	 */
	@Override
	public Object visit(Return returnStatement) {
		if (returnStatement.hasValue()) {
			Type t = (Type) returnStatement.getValue().accept(this);
			//The return type equals the declaration, or null is returned when string/class is declared - The statement's valid
			if (this.currMethodType.getName().equals(t.getName()) || 
					(t.getName().equals("null") &&
				     !this.currMethodType.getName().equals("int") && !this.currMethodType.getName().equals("boolean") && !this.currMethodType.getName().equals("void"))) {
				this.hasReturn = true;
				return this.currMethodType;
			//The method declared to return void, and still there's a variable written after the return	
			} else if (this.currMethodType.getName().equals("void")) {
				throw new SemanticException(returnStatement,
						"Returning a variable of type " + t.getName() + " while expected void");
			} else
				throw new SemanticException(returnStatement,
						"Return statement is not of type "
								+ this.currMethodType.getName());
		} else {//No return value - Method has to declare returning void
			if (this.currMethodType.getName().equals("void")) {
				this.hasReturn = true;
				return this.currMethodType;
			} else
				throw new SemanticException(returnStatement,
						"Return statement is not of type "
								+ this.currMethodType.getName());

		}
	}

	/**Verifies the if statement - The condition has a boolean typed value, 
	 * and each if scope returns a value in case a return is required by the method
	 * @param ifStatement - The visited if statement
	 * @return null
	 */
	@Override
	public Object visit(If ifStatement) {
		boolean operationHasReturn = false;
		boolean elseOperationHasReturn = false;
		boolean hasReturnBefore;

		hasReturnBefore = hasReturn;
		hasReturn = false;
		Type cond = (Type) ifStatement.getCondition().accept(this);
		if (!cond.getName().equals("boolean")) {
			throw new SemanticException(ifStatement,
					"Non boolean condition for if statement");
		}
		//In case there are many if statements in a raw, we need to know which if scope is handled
		ifStatement.getOperation().accept(this);
		if(hasReturn) operationHasReturn = true;
		hasReturn=false;
		
		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().accept(this);
			if(hasReturn) elseOperationHasReturn = true;
		} else {
			elseOperationHasReturn = true;
		}
		
		
		if (operationHasReturn && elseOperationHasReturn){//all paths from here has return
			hasReturn = true;
		} else {
			hasReturn = hasReturnBefore;
		}
		
		return null;
	}

	/**Verifies the while loop - The condition is a boolean expression, and visits the statement in the while loop
	 * @param whileStatement - The visited while statement
	 * @return null
	 */
	@Override
	public Object visit(While whileStatement) {
		Type cond = (Type) whileStatement.getCondition().accept(this);
		if (cond == null) {
			return null;
		}
		if (!cond.getName().equals("boolean")) {
			throw new SemanticException(whileStatement,
					"Non boolean condition for while statement");
		}
		this.whileLoop.push(true);
		if (whileStatement.getOperation() != null) {
			whileStatement.getOperation().accept(this);
		}
		this.whileLoop.pop();
		return null;
	}

	/**Verifies the break statement belongs to a while loop
	 * @param breakStatement - The visited break statement
	 * @return null
	 */
	@Override
	public Object visit(Break breakStatement) {

		if (this.whileLoop.empty() || !this.whileLoop.peek().booleanValue())
			throw new SemanticException(breakStatement,
					"Use of 'break' statement outside of loop not allowed");
		return null;
	}

	/**Verifies the continue statement belongs to a while loop
	 * @param continueStatement - The visited continueStatement
	 * @return null
	 */
	@Override
	public Object visit(Continue continueStatement) {

		if (this.whileLoop.empty() || !this.whileLoop.peek().booleanValue())
			throw new SemanticException(continueStatement,
					"Use of 'continue' statement outside of loop not allowed");
		return null;
	}

	/**
	 * @param statementsBlock
	 * @return
	 */
	@Override
	public Object visit(StatementsBlock statementsBlock) {

		for (Statement s : statementsBlock.getStatements()) {
			s.accept(this);
		}
		return null;
	}

	/**Verifies the local variable definition - The definition holds a valid type, and the initial value,
	 * if exists, follows the assignment rules
	 * @param localVariable - The visited variable
	 * @return the variable's type
	 */
	@Override
	public Object visit(LocalVariable localVariable) {

		if (localVariable.hasInitValue()) {
			Type init = (Type) localVariable.getInitValue().accept(this);
			if (init == null) {
				return null;
			}
			compareTypes(localVariable.getType(), init, localVariable);
		}
		return localVariable.getType();
	}

	/**Verifies the variable location calling - The location is a valid field of a class, whether it's the same class defined
	 * or an instantiated class in the scope 
	 * @param location - The visited location
	 * @return the variable's type 
	 */
	@Override
	public Object visit(VariableLocation location) {
		//The variable reference is of the type <var>, it's not a class field
		if (location.getLocation() == null){
			Object variable = location.scope.retrieveIdentifier(location.getName());
			if (variable == null) {
				throw new SemanticException(location, location.getName()
						+ " not found in symbol table");
			} else if (variable instanceof Field) {
				//If the variable is defined as a field in a static scope - We get an error
				if (static_scope == true)
					throw new SemanticException(location,
							"Use of field inside static method is not allowed");
				location.setLocationType(((Field) variable).getType());
				return ((Field) variable).getType();
			}
			
			if (variable instanceof VirtualMethod || variable instanceof StaticMethod) {
				throw new SemanticException(location,
						"Inappropriate Use Of Variable Location! Entered Method Name Instead Of Variable");
			}
			location.setLocationType((Type) variable);
			return (Type) variable;
			
		} else {
			//The variable is a class, so it is looked up
			Type ctype = (Type) location.getLocation().accept(this);
			location.setcName(ctype.getName());
			ICClass c = (ICClass) location.scope.retrieveIdentifier(ctype
					.getName());
			if (c == null) {
				throw new SemanticException(location, location.getName()
						+ " is not a reachable field ");
			}
			//Look up the field definition in the class
			Field field = c.scope.getField(location.getName());
			if (field == null) {
				throw new SemanticException(location, location.getName()
						+ " doesn't exist in " + (location.getLocation()));
			}
			location.setLocationType(field.getType());
			return field.getType();
		}
	}

	/**Verifies an array location access - Of the form <array name>[<index>]
	 * @param location - The visited array location
	 * @return the location's type
	 */
	@Override
	public Object visit(ArrayLocation location) {

		Object type = location.getIndex().accept(this);
		if (type instanceof String) {
			throw new SemanticException(location, " index should be integer");
		} else if (type instanceof PrimitiveType) {
			PrimitiveType ptype = (PrimitiveType) type;
			if (!ptype.getName().equals("int")) {
				throw new SemanticException(location,
						" index should be integer");
			}
		}
		//the type returned has to be with the LHS's dimensions-1, 
		//for example: A location to an array of integers with 1 dimension, has 0 dimensions
		Type t = (Type)location.getArray().accept(this);
		  Type temp = null;
		  
		  if (t instanceof PrimitiveType) { 
			   if (t.getName().equals("string"))
			   temp = new PrimitiveType(location.getArray().getLine(), DataTypes.STRING);
			   if (t.getName().equals("int"))
				   temp = new PrimitiveType(location.getArray().getLine(), DataTypes.INT);
			   if (t.getName().equals("boolean"))
				   temp = new PrimitiveType(location.getArray().getLine(), DataTypes.BOOLEAN);
		  } else {
		   temp = new UserType(location.getArray().getLine(), t.getName());
		  }
		  
		  for (int i=0; i<t.getDimension()-1;i++) {
			  temp.incrementDimension();
		  }
		  
		  location.setArrType(temp.getName());
		  return temp;
	}

	/**Verifies a static function call
	 * @param call
	 * @return the method call return type if successful
	 */
	@Override
	public Object visit(StaticCall call) {

		Object c = call.scope.retrieveIdentifier(call.getClassName());
		//Static calls have to be in a form of "<class>."
		if (!(c instanceof ICClass)) {
			throw new SemanticException(call, call.getClassName()
					+ " class doesn't exist");
		}
		//The method called has to be predefined in the class, so it looks for it
		Method method = ((ICClass) c).scope.getMethod(call.getName());
		if (method == null) {
			throw new SemanticException(call, "Method " + call.getName()
					+ " doesn't exist");
		}
		//The static method has to contain the same number of parameters as the call
		if (call.getArguments().size() != method.getFormals().size()) {
			throw new SemanticException(call,
					"Invalid number of arguments for "
							+ ((ICClass) c).getName() + "."
							+ call.getName());
		}
		//The method called has to be static as well..
		if (method instanceof VirtualMethod)
			throw new SemanticException(call, " called method isn't static");
		
		//Iterate over the method calling arguments, 
		//compare the method arguments predefined accordingly
		for (int i = 0; i < call.getArguments().size(); i++) {
			Type t = (Type) call.getArguments().get(i).accept(this);
			Type formal = method.getFormals().get(i).getType();
			if (!t.getName().equals(formal.getName())) {
				//formals aren't of the same type, it can still be valid
				//if the called argument is a sub class of the defined argument 
				if (formal instanceof UserType && t instanceof UserType) {
					ICClass classA = (ICClass) call.scope.retrieveIdentifier(t
							.getName());
					ICClass classB = (ICClass) call.scope.retrieveIdentifier(formal
							.getName());
					if (!isSubClass(classB.scope, classA.scope) || !(t.getDimension()==0 && formal.getDimension()==0)) {
						throw new SemanticException(call, "Method "
								+ ((ICClass) c).getName() + "."
								+ call.getName()
								+ " is not applicable for the arguments given");
					}
				
				//In case this is not a call of an inherited argument,
				//It may have been a null reference call for a user-defined type or string
				} else if (!(((formal instanceof UserType) || formal.getName().equals("string")) &&
						t.getName().equals("null"))) {
					throw new SemanticException(call, "Method "
								+ ((ICClass) c).getName() + "."
								+ call.getName()
								+ " is not applicable for the arguments given");
				}
			} else if (t.getDimension() != formal.getDimension()) {
				//Same types, but if dimensions are different it's still an error 
				throw new SemanticException(call, "Method "
						+ ((ICClass) c).getName() + "."
						+ call.getName()
						+ "Invalid calling of type " + t.getName() + " with " + t.getDimension() +" dimensions"
								+ " when argument defined is of type " + formal.getName()+ " with " + formal.getDimension() +" dimensions");
			}
		}
		call.setMethod(method);
		return method.getType();
	}

	/**Verifies a virtual function call
	 * @param call
	 * @return the method return type if successful
	 */
	@Override
	public Object visit(VirtualCall call) {
		Object m = null;
		String class_name = null;
		//The calling is not of the format "<exp>."
		if (call.getLocation() == null) {
			//Look for the method definition in its class
			m = call.scope.retrieveIdentifier(call.getName());
			//Look for the name of the class the method is defined in
			class_name = lookupClassScopeName(call.scope);
			if (m == null || !(m instanceof Method)) {
				throw new SemanticException(call, call.getName()
						+ " not found in symbol table");
			}
			//can't call a virtual method in a static scope
			if (this.static_scope == true && m instanceof VirtualMethod) {

				throw new SemanticException(call,
						" Calling a local virtual method from inside a static method is not allowed");
			}

		} else {
			//Classify the class ID of the called method (before the ".")
			Type class_type = (Type) call.getLocation().accept(this);
			//Primitive type isn't a class - Can't call a method with "." 
			if (class_type instanceof PrimitiveType) {
				throw new SemanticException(call,
						" Primitive type has no methods");
			}
			//Look for the class's method in the class's table
			Object c = call.scope.retrieveIdentifier(class_type.getName());
			m = ((ICClass) c).scope.retrieveIdentifier(call.getName());
			//Get the instance's class name
			class_name = ((ICClass) c).getName();
			if (m == null || !(m instanceof Method)) {
				throw new SemanticException(call, "Method " + class_name + "."
						+ call.getName() + " not found in type table");
			}

		}
		//Checks the calling and the definition have the same amount of arguments
		if (call.getArguments().size() != ((Method) m).getFormals().size()) {
			throw new SemanticException(call,
					"Invalid number of arguments for method "
							+ call.getName());
		}
		
		//Iterates over the arguments - Compares each of them typewise: See static call doc
		for (int i = 0; i < call.getArguments().size(); i++) {
			Type t = (Type) call.getArguments().get(i).accept(this);
			Type formal = ((Method) m).getFormals().get(i).getType();

			if (!formal.getName().equals(t.getName())) {
				if (formal instanceof UserType && t instanceof UserType) {
					ICClass classA = (ICClass) call.scope.retrieveIdentifier(t
							.getName());
					ICClass classB = (ICClass) call.scope.retrieveIdentifier(formal
							.getName());
					if (!isSubClass(classB.scope, classA.scope) || !(t.getDimension()==0 && formal.getDimension()==0)) {
						throw new SemanticException(call, "Method "
								+ class_name + "." + call.getName()
								+ " is not applicable for the arguments given");
						
					}
				} else if (!(((formal instanceof UserType) || formal.getName().equals("string")) &&
						t.getName().equals("null"))) {
					throw new SemanticException(call, "Method " + class_name
							+ "." + call.getName()
							+ " is not applicable for the arguments given");
				}
			} else if (t.getDimension() != formal.getDimension()) {
				//Same types, but if dimensions are different it's still an error 
				throw new SemanticException(call, "Method "
						+ class_name + "."
						+ call.getName()
						+ "Invalid calling of type " + t.getName() + " with " + t.getDimension() +" dimensions"
								+ " when argument defined is of type " + formal.getName()+ " with " + formal.getDimension() +" dimensions");
			}
		}
		call.setMethod((Method)m);
		return ((Method) m).getType();
	}

	/**
	 * @param thisExpression
	 * @return the type 'this' refers to
	 */
	@Override
	public Object visit(This thisExpression) {
		//check if 'this' was used inside a static scope
		if (this.static_scope == true) {
			throw new SemanticException(thisExpression,
					" Use of 'this' expression inside static method is not allowed");
		}
		return new UserType(thisExpression.getLine(),
				lookupClassScopeName(thisExpression.scope));
	}

	/**Verifies that the class instantiation is valid - Of an existing type
	 * @param newClass - The visited newClass declaration
	 * @return the type of the new 'User Type' object
	 */
	@Override
	public Object visit(NewClass newClass) {

		Object c = newClass.scope.retrieveIdentifier(newClass.getName());
		//check if class was declared before
		if (c == null) {
			throw new SemanticException(newClass, newClass.getName()
					+ " not found in symbol table");
		}
		return new UserType(newClass.getLine(), newClass.getName());
	}

	/**Verifies the new array declaration - The array gets an integer for size when instantiated
	 * @param newArray
	 * @return the type of the new array
	 */
	@Override
	public Object visit(NewArray newArray) {

		Type sizeType = (Type) newArray.getSize().accept(this);
		//a new array should always be created with expression size of type int
		if (!sizeType.getName().equals("int")) {
			throw new SemanticException(newArray, " size should be int");
		}
		return newArray.getType();
	}

	/**Verifies the length expression - <array>.length 
	 * @param length
	 * @return the length type (integer)
	 */
	@Override
	public Object visit(Length length) {

		Type t = (Type) length.getArray().accept(this);
		if (t.getDimension() == 0) {
			throw new SemanticException(length,
					"Calling length on non array type!");
		}
		return new PrimitiveType(length.getLine(), DataTypes.INT);
	}

	/**Verify the literal - Classify its data type
	 * @param literal
	 * @return the literal type, if literal is null returns void type.
	 */
	@Override
	public Object visit(Literal literal) {

		LiteralTypes literalType = literal.getType();
		PrimitiveType ret = null;
		switch(literalType.getDescription()) {
			case ("Boolean literal") : ret = new PrimitiveType(literal.getLine(), DataTypes.BOOLEAN);
				break;
			case ("Integer literal") : ret = new PrimitiveType(literal.getLine(), DataTypes.INT);
				break;
			case ("String literal") : ret = new PrimitiveType(literal.getLine(), DataTypes.STRING);
				break;
			case ("Literal") : ret = new PrimitiveType(literal.getLine(), DataTypes.NULL);
				break;	
		}
		return ret;
	}

	/**Verify the unaryOp is of a valid type - unary subtraction
	 * @param unaryOp
	 * @return the operand Type if its int, otherwise throws an exception
	 */
	@Override
	public Object visit(MathUnaryOp unaryOp) {

		Type operandType = (Type) unaryOp.getOperand().accept(this);
		switch (operandType.getName()) {
		case "int":
			if (!unaryOp.getOperator().getDescription().equals("unary subtraction")) {
				throw new SemanticException(unaryOp, " type mismatch");
			}
			return operandType;
		default:
			throw new SemanticException(unaryOp, " type mismatch");
		}
	}
	
	/**Verifies the unaryOp is of a valid type - logical negative
	 * @param unaryOp
	 * @return the operand Type if its boolean, otherwise throws an exception
	 */
	@Override
	public Object visit(LogicalUnaryOp unaryOp) {

		Type operandType = (Type) unaryOp.getOperand().accept(this);
		switch (operandType.getName()) {
		case "boolean":
			if (!unaryOp.getOperator().getDescription().equals("logical negation")) {
				throw new SemanticException(unaryOp, " type mismatch");
			}
			return operandType;
		default:
			throw new SemanticException(unaryOp, " type mismatch");
		}
	}

	/**Checks the binary operation - two integers, or two strings on addition, two integers on the rest of the ops
	 * @param binaryOp
	 * @return the type of the Mathematical expression which has to be int or string(if its addition).
	 */
	@Override
	public Object visit(MathBinaryOp binaryOp) {
		Type a = (Type) binaryOp.getFirstOperand().accept(this);
		Type b = (Type) binaryOp.getSecondOperand().accept(this);
		switch (binaryOp.getOperator().getDescription()) {
		case "addition":
			if (a.getName().equals("int") //check if both ints
					&& b.getName().equals("int")) {
				return a;
			} else if (a.getName().equals("string") //check if both strings
					&& b.getName().equals("string")) {
				binaryOp.setStrCat(true);
				return a;
			} else {
				throw new SemanticException(binaryOp, "Type mismatch: "
						+ a.getName() + " " + binaryOp.getOperator()
						+ " " + b.getName());
			}
		case "subtraction":
		case "multiplication":
		case "division":
		case "modulo":
			if (a.getName().equals("int")
					&& b.getName().equals("int")) {
				return a;
			} else {
				throw new SemanticException(binaryOp, "Type mismatch: "
						+ a.getName() + " " + binaryOp.getOperator()
						+ " " + b.getName());
			}
		}
		return null;
	}
	
	/**Verifies the logical binary operation - for and/or checks both have a boolean value,
	 * for the </<=/>=/> - two integers, for ==/!= according to the assignment rolls - 
	 * @param binaryOp
	 * @return the type of the Logical Binary expression which has to be boolean.
	 */
	public Object visit(LogicalBinaryOp binaryOp) {
		Type a = (Type) binaryOp.getFirstOperand().accept(this);
		Type b = (Type) binaryOp.getSecondOperand().accept(this);
		switch (binaryOp.getOperator().getDescription()) {
		case "logical and":
		case "logical or":
			if (a.getName().equals("boolean")
					&& b.getName().equals("boolean")) {
				return a;
			} else {
				throw new SemanticException(binaryOp, "Type mismatch: "
						+ a.getName() + " " + binaryOp.getOperator()
						+ " " + b.getName());
			}
		case "less than":
		case "less than or equal to":
		case "greater than":
		case "greater than or equal to":
			if (a.getName().equals("int")
					&& b.getName().equals("int")) {
				return new PrimitiveType(binaryOp.getLine(), DataTypes.BOOLEAN);

			} else {
				throw new SemanticException(binaryOp,
						"Invalid logical binary op (" + binaryOp.getOperator()
								+ ") on non-integer expression");
			}
		case "equality":
		case "inequality":
			//check if both the same
			if (a.getName().equals(b.getName())) { 
				return new PrimitiveType(binaryOp.getLine(), DataTypes.BOOLEAN);
				//check if one is null and the other is an UserType object
			} else if ((a.getName().equals("null") && b instanceof UserType)
					|| (b.getName().equals("null") && a instanceof UserType)) { 
				return new PrimitiveType(binaryOp.getLine(), DataTypes.BOOLEAN);
			} else if (b instanceof UserType && a instanceof UserType) {
				//check if both UserType objects, if one is a subclass of the other
				ICClass classA = (ICClass) binaryOp.scope.retrieveIdentifier(a
						.getName());
				ICClass classB = (ICClass) binaryOp.scope.retrieveIdentifier(b
						.getName());
				if (!isSubClass(classA.scope, classB.scope)
						&& !isSubClass(classB.scope, classA.scope)) {
					throw new SemanticException(binaryOp, "Type mismatch: "
							+ a.getName() + " " + binaryOp.getOperator()
							+ " " + b.getName());
				}
				return new PrimitiveType(binaryOp.getLine(), DataTypes.BOOLEAN);
				//check if one is null and the other is a string
			} else if ((a.getName().equals("null") && b.getName().equals("string"))
					|| (b.getName().equals("null") && a.getName().equals("string"))) { 
				return new PrimitiveType(binaryOp.getLine(), DataTypes.BOOLEAN);
			} else {
				throw new SemanticException(binaryOp, "Type mismatch: "
						+ a.getName() + " " + binaryOp.getOperator()
						+ " " + b.getName());
			}
		}
		return null;
	}
	
	/**
	 * @param expressionBlock
	 * @return the type of the expression within the block
	 */
	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		
		return expressionBlock.getExpression().accept(this);
	}

	/**Looks up the scope tree and checks the class the node belongs to
	 * @param node
	 * @return class name
	 */
	private String lookupClassScopeName(FrameScope node) {
		//search until you see scope of type class, return his name
		while (node.getType() != ScopeType.Class) {
			node = node.getParent();
		}
		return node.getName();

	}

	/**A function intended to check inheritance of one class from another
	 * @param classA
	 * @param subClassA
	 * @return Whether subClassA is indeed a subclass of A
	 */
	private boolean isSubClass(FrameScope classA, FrameScope subClassA) {
		if (classA.getName().equals(subClassA.getName()))
			return true;
		while (true) {
			FrameScope eClass = subClassA.getParent();
			if (eClass.getType() == ScopeType.Global) {
				break;
			}
			if (eClass.getName().equals(classA.getName())) {
				return true;
			}
		}
		return false;
	}

	/**Verifies all method arguments in terms of type checking
	 * @param method - Visited method
	 */
	private void checkParams(Method method) {
		for (Formal f : method.getFormals()) {
			//If it is a primitive type - The checker knows the type and moves on
			if (f.getType() instanceof PrimitiveType) {
				continue;
			}
			//Not a primitive type - Checks whether the type has been declared in table
			Object c = method.scope.retrieveIdentifier(f.getType().getName());
			if (c == null) {
				throw new SemanticException(method, f.getType()
						.getName() + " not found in type table");
			}
		}
	}

	/**
	 * Checks whether the assignment is legitimate in terms of type checking
	 * @param a
	 * @param b
	 * @param assignment
	 */
	private void compareTypes(Type a, Type b, ASTNode assignment) {
		if (!a.getName().equals(b.getName())) {//If a and b are not of the same type
			if (a instanceof UserType && b instanceof UserType) {//Both are user-defined types, in case of an inherited class assignment
				ICClass classA = (ICClass) assignment.scope.retrieveIdentifier(a
						.getName());
				ICClass classB = (ICClass) assignment.scope.retrieveIdentifier(b
						.getName());
				if (isSubClass(classA.scope, classB.scope)) {
					if (a.getDimension() != 0 || b.getDimension() != 0) {
						//Different types, an array assignment is illegal 
						throw new SemanticException(assignment,
								"Invalid array assignment of type " + b.getName() + " with " + a.getDimension() +" dimensions"
										+ " to variable of type " + a.getName()+ " with " + b.getDimension() +" dimensions");
					}
					return;//If a is a subclass of b - Checking is valid
				}
			} else if (a instanceof UserType && b instanceof PrimitiveType) {
				if (b.getName().equals("null")) {//b is null, so it can be assigned to a user-defined type
					return;
				}
			} else if (a instanceof PrimitiveType && a.getDimension() > 0) {
				if (b.getName().equals("null")) {//a is a primitive array - null can be assigned to it
					return;
				}
			} else if (a instanceof PrimitiveType && a.getName().equals("string")) {
				if (b.getName().equals("null")) {//a is a string - null can be assigned to it
					return;
				}
			}
			throw new SemanticException(assignment,
					"Invalid assignment of type " + b.getName()
							+ " to variable of type " + a.getName());
		} else if (a.getDimension() != b.getDimension()) {
		//Same types, but if dimensions are different it's still an error 
		throw new SemanticException(assignment,
				"Invalid assignment of type " + b.getName() + " with " + a.getDimension() +" dimensions"
						+ " to variable of type " + a.getName()+ " with " + b.getDimension() +" dimensions");
		}

	}


}
