package IC.SemanticChecks;


import IC.AST.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * @author nechomer
 *
 */
public class FrameScope {
	// Global scope
	private HashMap<String, ICClass> classes;

	// Class scope
	private HashMap<String, Method> methods;
	private HashMap<String, Field> fields;

	// Method scope
	private HashMap<String, Type> formals;

	// Statement block scope
	private HashMap<String, Type> localVars;

	private String scopeName;
	private ScopeType scopeType;

	private FrameScope parent;
	private List<FrameScope> children;
	

	public static enum ScopeType {
		Global("Global Symbol Table"), Children("Children tables"), 
			Class("Class Symbol Table"), Method("Method Symbol Table"), 
			StatementBlock("Statement Block Symbol Table");

		private final String name;

		private ScopeType(String s) {
			name = s;
		}

		public String toString() {
			return name;
		}
	}

	public FrameScope(ScopeType type, String name, FrameScope parent) {
		scopeType = type;
		scopeName = name;

		classes = new LinkedHashMap<String, ICClass>();
		methods = new LinkedHashMap<String, Method>();
		fields = new LinkedHashMap<String, Field>();
		formals = new LinkedHashMap<String, Type>();
		localVars = new LinkedHashMap<String, Type>();

		this.parent = parent;
		children = new LinkedList<FrameScope>();
	}

	public String getName() {
		return scopeName;
	}
	
	public ScopeType getType() {
		return scopeType;
	}

	public FrameScope getParent() {
		return parent;
	}

	public List<FrameScope> getChildren() {
		return children;
	}

	public ICClass getClass(String name) {
		return classes.get(name);
	}

	public Method getMethod(String name) {
		return methods.get(name);
	}

	public Field getField(String name) {
		return fields.get(name);
	}

	public Type getFormal(String name) {
		return formals.get(name);
	}

	public Type getLocalVar(String name) {
		return localVars.get(name);
	}

	
	/**
	 * adds class to scope
	 * 
	 * @param icClass
	 */
	public void addClass(ICClass icClass) {
		if (getClass(icClass.getName()) != null)
			throw new SemanticException(icClass, "Id " + icClass.getName()
					+ " already defined in current scope");

		classes.put(icClass.getName(), icClass);
	}
	
	/**
	 * adds a new method to scope
	 * 
	 * @param m
	 */
	public void addMethod(Method m) {
		
		// check for double declaration
		if (getMethod(m.getName()) != null)
			throw new SemanticException(m, "Id " + m.getName()
					+ " is already defined in current scope");

		Object ret;
		if ((ret = retrieveIdentifier(m.getName())) != null) {
			if (ret instanceof Method && !isMethodsSignatureEqual((Method) ret, m)) {
				throw new SemanticException(m, "Method " + m.getName() + " overloads a different method by the same name");
			} else if (ret instanceof Field) {
				throw new SemanticException(m, "Method " + m.getName() + " is overriding a field by the same name");
			}
		}

		methods.put(m.getName(), m);
	}

	/**
	 * adds a new field to scope
	 * 
	 * @param f
	 */
	public void addField(Field f) {
		
		//check for double declaration
		if (getField(f.getName()) != null)
			throw new SemanticException(f, "Id " + f.getName()
					+ " is already defined in current scope");

		Object ret;
		if ((ret = retrieveIdentifier(f.getName())) != null) {
			if (ret instanceof Field)
				throw new SemanticException(f, "Field " + f.getName() + " is overriding a field by the same name");
			else if (ret instanceof Method)
				throw new SemanticException(f, "Field " + f.getName() + " is overriding a method by the same name");
		}

		fields.put(f.getName(), f);
	}

	/**
	 * adds a new parameter to scope
	 * @param f
	 */
	public void addFormal(Formal f) {
		
		//check for double declaration
		if (getFormal(f.getName()) != null)
			throw new SemanticException(f, "Id " + f.getName() + " is already defined in current scope");

		formals.put(f.getName(), f.getType());
	}

	public void addLocalVar(LocalVariable lv) {
//		if (retrieveFormalType(lv.getName()) != null)
//			throw new SemanticException(lv, "Local variable " + lv.getName() + " is overriding a parameter");

		if (getLocalVar(lv.getName()) != null)
			throw new SemanticException(lv, "Id " + lv.getName() + " is already defined in current scope");

		localVars.put(lv.getName(), lv.getType());
	}
	
	/**
	* @param id
	* @return the object with corresponding id
	*/
	public Object retrieveIdentifier(String id) {
		Object ret = null;
		FrameScope scope = this;

		while (scope != null) {
			if ((ret = scope.getLocalVar(id)) != null)
				break;

			if ((ret = scope.getFormal(id)) != null)
				break;

			if ((ret = scope.getField(id)) != null)
				break;

			if ((ret = scope.getMethod(id)) != null)
				break;

			if ((ret = scope.getClass(id)) != null)
				break;

			scope = scope.getParent();//if have'nt found, advance to parent
		}

		return ret;
	}

	/**
	 * retrieve the type of a formal named 'name' up the scope hierarchy to the closest method scope
	 *  
	 * @param name
	 * @return 
	 */
	public Type retrieveFormalType(String name) {
		FrameScope scope = this;

		// search for the first non method scope
		while (scope != null && scope.getType() != ScopeType.Method)
			scope = scope.getParent();

		if (scope == null) // by default, found nothing 
			return null;

		return scope.getFormal(name);
	}

