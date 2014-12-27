package IC;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

import IC.Parser.LexicalError;
import IC.Parser.LibraryParser;
import IC.Parser.Lexer;
import IC.Parser.Parser;
import IC.Parser.SyntaxError;
import IC.SemanticChecks.BreakAndContCheck;
import IC.SemanticChecks.MethodReturnCheck;
import IC.SemanticChecks.SemanticError;
import IC.SemanticChecks.SingleMainMethod;
import IC.SemanticChecks.TypeCheck;
import IC.SymbolTable.SymbolTable;
import IC.SymbolTable.SymbolTableConstructor;
import IC.Types.TypeTable;
import IC.lir.DVCreator;
import IC.lir.StringMapper;
import IC.lir.Translator;
import java_cup.runtime.Symbol;
import IC.AST.Library;
import IC.AST.PrettyPrinter;
import IC.AST.Program;

/**
 * <H1>Tel Aviv University</H1>
 * <H2>Compilation course</H2>
 * <H3>Compiler project</H3>
 * This is the main compiler class for the IC language.
 * 
 * @author Asaf Bruner, Aviv Goll
 */

public class Compiler {
	/**
	 * parses the program file and the Library file and constructs the proper AST.
	 * 
	 * @param args - as stated in Usage.
	 */
    public static void main(String[] args)
    {
    	
		if (args.length == 0) {
			System.err.println("Error: Missing input file argument!");
			printUsage();
			System.exit(-1);
		}
		
		// handle arguments parsing
		boolean printAST = false;
		boolean dumpSymTab = false;
		boolean printLibrary = false;
		boolean printLir = false;
		String libFileName = "libic.sig";
		for ( int i = 1; i < args.length; i++) {
			if ( ( args[i].charAt(0) == '-') && (args[i].charAt(1) == 'L') ) {
				libFileName = args[i].substring(2);
				printLibrary = true;
			} else if ( args[i].equals("-print-ast") ){
				printAST = true;
			}
			else if (args[i].equals("-dump-symtab") ){
				dumpSymTab = true;
			}
			else if (args[i].equals("-print-lir") ){
				printLir = true;
			}
		}

		// Parse the library file
		FileReader libFile = null;
		try {
			libFile = new FileReader(libFileName);
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + libFileName);
		}
		Lexer libscanner = new Lexer(libFile);
		LibraryParser libparser = new LibraryParser(libscanner);
		Symbol libSym = null;
		try {
			libSym = libparser.parse();
			if(printLibrary)
				System.out.println("Parsed " + libFileName + " successfully!");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}
		Library libRoot = (Library) libSym.value;
		if ( printAST ) {
			PrettyPrinter printer = new PrettyPrinter(libFileName);
			System.out.println( printer.visit(libRoot) );
		}
		
		// Parse the input file
		FileReader txtFile = null;
		try {
			txtFile = new FileReader(args[0]);
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + args[0]);
		}
		Lexer scanner = new Lexer(txtFile);
		Parser parser = new Parser(scanner);
		try {
			Symbol parseSymbol = parser.parse();
			System.out.println("Parsed " + args[0] + " successfully!");
			System.out.println("");
			Program root = (Program) parseSymbol.value;
			root.setLibrary(libRoot);
			if ( printAST ) {
				PrettyPrinter printer = new PrettyPrinter(args[0]);
				System.out.println( printer.visit(root) );
			}
			
			// Build symbol tables
			SymbolTableConstructor stc = new SymbolTableConstructor();
			SymbolTable st = (SymbolTable) stc.visit(root);

			
			// Check for single main
			SingleMainMethod smm = new SingleMainMethod();
			smm.checkForSingleMain(root);
			
			// Check that break and continue are only in loops
			BreakAndContCheck bacc = new BreakAndContCheck();
			bacc.visit(root);
			
			// Type check
			TypeCheck tc = new TypeCheck();
			tc.visit(root);
			
			//prints the symbol and check tables
			if(dumpSymTab){
				st.printSymbolTable("Global",st, args[0], printLibrary);
				TypeTable.printTable(args[0], printLibrary);
			}
			
			// Check that a non-void method has return on all execution paths
			MethodReturnCheck mrc = new MethodReturnCheck();
			mrc.checkMethodsReturn(root);
			
			// Generate string table
			StringMapper sm = new StringMapper();
			sm.visit(root);

			//Calculate dispatch vector
			DVCreator.createDV(st);
			;
			
			// Create LIR code
			Translator tr = new Translator(StringMapper.getStringMap());
			String lir = 
					StringMapper.stringMapText() + "\n" +
					DVCreator.printDVS()+ "\n" +
					tr.visit(root);
			if ( printLir ) {
				String lirFileName = args[0].replaceAll(".ic$", ".lir");
				FileWriter fw = new FileWriter(args[0].replaceAll(".ic$", ".lir"));
				fw.write(lir);
				fw.close();
				System.out.println("IR written to " + lirFileName);
			}
			
		} catch (SyntaxError e) {
			System.err.println("Syntax Error: Line " + e.getLine() + ": " + e.getMessage());
		} catch (SemanticError e) {
			System.err.println(e.getMessage());
		} catch ( LexicalError e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		
	}
	
	/** 
	 * Prints usage information about this application to System.out
	 */
	public static void printUsage() {
		System.err.println("Usage: java IC.Compiler <file.ic> [ -L</path/to/libic.sig> ]");
	}
}

