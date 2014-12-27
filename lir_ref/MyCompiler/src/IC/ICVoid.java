package IC;

import IC.AST.Type;
import IC.AST.Visitor;


/**
 * ICVoid class
 * ICVoid is a special Type and represents the "void" return type.
 * Allows us to differentiate it from the other variable types.
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public class ICVoid extends Type {

	/**
	 * A simple constructor
	 * @param line -the line number
	 */
	public ICVoid(int line) {
		super(line);
	}

	@Override
	public String getName() {
		return "void";
	}

	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
		
	}

}
