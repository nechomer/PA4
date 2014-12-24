package ic.ast.decl;

import ic.ast.Node;
import ic.ast.Visitor;

import java.util.List;

/**
 * Class declaration AST node.
 * 
 */
public class DeclClass extends Node {

	private String name;

	private String superClassName = null;

	private List<DeclField> fields;

	private List<DeclMethod> methods;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new class node.
	 * 
	 * @param line
	 *            Line number of class declaration.
	 * @param name
	 *            Class identifier name.
	 * @param fields
	 *            List of all fields in the class.
	 * @param methods
	 *            List of all methods in the class.
	 */
	public DeclClass(int line, String name, List<DeclField> fields,
			List<DeclMethod> methods) {
		super(line);
		this.name = name;
		this.fields = fields;
		this.methods = methods;
	}

	/**
	 * Constructs a new class node, with a superclass.
	 * 
	 * @param line
	 *            Line number of class declaration.
	 * @param name
	 *            Class identifier name.
	 * @param superClassName
	 *            Superclass identifier name.
	 * @param fields
	 *            List of all fields in the class.
	 * @param methods
	 *            List of all methods in the class.
	 */
	public DeclClass(int line, String name, String superClassName,
			List<DeclField> fields, List<DeclMethod> methods) {
		this(line, name, fields, methods);
		this.superClassName = superClassName;
	}

	public String getName() {
		return name;
	}

	public boolean hasSuperClass() {
		return (superClassName != null);
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public List<DeclField> getFields() {
		return fields;
	}

	public List<DeclMethod> getMethods() {
		return methods;
	}

}
