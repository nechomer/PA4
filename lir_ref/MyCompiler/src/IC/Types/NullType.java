package IC.Types;

/**
 * The type of null in the program
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public class NullType extends Type {
	public NullType() {
		super("null");
	}
	
	@Override
	public boolean subtypeof(Type t) {
		
		if( (t == TypeTable.intType) ||
				(t == TypeTable.boolType ) )
			return false;
				
		return true;
	}
}
