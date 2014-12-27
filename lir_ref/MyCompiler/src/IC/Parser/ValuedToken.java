package IC.Parser;

/**
 * Valued Token class
 * This is a class for tokens with meaningful content such as Strings, ID etc.
 * @author Asaf Bruner, Aviv Goll
 */
public class ValuedToken extends Token {

	//String value ;
	
	/** 
     * Class constructor
     * @param line the line in which the token appeared.
     * @param id the ID of the token 
     * @see sym
     * @param value the content of the token 
     */
	public ValuedToken(int line, int id, String value) {
		super(line, id, value);
		//this.value = value;
	}
	
	@Override
	public String toString() {
		return super.toString() + "(" + value + ")";
	}

	public String getValue() {
		return (String) value;
	}
}
