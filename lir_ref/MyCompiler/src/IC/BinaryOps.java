package IC;

/**
 * Enum of the IC language's binary operators.
 * 
 * @author Tovi Almozlino
 */
public enum BinaryOps {

	PLUS("+", "addition", "Add"),
	MINUS("-", "subtraction", "Sub"),
	MULTIPLY("*", "multiplication", "Mul"),
	DIVIDE("/", "division", "Div"),
	MOD("%", "modulo", "Mod"),
	LAND("&&", "logical and", "And"),
	LOR("||", "logical or", "Or"),
	LT("<", "less than", "JumpLE"),
	LTE("<=", "less than or equal to", "JumpL"),
	GT(">", "greater than", "JumpGE"),
	GTE(">=", "greater than or equal to", "JumpG"),
	EQUAL("==", "equality", "JumpFalse"),
	NEQUAL("!=", "inequality", "JumpTrue");
	
	private String operator;
	
	private String description;
	
	private String lirOp;

	private BinaryOps(String operator, String description, String lirOp) {
		this.operator = operator;
		this.description = description;
		this.lirOp = lirOp;
	}

	/**
	 * Returns a string representation of the operator.
	 * 
	 * @return The string representation.
	 */
	public String getOperatorString() {
		return operator;
	}
	
	/**
	 * Returns a description of the operator.
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	public String getLirOp() {
		return lirOp;
	}
}