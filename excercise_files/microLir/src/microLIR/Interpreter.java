package microLIR;

import java.util.*;

import microLIR.instructions.*;

/** This class is the heart of the application.
 * It is responsible for executing a LIR program,
 * and also allocating literal strings and dispatch vectors.
 */
public class Interpreter implements Visitor {
	public static int verbose = 0;
	
	/** If true then interpretations of Boolean operations
	 * is according to the AT&T syntax assembly.
	 */
	public boolean atntSemantics;
	
	/** The list of instructions in the program stored in an array for
	 * convenient access by address.
	 */
	protected final Instruction[] instructions;
	
	// We maintain a mapping between label names and their address (position)
	// in the program (in the instructions array).
	protected Map<Label,Integer> labelToPosition = new HashMap<Label,Integer>();
	protected Map<Integer,Label> positionToLabel = new HashMap<Integer,Label>();

	/** The current state of the execution.
	 */
	protected Environment env = new Environment();
	
	/** Represents the <code>this</code> variable, which is the receiver
	 * in virtual function calls.
	 */
	protected Memory thisVar = new Memory("this");
	
	protected static final Label icMainLabel = new Label("_ic_main");
	
	/** Reads a LIR program and performs pre-processing, including
	 * alloaction of literal strings and dispatch vectors.
	 * 
	 * @param program A LIR program.
	 */
	public Interpreter(Program program) {
		// Store the list of instructions in an array.
		this.instructions = new Instruction[program.instructions.size()];
		program.instructions.toArray(this.instructions);
		
		// Associate a position with every label.
		for (int i = 0; i < this.instructions.length; ++i) {
			Instruction instr = this.instructions[i];
			if (instr instanceof LabelInstr) {
				LabelInstr labelInstr = (LabelInstr) instr;
				labelToPosition.put(labelInstr.label, i);
				positionToLabel.put(i, labelInstr.label);
				env.assignGlobal(labelInstr.label, i);
			}
		}
		
		// Allocate space for string literals and dispatch vectors
		for (Object data : program.data) {
			if (data instanceof StringLiteral) {
				StringLiteral stringLiteral = (StringLiteral) data;
				int objectSize = stringLiteral.value.length() * 4;
				int address = env.allocateObject(objectSize);
				// Now fill the object with string characters.
				int[] object = env.getObject(address);
				for (int i = 0; i < object.length; ++i) {
					object[i] = stringLiteral.value.charAt(i);
				}
				env.assignGlobal(new Memory(stringLiteral.var), address);
			}
			else if (data instanceof DispatchVector) {
				DispatchVector dv = (DispatchVector) data;
				if (verbose > 1) {
					System.out.println("Processing DispatchVector " + dv.name);
				}
				int objectSize = dv.labels.size() * 4;
				int address = env.allocateObject(objectSize);
				// Now fill the object with label addresses.
				int[] object = env.getObject(address);
				for (int i = 0; i < object.length; ++i) {
					String label = dv.labels.get(i);
					if (verbose > 1) {
						System.out.println("Processing DispatchVector label " + label);
					}
					int labelAddress = env.eval(new Label(label));
					object[i] = labelAddress;
				}
				env.assignGlobal(dv.name, address);
			}
			else {
				throw new RuntimeException("Encountered unknown data type:" + data.getClass() + ", " + data);
			}
		}
		
		// Find the label _ic_main
		Integer icMainPosition = labelToPosition.get(icMainLabel);
		if (icMainPosition == null) {
			throw new RuntimeException("Program does not contain a label _ic_main!");
		}
		Environment.programCounter = icMainPosition;
	}
	
	public void printStatistics() {
		// Create the set of distinct registers.
		Set<Reg> differentRegs = new HashSet<Reg>();
		InstructionInfo instrInfo = new InstructionInfo();
		for (Instruction instr : instructions) {
			instrInfo.getInfo(instr);
			differentRegs.addAll(instrInfo.registers);
		}
		
		// Print the statistics		
		String header = "Program statistics for " + Main.file + ":";
		System.out.println(header);
		for (int i = 0; i < header.length(); ++i)
			System.out.print("=");
		System.out.println();		
		System.out.println("#instructions:          " + instructions.length);
		System.out.println("#labels:                " + labelToPosition.size());
		System.out.println("#registers (total):     " + Reg.getNumberOfRegisters());
		System.out.println("#registers (different): " + differentRegs.size());
		System.out.println("#variables:             " + Memory.getNumberOfVars());
		System.out.println("#strings:               " + StringLiteral.getNumberOfStringLiterals());
		System.out.println("#dispatch tables:       " + DispatchVector.getNumberOfDispatchTables());
	}
	
