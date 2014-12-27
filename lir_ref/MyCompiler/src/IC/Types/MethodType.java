package IC.Types;

/**
 * The type of methods in the program
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public class MethodType extends Type {
	
	public MethodType(Type[] paramTypes, Type returnType) {
		super("");
		this.paramTypes = paramTypes;
		this.returnType = returnType;
		setName(this.toString());
	}

	private Type[] paramTypes;
	private Type returnType;
	private boolean libraryMethod = false;
	
	/**
	 * 
	 * @return an array of the method parameter types
	 */
	public Type[] getParamTypes() {
		return paramTypes;
	}

	/**
	 * Sets the parameter types array
	 * @param paramTypes the parameter types array
	 */
	public void setParamTypes(Type[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	/**
	 * 
	 * @return the return type of this method
	 */
	public Type getReturnType() {
		return returnType;
	}

	/**
	 * Sets the return type of this method
	 * @param returnType the return type of this method
	 */
	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}
	
	@Override
	public String toString() {
		String str = "";
		Type[] ps = getParamTypes();
		for(int i=0; i<ps.length-1; i++){
			str += ps[i].toString() + ", ";
		}
		if(ps.length > 0){
			str += ps[ps.length-1].toString();
		}
			
		str += " -> " + returnType.toString();
		
		return str;
	}

	/**
	 * 
	 * @return true if this method is a library method
	 */
	public boolean isLibraryMethod() {
		return libraryMethod;
	}

	/**
	 * Sets the method  library property
	 * @param libraryMethod true => this method is a library method
	 */
	public void setLibraryMethod(boolean libraryMethod) {
		this.libraryMethod = libraryMethod;
	}
}
