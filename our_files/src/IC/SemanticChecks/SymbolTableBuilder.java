package IC.SemanticChecks;

import IC.AST.*;
import IC.SemanticChecks.FrameScope.ScopeType;

public class SymbolTableBuilder implements Visitor {

    private final FrameScope rootScope;
    
    public FrameScope getCurrScope() {
		return currScope;
	}

	private FrameScope currScope;
    

    public SymbolTableBuilder(String name) {
        rootScope = new FrameScope(ScopeType.Global, name, null);
        currScope = rootScope;
    }
    
    public FrameScope getRootScope() {
        return rootScope;
    }
    
    @Override
    public Object visit(Program program) {
        program.scope = currScope;
        
        for (ICClass icClass : program.getClasses()) {
            icClass.accept(this);
        }
        
        return null;
    }

    @Override
    public Object visit(ICClass icClass) {
        // Find the parent scope for the class
        FrameScope parentScope = rootScope;
        if (icClass.hasSuperClass()) {
        	ICClass sup = rootScope.getClass(icClass.getSuperClassName()); 
            if (sup == null)
                throw new SemanticException(icClass, "Class " + icClass.getName() + " cannot extend "
                                      + icClass.getSuperClassName() + ", since it's not yet been defined");
            parentScope = sup.scope;
        }
        
        // begin a new Class scope for the fields and methods of the icClass
        currScope = currScope.addScope(ScopeType.Class, icClass.getName(), parentScope);        
        icClass.scope = currScope;

        // add class to the global scope class list
        rootScope.addClass(icClass);        
        
        // accept fields and methods
        for (Field field : icClass.getFields()) {
            field.accept(this);
        }
        
        for (Method method : icClass.getMethods()) {
            method.accept(this);        
        }
        
        // return to the global scope
        currScope = rootScope;
        
        return null;
    }

    @Override
    public Object visit(Field field) {
    	
    	// assign scope for the field
        field.scope = currScope;
        
        // add field to current scope
        currScope.addField(field);
        
        return null;
    }

    private void visitMethod(Method method, ScopeType type) {
    	
    	// add method to current scope
        currScope.addMethod(method);
        
        // create a new scope for the method
        currScope = currScope.addScope(type, method.getName(), currScope);
        
        // assign scope for the method
        method.scope = currScope;     
        
        // accept formals and statements
        for (Formal formal : method.getFormals()) 
            formal.accept(this);
        
        for (Statement statement : method.getStatements())
            statement.accept(this);        
        
        // return to parent scope
        currScope = currScope.getParent();        
    }
    
    @Override
    public Object visit(VirtualMethod method) {
        visitMethod(method, ScopeType.Method);
        return null;
    }

    @Override
    public Object visit(StaticMethod method) {
        visitMethod(method, ScopeType.Method);
        return null;
    }

    @Override
    public Object visit(LibraryMethod method) {
        visitMethod(method, ScopeType.Method);
        return null;
    }

    @Override
    public Object visit(Formal formal) {
    	
    	//assign scope for the formal
        formal.scope = currScope;
        
        // add formal to current scope
        currScope.addFormal(formal);
                
        return null;
    }

    @Override
    public Object visit(PrimitiveType type) {
    	
    	//assign scope for the type
        type.scope = currScope;
        return null;
    }

    @Override
    public Object visit(UserType type) {
    	
    	//assign scope for the type
        type.scope = currScope;
        return null;
    }

    @Override
    public Object visit(Assignment assignment) {
    	
    	//assign scope for the assignment
        assignment.scope = currScope;
        
        // accept variable and assignment expression
        assignment.getVariable().accept(this);
        assignment.getAssignment().accept(this);
        
        return null;
    }

    @Override
    public Object visit(CallStatement callStatement) {
    	
    	//assign scope for the call statement
        callStatement.scope = currScope;
        
        // accept the call
        callStatement.getCall().accept(this);

        return null;
    }

    @Override
    public Object visit(Return returnStatement) {
    	
    	//assign scope for the return statement
        returnStatement.scope = currScope;
        
        //accept return value
        if (returnStatement.hasValue())
            returnStatement.getValue().accept(this);
        
        return null;
    }

    @Override
    public Object visit(If ifStatement) {
    	
    	//assign scope for the if statement
        ifStatement.scope = currScope;

        //accept condition operation and 'else' operation of if statement
        ifStatement.getCondition().accept(this);
        ifStatement.getOperation().accept(this);
        if (ifStatement.hasElse())
            ifStatement.getElseOperation().accept(this);
        
        return null;
    }

