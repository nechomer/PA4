package ic.ast.decl;

import ic.ast.Visitor;

/**
 * User-defined data type AST node.
 * 
 */
public class ClassType extends Type {

	private String name;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new user-defined data type node.
	 * 
	 * @param line
	 *            Line number of type declaration.
	 * @param name
	 *            Name of data type.
	 */
	public ClassType(int line, String name) {
		super(line);
		this.name = name;
	}

	public String getClassName() {
		return name;
	}
	
	public String getDisplayName() {
		return name;
	}

}
