package IC.SemanticChecks;

import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.Program;
import IC.AST.StaticMethod;
import IC.SymbolTable.Kind;
import IC.SymbolTable.Symbol;
import IC.SymbolTable.SymbolTable;
import IC.Types.MethodType;
import IC.Types.Type;
import IC.Types.TypeTable;

/**
 * Checks the program to make sure there's a single main with the correct sugnature:
 * static void main(string[] args)
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public class SingleMainMethod {
	private boolean mainFound = false;

	/**
	 * checks that only a single 'main' method exists
	 * @param program The AST of a program to be checked
	 * @throws SemanticError if more than 1 main is found or no main at all
	 */
	public void checkForSingleMain(Program program) throws SemanticError {

		for ( ICClass icClass : program.getClasses() ) {
			SymbolTable st = icClass.getEnclosingScope();
			Symbol mainSym = st.lookin("main");
			if ( mainSym != null ) {
				if ( checkMain(mainSym) ){
					if ( mainFound  )
						throw new SemanticError(mainSym.getLine(),
								"program can only contain one 'main' method");
					else {
						mainFound = true;
						for ( Method m : icClass.getMethods() ) { 
							if ( m.getName().equals("main"))
								m.setProgramMain(true);
						}
					}
				}
			}
		}
		if ( ! mainFound ){
			throw new SemanticError(program.getLine(),
					"program must have a 'static void main(string[])' method");
		}
	}

	/**
	 * Checks a main symbol to see if it is a main program method
	 * @param mainSym The symbol to be checked
	 * @return true if the symbol represent a "static void main(string[])" method
	 */
	private boolean checkMain(Symbol mainSym) {
		if ( mainSym.getKind() != Kind.METHOD )
			return false;
		if ( mainSym.isStatic() == false )
			return false;
		MethodType mt = (MethodType) mainSym.getType();
		if ( mt.getReturnType() != TypeTable.voidType )
			return false;
		Type[] paramTypes = mt.getParamTypes();
		if ( paramTypes.length != 1 )
			return false;
		if ( paramTypes[0] != TypeTable.arrayType(TypeTable.stringType, 1))
			return false;
		return true;
	}

}
