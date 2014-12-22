package microLIR;

import java.io.IOException;

import microLIR.instructions.Immediate;
import microLIR.instructions.LibraryCall;
import microLIR.instructions.Operand;
import microLIR.instructions.Reg;

/** A helper class that implements library functions. 
 */
public class Library {
	private static long programStartTime = System.currentTimeMillis();
	
	/** Executes a library function in the given environment.
	 * 
	 * @param instr A library call instruction.
	 * @param env An environment.
	 */
	public static void execute(LibraryCall instr, Environment env) {
		String name = instr.func.name;
		if (name.equals("__allocateObject")) {
			allocateObject(instr, env);
		}
		else if (name.equals("__allocateArray")) {
			allocateArray(instr, env);
		}
		else if (name.equals("__stringCat")) {
			stringCat(instr, env);
		}
		else if (name.equals("__print")) {
			print(instr, env);
		}
		else if (name.equals("__println")) {
			println(instr, env);
		}
		else if (name.equals("__printi")) {
			printi(instr, env);
		}
		else if (name.equals("__printb")) {
			printb(instr, env);
		}
		else if (name.equals("__readln")) {
			readln(instr, env);
		}
		else if (name.equals("__readi")) {
			readi(instr, env);
		}
		else if (name.equals("__eof")) {
			eof(instr, env);
		}
		else if (name.equals("__itos")) {
			itos(instr, env);
		}
		else if (name.equals("__stoi")) {
			stoi(instr, env);
		}
		else if (name.equals("__stoa")) {
			stoa(instr, env);
		}
		else if (name.equals("__atos")) {
			atos(instr, env);
		}
		else if (name.equals("__time")) {
			time(instr, env);
		}
		else if (name.equals("__random")) {
			random(instr, env);
		}
		else if (name.equals("__exit")) {
			exit(instr, env);
		}
		else {
			throw new RuntimeException("Encountered unknown/unsupported library function " + instr);
		}
	}

	private static void allocateObject(LibraryCall instr, Environment env) {
		String name = instr.func.name;
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		if (!(arg1 instanceof Immediate)) 
			throw new RuntimeException("Encountered call to " + name + " with non-immediate size argumsnt:" + instr + "!");
		Immediate size = (Immediate) arg1;
		Reg dst = instr.dst;
		int address = env.allocateObject(size.val);
		env.assignLocal(dst, address);
	}

	private static void allocateArray(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		int size = env.eval(arg1);
		Reg dst = instr.dst;
		int address = env.allocateObject(size);
		env.assignLocal(dst, address);
	}

	private static void stringCat(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 2);
		Operand arg1 = instr.args.get(0);
		Operand arg2 = instr.args.get(1);
		Integer address1 = env.eval(arg1);
		int [] object1 = env.getObject(address1);
		Integer address2 = env.eval(arg2);
		int [] object2 = env.getObject(address2);
		int resultAddress = env.allocateObject((object1.length + object2.length) * 4);
		int[] result = env.getObject(resultAddress);
		int i = 0;
		for (int j = 0; j < object1.length; ++i, ++j) {
			result[i] = object1[j];
		}
		for (int j = 0; j < object2.length; ++i, ++j) {
			result[i] = object2[j];
		}
		env.assignLocal(instr.dst, resultAddress);
	}

	private static void print(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		Integer address = env.eval(arg1);
		int [] object = env.getObject(address);
		char [] chars = new char[object.length];
		for (int i = 0; i < object.length; ++i) {
			chars[i] = (char) object[i];
		}
		String str = String.valueOf(chars);
		System.out.print(str);
	}

	private static void println(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		Integer address = env.eval(arg1);
		int [] object = env.getObject(address);
		char [] chars = new char[object.length];
		for (int i = 0; i < object.length; ++i) {
			chars[i] = (char) object[i];
		}
		String str = String.valueOf(chars);
		System.out.println(str);
	}

	private static void printi(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		int val = env.eval(arg1);
		System.out.print(val);
	}

	private static void printb(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		int val = env.eval(arg1);
		System.out.print(val != 0 ? "true" : "false");
	}

	private static void readi(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 0);
		int val;
		try {
			val = System.in.read();
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		Operand dst = instr.dst;
		env.assignLocal(dst, val);
	}
	
	private static void eof(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 0);
		int available;
		try {
			available = System.in.available();
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		int isEof = (available == 0) ? 1 : 0;
		Operand dst = instr.dst;
		env.assignLocal(dst, isEof);
	}	
	
	private static void readln(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 0);
		byte[] bytes = new byte[256];
		int bytesRead;
		try {
			bytesRead = System.in.read(bytes);
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		String line = "";
		if (bytesRead >=0)
			line = new String(bytes, 0, bytesRead);
		Operand dst = instr.dst;
		
		int address = env.allocateObject(line.length() * 4);
		int[] arr = env.getObject(address);
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = line.charAt(i);
		}
		env.assignLocal(dst, address);		
	}

	private static void itos(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		Integer val = env.eval(arg1);
		String str = val.toString();
		int address = env.allocateObject(str.length() * 4);
		int[] arr = env.getObject(address);
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = str.charAt(i);
		}
		env.assignLocal(instr.dst, address);
	}

	private static void atos(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		int strAddress = env.eval(arg1);
		int[] str = env.getObject(strAddress);
		// Now allocate an array and duplicate str
		int arrAddress = env.allocateObject(str.length * 4);
		int [] arr = env.getObject(arrAddress);
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = str[i];
		}
		env.assignLocal(instr.dst, arrAddress);
	}
	
	private static void stoa(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		int strAddress = env.eval(arg1);
		int[] str = env.getObject(strAddress);
		// Now allocate an array and duplicate str
		int arrAddress = env.allocateObject(str.length * 4);
		int [] arr = env.getObject(arrAddress);
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = str[i];
		}
		env.assignLocal(instr.dst, arrAddress);
	}

	private static void stoi(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		int strAddress = env.eval(arg1);
		int[] str = env.getObject(strAddress);
		String sval = new String(str, 0, str.length);
		sval = sval.trim();
		Integer ival = -1;
		try {
		  ival = Integer.valueOf(sval);
		}
		catch (NumberFormatException e) {
		}
		env.assignLocal(instr.dst, ival.intValue());
	}
	
	private static void time(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 0);
		int val = (int) (System.currentTimeMillis() - programStartTime);
		Operand dst = instr.dst;
		env.assignLocal(dst, val);
	}

	private static void exit(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 1);
		Operand arg1 = instr.args.get(0);
		int val = env.eval(arg1);
		System.exit(val);
	}

	private static void random(LibraryCall instr, Environment env) {
		checkNumOfArgs(instr, 0);
		int val = (int) (Math.random() * Integer.MAX_VALUE - Integer.MAX_VALUE/2);
		Operand dst = instr.dst;
		env.assignLocal(dst, val);
	}
	
	private static void checkNumOfArgs(LibraryCall instr, int num) {
		String name = instr.func.name;
		if (instr.args.size() != num)
			throw new RuntimeException("Encountered call to " + name + " with incorrect number of argumsnts:" + instr + "!");
	}
}