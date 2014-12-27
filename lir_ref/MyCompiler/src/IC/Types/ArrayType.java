package IC.Types;

/**
 * Type of arrays in the program.
 *
 * @author Asaf Bruner, Aviv Goll
 */
public class ArrayType extends Type {
	private Type elemType;
	
	public ArrayType(Type eType, int dim) {
		super("");
		String arrName = eType.getName();
		for ( int i = 0; i < dim ; i++ ){
			arrName += "[]";
		}
		setElemType(eType);
		setName(arrName);
	}

	/**
	 * 
	 * @return The type of an element in the array
	 */
	public Type getElemType() {
		return elemType;
	}

	/**
	 * Setter for the array element type
	 * @param elemType the type of an element.
	 */
	public void setElemType(Type elemType) {
		this.elemType = elemType;
	}
}
