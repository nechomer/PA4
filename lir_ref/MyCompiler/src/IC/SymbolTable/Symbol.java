package IC.SymbolTable;

import IC.Types.Type;

/**
 * An entry in the symbol table. 
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public class Symbol {
	private String id;
	private Type type;
	private Kind kind;
	private boolean isStatic = false;
	private boolean unresolved = false;
	private int line;
	private String uid;
	
	public Symbol(String id, Type type, Kind kind, int line, String uid) {
		super();
		this.id = id;
		this.setType(type);
		this.setKind(kind);
		this.setLine(line);
		this.setUid(uid);
	}

	/**
	 * @return the symbol ID
	 */
	public String getId() {
		return id;
	}


	/**
	 * @return true if the symbol is yet unresolved
	 */
	public boolean isUnresolved() {
		return unresolved;
	}


	/**
	 * Set the unresolved state
	 * @param unresolved unresolved state (true => the symbol is unresolved)
	 */
	public void setUnresolved(boolean unresolved) {
		this.unresolved = unresolved;
	}


	/**
	 * @return true if the symbol is a static
	 */
	public boolean isStatic() {
		return isStatic;
	}


	/**
	 * Sets the static state
	 * @param isStatic static state (true => the symbol is static)
	 */
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}


	/**
	 * @return the kind of the symbol
	 * @see Kind
	 */
	public Kind getKind() {
		return kind;
	}


	/**
	 * sets the symbol kind
	 * @see Kind
	 * @param kind the symbol kind
	 */
	public void setKind(Kind kind) {
		this.kind = kind;
	}


	/**
	 * @see Type
	 * @return The symbol type
	 */
	public Type getType() {
		return type;
	}


	/**
	 * Sets the symbol type
	 * @param type the symbol type
	 * @see Type
	 */
	public void setType(Type type) {
		this.type = type;
	}


//	public SymbolTable getRelatedSymTab() {
//		return relatedSymTab ;
//	}
//
//
//	public void setRelatedSymTab(SymbolTable relatedSymTab) {
//		this.relatedSymTab = relatedSymTab;
//	}


	/**
	 * @return the line in which the symbol was found.
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Sets the line in which the symbol was found.
	 * @param line the line in which the symbol was found.
	 */
	public void setLine(int line) {
		this.line = line;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
