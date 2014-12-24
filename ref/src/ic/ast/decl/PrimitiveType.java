package ic.ast.decl;

import ic.ast.Visitor;

/**
 * Primitive data type AST node.
 * 
 */
public class PrimitiveType extends Type {

	private DataType type;

	public DataType getType() {
		return type;
	}

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new primitive data type node.
	 * 
	 * @param line
	 *            Line number of type declaration.
	 * @param type
	 *            Specific primitive data type.
	 */
	public PrimitiveType(int line, DataType type) {
		super(line);
		this.type = type;
	}

	public String getDisplayName() {
		return type.toString();
	}

	/**
	 * Enumerated type listing the primitive data types.
	 */
	public enum DataType {

		INT("int"), 
		BOOLEAN("boolean"), 
		STRING("string"), 
		VOID("void");
		
		private String description;

		private DataType(String description) {
			this.description = description;
		}

		@Override
		public String toString()
		{
			return description;
		}
	}

}
