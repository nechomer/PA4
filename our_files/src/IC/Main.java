package IC;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;
import IC.*;
import IC.AST.*;
import IC.Parser.*;

public class Main
{
	public static void main(String[] args) {
        List<Token> tokens = new ArrayList<Token>();
        List<Token> libtokens = new ArrayList<Token>();
        
        try {
//            // Lexical analysis
//        	Scanner.process(new FileReader(args[0]), tokens);
//            if (args.length > 1) 
//            	Scanner.process(new FileReader(args[1].substring(2)), libtokens);
        	
//        	Token token;
//        	Object objValue;
//        	Lexer lexer = new Lexer(new FileReader(args[0]));
//        	while ((token = lexer.next_token()) != null) {
//            	objValue = token.value;
//            	System.out.println((objValue != null ? objValue.toString(): token.sym)+"\t"+token.tag+"\t"+token.line+":"+token.column+"\n");
//            }
//        	System.out.println("finished!");
//        	boolean skipParser = true;
//        	try {
//        		debugOverFolder(args[1], skipParser, args[2]);
//        	} catch (Exception e){
//        		System.out.println(e.getMessage());
//        	}
        	
        	
//            LibParser lp = new LibParser(new Lexer(new FileReader(args[0])));
//            System.out.println("finished part 1!");
//            Symbol result = lp.parse();
//            System.out.println("finished part 2!");
//            ASTNode libraryProgramNode = (ASTNode) result.value;
//            System.out.println("finished part 3!");
//            
//            if (libraryProgramNode != null) 
//                System.out.println(libraryProgramNode.accept(new PrettyPrinter(args[0])));
            
            parser pp = new parser(new Lexer(new FileReader(args[0])));
            System.out.println("finished part 1!");
            Symbol result = pp.parse();
            System.out.println("finished part 2!");
            ASTNode programNode = (ASTNode) result.value;
            System.out.println("finished part 3!");
            
            if (programNode != null) 
                System.out.println(programNode.accept(new PrettyPrinter(args[0],false)));
            
//            // Syntax Analysis
//            ASTNode progAst = null, libAst = null;            
//            progAst = parser.processProgram(tokens);
//            if (args.length > 1) 
//                libAst = parser.processLibrary(libtokens);
            
//            // print library AST (if exists)
//            if (libAst != null) 
//                System.out.println(libAst.accept(new PrettyPrint()));
//            
//            // print program AST
//            if (progAst != null) 
//                System.out.println(progAst.accept(new PrettyPrint()));
            
        } catch (ParserException  | LexicalError e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.printf("IO Error:\n%s\n", e.getMessage());
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	
	
	/**********************************************DEBUG*******************************************************/
	
	
	
	public static void PrintHeader() { 
		System.out.println("token\ttag\tline :column");
	}
	
	public static void PrintToken(String token, String tag , int line , int column) {
		System.out. println (token+"\t"+tag+"\t"+line+":"+column); 
	}
	public static void PrintTokenError(String errMsg) {
		System.err. println ("Error!\t"+errMsg);
	}
	
	//folderPath is source of files folder
	//onlyLexer is true if we try only the lexer
	//outputFolder is the output folder if its supplied, else its a new folder named "output_pa2" that need to be present at destination
	private static void debugOverFolder(String folderPath, boolean onlyLexer, String outputFolder) throws Exception {
		
		if(folderPath == null) return; 
		
		File[] files = new File(folderPath).listFiles();
		String tempFile, targetFile, targetDirPath, tempFileName;
		Token token;
    	Object objValue;
    	FileWriter fw;
    	
    	//make target dir path
    	targetDirPath = String.format("%s", folderPath);
    	targetDirPath = removeDirOfFile(targetDirPath);
    	targetDirPath += File.separator + "output_pa2" + File.separator;
    	
		for (File file : files) {
	        if (file.isDirectory()) {
	           //do nothing
	        } else {
	        	tempFileName = file.getName();
	        	if(!tempFileName.endsWith(".ic") && !tempFileName.endsWith(".sig")) continue; // skip non ic sig files
	        	
	            System.out.println("Entered File: " + tempFileName);
	            tempFile = String.format("%s%s", folderPath, tempFileName);
	        	System.out.println("temp file is: " + tempFile);
	        	
	        	//make target fileName for lexer
	        	targetFile = String.format("%s", tempFileName);
	        	targetFile = removeEnd(targetFile);
	        	targetFile = String.format("%s.output", targetFile);
	        	
	        	//make target file path
	        	if (null == outputFolder) {
	        		targetFile = targetDirPath + targetFile;
	        	} else {
	        		targetFile = outputFolder + targetFile;
	        	}
	        	
	        	
	        	fw = new FileWriter(targetFile);
    			fw.write("token\ttag\tline :column\n");
    			
    			
	        	Lexer lexer = new Lexer(new FileReader(tempFile));
	        	while ((token = lexer.next_token()) != null) {
	            	objValue = token.value;
	            	if (token.sym == LibParserSym.EOF) break;
	            	System.out.println((objValue != null ? objValue.toString(): token.tag)+"\t"+token.tag+"\t"+token.line+":"+token.column+"\n");
	            	fw.write((objValue != null ? objValue.toString(): token.tag)+"\t"+token.tag+"\t"+token.line+":"+token.column+"\n");
	            }
	        	fw.close();
	        	
	        	System.out.println("finished lexing file " + tempFile);
	            if (!onlyLexer){
	            	LibParser lp = new LibParser(new Lexer(new FileReader(tempFile)));
	                System.out.println("finished constructing parser for" + tempFile);
	                Symbol result = lp.parse();
	                System.out.println("finished parsing!");
	                Object symbol = result.value;
	                System.out.println("finished all");
	            }
	        }
	    }
	}
	
	
	public static String removeEnd(String in){
        if(in == null) {
            return null;
        }
        int p = in.lastIndexOf(".");
        if(p <= 0){
            return in;
        }
        return in.substring(0, p);
    }
	
	public static String getFileName(String in) {
		if(in == null) {
            return null;
        }
        int p = in.lastIndexOf(File.separator);
        if(p <= 0){
            return in;
        }
        return in.substring(p, in.length());
	}
	
	public static String removeDirOfFile(String in) {
		if(in == null) {
            return null;
        }
        int p = in.lastIndexOf(File.separator);
        if(p <= 0){
            return in;
        }
        String beforeLast = in.substring(0, p);
        p = beforeLast.lastIndexOf(File.separator);
        if(p <= 0){
            return in;
        }
        return beforeLast.substring(0, p);
	}
	
}