	/** Starts executing the program from _ic_main
	 */
	public void execute() {
		while (Environment.programCounter < instructions.length) {
			Instruction instr = instructions[Environment.programCounter];
			if (verbose > 0)
				System.out.println("Interpreting " + instr);
			instr.accept(this);
		}
	}	
	
	public void visit(MoveInstr instr) {
		Integer srcVal = env.eval(instr.src);
		env.assignLocal(instr.dst, srcVal);
		if (verbose > 1)
			System.out.println("Assigned " + srcVal + " to " + instr.dst);
		Environment.programCounter++;
	}	
	
	public void visit(CompareInstr instr) {
		Integer srcVal = env.eval(instr.src);
		Integer dstVal = env.eval(instr.dst);
		Environment.compareFlag = dstVal - srcVal;
		if (verbose > 1)
			System.out.println("Assigned " + Environment.compareFlag + " to compare flag");
		Environment.programCounter++;
	}		
	
	public void visit(BinOpInstr instr) {
		Integer srcVal = env.eval(instr.src);
		Integer dstVal = env.eval(instr.dst);
		int result;
		switch (instr.op) {
		case ADD:
			result = srcVal + dstVal;
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case SUB:
			if (atntSemantics) {
				result = srcVal - dstVal;
			}
			else {
				result = dstVal - srcVal;
			}
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case MUL:
			result = dstVal * srcVal;
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case DIV:
			if (srcVal == 0)
				throw new RuntimeException("Encountered attempt to divide by zero in " + instr);
			if (atntSemantics) {
				result = srcVal / dstVal;
			}
			else {
				result = dstVal / srcVal;
			}
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case MOD:
			if (srcVal == 0)
				throw new RuntimeException("Encountered attempt to divide by zero in " + instr);
			if (atntSemantics) {
				result = srcVal % dstVal;
			}
			else {
				result = dstVal % srcVal;
			}
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case AND:
			result = dstVal & srcVal;
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case OR:
			result = dstVal | srcVal;
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case XOR:
			result = dstVal ^ srcVal;
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		default:
			throw new RuntimeException("Encountered unknown operator type in " + instr);
		}
		Environment.programCounter++;
	}	
	
	public void visit(UnaryOpInstr instr) {
		Integer dstVal = env.eval(instr.dst);
		int result;
		switch (instr.op) {
		case INC:
			result = dstVal + 1;
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case DEC:
			result = dstVal - 1;
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case NEG:
			result = -dstVal;
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		case NOT:
			result = ~dstVal;
			env.assignLocal(instr.dst, result);
			if (verbose > 1)
				System.out.println("Assigned " + result + " to " + instr.dst);
			break;
		default:
			throw new RuntimeException("Encountered unknown operator type in " + instr);
		}		
		Environment.programCounter++;
	}	
	
	public void visit(LabelInstr instr) {
		Environment.programCounter++;
	}	
	
	public void visit(MoveArrayInstr instr) {
		if (instr.isLoad) {
			Integer address = env.eval(instr.base);
			int[] object = env.getObject(address);
			int offset = env.eval(instr.offset);
			if (offset < 0)
				throw new RuntimeException("Attempt to access array with negative offset (" + offset + ") in " + instr);
			if (offset >= object.length)
				throw new RuntimeException("Attempt to access array with out-of-bounds offset (" + offset + ") in " + instr);
			int value = object[offset];
			env.assignLocal(instr.mem, value);
		}
		else {
			Integer address = env.eval(instr.base);
			int[] object = env.getObject(address);
			int offset = env.eval(instr.offset);
			int value = env.eval(instr.mem);
			if (offset < 0)
				throw new RuntimeException("Attempt to access array with negative offset (" + offset + ") in " + instr);
			if (offset >= object.length)
				throw new RuntimeException("Attempt to access array with out-of-bounds offset (" + offset + ") in " + instr);
			object[offset] = value;			
		}
		Environment.programCounter++;
	}
	
