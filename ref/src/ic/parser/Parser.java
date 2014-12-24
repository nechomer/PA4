package ic.parser;

import fun.grammar.Grammar;
import fun.grammar.Word;
import fun.parser.Tree;
import fun.parser.earley.EarleyParser;
import fun.parser.earley.EarleyParser.PostMortem;
import fun.parser.earley.EarleyState;
import ic.ast.decl.ClassType;
import ic.ast.decl.DeclClass;
import ic.ast.decl.DeclField;
import ic.ast.decl.DeclLibraryMethod;
import ic.ast.decl.DeclMethod;
import ic.ast.decl.Parameter;
import ic.ast.decl.PrimitiveType;
import ic.ast.decl.Type;
import ic.lexer.Token;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Parser {


	private static fun.parser.Tree parseProgram(Iterable<Token> tokens) {
	    InputStream is = Parser.class.getResourceAsStream("/ic/parser/prog.grammar");
	    if (is == null)
	        throw new ParserException("Parser Error: Cannot find program grammar.");
	    
        Scanner s = new Scanner(is);
        String content = s.useDelimiter("\\A").next();
        s.close();
        
		return parse(tokens, new Grammar(content));
	}

	private static fun.parser.Tree parseLibrary(Iterable<Token> tokens) {
        InputStream is = Parser.class.getResourceAsStream("/ic/parser/lib.grammar");
        if (is == null)
            throw new ParserException("Parser Error: Cannot find library grammar.");
        
        Scanner s = new Scanner(is);
        String content = s.useDelimiter("\\A").next();
        s.close();
        
        return parse(tokens, new Grammar(content));
	}

	private static fun.parser.Tree parse(Iterable<Token> tokens, Grammar grammar) {
		EarleyParser e = new EarleyParser(tokens, grammar);
		List<EarleyState> pts = e.getCompletedParses();

		// A parsing error occurred
		if (pts.size() == 0) {
			StringBuilder sb = new StringBuilder();

			PostMortem pm = e.diagnoseError();
			Token tk = null;
			if (pm.token instanceof Token)
				tk = (Token) pm.token;

			int line = (tk == null) ? -1 : tk.line;
			int column = (tk == null) ? -1 : tk.column;

			// Build the error message
			Iterator<String> it = pm.expecting.iterator();
			sb.append("expected '" + it.next() + "'");
			while (it.hasNext())
				sb.append(" or '" + it.next() + "'");

			if (tk != null)
				sb.append(", but found '" + tk.tag + "'");

			throw new ParserException(line, column, sb.toString());
		}

		// Grammar is ambiguous
		if (pts.size() > 1)
			throw new ParserException(
					"Parser Error: Grammar is ambiguous (returned "
							+ pts.size() + " trees).");

		return pts.get(0).parseTree();
	}

	// Construct AST from the parse tree
    private static ic.ast.Node constructProgAst(fun.parser.Tree parseTree, ic.ast.Node libAst) {
        return ProgParser.Parse(parseTree, libAst);
    }

    private static ic.ast.Node constructLibAst(fun.parser.Tree parseTree) {
        Word root = parseTree.root;
        Tree[] subtrees = parseTree.subtrees.toArray(new Tree[parseTree.subtrees.size()]);
        switch (root.tag) {
            case "S":
                return constructLibAst(subtrees[0]);
            case "libic":
                List<DeclMethod> methods = new ArrayList<>();
                List<DeclField> fields = new ArrayList<>();
                Tree method_ptr = subtrees[3];
                Tree[] method_subtree = method_ptr.subtrees.toArray(new Tree[method_ptr.subtrees.size()]);
                
                while (method_subtree.length != 0) {
                    List<Parameter> formals = new ArrayList<>();
                    Tree formal_ptr = method_subtree[4];
                    Tree[] formal_subtree = formal_ptr.subtrees.toArray(new Tree[formal_ptr.subtrees.size()]);
                    
                    if (formal_subtree.length != 0) {
                        formals.add(new Parameter((Type) constructLibAst(formal_subtree[0]),
                                ((Token) formal_subtree[1].root).value));
                        Tree formal_more_ptr = formal_subtree[2];
                        Tree[] formal_more_subtree = formal_more_ptr.subtrees.
                                toArray(new Tree[formal_more_ptr.subtrees.size()]);
                        
                        while (formal_more_subtree.length != 0) {
                            formals.add(new Parameter((Type) constructLibAst(formal_more_subtree[1]),
                                    ((Token) formal_more_subtree[2].root).value));
                            formal_more_ptr = formal_more_subtree[3];
                            formal_more_subtree = formal_more_ptr.subtrees.toArray(new Tree[formal_more_ptr.subtrees.size()]);
                        }
                    }
                    methods.add(new DeclLibraryMethod((Type)constructLibAst(method_subtree[1]),
                            ((Token) method_subtree[2].root).value, formals));
                    method_ptr = method_subtree[7];
                    method_subtree = method_ptr.subtrees.toArray(new Tree[method_ptr.subtrees.size()]);
                }
                return new DeclClass(((Token) subtrees[0].root).line,
                        ((Token) subtrees[1].root).value, fields, methods);
            case "libmethod_type":
                switch (subtrees[0].root.tag) {
                    case "void":
                        return new PrimitiveType(((Token) subtrees[0].root).line,
                                PrimitiveType.DataType.VOID);
                    case "type":
                        return constructLibAst(subtrees[0]);
                }
                break;
            case "type":
                switch (subtrees[0].root.tag) {
                    case "int":
                        return new PrimitiveType(((Token) subtrees[0].root).line,
                                PrimitiveType.DataType.INT);
                    case "boolean":
                        return new PrimitiveType(((Token) subtrees[0].root).line,
                                PrimitiveType.DataType.BOOLEAN);
                    case "string":
                        return new PrimitiveType(((Token) subtrees[0].root).line,
                                PrimitiveType.DataType.STRING);
                    case "CLASS_ID":
                        return new ClassType(((Token) subtrees[0].root).line,
                                ((Token) subtrees[0].root).value);
                    case "type":
                        Type new_type = (Type) constructLibAst(subtrees[0]);
                        new_type.incrementDimension();
                        return new_type;
                }
                break;
        }
        
        return null;
    }

	public static ic.ast.Node processProgram(Iterable<Token> tokens, ic.ast.Node libAst) {
		return constructProgAst(parseProgram(tokens), libAst);
	}

	public static ic.ast.Node processLibrary(Iterable<Token> tokens) {
		return constructLibAst(parseLibrary(tokens));
	}

}