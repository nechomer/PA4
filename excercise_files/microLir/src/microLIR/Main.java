package microLIR;

import microLIR.instructions.*;
import microLIR.parser.*;

import java.io.*;

import java_cup.runtime.*;

/** The entry point of the application, responsible for parsing
 * command-line arguments and activating the simulator.
 */
public class Main {
	protected static boolean printtokens = false;
	protected static boolean printprog = false;
	protected static String file;
	protected static boolean printStatistics;
	protected static boolean atntSemantics = false;
	
	public static void main(String[] args) {
		parseArgs(args);

		try {			
			// Parse the input file
			FileReader txtFile = new FileReader(file);
			Lexer scanner = new Lexer(txtFile);
			Parser parser = new Parser(scanner);
			parser.printTokens = printtokens;
			
			Symbol parseSymbol = parser.parse();
			Program program = (Program) parseSymbol.value;
			
			if (printprog) {
				new Printer(program).print();
			}
			
			Interpreter interpreter = new Interpreter(program);
			interpreter.atntSemantics = atntSemantics;
			if (printStatistics) {
				interpreter.printStatistics();
			}
			else {
				interpreter.execute();
			}
		} catch (Exception e) {
			System.out.print(e);
		}
	}
	
	protected static void parseArgs(String [] args) {
		if (args.length == 0) {
			System.out.println("Error: Missing input file argument!");
			printUsage();
			System.exit(-1);
		}
		else {
			file = args[0];
		}
		
		for (int i = 1; i < args.length; ++i) {
			if (args[i].equals("-printtokens")) {
					printtokens = true;
			}
			else if (args[i].equals("-printtokens")) {
				printtokens = true;
			}
			else if (args[i].equals("-printprog")) {
				printprog = true;
			}
			else if (args[i].equals("-stats")) {
				printStatistics = true;
			}
			else if (args[i].equals("-atnt")) {
				atntSemantics = true;
			}
			else if (args[i].startsWith("-verbose:")) {
				int colonIndex = args[i].indexOf(':');
				String levelString = args[i].substring(colonIndex+1, args[i].length());
				try {
					int level = Integer.parseInt(levelString);
					Interpreter.verbose = level;
				}
				catch (Exception e) {
					System.out.println("Erroneous verbosity level: " + levelString);
					System.exit(-1);
				}
			}
			else {
				System.out.println("Encountered unknown option: " + args[i]);
				printUsage();
				System.exit(-1);
			}
		}
	}
	
	/** Prints usage information for this application to System.out
	 */
	public static void printUsage() {
		System.out.println("microLIR version 1.5");
		System.out.println("Usage: file [options]");
		System.out.println("options:");
		//System.out.println("  -printtokens			Prints every token encountered during parsing");
		System.out.println("  -printprog            Prints the program");
		System.out.println("  -verbose:num          Execution verbosity level (default=0)");
		System.out.println("                        0 - quiet mode");
		System.out.println("                        1 - announce next instruction to be executed");
		System.out.println("                        2 - announce instructions and computed values");
		System.out.println("  -stats                Prints program statistics and exits");
		System.out.println("  -atnt                 Turns on the AT&T-like semantics for binary operations");
	}
}