	public void visit(ArrayLengthInstr instr) {
		Integer address = env.eval(instr.arr);
		int[] object = env.getObject(address);
		env.assignLocal(instr.dst, object.length);
		Environment.programCounter++;
	}
	
	public void visit(MoveFieldInstr instr) {
		if (instr.isLoad) {
			Integer address = env.eval(instr.base);
			int[] object = env.getObject(address);
			int offset = env.eval(instr.offset);
			if (offset < 0)
				throw new RuntimeException("Attempt to access object field with negative offset (" + offset + ") in " + instr);
			if (offset >= object.length)
				throw new RuntimeException("Attempt to access object field with out-of-bounds offset (" + offset + ") in " + instr);
			int value = object[offset];
			env.assignLocal(instr.mem, value);
		}
		else {
			Integer address = env.eval(instr.base);
			int[] object = env.getObject(address);
			int offset = env.eval(instr.offset);
			int value = env.eval(instr.mem);
			if (offset < 0)
				throw new RuntimeException("Attempt to access object field with negative offset (" + offset + ") in " + instr);
			if (offset >= object.length)
				throw new RuntimeException("Attempt to access object field with out-of-bounds offset (" + offset + ") in " + instr);
			object[offset] = value;			
		}
		Environment.programCounter++;
	}	
	
	public void visit(JumpInstr instr) {
		if (!labelToPosition.containsKey(instr.label)) {
			throw new RuntimeException("Unable to find label specified by " + instr);
		}
		Environment.programCounter = labelToPosition.get(instr.label);
	}	
	
	public void visit(CondJumpInstr instr) {
		if (!labelToPosition.containsKey(instr.label)) {
			throw new RuntimeException("Unable to find label specified by " + instr);
		}
		//int jumpLabel = labelToPosition.get(instr.label);
		int jumpLabel = env.eval(instr.label);
		if (jumpLabel < 0)
			throw new RuntimeException("Encountered a jump to a negative address " + jumpLabel + " in " + instr);
		if (jumpLabel >= instructions.length)
			throw new RuntimeException("Encountered a jump to an illegal address " + jumpLabel + " in " + instr);
		boolean answer;
		switch (instr.cond) {
		case True:
			answer = Environment.compareFlag == 0;
			break;
		case False:
			answer = Environment.compareFlag != 0;
			break;
		case G:
			answer = Environment.compareFlag > 0;
			break;
		case GE:
			answer = Environment.compareFlag >= 0;
			break;
		case L:
			answer = Environment.compareFlag < 0;
			break;
		case LE:
			answer = Environment.compareFlag <= 0;
			break;
		default:
			throw new RuntimeException("Encountered unknown condition " + instr + "!");
		}
		if (verbose > 1)
			System.out.println(answer ? "Condition holds" : "Condition fails");
		if (answer)
			Environment.programCounter = jumpLabel;
		else
			Environment.programCounter++;
	}	
	
	public void visit(StaticCall instr) {
		env.procedureCall(instr.func, instr.args, instr.dst);
	}	
	
	public void visit(VirtualCall instr) {
		// Retrieve the dispatch table address from the object.
		Integer objectAddress = env.eval(instr.obj);
		int[] object = env.getObject(objectAddress);
		if (object.length < 1) {
			throw new RuntimeException("Found an illegal object (no fields) at address " + objectAddress + " in " + instr + "!");
		}
		int dvptr = object[0];
		int[] dispatchTable = env.getObject(dvptr);
		// Now do a lookup for the method with the specified offset.
		int offset = env.eval(instr.func);
		if (offset < 0 || offset >= dispatchTable.length) {
			throw new RuntimeException("Encountered illegal dispatch offset: " + offset + " in " + instr + "!");
		}
		int methodAddress = dispatchTable[offset];
		// Convert the address to a string label and call the method.
		Label methodLabel = positionToLabel.get(methodAddress);
		env.procedureCall(methodLabel, instr.args, instr.dst);
		// Update the this variable.
		env.assignLocal(thisVar, objectAddress);
	}	
	
	public void visit(LibraryCall instr) {
		Library.execute(instr, env);
		++Environment.programCounter;
	}	
	
	public void visit(ReturnInstr instr) {
		env.procedureReturn(instr.dst);
	}
}