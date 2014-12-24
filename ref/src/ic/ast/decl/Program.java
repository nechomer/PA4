package ic.ast.decl;

import ic.ast.Node;
import ic.ast.Visitor;

import java.util.List;

/**
 * Root AST node for an IC program.
 * 
 */
public class Program extends Node {

	private List<DeclClass> classes;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new program node.
	 * 
	 * @param classes
	 *            List of all classes declared in the program.
	 */
	public Program(List<DeclClass> classes) {
		super(0);
		this.classes = classes;
	}

	public List<DeclClass> getClasses() {
		return classes;
	}

}
