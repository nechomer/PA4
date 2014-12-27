package IC.Types;

import IC.AST.ICClass;
import IC.SemanticChecks.SemanticError;
import IC.SymbolTable.SymbolTable;

/**
 * The type of user classes in the program
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public class ClassType extends Type {
	private ICClass classAST;
	private ClassType parent = null;
	private SymbolTable symbolTable = null;
	

	public ClassType(ICClass classObj) throws SemanticError {
		super(classObj.getName());
		this.classAST = classObj;
		if ( classObj.hasSuperClass() ){
			this.parent = TypeTable.getUserType(classObj.getSuperClassName());
			if ( this.parent == null )
				throw new SemanticError(classObj.getLine(), "Undefined: " + classObj.getSuperClassName() + " which stated as super class");
		}
	}

	public ClassType(String className) {
		super(className);
	}

	/**
	 * 
	 * @return the AST class object of this class
	 */
	public ICClass getClassAST() {
		return classAST;
	}

	/**
	 * Sets the AST class object of this class
	 * @param classAST the AST class object of this class
	 */
	public void setClassAST(ICClass classAST) {
		this.classAST = classAST;
	}

	/**
	 * 
	 * @return parent class type of this class type, null if no such class
	 */
	public ClassType getParent() {
		return parent;
	}

	/**
	 * Sets the parent class for this class type
	 * @param parent The parent class for this class type
	 */
	public void setParent(ClassType parent) {
		this.parent = parent;
	}

	
	@Override
	public boolean subtypeof(Type t) {
		if (super.subtypeof(t))
			return true;
		else if ( getParent() != null )
			return getParent().subtypeof(t);
		else
			return false;
	}

	/**
	 * 
	 * @return the class symbol table
	 */
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	/**
	 * Sets the class symbol table
	 * @param symbolTable the class symbol table
	 */
	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

}
