package IC.lir;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import IC.AST.*;
import IC.SemanticChecks.*;
import IC.SemanticChecks.FrameScope.ScopeType;

public class DispatchTableBuilder {
	
	public static Map<String, LinkedHashMap<String, Integer>> classMethodOffsets = new LinkedHashMap<>();
	public static Map<String, LinkedHashMap<String, Integer>> classFieldOffsets = new LinkedHashMap<>();
	
	/**
	 * @param fieldName
	 * @return the amount of fields the class contains
	 */
	public static int getNumFields(String fieldName) {
		return classFieldOffsets.get(fieldName).size();
	}
	
	/**
	 * @param name - Class name 
	 * @return The string representing the class in LIR
	 */
	public static String getDispatchTableName(String name) {
	    return "_DV_" + name;
	}
	 
	/**
	 * @param currScope - The scope which is examined, its classes, fields or methods will be added 
	 * 					  to the field & method data structures
	 */
	public static void createDispatchTable(FrameScope currScope) {
		LinkedHashMap<String, Integer> fieldOffsets;
	    LinkedHashMap<String, Integer> methodOffsets;
	    //The scope isn't the root - It should be a class or a subclass
	    if (currScope.getType() != ScopeType.Global) {
	    //This is a subclass - Its parent scope is the parent class and it inherits
	    //its methods & fields	
	    if(currScope.getParent().getType() != ScopeType.Global){
	    	fieldOffsets = (LinkedHashMap<String, Integer>) 
	    		classFieldOffsets.get(currScope.getParent().getName()).clone();
	    	methodOffsets = (LinkedHashMap<String, Integer>) 
	    		classMethodOffsets.get(currScope.getParent().getName()).clone();
	    } 
	    else{//This is a parent class - Its data structures should be initialized
	    	fieldOffsets = new LinkedHashMap<String, Integer>();
	    	methodOffsets = new LinkedHashMap<String, Integer>();
	    }
		    //Add the scope's data:
	    	//Add fields
		    for (Entry<String, Field> fieldSet : currScope.getFields().entrySet()) {
		    	int offSet = fieldOffsets.size() + 1;
		    	fieldOffsets.put(fieldSet.getKey(), offSet);
		    }
		    //Add methods
		    for (Entry<String, Method> methodSet : currScope.getMethods().entrySet()) {
		    	if ((methodSet.getValue() instanceof VirtualMethod)) {
		    		String parentName = currScope.getParent().getName();
		    		addToMethodOffsets(methodOffsets, methodSet.getKey(), currScope.getName(), parentName);
		    	}
		    }
		    //Finally, add the scope's data to the global data structure - 
		    //Here the classe's data is added to the maps
		    classMethodOffsets.put(currScope.getName(), methodOffsets);
		    classFieldOffsets.put(currScope.getName(), fieldOffsets);
	    } else {//This is the global scope - Analyze the dispatch table of all its classes!
		    for (Entry<String, ICClass> classSet : currScope.getClasses().entrySet()) {
		    	createDispatchTable(classSet.getValue().scope);
		    }
	    }
	}

	/**This method is responsible of changing the parent methods with inheriting methods, if necessary
	 * @param methodOffsets - The method offsets of the class, including what it inherited from parent, if exists
	 * @param methodName - The current added method's name 
	 * @param currClassName - The classe's name
	 * @param parentClassName - The parent name, if exists
	 */
	private static void addToMethodOffsets(LinkedHashMap<String, Integer> methodOffsets, String methodName, String currClassName, String parentClassName) {
		int offset = methodOffsets.size();
		String methodKey = "_" + parentClassName + "_#" + methodName;
		if (methodOffsets.containsKey(methodKey)) {
			offset = methodOffsets.get(methodKey);
			methodOffsets.remove(methodKey);
		}
		methodOffsets.put("_"+currClassName+"_#"+methodName, offset);
	}
	
	/**
	 * @param className - The classe's name
	 * @return - The string representing the entire dispatch table, in LIR format
	 */
	public static String getDispatchTable(String className) {
	    String result = getDispatchTableName(className) + ": [";
	    
	    LinkedHashMap<String, Integer> methodOffsets = classMethodOffsets.get(className);
	    if ( methodOffsets == null )
	      return "";
	    
	    String[] sortedNames = new String[methodOffsets.size()];
	    for ( String name : methodOffsets.keySet() ) {
	      String newName = name.replace("#", "");
	      sortedNames[methodOffsets.get(name)] = newName;
	    }
	    
	    for ( int i = 0; i < sortedNames.length-1; i++  )
	      result += sortedNames[i]+", ";
	    if ( sortedNames.length > 0 ) result += sortedNames[sortedNames.length-1];
	    result += "]";
	    return result;
	  }
	  
	  /**
	   * @return - The string for the classe's dispatch table, 
	   * 		   concatenated to the field offset's commented out table
	   */
	public static String printDispatchTable() {
	    String dispatchTable = "";
	    for ( String className : classMethodOffsets.keySet() ) {
	      if ( !( className.equals("Library") || className.equals("Global") ) )
	          dispatchTable += "# class " + className + "\n# Dispatch vector:\n" + 
	        		  getDispatchTable(className) + "\n" + getfieldOffsetsCommentString(className) + "\n";
	    }
	    return dispatchTable;
	  }


	  /**
	 * @param className - The classe's name
	 * @param methodName - The inspected method in the class
	 * @return The method's offset in the classe's methods
	 */
	public static int getMethodOffset(String className, String methodName) {
	    LinkedHashMap<String, Integer> methodOffsets = classMethodOffsets.get(className);
	    for ( String label : methodOffsets.keySet() ) {
	      if ( label.substring(label.indexOf('#')+1).equals(methodName) )
	        return methodOffsets.get(label);
	    }
	    return 0;
	    
	  }
	  
	  /**
	 * @param className - The classe's name
	 * @param fieldName - The inspected field in the class
	 * @return The fields's offset in the classe's fields
	 */
	public static int getFieldOffset(String className, String fieldName) {
		    return classFieldOffsets.get(className).get(fieldName);
	  }
	  
	  /**
	 * @param className - The classe's name
	 * @return  A comment table containing the fields offsets
	 */
	private static String getfieldOffsetsCommentString(String className) {
	    
	    LinkedHashMap<String, Integer> fieldOffests = classFieldOffsets.get(className);
	    if ( fieldOffests == null )
	      return "";
	    String comment = "# Field offsets\n";
	    String[] fields = new String[fieldOffests.size()];
	    for ( String name : fieldOffests.keySet() )
	      fields[fieldOffests.get(name)-1] = name;

	    for ( int i = 0; i < fieldOffests.size(); i++ ) {
	      comment += "# " + (i+1) + ": " + fields[i] + "\n";
	    }
	    return comment;
	  }
	  
	
	  
}


