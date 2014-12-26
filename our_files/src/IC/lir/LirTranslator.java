package IC.lir;

import java.util.Map;

import IC.BinaryOps;
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
import IC.SymbolTable.Kind;

public class LirTranslator implements Visitor {
	
	private Map<String,String> strMap;
	private String currClass;
	private String globalTestLabel = null;
	private String globalEndLabel = null;
	
	private int currReg = 0;
	private int currLabel = 0;
	
	
	public LirTranslator(Map<String,String> strMap) {
		this.strMap = strMap;
	}

	@Override
	public Object visit(Program program) {
		String lir = "";
		for (ICClass icClass : program.getClasses()) {
            lir += icClass.accept(this);
        }
		return lir;
	}

	@Override
	public Object visit(ICClass icClass) {
		String lir = "";
        for (Method method : icClass.getMethods()) {
            lir += method.accept(this);        
        }
		return lir;
	}

	@Override
	public Object visit(Field field) {
		// Translation is not required 
		return null;
	}
	
	private String visitMethod(Method method) {
		String lir = "";
        for (Statement statement : method.getStatements())
            lir += statement.accept(this); 
        return lir;
	}
	
	private String getMethodLabel(Method method) {
		return "_" + currClass + "_" + method.getName() + ":\n"; 
	}
	
	@Override
	public Object visit(VirtualMethod method) {
		String label = "\n" + getMethodLabel(method);
		String lir = visitMethod(method);
		
		if( method.getType().getName().equals("void"))
				lir += "Return Rdummy\n";
		
		return label +lir;
	}
	
	@Override
	public Object visit(StaticMethod method) {
		String label = "\n" + getMethodLabel(method);
		String lir = visitMethod(method);
		
		if( method.getType().getName().equals("void")) {
			if(method.getName().equals("main")){
				label = "\n_ic_main:\n";
				lir += "Library __exit(0),Rdummy\n";
			}
			else {
				lir += "Return Rdummy\n";
			}
		}
		
		return label +lir;
	}

	@Override
	public Object visit(LibraryMethod method) {
		// Translation is not required 
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		// Translation is not required 
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// Translation is not required 
		return null;
	}

	@Override
	public Object visit(UserType type) {
		// Translation is not required 
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		String lir = "";
		String reg = getNextReg();
		lir += assignment.getAssignment().accept(this);
		currReg++;
		/*assignment.getVariable().setLvalue(true);*/
		lir += assignment.getVariable().accept(this);
		lir += reg;
		currReg--;
		return lir;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}

	@Override
	public Object visit(Return returnStatement) {
		
		if ( returnStatement.hasValue() ) {
			String reg = getNextReg() ;
			String lir = returnStatement.getValue().accept(this);
			lir += "Return " + reg + "\n";
			return lir;
		}
		else { 
			return "Return Rdummy\n";
		}
	}

	@Override
	public Object visit(If ifStatement) {
		String lir = "";
		String condReg = getNextReg();
		String falseLabel = getNextLabel("false_label");
		String endLabel = getNextLabel("end_label");
		
		lir += ifStatement.getCondition().accept(this);
		lir += "Compare 0, " + condReg + "\n";
		lir += "JumpTrue " + falseLabel + "\n";
		lir += ifStatement.getOperation().accept(this);
		
		if ( ifStatement.hasElse() ) {
			lir += "Jump " + endLabel + "\n";
		}
		
		lir += falseLabel + ":\n";
		
		if ( ifStatement.hasElse() ) {
			lir += ifStatement.getElseOperation().accept(this);
			lir += endLabel + ":\n";
		}		
		
		return lir;
	}

	@Override
	public Object visit(While whileStatement) {
		String lir = "";
		String condReg = getNextReg();
		
		String testLabel = getNextLabel("test_label");
		String endLabel = getNextLabel("end_label");
		String oldTestLabel = globalTestLabel;
		String oldEndLabel = globalEndLabel;
		globalTestLabel = testLabel;
		globalEndLabel = endLabel;
		
		lir += testLabel + ":\n";
		lir += whileStatement.getCondition().accept(this);
		lir += "Compare 0, " + condReg + "\n";
		lir += "JumpTrue " + endLabel + "\n";
		lir += whileStatement.getOperation().accept(this);
		lir += "Jump " + testLabel + "\n";
		lir += endLabel + ":\n";
		
		globalTestLabel = oldTestLabel;
		globalEndLabel = oldEndLabel;
		
		return lir;
	}

	@Override
	public Object visit(Break breakStatement) {
		return "Jump " + globalEndLabel + "\n";
	}

	@Override
	public Object visit(Continue continueStatement) {
		return "Jump " + globalTestLabel + "\n";
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		String lir = "";
		
        for (Statement statement : statementsBlock.getStatements())
        	lir += statement.accept(this);
        
		return lir;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		String lir = "";
		if ( localVariable.hasInitValue() ) {
			String reg = getNextReg();
			lir += localVariable.getInitValue().accept(this);
			lir += "Move " + reg + ", " /*+ localVariable.getUID()*/ + "\n";
		} else {
			lir += "Move 0, " /*+ localVariable.getUID()*/ + "\n";
		}
		return lir;
	}

	@Override
	public Object visit(VariableLocation location) {
		if ( ( ! location.isExternal() )  &&
				 ( location.scope.retrieveIdentifier(location.getName()) instanceof Field  )  ){ // Implicit this
				String lir = "";
				String resReg = getNextReg();
				
				// allocate object register and evaluate its value
				if ( ! location.isLvalue() )
					currReg++;
				String objReg = getNextReg();
				lir += "Move this, " + objReg + "\n";
				
				// get field offset
				String className = getFieldClass(location.scope, location.getName());
				int offset = DispatchTableBuilder.getFieldOffset(className, location.getName());
				
				if ( location.isLvalue() ) {
					lir += "MoveField %s, " + objReg + "." + offset + "\n";
				} else {
					lir += "MoveField " + objReg + "." + offset + ", " + resReg + "\n";
					currReg--;
				}
				
				return lir;
			
			} else if ( location.isExternal() ) {
				String lir = "";
				String resReg = getNextReg();
				
				// allocate object register and evaluate its value
				if ( ! location.isLvalue() )
					currReg++;
				String objReg = getNextReg();
				lir += location.getLocation().accept(this);
				lir += nullCheckStr(objReg);
				
				// get field offset
				String className = location.getLocation().getTypeScope().getId();
				int offset = DispatchTableBuilder.getFieldOffset(className, location.getName());
				
				if ( location.isLvalue() ) {
					lir += "MoveField %s, " + objReg + "." + offset + "\n";
				} else {
					lir += "MoveField " + objReg + "." + offset + ", " + resReg + "\n";
					currReg--;
				}
				
				return lir;
			} else {
				if ( location.isLvalue() ) {
					// variable is assignment target
					String lir = "Move %s, " + location.getLirName() + "\n";
					return lir;
				} else {
					// variable is a part of an expression
					String lir = "Move " + location.getLirName() + ", " + curMaxReg() + "\n";
					return lir;
				}
			}
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
	    else {
	      binaryLir += binaryOp.getOperator().getLirOp()  + " " + secReg + ", " + firstReg  + "\n";
	    }
	    
		return null;
	    
	    return binaryLir;
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
	
	private String getNextReg() {
		return "R" + (currReg);
	}
	
	private String getNextLabel(String label) {
		return "_" + label + "_" + (++currLabel);
	}
	
}
