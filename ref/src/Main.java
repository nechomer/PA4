import ic.ast.Node;
import ic.ast.PrettyPrint;
import ic.gen.Gen3ac;
import ic.lexer.Lexer;
import ic.lexer.LexerException;
import ic.lexer.Token;
import ic.parser.Parser;
import ic.parser.ParserException;
import ic.sem.ScopeNode;
import ic.sem.SemanticChecker;
import ic.sem.SemanticException;
import ic.sem.SymbolTableBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @team TheCunningLinguists <velbaumm@mail.tau.ac.il>
 * 1. Stanislav Podolsky
 * 2. Artyom Lukianov
 * 3. Michael Velbaum
 */

public class Main {

	public static void main(String[] args) {
		List<Token> tokens = new ArrayList<Token>();
		List<Token> libtokens = new ArrayList<Token>();

		try {
			// Lexical analysis
			Lexer.process(new FileReader(args[0]), tokens);
			if (args.length > 1 && args[1].startsWith("-L")) {
				Lexer.process(new FileReader(args[1].substring(2)), libtokens);
			}

			// Syntax Analysis
			Node progAst = null, libAst = null;
			if (args.length > 1 && args[1].startsWith("-L")) {
				libAst = Parser.processLibrary(libtokens);
			}
			progAst = Parser.processProgram(tokens, libAst);

			// Build the symbol table
			SymbolTableBuilder stb = new SymbolTableBuilder();
			progAst.accept(stb);

			// Run semantic checks
			SemanticChecker sck = new SemanticChecker();
			progAst.accept(sck);

			// Generate 3AC code
			Gen3ac gen = new Gen3ac();
			progAst.accept(gen);

		} catch (LexerException | ParserException | SemanticException
				| IOException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	public static void printSymbolTable(ScopeNode n) {
		if (n == null)
			return;

		System.out.print(n);
		for (ScopeNode child : n.getChildren()) {
			System.out.println();
			printSymbolTable(child);
		}
	}
}