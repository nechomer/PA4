package microLIR;

import java.util.*;

import microLIR.instructions.Immediate;
import microLIR.instructions.Label;
import microLIR.instructions.Operand;
import microLIR.instructions.ParamOpPair;
import microLIR.instructions.Reg;

/** This class represents the current state of the memory during an execution
 * of a LIR program.
 */
public class Environment {
	/** A flag storing the result of the last Compare instruction.
	 */
	public static int compareFlag;

	/** The address of the next instruction to be performed.
	 */
	public static int programCounter;

	/** A stack of local states.
	 */
	public Stack<LocalState> callStack = new Stack<LocalState>();
	
	/** The lowest address of heap-allocated object addresses.
	 */
	public static final int heapStartAddress = 30000;
	
	/** Maps local operands to (integer) values.
	 */
	protected Map<Operand,Integer> localOpToVal = new HashMap<Operand,Integer>();

	/** Maps global operands to (integer) values.
	 * Global operands are things in the data section like strings and labels.
	 */
	protected Map<Operand,Integer> globalOpToVal = new HashMap<Operand,Integer>();
	
	/** Maps addresses to objects (arrays of integers).
	 */
	protected static Map<Integer,int[]> heap = new HashMap<Integer,int[]>();
	
	/** A counter used to determine the addres of the next allocated object.
	 */
	protected int addressCounter = heapStartAddress;

	/** Performs a call to a procedure.
	 * 
	 * @param label The label of the function.
	 * @param args A list of parameter=op pairs.
	 * @param retDestReg The register that will recieve the value returned from the
	 * function.
	 */
	public void procedureCall(Label label, List<ParamOpPair> args, Reg retDestReg) {
		// Store the values of the arguments in the caller's environment.
		Integer[] argValues = new Integer[args.size()];
		for (int i = 0; i < args.size(); ++i) {
			argValues[i] = eval(args.get(i).op);
		}
		
		// Push the local values to the stack.
		LocalState currentState = new LocalState(localOpToVal, programCounter, retDestReg);
		callStack.push(currentState);

		// Create a fresh local environment for the callee.		
		localOpToVal = new HashMap<Operand,Integer>();
		// Update the formal parameters in the callee environment with the values of the argument 
		// in the caller's environment.
		for (int i = 0; i < args.size(); ++i) {
			assignLocal(args.get(i).param, argValues[i]);
		}
		// Jump to the label of the function.
		programCounter = eval(label);
	}
	
	/** Performs a return from a procedure.
	 * 
	 * @param op An operand storing the value returned from the function.
	 */
	public void procedureReturn(Operand op) {
		if (callStack.isEmpty()) {
			throw new RuntimeException("Attempt to return from the main function!");
		}
		// Evaluate the returned value in the callee's enviroenmtn.
		Integer result = null;
		if (op instanceof Reg) { // special treatment for Rdummy
			Reg returnReg = (Reg) op;
			if (returnReg.name.equals("Rdummy")) {
				result = 0;
			}
		}
		if (result == null) {
			result = eval(op);
		}
		// Pop the callee's environment.
		LocalState previousState = callStack.pop();
		// The current environment becomes the caller's environemnt.
		localOpToVal = previousState.localOpToVal;
		// Assigned the value returned from the function to the destination register,
		// in the caller's environemnt.
		assignLocal(previousState.retReg, result);
		// Move to the next instruction after the call.  
		programCounter = previousState.programCounter + 1;
	}
	
	/** In Java |int| = 4 bytes
	 * 
	 * @param size Size of allocatedObject in bytes.  Expected to be a multiple
	 * of 4, since this allocator is specific for PA4 of IC.
	 * @return The address of the allocated object.
	 */
	public int allocateObject(int size) {
		if (size % 4 != 0) {
			throw new RuntimeException("__allocaeObject invoked with size argument " + size +
					" which is not a multiple of 4!");
		}
		int address = addressCounter;
		int sizeInUnits = size / 4;
		addressCounter += sizeInUnits + 4;
		int[] memObject = new int[sizeInUnits];
		heap.put(address, memObject);
		return address;
	}
	
	/** Performs memory access with the given address and return the object
	 * at that address.
	 * 
	 * @param address An address.
	 * @return The object stored in the given address.
	 */
	public int[] getObject(Integer address) {
		if (!(heap.containsKey(address))) {
			throw new RuntimeException("Encountered attempt to access an illegal address: " + address + "!");
		}
		int [] result = heap.get(address);
		return result;
	}
	
	/** Updates a memory object at the given offset with the given value. 
	 * 
	 * @param base The address of the object
	 * @param offset The position inside the object at which to perform the update.
	 * @param newVal The new value to store.
	 */
	public void assignValToObject(Operand base, Operand offset, Operand newVal) {
		if (base instanceof Immediate || base instanceof Label) {
			throw new RuntimeException("Attempt to assign to constant data: " + base + "!");
		}
		Integer baseVal = eval(base);
		if (baseVal < heapStartAddress) {
			throw new RuntimeException("Attempt to assign to global data at address: " + baseVal + "!");
		}
		Integer offsetVal = eval(offset);
		Integer newValVal = eval(newVal);
		int[] memObject = heap.get(baseVal);
		memObject[offsetVal.intValue()] = newValVal.intValue();
	}

	/** Updates the value of the given operand in the current environment.
	 * 
	 * @param op An operand.
	 * @param newVal The new value assigned to the operand.
	 */
	public void assignLocal(Operand op, Integer newVal) {
		if (op instanceof Immediate || op instanceof Label) {
			throw new RuntimeException("Attempt to assign to an immedite or label " + op + "!");
		}
		localOpToVal.put(op, newVal);
	}

	/** Updates the value of a global operand.
	 * 
	 * @param op A global operand.
	 * @param newVal The new value assigned to the operand.
	 */
	public void assignGlobal(Operand op, Integer newVal) {
		if (op instanceof Immediate) {
			throw new RuntimeException("Attempt to assign to an immedite " + op + "!");
		}
		globalOpToVal.put(op, newVal);
	}
	
	/** Returns the value of the operand in the current environment.
	 * 
	 * @param op An operand.
	 * @return The value of the given operand.
	 */
	public Integer eval(Operand op) {
		if (op instanceof Immediate) {
			int val = ((Immediate)op).val;
			return new Integer(val);
		}
		else {
			if (localOpToVal.containsKey(op)) {
				Integer val = localOpToVal.get(op);
				return val;
			}
			else if (globalOpToVal.containsKey(op)) {
				Integer val = globalOpToVal.get(op);
				return val;
			}
			else {
				throw new RuntimeException("Operand " + op + " was not found in environment. " +
				"Possibly used before defined!");
			}
		}
	}
	
	/** The local state of a procedure - the values of the local variables and register
	 * and the value of the program counter.
	 */
	public static class LocalState {
		public Map<Operand,Integer> localOpToVal;
		public int programCounter;
		public Reg retReg;
		
		public LocalState(Map<Operand,Integer> localOpToVal, int programCounter, Reg retReg) {
			this.localOpToVal = localOpToVal;
			this.programCounter = programCounter;
			this.retReg = retReg;
		}
	}
}