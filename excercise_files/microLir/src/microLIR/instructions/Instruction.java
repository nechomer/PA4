package microLIR.instructions;

/** The super class of all LIR instructions.
 */
public abstract class Instruction {
	public abstract void accept(Visitor c);
}