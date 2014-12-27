package IC.lir;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import IC.SymbolTable.Kind;
import IC.SymbolTable.Symbol;
import IC.SymbolTable.SymbolTable;

/**
 * Builds the dispatch vector and function offsets data structure
 * 
 * @author Asaf Bruner, Aviv Goll
 */
public class DVCreator {

	public static Map<String, LinkedHashMap<String, Integer>> classToMTO = new LinkedHashMap<>();
	public static Map<String, LinkedHashMap<String, Integer>> classToFTO = new LinkedHashMap<>();
	
	
	
	public static int getNumFields(String name) {
		return classToFTO.get(name).size();
	}

	public static String getDVName(String name) {
		return "_DV_" + name;
	}

	/**
	 * for every symbol table populates the appropriate fto, mto data structures.
	 */
	@SuppressWarnings("unchecked")
	public static void createDV(SymbolTable st) {
		
		LinkedHashMap<String, Integer> fto;
		LinkedHashMap<String, Integer> mto;
		
		//copying parent st DV
		if(st.getParentSymbolTable() != null){ //global st
			fto = (LinkedHashMap<String, Integer>) 
					classToFTO.get(st.getParentSymbolTable().getId()).clone();
			mto = (LinkedHashMap<String, Integer>) 
					classToMTO.get(st.getParentSymbolTable().getId()).clone();
		} 
		else{
			fto = new LinkedHashMap<String, Integer>();
			mto = new LinkedHashMap<String, Integer>();
		}
		
		classToFTO.put(st.getId(), fto );
		classToMTO.put(st.getId(), mto);
		
		for(Entry<String, Symbol> e : st.getEntries().entrySet()){
			Symbol s = e.getValue();
			if(s.getKind() == Kind.METHOD && !s.isStatic()){
				insertToMTO(mto, st, s.getId());
			}
			if(s.getKind() == Kind.FIELD) {
				int nextOffset = fto.size()+1;
				fto.put(s.getId(), nextOffset);
			}
		}
		
		//Add functions and fields to the data structures
		for(SymbolTable symTab : st.getChilds()) {
			if ( symTab.getKind() == Kind.CLASS)
				createDV(symTab);
		}
		
	}

	/**
	 * populates mto 
	 */
	private static void insertToMTO(LinkedHashMap<String, Integer> mto,
			SymbolTable symTab,String methodName) {
		
		int offset = mto.size();
		
		// check if the method overrides another
		SymbolTable parent = symTab.getParentSymbolTable();
		String key = null;
		while ( parent != null ) {
			if ( parent.lookin(methodName) != null ) {
				key = "_" + parent.getId() + "_#" + methodName;
				break;
			}
			parent = parent.getParentSymbolTable();
		}
		
		// if the method was found
		if ( key != null ) {
			offset = mto.get(key);
			mto.remove(key);
		}
		
		mto.put("_"+symTab.getId()+"_#"+methodName, offset);
	}
	
	/**
	 * returns a stirng containing the dispatch vector
	 */
	public static String getDV(String className) {
		String res = getDVName(className) + ": [";
		
		LinkedHashMap<String, Integer> mto = classToMTO.get(className);
		if ( mto == null )
			return "";
		
		String[] sortedNames = new String[mto.size()];
		for ( String name : mto.keySet() ) {
			String newName = name.replace("#", "");
			sortedNames[mto.get(name)] = newName;
		}
		
		for ( int i = 0; i < sortedNames.length-1; i++  )
			res += sortedNames[i]+", ";
		if ( sortedNames.length > 0 )
			res += sortedNames[sortedNames.length-1];
		res += "]";
		return res;
	}
	
	/**
	 * prints the dispatch vector
	 */
	public static String printDVS() {
		String dvs = "";
		for ( String className : classToMTO.keySet() ) {
			if ( !( className.equals("Library") || className.equals("Global") ) )
					dvs += "# class " + className + "\n# Dispatch vector:\n" + 
							getDV(className) + "\n" + getOffsetComment(className) + "\n";
		}
		return dvs;
	}


	/**
	 * return the method's offset
	 */
	public static int getMethodOffset(String className, String methodName) {
		LinkedHashMap<String, Integer> mto = classToMTO.get(className);
		for ( String label : mto.keySet() ) {
			if ( label.substring(label.indexOf('#')+1).equals(methodName) )
				return mto.get(label);
		}
		return 0;
		
	}

	/**
	 * returns a string containing the fields offsets
	 */
	private static String getOffsetComment(String className) {
		
		LinkedHashMap<String, Integer> fto = classToFTO.get(className);
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
		return classToFTO.get(className).get(fieldName);
	}
}
