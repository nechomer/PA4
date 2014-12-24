package ic.interp;

import ic.ast.Node;
import ic.ast.Visitor;
import ic.ast.decl.*;
import ic.ast.expr.*;
import ic.ast.stmt.*;

import java.util.Iterator;

public class Interpreter implements Visitor {
    State state;
    String[] methodArgs;
    int counter = 0;

    public Interpreter(String[] methodArgs) {
        state = new State();
        this.methodArgs = methodArgs;
    }

    // locate method for interpretation in ast
    @SuppressWarnings({"LoopStatementThatDoesntLoop", "ResultOfMethodCallIgnored"})
    static public Node locateMethod(Node program, String methodName, String[] methodArgs) {
        int index = 0;
        DeclClass classNode;
        DeclMethod methodNode = null;
        Parameter argNode;
        String[] classMethodName = methodName.split("\\.");
        Program programNode = (Program) program;
        if (classMethodName.length != 2)
            throw new RuntimeError("Method Error: Name of method must be class.method");
        Iterator<DeclClass> classIter;
        Iterator<DeclMethod> methodIter;
        Iterator<Parameter> paramIter;
        classIter = programNode.getClasses().iterator();
        while (classIter.hasNext()) {
            classNode = classIter.next();
            if (classNode.getName().equals(classMethodName[0])) {
                methodIter = classNode.getMethods().iterator();
                while (methodIter.hasNext()) {
                    methodNode = methodIter.next();
                    if (methodNode.getName().equals(classMethodName[1]) && methodNode instanceof DeclStaticMethod) {
                        if (methodArgs == null) {
                            if (methodNode.getFormals().size() != 0)
                                throw new RuntimeError("Method Error: Number of given and method arguments are different");
                        } else if (methodArgs.length != methodNode.getFormals().size()) {
                            throw new RuntimeError("Method Error: Number of given and method arguments are different");
                        }
                        paramIter = methodNode.getFormals().iterator();
                        while (paramIter.hasNext()) {
                            argNode = paramIter.next();
                            if (argNode.getType().getDisplayName().equals("int"))
                                Integer.parseInt(methodArgs[index]);
                            else if (argNode.getType().getDisplayName().equals("string")) {
                                index++;
                            } else
                                throw new RuntimeError("Method Error: Method with arguments of type "
                                        + argNode.getType().getDisplayName() + " is not support");
                        }
                        return methodNode;
                    }
                }
            }
        }
        throw new RuntimeError("Method Error: No suitable function to apply");
    }

