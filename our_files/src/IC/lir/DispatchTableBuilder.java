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
	
	public static int getNumFields(String fieldName) {
		return classFieldOffsets.get(fieldName).size();
	}
	
	public static String getDispatchTableName(String name) {
	    return "_DV_" + name;
	}
	 
	public static void createDispatchTable(FrameScope currScope) {
		LinkedHashMap<String, Integer> fieldOffsets;
	    LinkedHashMap<String, Integer> methodOffsets;
	    
	    if (currScope.getType() != ScopeType.Global) {
	    //copying parent's dispatcher table if exists
	    if(currScope.getParent().getType() != ScopeType.Global){
	    	fieldOffsets = (LinkedHashMap<String, Integer>) 
	    		classFieldOffsets.get(currScope.getParent().getName()).clone();
	    	methodOffsets = (LinkedHashMap<String, Integer>) 
	    		classMethodOffsets.get(currScope.getParent().getName()).clone();
	    } 
	    else{
	    	fieldOffsets = new LinkedHashMap<String, Integer>();
	    	methodOffsets = new LinkedHashMap<String, Integer>();
	    }
		    
		    for (Entry<String, Field> fieldSet : currScope.getFields().entrySet()) {
		    	int offSet = fieldOffsets.size() + 1;
		    	fieldOffsets.put(fieldSet.getKey(), offSet);
		    }
		    
		    for (Entry<String, Method> methodSet : currScope.getMethods().entrySet()) {
		    	if ((methodSet.getValue() instanceof VirtualMethod)) {
		    		String parentName = currScope.getParent().getName();
		    		addToMethodOffsets(methodOffsets, methodSet.getKey(), methodSet.getValue(), currScope.getName(), parentName);
		    	}
		    }
		    
		    classMethodOffsets.put(currScope.getName(), methodOffsets);
		    classFieldOffsets.put(currScope.getName(), fieldOffsets);
	    } else {
		    for (Entry<String, ICClass> classSet : currScope.getClasses().entrySet()) {
		    	createDispatchTable(classSet.getValue().scope);
		    }
	    }
	}

	private static void addToMethodOffsets(LinkedHashMap<String, Integer> methodOffsets, String methodName, Method method, String currClassName, String parentClassName) {
		int offset = methodOffsets.size();
		String methodKey = "_" + parentClassName + "_#" + methodName;
		if (methodOffsets.containsKey(methodKey)) {
			offset = methodOffsets.get(methodKey);
			methodOffsets.remove(methodKey);
		}
		methodOffsets.put("_"+currClassName+"_#"+methodName, offset);
	}
	
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
	   * prints the dispatch vector
	   */
	  public static String printDVS() {
	    String dvs = "";
	    for ( String className : classMethodOffsets.keySet() ) {
	      if ( !( className.equals("Library") || className.equals("Global") ) )
	          dvs += "# class " + className + "\n# Dispatch vector:\n" + 
	        		  getDispatchTableName(className) + "\n" + getOffsetComment(className) + "\n";
	    }
	    return dvs;
	  }


	  /**
	   * return the method's offset
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
	   * returns a string containing the fields offsets
	   */
	  private static String getOffsetComment(String className) {
	    
	    LinkedHashMap<String, Integer> fto = classFieldOffsets.get(className);
	    if ( fto == null )
	      return "";
	    String comment = "# Field offsets\n";
	    String[] fields = new String[fto.size()];
	    for ( String name : fto.keySet() )
	      fields[fto.get(name)-1] = name;

	    for ( int i = 0; i < fto.size(); i++ ) {
	      comment += "# " + (i+1) + ": " + fields[i] + "\n";
	    }
	    return comment;
	  }
	  
	  public static int getFieldOffset(String className, String fieldName) {
	    return classFieldOffsets.get(className).get(fieldName);
	  }
	  
}