    @Override
    public Object visit(While whileStatement) {
    	
    	//assign scope for the while statement
        whileStatement.scope = currScope;
        
        //accept condition and operation of while statement
        whileStatement.getCondition().accept(this);
        whileStatement.getOperation().accept(this);        
        
        return null;
    }

    @Override
    public Object visit(Break breakStatement) {
    	
    	//assign scope for the break statement
        breakStatement.scope = currScope;
        return null;
    }

    @Override
    public Object visit(Continue continueStatement) {
    	
    	//assign scope for the continue statement
        continueStatement.scope = currScope;
        return null;
    }

    @Override
    public Object visit(StatementsBlock statementsBlock) {
    	
    	//retrieve parent name of scope
        String parentName = currScope.getName();
        if (parentName.startsWith("statement block in"))
            parentName = parentName.substring(parentName.lastIndexOf("statement block in "));

        //create a new scope for statement block
        currScope = currScope.addScope(ScopeType.StatementBlock, "statement block in "+parentName, currScope);
        statementsBlock.scope = currScope;
        
        //accept statements
        for (Statement statement : statementsBlock.getStatements())
            statement.accept(this);

        //return to scope before statement block acception
        currScope = currScope.getParent();
        return null;
    }

    @Override
    public Object visit(LocalVariable localVariable) {
    	
    	//assign scope for the local var
        localVariable.scope = currScope;
        
        //add variable to current scope
        currScope.addLocalVar(localVariable);
        
        //accept variable type and initial value
        localVariable.getType().accept(this);
        if (localVariable.getInitValue() != null)
            localVariable.getInitValue().accept(this);
            
        return null;
    }

    @Override
    public Object visit(VariableLocation location) {
    	if (location.getLocation() == null) {
    		location.scope = currScope;
            return null;
    	} else {
    		location.scope = currScope;
            location.getLocation().accept(this);
            return null;
    	}
        
    }


    @Override
    public Object visit(ArrayLocation location) {
    	
    	//assign scope for the location
        location.scope = currScope;
        
        //accept array and index
        location.getArray().accept(this);
        location.getIndex().accept(this);        
        return null;
    }

    @Override
    public Object visit(StaticCall call) {
    	
    	//assign scope for the call
        call.scope = currScope;
        
        //accept call arguments
        for (Expression argument : call.getArguments())
            argument.accept(this);        
        
        return null;
    }

    @Override
    public Object visit(VirtualCall call) {
    	
    	//assign scope for the call
        call.scope = currScope;
        
        if (call.isExternal()) 
            call.getLocation().accept(this);

        // accept arguments
        for (Expression argument : call.getArguments())
            argument.accept(this);        

        return null;
    }

    @Override
    public Object visit(This thisExpression) {
    	
    	//assign scope for this expression
        thisExpression.scope = currScope;
        return null;
    }

    @Override
    public Object visit(NewClass newClass) {
    	
    	//assign scope for the new class
        newClass.scope = currScope;
        return null;
    }

    @Override
    public Object visit(NewArray newArray) {
    	
    	//assign scope for the new array
        newArray.scope = currScope;
        
        //accept array type and size
        newArray.getType().accept(this);
        newArray.getSize().accept(this);
        return null;
    }

    @Override
    public Object visit(Length length) {
    	
    	//assign scope for the length
        length.scope = currScope;
        
        //accept the array length refers to
        length.getArray().accept(this);
        return null;
    }

    @Override
    public Object visit(Literal literal) {
    	
    	//assign scope for the literal
        literal.scope = currScope;
        return null;
    }

    @Override
    public Object visit(MathUnaryOp unaryOp) {
    	
    	//assign scope for the unary operation
        unaryOp.scope = currScope;
        
        //accept the operand
        unaryOp.getOperand().accept(this);        
        return null;
    }
    
    public Object visit(LogicalUnaryOp unaryOp) {
    	
    	//assign scope for the unary operation 
        unaryOp.scope = currScope;
        
      //accept the operand
        unaryOp.getOperand().accept(this);        
        return null;
    }

    @Override
    public Object visit(MathBinaryOp binaryOp) {
    	
    	//assign scope for the binary operation
        binaryOp.scope = currScope;
        
        //accept the operands
        binaryOp.getFirstOperand().accept(this);
        binaryOp.getSecondOperand().accept(this);        

        return null;
    }
    
    @Override
    public Object visit(LogicalBinaryOp binaryOp) {
    	
    	//assign scope for the binary operation
        binaryOp.scope = currScope;
        
        //accept the operands
        binaryOp.getFirstOperand().accept(this);
        binaryOp.getSecondOperand().accept(this);        

        return null;
    }

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		
		//assign scope for the expression block
		expressionBlock.scope = currScope;
		
		//accept the expression
		expressionBlock.getExpression().accept(this);
		return null;
	}

}