    @Override
    public Object visit(Program program) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + program.getLine());
    }

    @Override
    public Object visit(DeclClass icClass) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + icClass.getLine());
    }

    @Override
    public Object visit(DeclField field) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + field.getLine());
    }

    @Override
    public Object visit(DeclVirtualMethod method) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + method.getLine());
    }

    @Override
    public Object visit(DeclStaticMethod method) {
        Object methodType = method.getType().accept(this);
        Object value;
        for (Node parameter : method.getFormals())
            parameter.accept(this);
        for (Node statement : method.getStatements()) {
            value = statement.accept(this);
            if (statement instanceof StmtReturn) {
                if (methodType == null)
                    throw new RuntimeError("Interpreter Error: Method of type void have statement return:"
                            + statement.getLine());
                else {
                    throw new ReturnSuccess(value.toString());
                }
            }
        }
        if (methodType != null)
            throw new RuntimeError("Interpreter Error: Method must return value:" + method.getLine());
        return null;
    }

    @Override
    public Object visit(DeclLibraryMethod method) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + method.getLine());
    }

    @Override
    public Object visit(Parameter formal) {
        Object type = formal.getType().accept(this);
        if (type.equals("int")) {
            state.a_stack.peek().addNewVariable(formal.getName(),
                    new Element(Integer.parseInt(methodArgs[counter]), formal.getName(), "int"));
        }
        if (type.equals("string")) {
            state.a_stack.peek().addNewVariable(formal.getName(),
                    new Element(methodArgs[counter], formal.getName(), "string"));
        }
        counter++;
        return null;
    }

    @Override
    public Object visit(PrimitiveType type) {
        switch (type.getDisplayName()) {
            case "int":
                return "int";
            case "string":
                return "string";
            case "boolean":
                return "boolean";
            case "void":
                return null;
            default:
                throw new RuntimeError("Interpreter Error: Type of variable not correct:" + type.getLine());
        }
    }

    @Override
    public Object visit(ClassType type) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + type.getLine());
    }

    @Override
    public Object visit(StmtAssignment assignment) {
        Object name = assignment.getVariable().accept(this);
        Object value = assignment.getAssignment().accept(this);
        if (!state.varExist(((Element) name).getName()))
            throw new RuntimeError("Interpreter Error: Variable is used before it was initialized:"
                    + assignment.getVariable().getLine());
        else if (value instanceof Element[]) {
            for (Element element : (Element[]) value) {
                element.setName(((Element) name).getName());
            }
            ((Element) name).setValue(value);
        } else
            ((Element) name).setValue(((Element) value).getValue());
        return null;
    }

    @Override
    public Object visit(StmtCall callStatement) {
        throw new RuntimeError("Interpreter Error:Syntax not support:" + callStatement.getLine());
    }

    @Override
    public Object visit(StmtReturn returnStatement) {
        return returnStatement.getValue().accept(this);
    }

    @Override
    public Object visit(StmtIf ifStatement) {
        Object condition = ifStatement.getCondition().accept(this);
        if ((Boolean) ((Element) condition).getValue()) {
            state.a_stack.push(new ActivationRecord());
            ifStatement.getOperation().accept(this);
            state.a_stack.pop();
        } else if (ifStatement.hasElse()) {
            state.a_stack.push(new ActivationRecord());
            ifStatement.getElseOperation().accept(this);
            state.a_stack.pop();
        }
        return null;
    }

    @Override
    public Object visit(StmtWhile whileStatement) {
        Object condition = whileStatement.getCondition().accept(this);
        while ((Boolean) ((Element) condition).getValue()) {
            try {
                state.a_stack.push(new ActivationRecord());
                whileStatement.getOperation().accept(this);
                condition = whileStatement.getCondition().accept(this);
                state.a_stack.pop();
            } catch (BreakError e) {
                state.a_stack.pop();
                break;
            } catch (ContinueError ignored) {
            }
        }
        return null;
    }

    @Override
    public Object visit(StmtBreak breakStatement) {
        throw new BreakError();
    }

    @Override
    public Object visit(StmtContinue continueStatement) {
        throw new ContinueError();
    }

    @Override
    public Object visit(StmtBlock statementsBlock) {
        for (Statement statement : statementsBlock.getStatements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(LocalVariable localVariable) {
        Object value = null;
        if (localVariable.isInitialized()) {
            value = localVariable.getInitialValue().accept(this);
        }
        Object type = localVariable.getType().accept(this);
        if (state.a_stack.peek().variables.containsKey(localVariable.getName()))
            throw new RuntimeError("Interpreter Error: Variable with the same name already exist:" +
                    localVariable.getLine());
        if (value != null) {
            if (value instanceof Element[]) {
                for (int i = 0; i < ((Element[]) value).length; i++) {
                    ((Element[]) value)[i].setName(localVariable.getName());
                }
                state.a_stack.peek().addNewVariable(localVariable.getName(),
                        new Element(value, localVariable.getName(), (String) type));
            } else
                state.a_stack.peek().addNewVariable(localVariable.getName(),
                        new Element(((Element) value).getValue(), localVariable.getName(), (String) type));
        } else {
            state.a_stack.peek().addNewVariable(localVariable.getName(),
                    new Element(null, localVariable.getName(), null));
        }
        return null;
    }

    @Override
    public Object visit(RefVariable location) {
        return state.lookup(location.getName());
    }

    @Override
    public Object visit(RefField location) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + location.getLine());
    }

    @Override
    public Object visit(RefArrayElement location) {
        Element array = (Element) location.getArray().accept(this);
        Element index = (Element) location.getIndex().accept(this);
        return ((Object[]) (array.getValue()))[(Integer) index.getValue()];
    }

    @Override
    public Object visit(StaticCall call) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + call.getLine());
    }

    @Override
    public Object visit(VirtualCall call) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + call.getLine());
    }

    @Override
    public Object visit(This thisExpression) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + thisExpression.getLine());
    }

    @Override
    public Object visit(NewInstance newClass) {
        throw new RuntimeError("Interpreter Error: Syntax not support:" + newClass.getLine());
    }

    @Override
    public Object visit(NewArray newArray) {
        Object size = newArray.getSize().accept(this);
        Object type = newArray.getType().accept(this);
        Element[] value = new Element[(Integer) ((Element) size).getValue()];
        for (int i = 0; i < value.length; i++) {
            if (type.equals("string"))
                value[i] = new Element(0, null, "string");
            else if (type.equals("int"))
                value[i] = new Element(0, null, "int");
            else if (type.equals("boolean"))
                value[i] = new Element(0, null, "boolean");
            else
                throw new RuntimeError("Interpreter Error: Incorrect type of array:" + newArray.getLine());
        }
        return value;
    }

    @Override
    public Object visit(Length length) {
        Object array = length.getArray().accept(this);
        return ((Object[]) ((Element) array).getValue()).length;
    }

    @Override
    public Object visit(Literal literal) {
        Object type = literal.getType().toString();
        if (type.equals("int"))
            return new Element(Integer.parseInt(literal.getValue().toString()), "int");
        if (type.equals("string"))
            return new Element(literal.getValue().toString(), "string");
        if (type.equals("boolean"))
            return new Element(Boolean.parseBoolean(literal.getValue().toString()), "boolean");
        if (literal.getValue().equals("null"))
            return null;
        else
            throw new RuntimeError("Interpreter Error: Literal of unknown type:" + literal.getLine());
    }

    @Override
    public Object visit(UnaryOp unaryOp) {
        Element operand = (Element) unaryOp.getOperand().accept(this);
        Object operator = unaryOp.getOperator().toString();
        if (operator.equals("-"))
            return new Element(-(Integer) operand.getValue(), "int");
        if (operator.equals("!"))
            return new Element(!(Boolean) operand.getValue(), "boolean");
        else
            throw new RuntimeError("Interpreter Error: Unary operation not exist:" + unaryOp.getLine());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Object visit(BinaryOp binaryOp) {
        Element firstOperand = (Element) binaryOp.getFirstOperand().accept(this);
        Element secondOperand = (Element) binaryOp.getSecondOperand().accept(this);
        Object operator = binaryOp.getOperator();
        try {
            switch (operator.toString()) {
                case "+":
                    if (firstOperand.getType().equals("string") ||
                            secondOperand.getType().equals("string"))
                        return new Element((String) (firstOperand.getValue()) + (secondOperand.getValue()), "string");
                    if (firstOperand.getType().equals("int") &&
                            secondOperand.getType().equals("int"))
                        return new Element((Integer) (firstOperand.getValue()) + (Integer) (secondOperand.getValue()), "int");
                case "*":
                    return new Element((Integer) (firstOperand.getValue()) * (Integer) (secondOperand.getValue()), "int");
                case "-":
                    return new Element((Integer) (firstOperand.getValue()) - (Integer) (secondOperand.getValue()), "int");
                case "/":
                    if (secondOperand.getValue().equals(0))
                        throw new RuntimeError("Divide by zero");
                    return new Element((Integer) (firstOperand.getValue()) / (Integer) (secondOperand.getValue()), "int");
                case "%":
                    return new Element((Integer) (firstOperand.getValue()) % (Integer) (secondOperand.getValue()), "int");
                case "&&":
                    return new Element((Boolean) (firstOperand.getValue()) && (Boolean) (secondOperand.getValue()), "boolean");
                case "||":
                    return new Element((Boolean) (firstOperand.getValue()) || (Boolean) (secondOperand.getValue()), "boolean");
                case "<":
                    return new Element((Integer) (firstOperand.getValue()) < (Integer) (secondOperand.getValue()), "boolean");
                case "<=":
                    return new Element((Integer) (firstOperand.getValue()) <= (Integer) (secondOperand.getValue()), "boolean");
                case ">":
                    return new Element((Integer) (firstOperand.getValue()) > (Integer) (secondOperand.getValue()), "boolean");
                case ">=":
                    return new Element((Integer) (firstOperand.getValue()) >= (Integer) (secondOperand.getValue()), "boolean");
                case "==":
                    return new Element(firstOperand.getValue() == secondOperand.getValue(), "boolean");
                case "!=":
                    return new Element(firstOperand.getValue() != secondOperand.getValue(), "boolean");
                default:
                    throw new RuntimeError("Interpreter Error: Unknown binary operator:" + binaryOp.getLine());

            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw new RuntimeError(e.getMessage());
        }
    }

    public static class RuntimeError extends Error {
        public RuntimeError(String msg) {
            super(msg);
        }
    }

    public static class ContinueError extends Error {
    }

    public static class BreakError extends Error {
    }

    public static class ReturnSuccess extends Error {
        public ReturnSuccess(String msg) {
            super(msg);
        }
    }
}
