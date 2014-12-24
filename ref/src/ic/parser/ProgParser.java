package ic.parser;

import ic.ast.decl.*;
import ic.ast.expr.*;
import ic.ast.expr.BinaryOp.BinaryOps;
import ic.ast.expr.UnaryOp.UnaryOps;
import ic.ast.stmt.*;
import ic.lexer.Token;

import java.util.ArrayList;
import java.util.List;

import fun.grammar.Word;
import fun.parser.Tree;

public class ProgParser {

	public static ic.ast.Node Parse(fun.parser.Tree parseTree, ic.ast.Node libAst) {
		Word root = parseTree.root;
		Tree[] subtrees = parseTree.subtrees.toArray(new Tree[0]);
		switch (root.tag) {
		case "S":
			return constructAst(subtrees[0], libAst);
		}
		return null;
	}

	private static ic.ast.Node constructAst(fun.parser.Tree parseTree, ic.ast.Node libAst) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		if (subtrees.length == 0) {
			return null;
		}
		List<DeclClass> classes = new ArrayList<DeclClass>();
		
		// Add the Library AST to the list of class declarations
		if (libAst != null) classes.add((DeclClass)libAst);
		
		fun.parser.Tree classPtr = subtrees[0];
		while (true) {
			if (subtrees.length == 2) {
				classes.add((DeclClass) constructClass(classPtr));
				subtrees = subtrees[1].subtrees.toArray(new fun.parser.Tree[0]);
				if (subtrees.length == 0)
					break;
				classPtr = subtrees[0];
			} else {
				break;
			}
		}
		return new Program(classes);
	}

	private static ic.ast.Node constructClass(fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		List<DeclMethod> methods = new ArrayList<DeclMethod>();
		List<DeclField> fields = new ArrayList<DeclField>();
		// check for extends
		fun.parser.Tree[] extendsSubTree = subtrees[2].subtrees
				.toArray(new fun.parser.Tree[0]);
		String superClassName = null;
		if (extendsSubTree.length == 2) {
			superClassName = ((Token) extendsSubTree[1].root).value;
		}

		fun.parser.Tree[] classContent = subtrees[4].subtrees
				.toArray(new fun.parser.Tree[0]);

		// iterate over class content and add fields and methods
		while (true) {
			if (classContent.length < 2) {
				break;
			}
			switch (classContent[0].root.tag) {
			// there might be more than one field declaration in one line so
			// there goes the loop...
			case "field":
				fun.parser.Tree fieldPtr = classContent[0];
				fun.parser.Tree[] subFields = fieldPtr.subtrees
						.toArray(new fun.parser.Tree[0]);
				Type fieldType = (Type) constructType(subFields[0]);

				while (true) {
					fields.add(new DeclField(fieldType,
							((Token) subFields[1].root).value));
					fieldPtr = subFields[2];
					subFields = fieldPtr.subtrees
							.toArray(new fun.parser.Tree[0]);
					if (subFields.length == 0)
						break;
				}
				break;

			// declarations of methods
			case "method":
				methods.add((DeclMethod) constructMethod(classContent[0]));
				break;
			}
			classContent = classContent[1].subtrees
					.toArray(new fun.parser.Tree[0]);

		}
		return new DeclClass(((Token) subtrees[0].root).line,
				((Token) subtrees[1].root).value, superClassName, fields,
				methods);
	}

	private static ic.ast.Node constructMethod(fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		List<Parameter> formals = new ArrayList<Parameter>();
		List<Statement> statements = new ArrayList<Statement>();

		fun.parser.Tree[] subStatic = subtrees[0].subtrees
				.toArray(new fun.parser.Tree[0]);
		fun.parser.Tree[] subMethodType = subtrees[1].subtrees
				.toArray(new fun.parser.Tree[0]);

		// construct methods type
		Type type = null;
		switch (subMethodType[0].root.tag) {
		case "void":
			type = new PrimitiveType(((Token) subMethodType[0].root).line,
					PrimitiveType.DataType.VOID);
			break;
		case "type":
			type = (Type) constructType(subMethodType[0]);
			break;
		}

		// fill formals
		fillFormals(subtrees[4], formals);

		// statments loop
		fun.parser.Tree[] subBody = subtrees[7].subtrees
				.toArray(new fun.parser.Tree[0]);
		if (subBody.length != 0) {
			while (true) {
				statements.add((Statement) constructStmt(subBody[0]));
				subBody = subBody[1].subtrees.toArray(new fun.parser.Tree[0]);
				if (subBody.length < 1)
					break;

			}
		}

		// return method
		if (subStatic.length > 0) {
			return new DeclStaticMethod(type, ((Token) subtrees[2].root).value,
					formals, statements);
		} else {
			return new DeclVirtualMethod(type,
					((Token) subtrees[2].root).value, formals, statements);
		}

	}

	private static ic.ast.Node constructStmt(fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		switch (subtrees[0].root.tag) {
		case "stmt_open":
			return constructStmt(subtrees[0]);
		case "stmt_closed":
			return constructStmt(subtrees[0]);
		case "stmt_simple":
			return constructStmt(subtrees[0]);
		case "call":
			return constructStmt(subtrees[0]);
		case "staticCall":
			return constructStaticStmtCall(subtrees[0]);
		case "virtualCall":
			return constructVirtualStmtCall(subtrees[0]);
		case "if":
			if (subtrees.length == 5) {
				return new StmtIf((Expression) constructExpr(subtrees[2]),
						(Statement) constructStmt(subtrees[4]));
			} else {
				return new StmtIf((Expression) constructExpr(subtrees[2]),
						(Statement) constructStmt(subtrees[4]),
						(Statement) constructStmt(subtrees[6]));
			}
		case "while":
			return new StmtWhile((Expression) constructExpr(subtrees[2]),
					(Statement) constructStmt(subtrees[4]));
		case "return":
			return new StmtReturn(((Token) subtrees[0].root).line,
					(Expression) constructExpr(subtrees[1]));
		case "break":
			return new StmtBreak(((Token) subtrees[0].root).line);

		case "location":
			return new StmtAssignment((Ref) constructExpr(subtrees[0]),
					(Expression) constructExpr(subtrees[2]));
		case "continue":
			return new StmtContinue(((Token) subtrees[0].root).line);

		case "{":
			return constructBlock(subtrees[1], ((Token) subtrees[0].root).line);
		case "type":
			int line = ((Token) subtrees[1].root).line;
			Type type = (Type) constructType(subtrees[0]);
			return new LocalVariable(line, type,
					((Token) subtrees[1].root).value,
					(Expression) constructExpr(subtrees[2]));
		}

		return null;
	}

	private static ic.ast.Node constructBlock(fun.parser.Tree parseTree,
			int line) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		List<Statement> statements = new ArrayList<Statement>();
		if (subtrees.length == 0) {
			
			return new StmtBlock(line, statements);
		}
		while (true) {
			statements.add((Statement) constructStmt(subtrees[0]));
			subtrees = subtrees[1].subtrees.toArray(new fun.parser.Tree[0]);
			if (subtrees.length == 0)
				break;
		}
		return new StmtBlock(line, statements);
	}

	private static ic.ast.Node constructExpr(fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		if (subtrees.length == 0) {
			return null;
		}
		switch (subtrees[0].root.tag) {
		case "=":
			return constructExpr(subtrees[1]);
		case "expr":
			return constructExpr(subtrees[0]);
		case "expr_8":
			if (subtrees.length == 1) {
				return constructExpr(subtrees[0]);
			}
			return new BinaryOp(((Token) subtrees[1].root).line,
					(Expression) constructExpr(subtrees[0]), BinaryOps.LOR,
					(Expression) constructExpr(subtrees[2]));
		case "expr_7":
			if (subtrees.length == 1) {
				return constructExpr(subtrees[0]);
			}
			return new BinaryOp(((Token) subtrees[1].root).line,
					(Expression) constructExpr(subtrees[0]), BinaryOps.LAND,
					(Expression) constructExpr(subtrees[2]));
		case "expr_6":
			if (subtrees.length == 1) {
				return constructExpr(subtrees[0]);
			}
			switch (subtrees[1].root.tag) {
			case "==":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]),
						BinaryOps.EQUAL,
						(Expression) constructExpr(subtrees[2]));
			case "!=":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]),
						BinaryOps.NEQUAL,
						(Expression) constructExpr(subtrees[2]));
			}
		case "expr_5":
			if (subtrees.length == 1) {
				return constructExpr(subtrees[0]);
			}
			switch (subtrees[1].root.tag) {
			case "<":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]), BinaryOps.LT,
						(Expression) constructExpr(subtrees[2]));
			case "<=":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]), BinaryOps.LTE,
						(Expression) constructExpr(subtrees[2]));
			case ">":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]), BinaryOps.GT,
						(Expression) constructExpr(subtrees[2]));
			case ">=":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]), BinaryOps.GTE,
						(Expression) constructExpr(subtrees[2]));
			}
		case "expr_4":
			if (subtrees.length == 1) {
				return constructExpr(subtrees[0]);
			}
			switch (subtrees[1].root.tag) {
			case "+":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]),
						BinaryOps.PLUS, (Expression) constructExpr(subtrees[2]));
			case "-":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]),
						BinaryOps.MINUS,
						(Expression) constructExpr(subtrees[2]));
			}
		case "expr_3":
			if (subtrees.length == 1) {
				return constructExpr(subtrees[0]);
			}
			switch (subtrees[1].root.tag) {
			case "*":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]),
						BinaryOps.MULTIPLY,
						(Expression) constructExpr(subtrees[2]));
			case "/":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]),
						BinaryOps.DIVIDE,
						(Expression) constructExpr(subtrees[2]));
			case "%":
				return new BinaryOp(((Token) subtrees[1].root).line,
						(Expression) constructExpr(subtrees[0]), BinaryOps.MOD,
						(Expression) constructExpr(subtrees[2]));
			}
		case "-":
			Literal literal = parseInt(subtrees[1]);
			if (literal != null) {
				String value = "-" + (String) literal.getValue();
				if (value.length() > 9)
					try {
						Integer.parseInt(value);
					} catch (NumberFormatException e) {
						throw new ParserException(
								((Token) subtrees[0].root).line,
								((Token) subtrees[0].root).column,
								"numeric literal out of range: "
										+ ((Token) subtrees[0].root).value);
					}
				return new Literal(((Token) subtrees[0].root).line,
						PrimitiveType.DataType.INT, value);
			}
			return new UnaryOp(((Token) subtrees[0].root).line,
					UnaryOps.UMINUS, (Expression) constructExpr(subtrees[1]));
		case "!":
			return new UnaryOp(((Token) subtrees[0].root).line, UnaryOps.LNEG,
					(Expression) constructExpr(subtrees[1]));
		case "expr_2":
			if (subtrees.length == 1) {
				return constructExpr(subtrees[0]);
			}
		case "expr_1":
			if (subtrees.length == 1) {
				return constructExpr(subtrees[0]);
			} else if (subtrees.length == 3) {
				if (subtrees[2].root.tag.equals("length")) {
					return new Length(((Token) subtrees[1].root).line,
							(Expression) constructExpr(subtrees[0]));
				} else {
					return new RefField(((Token) subtrees[1].root).line,
							(Expression) constructExpr(subtrees[0]),
							((Token) subtrees[2].root).value);
				}
			}
		case "new":
			if (subtrees.length == 4) {
				return new NewInstance(((Token) subtrees[0].root).line,
						((Token) subtrees[1].root).value);
			} else {
				return new NewArray((Type) constructType(subtrees[1]),
						(Expression) constructExpr(subtrees[3]));
			}
		case "expr_0":
			if (subtrees.length == 1)
				return constructExpr(subtrees[0]);
			return new RefArrayElement((Expression) constructExpr(subtrees[0]),
					(Expression) constructExpr(subtrees[2]));
		case "location":
			return constructExpr(subtrees[0]);
		case "call":
			return constructExpr(subtrees[0]);
		case "this":
			return new This(((Token) subtrees[0].root).line);
		case "literal":
			subtrees = subtrees[0].subtrees.toArray(new fun.parser.Tree[0]);
			switch (subtrees[0].root.tag) {
			case "INTEGER":
				String value = ((Token) subtrees[0].root).value;
				if (value.length() > 9)
					try {
						Integer.parseInt(value);
					} catch (NumberFormatException e) {
						throw new ParserException(
								((Token) subtrees[0].root).line,
								((Token) subtrees[0].root).column,
								"numeric literal out of range: "
										+ ((Token) subtrees[0].root).value);
					}
				return new Literal(((Token) subtrees[0].root).line,
						PrimitiveType.DataType.INT,
						((Token) subtrees[0].root).value);
			case "STRING":
				String s = ((Token) subtrees[0].root).value;
				s = s.substring(1, s.length() - 1);
				s = escapedChars(s);
				return new Literal(((Token) subtrees[0].root).line,
						PrimitiveType.DataType.STRING, s);
			case "true":
				return new Literal(((Token) subtrees[0].root).line,
						PrimitiveType.DataType.BOOLEAN,
						((Token) subtrees[0].root).value);
			case "false":
				return new Literal(((Token) subtrees[0].root).line,
						PrimitiveType.DataType.BOOLEAN,
						((Token) subtrees[0].root).value);
			case "null":
				return new Literal(((Token) subtrees[0].root).line,
						PrimitiveType.DataType.VOID,
						((Token) subtrees[0].root).value);
			}
		case "(":
			return constructExpr(subtrees[1]);
		case "ID":
			return new RefVariable(((Token) subtrees[0].root).line,
					((Token) subtrees[0].root).value);
		case "staticCall":
			return constructStaticCall(subtrees[0]);
		case "virtualCall":
			return constructVirtualCall(subtrees[0]);
		}

		return null;
	}

	private static String escapedChars(String s) {
		return s.replace("\\t", "\t").replace("\\n", "\n")
				.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	private static void fillFormals(fun.parser.Tree tree,
			List<Parameter> formals) {
		fun.parser.Tree[] subFormals = tree.subtrees
				.toArray(new fun.parser.Tree[0]);
		if (subFormals.length == 0)
			return;
		Type fieldType = (Type) constructType(subFormals[0]);
		formals.add(new Parameter(fieldType, ((Token) subFormals[1].root).value));
		fun.parser.Tree formalPTR = subFormals[2];
		subFormals = formalPTR.subtrees.toArray(new fun.parser.Tree[0]);
		while (true) {
			if (subFormals.length == 0)
				break;
			fieldType = (Type) constructType(subFormals[1]);
			formals.add(new Parameter(fieldType,
					((Token) subFormals[2].root).value));
			formalPTR = subFormals[3];
			subFormals = formalPTR.subtrees.toArray(new fun.parser.Tree[0]);
		}

	}

	private static ic.ast.Node constructVirtualCall(fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		if (subtrees.length == 4) {
			return new VirtualCall(((Token) subtrees[1].root).line,
					((Token) subtrees[0].root).value,
					constructCallArgs(subtrees[2]));
		} else {
			return new VirtualCall(((Token) subtrees[1].root).line,
					(Expression) constructExpr(subtrees[0]),
					((Token) subtrees[2].root).value,
					constructCallArgs(subtrees[4]));
		}

	}

	private static ic.ast.Node constructStaticCall(fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		return new StaticCall(((Token) subtrees[0].root).line,
				((Token) subtrees[0].root).value,
				((Token) subtrees[2].root).value,
				constructCallArgs(subtrees[4]));

	}

	private static ic.ast.Node constructStaticStmtCall(fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		return new StmtCall(new StaticCall(((Token) subtrees[0].root).line,
				((Token) subtrees[0].root).value,
				((Token) subtrees[2].root).value,
				constructCallArgs(subtrees[4])));

	}

	private static ic.ast.Node constructVirtualStmtCall(
			fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		if (subtrees.length == 4) {
			return new StmtCall(new VirtualCall(
					((Token) subtrees[1].root).line,
					((Token) subtrees[0].root).value,
					constructCallArgs(subtrees[2])));
		} else {
			return new StmtCall(new VirtualCall(
					((Token) subtrees[1].root).line,
					(Expression) constructExpr(subtrees[0]),
					((Token) subtrees[2].root).value,
					constructCallArgs(subtrees[4])));
		}

	}

	private static List<Expression> constructCallArgs(fun.parser.Tree tree) {
		List<Expression> args = new ArrayList<Expression>();
		fun.parser.Tree[] subFormals = tree.subtrees
				.toArray(new fun.parser.Tree[0]);
		if (subFormals.length == 0)
			return args;
		args.add((Expression) constructExpr(subFormals[0]));
		fun.parser.Tree formalPTR = subFormals[1];
		subFormals = formalPTR.subtrees.toArray(new fun.parser.Tree[0]);
		while (true) {
			if (subFormals.length == 0)
				break;
			args.add((Expression) constructExpr(subFormals[1]));
			formalPTR = subFormals[2];
			subFormals = formalPTR.subtrees.toArray(new fun.parser.Tree[0]);
		}
		return args;

	}

	private static ic.ast.Node constructType(fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
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
			Type new_type = (Type) constructType(subtrees[0]);
			new_type.incrementDimension();
			return new_type;
		}
		return null;

	}

	private static Literal parseInt(fun.parser.Tree parseTree) {
		fun.parser.Tree[] subtrees = parseTree.subtrees
				.toArray(new fun.parser.Tree[0]);
		if (subtrees.length > 1) {
			return null;
		}
		switch (subtrees[0].root.tag) {
		case "expr_1":
			return parseInt(subtrees[0]);
		case "expr_0":
			return parseInt(subtrees[0]);
		case "literal":
			return parseInt(subtrees[0]);
		case "INTEGER":
			return new Literal(((Token) subtrees[0].root).line,
					PrimitiveType.DataType.INT,
					((Token) subtrees[0].root).value);

		}
		return null;
	}

}
