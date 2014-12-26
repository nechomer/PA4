package IC.AST;

import java.util.ArrayList;
import java.util.List;

public class FieldsAndMethodsList {

	List<Field> fields;
	List<Method> methods;
	
	public FieldsAndMethodsList() {
		fields = new ArrayList<Field>();
		methods = new ArrayList<Method>();
	}

	public void insertMethod(Method m) {
		methods.add(m);
	}
	
	public void insertFields(List<Field> fl) {
		fields.addAll(fl);
	}
	
	public List<Method> getMethods() {
		return methods;
	}
	
	public List<Field> getFields() {
		return fields;
	}
}
