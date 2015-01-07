package IC;

import IC.AST.*;
import IC.Parser.*;
import IC.SemanticChecks.*;
import IC.lir.DispatchTableBuilder;
import IC.lir.LirTranslator;
import IC.lir.StringsBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;

public class Compiler {
    public static void main(String[] args)
    {
    	parser pp;
    	LibParser lp;
    	Symbol result;
    	ASTNode programNode = null, libraryProgramNode = null;
    	String LibraryFile;
    	String fullPath;
    	String progFileName, libFileName;
    	int index;
    	boolean printAst = false, dumpSymtab = false, printLir = false;
    	try {
    		
    		for (int i = 0; i< args.length; i++ ) {
    			if (args[i].equals("-dump-symtab")) dumpSymtab = true;
    			if (args[i].equals("-print-ast")) printAst = true;
    			if (args[i].equals("-print-lir")) printLir = true;
    		}
    		
    		if (args.length > 1) { // Library file is also supplied
    			if (args[1].substring(0,2).equals("-L"))  {
	    			LibraryFile = args[1].substring(2);
	                lp = new LibParser(new Lexer(new FileReader(LibraryFile)));
	                result = lp.parse();
	                libraryProgramNode = (ASTNode) result.value;
	                fullPath = args[1];
	        		index = fullPath.lastIndexOf(File.separator);
	        		libFileName = fullPath.substring(index + 1);
	        		if (libraryProgramNode != null) {
	        			System.out.println("Parsed " + libFileName + " successfully!");
	        		}
	        	//In case it's not a library file - It should be a switch for printing!		
        		} else if (!args[1].equals("-dump-symtab") && !args[1].equals("-print-ast")) {
    				System.out.println("\n ERROR: Library file must be supplied with preceding -L ");
    				return;
        		}
    		}
    		          
    		fullPath = args[0];
    		index = fullPath.lastIndexOf(File.separator);
    		progFileName = fullPath.substring(index + 1);
    		
    		
    		pp = new parser(new Lexer(new FileReader(args[0])));
    		result = pp.parse();
    		programNode = (ASTNode) result.value;
    		if (programNode != null) { 
    			System.out.println("Parsed " + progFileName + " successfully!");
    			
    		}
    		
    		
    		    		
    		// Add the Library AST to the list of class declarations
    		if (libraryProgramNode != null) { 
    			((Program) programNode).getClasses().add(0, ((Program) libraryProgramNode).getClasses().get(0));
    		}
    		    					
    		// Build the symbol table
            SymbolTableBuilder stb = new SymbolTableBuilder(progFileName);
            programNode.accept(stb);
			
            // Run semantic checks
			SemanticChecker sck = new SemanticChecker();
			programNode.accept(sck);
			
            // Build the type table builder
            TypeTabelBuilder ttb = new TypeTabelBuilder(progFileName); 
     		programNode.accept(ttb);
     		
     		// Build the Dispatch Table
     		DispatchTableBuilder.createDispatchTable(stb.getRootScope());
     		
     		if (printAst) {
    			if (libraryProgramNode!= null) System.out.println(libraryProgramNode.accept(new PrettyPrinter(args[1],false))); 
    			if (programNode!= null) System.out.println(programNode.accept(new PrettyPrinter(args[0],libraryProgramNode != null))); 
    		}
     		
     		if (dumpSymtab) {
	            // Print the symbol table
	            System.out.println();
	            printSymbolTable(stb.getRootScope());
	
	            // Print the Type table
	            System.out.println();
	     		System.out.println(ttb);
     		}
     		
     		//generate strings for lir
     		StringsBuilder sb = new StringsBuilder();
     		programNode.accept(sb);
     		
     		//translate program to lir
     		LirTranslator lt = new LirTranslator(StringsBuilder.getStringsMap());
     		
     		
     		String lir = 
     				StringsBuilder.exportStringLirTable() + "\n" +
     				DispatchTableBuilder.printDispatchTable()+ "\n" +
     				programNode.accept(lt);
     		if(printLir) {
     			//print lir program
     			String lirFileName = args[0].replaceAll(".ic$", ".lir");
				FileWriter fw = new FileWriter(lirFileName);
				fw.write(lir);
				fw.close();
     		}
	
    	} catch (ParserException | SemanticException | LexicalError e) {
    		System.out.println(e.getMessage());
    		//System.exit(1);
    	} catch (IOException e) {
    		System.err.printf("IO Error:\n%s\n", e.getMessage());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    
    }
    
    public static void printSymbolTable(FrameScope n) {
        if (n == null)
            return;
        
        System.out.print(n);
        for (FrameScope child : n.getChildren()) {
            System.out.println();
            printSymbolTable(child);
        }
    }
    
}
