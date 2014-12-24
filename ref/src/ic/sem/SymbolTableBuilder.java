package ic.sem;

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
import ic.ast.decl.Program;
import ic.ast.expr.BinaryOp;
import ic.ast.expr.Expression;
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

public class SymbolTableBuilder implements Visitor {

    private final ScopeNode rootScope;
    private ScopeNode currScope;
    
    public SymbolTableBuilder() {
        rootScope = new ScopeNode(ScopeType.Global, null, null);
        currScope = rootScope;
    }
    
    public ScopeNode getRootScope() {
        return rootScope;
    }
    
    @Override
    public Object visit(Program program) {
        program.scope = currScope;
        
        for (DeclClass icClass : program.getClasses()) {
            icClass.accept(this);
        }
        
        return null;
    }

    @Override
    public Object visit(DeclClass icClass) {
        // Find the proper enclosing (parent) scope for a class
        ScopeNode parentScope = rootScope;
        if (icClass.hasSuperClass()) {
            DeclClass sup = rootScope.getClass(icClass.getSuperClassName()); 
            if (sup == null)
                throw new SemanticException(icClass, "Class " + icClass.getName() + " cannot extend "
                                      + icClass.getSuperClassName() + ", since it's not yet defined");
            parentScope = sup.scope;
        }
        
        // begin a new Class scope for the fields and methods of the class
        currScope = currScope.addScope(ScopeType.Class, icClass.getName(), parentScope);        
        icClass.scope = currScope;

        // Add any class to the class list of the global scope
        rootScope.addClass(icClass);        
        
        for (DeclField field : icClass.getFields()) {
            field.accept(this);
        }
        
        for (DeclMethod method : icClass.getMethods()) {
            method.accept(this);        
        }
        
        // return to the global scope
        currScope = rootScope;
        
        return null;
    }

    @Override
    public Object visit(DeclField field) {
        field.scope = currScope;
        
        currScope.addField(field);
        
        return null;
    }

    private void visitMethod(DeclMethod method, ScopeType type) {
        currScope.addMethod(method);
        
        // create a new scope for the method
        currScope = currScope.addScope(type, method.getName(), currScope);        
        method.scope = currScope;     
        
        for (Parameter formal : method.getFormals()) 
            formal.accept(this);
        
        for (Statement statement : method.getStatements())
            statement.accept(this);        
        
        // return to parent scope
        currScope = currScope.getParent();        
    }
    
    @Override
    public Object visit(DeclVirtualMethod method) {
        visitMethod(method, ScopeType.Method);
        return null;
    }

    @Override
    public Object visit(DeclStaticMethod method) {
        visitMethod(method, ScopeType.Method);
        return null;
    }

    @Override
    public Object visit(DeclLibraryMethod method) {
        visitMethod(method, ScopeType.Method);
        return null;
    }

    @Override
    public Object visit(Parameter formal) {
        formal.scope = currScope;
        
        currScope.addParameter(formal);
                
        return null;
    }

    @Override
    public Object visit(PrimitiveType type) {
        type.scope = currScope;

        return null;
    }

    @Override
    public Object visit(ClassType type) {
        type.scope = currScope;
        
        return null;
    }

    @Override
    public Object visit(StmtAssignment assignment) {
        assignment.scope = currScope;
        
        assignment.getVariable().accept(this);
        assignment.getAssignment().accept(this);
        
        return null;
    }

    @Override
    public Object visit(StmtCall callStatement) {
        callStatement.scope = currScope;
        
        callStatement.getCall().accept(this);

        return null;
    }

    @Override
    public Object visit(StmtReturn returnStatement) {
        returnStatement.scope = currScope;
        if (returnStatement.hasValue())
            returnStatement.getValue().accept(this);
        
        return null;
    }

    @Override
    public Object visit(StmtIf ifStatement) {
        ifStatement.scope = currScope;

        ifStatement.getCondition().accept(this);
        ifStatement.getOperation().accept(this);
        if (ifStatement.hasElse())
            ifStatement.getElseOperation().accept(this);
        
        return null;
    }

    @Override
    public Object visit(StmtWhile whileStatement) {
        whileStatement.scope = currScope;
        
        whileStatement.getCondition().accept(this);
        whileStatement.getOperation().accept(this);        
        
        return null;
    }

    @Override
    public Object visit(StmtBreak breakStatement) {
        breakStatement.scope = currScope;
        return null;
    }

    @Override
    public Object visit(StmtContinue continueStatement) {
        continueStatement.scope = currScope;
        return null;
    }

    @Override
    public Object visit(StmtBlock statementsBlock) {
        String parentName = currScope.getName();
        if (parentName.charAt(0) == '@')
            parentName = parentName.substring(parentName.lastIndexOf('@')+1);

        currScope = currScope.addScope(ScopeType.StatementBlock, "@"+parentName, currScope);
        statementsBlock.scope = currScope;
        
        for (Statement statement : statementsBlock.getStatements())
            statement.accept(this);

        currScope = currScope.getParent();
        return null;
    }

    @Override
    public Object visit(LocalVariable localVariable) {
        localVariable.scope = currScope;
        
        currScope.addLocalVar(localVariable);
        localVariable.getType().accept(this);
        if (localVariable.isInitialized())
            localVariable.getInitialValue().accept(this);
            
        return null;
    }

    @Override
    public Object visit(RefVariable location) {
        location.scope = currScope;
        return null;
    }

    @Override
    public Object visit(RefField location) {
        location.scope = currScope;
        location.getObject().accept(this);
        return null;
    }

    @Override
    public Object visit(RefArrayElement location) {
        location.scope = currScope;
        location.getArray().accept(this);
        location.getIndex().accept(this);        
        return null;
    }

    @Override
    public Object visit(StaticCall call) {
        call.scope = currScope;
        for (Expression argument : call.getArguments())
            argument.accept(this);        
        
        return null;
    }

    @Override
    public Object visit(VirtualCall call) {
        call.scope = currScope;
        
        if (call.hasExplicitObject()) 
            call.getObject().accept(this);

        for (Expression argument : call.getArguments())
            argument.accept(this);        

        return null;
    }

    @Override
    public Object visit(This thisExpression) {
        thisExpression.scope = currScope;
        return null;
    }

    @Override
    public Object visit(NewInstance newClass) {
        newClass.scope = currScope;
        return null;
    }

    @Override
    public Object visit(NewArray newArray) {
        newArray.scope = currScope;
        newArray.getType().accept(this);
        newArray.getSize().accept(this);
        return null;
    }

    @Override
    public Object visit(Length length) {
        length.scope = currScope;
        length.getArray().accept(this);
        return null;
    }

    @Override
    public Object visit(Literal literal) {
        literal.scope = currScope;

        return null;
    }

    @Override
    public Object visit(UnaryOp unaryOp) {
        unaryOp.scope = currScope;
        unaryOp.getOperand().accept(this);        
        return null;
    }

    @Override
    public Object visit(BinaryOp binaryOp) {
        binaryOp.scope = currScope;
        
        binaryOp.getFirstOperand().accept(this);
        binaryOp.getSecondOperand().accept(this);        

        return null;
    }

}
