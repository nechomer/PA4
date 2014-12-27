package IC.SemanticChecks;

import java.util.LinkedList;
import java.util.List;

import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Method;
import IC.AST.Program;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.SymbolTable.Symbol;
import IC.SymbolTable.SymbolTable;
import IC.Types.MethodType;
import IC.Types.TypeTable;

/**
 * Checks that there is a return statement in all foreseeable execution paths
 * (if necessary) 
 * @author Asaf Bruner, Aviv Goll
 */
public class MethodReturnCheck {
	
	/**
	 * Checks for return statement in the program's methods.
	 * @param program The program to check.
	 * @throws SemanticError if a foreseeable execution path is found without a return statement.
	 */
	public void checkMethodsReturn(Program program) throws SemanticError{
		for ( ICClass icClass : program.getClasses() ){
			SymbolTable classST = icClass.getEnclosingScope();
			
			for(Method method : icClass.getMethods()){
				Symbol methodSym = classST.lookin(method.getName());
				MethodType mt = (MethodType) methodSym.getType();
				boolean foundReturn = false;
				
				if ( mt.getReturnType() == TypeTable.voidType )
					continue;
					
				foundReturn = checkForReturn(method.getStatements());
				if(!foundReturn){
					throw new SemanticError(method.getLine(),
							"fail to find return statement in every control path");
				}
			}
		}
	}

	/**
	 * Does the checking over a statement list
	 * @param statements list of statements to go through
	 * @return true if a return is found in every foreseeable execution path
	 */
	private boolean checkForReturn(List<Statement> statements) {
		boolean foundReturn = false;
		for(Statement s : statements){
			if(s instanceof Return)
				foundReturn = true;
			if(s instanceof StatementsBlock)
				foundReturn = foundReturn || checkForReturn(((StatementsBlock)s).getStatements());
			if(s instanceof If){
				If ifStmt = (If)s;
				Statement thenStmt = ifStmt.getOperation();
				List<Statement> thenStmtList = new LinkedList<Statement>();
				thenStmtList.add(thenStmt);
				
				Statement elseStmt = ifStmt.getElseOperation();
				List<Statement> elseStmtList = new LinkedList<Statement>();
				elseStmtList.add(elseStmt);
				
				foundReturn = foundReturn || ((checkForReturn(thenStmtList) && checkForReturn(elseStmtList))) ;
			}
		}
		return foundReturn;
	}
	
}