	/**
	 * @param t1
	 * @param t2
	 * @return true if both type are equal by dimension and name
	 */
	private boolean isTypesEqual(Type t1, Type t2) {
		if (t1.getDimension() != t2.getDimension())
			return false;

		if (!t1.getName().equals(t2.getName()))
			return false;

		return true;
	}

	/**
	 * @param m1
	 * @param m2
	 * @return true if m1 method signature equals m2
	 */
	private boolean isMethodsSignatureEqual(Method m1, Method m2) {

		//the same return type
		if (!isTypesEqual(m1.getType(), m2.getType()))
			return false;
		
		List<Formal> f1 = m1.getFormals();
		List<Formal> f2 = m2.getFormals();
		
		//the same number of formals
		if (f1.size() != f2.size())
			return false;

		//check formals to be identical
		for (int i = 0; i < f1.size(); i++) {
			if (!isTypesEqual(f1.get(i).getType(), f2.get(i).getType()))
				return false;
		}

		return true;
	}

	/**
	 * adds a new scope to parent scope
	 * 
	 * @param type
	 * @param name
	 * @param parent
	 * @return generated scope
	 */
	public FrameScope addScope(ScopeType type, String name, FrameScope parent) {
		FrameScope scope = new FrameScope(type, name, parent);
		parent.children.add(scope);
		return scope;
	}

	
	/** 
	 * builds the symbol table to be printed
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(scopeType);
		if (getName() != null) {
			if (getName().startsWith("statement block in"))
				sb.append(" ( located in "+getName().substring(("statement block in ").length())+" )");
			else
			sb.append(": " + getName());
		}
		sb.append("\n");

		// Classes
		for (String className : classes.keySet()) {
			sb.append("    Class: ");
			sb.append(className);
			sb.append("\n");
		}

		// Fields
		for (Map.Entry<String, Field> entry : fields.entrySet()) {
			sb.append("    Field: ");
			sb.append(TypeTabelBuilder.formatType(entry.getValue().getType()));
			sb.append(" "+entry.getKey());
			sb.append("\n");
		}

		// Methods
		for (Map.Entry<String, Method> entry : methods.entrySet()) {
			if (entry.getValue() instanceof StaticMethod
					|| entry.getValue() instanceof LibraryMethod)
				sb.append("    Static method: ");
			else if (entry.getValue() instanceof VirtualMethod)
				sb.append("    Virtual method: ");

			sb.append(entry.getKey());
			sb.append(" {");
			sb.append(TypeTabelBuilder.formatSig(entry.getValue()));
			sb.append("}");
			sb.append("\n");
		}

		// Formals
		for (Map.Entry<String, Type> entry : formals.entrySet()) {
			sb.append("    Parameter: ");
			sb.append(TypeTabelBuilder.formatType(entry.getValue()));
			sb.append(" "+entry.getKey());
			sb.append("\n");
		}

		// Local variables
		for (Map.Entry<String, Type> entry : localVars.entrySet()) {
			sb.append("    Local variable: ");
			sb.append(TypeTabelBuilder.formatType(entry.getValue()));
			sb.append(" "+entry.getKey());
			sb.append("\n");
		}
		
		//Children
		if (!getChildren().isEmpty()) {
			sb.append(ScopeType.Children+": ");
			ListIterator<FrameScope> childIterator = children.listIterator();
			while(childIterator.hasNext()) {
				FrameScope child = childIterator.next();
				if(childIterator.hasNext())
					sb.append(child.scopeName.toString()+", ");
				else
					sb.append(child.scopeName.toString());
			}
			sb.append("\n");
		}

		return sb.toString();
	}

//	private String methodDeclaration(Method m) {
//		StringBuilder sb = new StringBuilder();
//
//		String delim = "";
//		for (Formal f : m.getFormals()) {
//			sb.append(delim);
//			sb.append(TypeTabelBuilder.formatType(f.getType()));
//			delim = ", ";
//		}
//
//		sb.append(" -> ");
//		sb.append(TypeTabelBuilder.formatType(m.getType()));
//
//		return sb.toString();
//	}
	
//	private String addBracketsToType(Type t) {
//		StringBuilder sb = new StringBuilder();
//
//		sb.append(t.getName());
//		for (int i = 0; i < t.getDimension(); i++)
//			sb.append("[]");
//
//		return sb.toString();
//	}

	public HashMap<String, ICClass> getClasses() {
		return classes;
	}

	public HashMap<String, Method> getMethods() {
		return methods;
	}

	public HashMap<String, Field> getFields() {
		return fields;
	}
	
	public HashMap<String, Type> getFormals() {
		return formals;
	}
	
	public boolean isStringType(int formalIdx) {
		Iterator<String> iter = formals.keySet().iterator();
		boolean ret = false;
		Type t = null;
		int idx = 0;
		while(iter.hasNext()) {
			if (idx == formalIdx) {
				t = formals.get(iter.next());
				if (t instanceof PrimitiveType && ((PrimitiveType)t).getName().equals("string")) {
					ret = true;
				}
				if (t instanceof UserType) {
					ret = true;
				}
				break;
			}
			idx++;
		}
		return ret;
	}
	
	public String getClassOfScope() {
		String ret = null;
		FrameScope scope = this;

		while (scope != null) {
			
			if (scope.getType() == ScopeType.Class) {
				ret = scope.scopeName;
				break;
			}
			scope = scope.getParent();//if have'nt found, advance to parent
		}
		return ret;
	}
}
