package IC.SymbolTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.ICVoid;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.Call;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.Expression;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.Library;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.While;
import IC.SemanticChecks.SemanticError;
import IC.Types.ArrayType;
import IC.Types.ClassType;
import IC.Types.MethodType;
import IC.Types.Type;
import IC.Types.TypeTable;

/**
 * A visitor implementation that builds the symbol table for the program compilation.
 * Simultaneously it also does the scope checking.
 *  
 * @author Asaf Bruner, Aviv Goll
 */
public class SymbolTableConstructor implements IC.AST.Visitor{

	private SymbolTable currentTable;
	private SymbolTable global;
	private SymbolTable libraryTable;
	private Map<Call, SymbolTable> unresolved = new HashMap<>();
	//private boolean unresCheck = false;
	private boolean isFieldMethodInserted = false;
	private int uid = 0;
	private static int BlockNumber = 0;
	
	private void errorHandler(SemanticError e){
		System.err.println(e.getMessage());
		System.exit(1);
	}
	
	@Override
	public Object visit(Program program) {
		SymbolTable globalTable = new SymbolTable("Global", null);
		setGlobal(globalTable);
		setCurrentTable(globalTable);
		program.setEnclosingScope(globalTable);
		
		// Build Type-Table and global Symbol table entries
		for ( ICClass c : program.getClasses() ){
			try {
				TypeTable.addUserType(c);
				ClassType ct = TypeTable.getUserType(c.getName());
				if ( ct == null )
					errorHandler(new SemanticError(c.getLine(), "Class not found: " + c.getName()));
				Symbol classSymbol = null;
				try {
					classSymbol = new Symbol(c.getName(), ct, Kind.CLASS, c.getLine(), c.getName());
					globalTable.insertSymbol(classSymbol);
				} catch (SemanticError e) {
					errorHandler(e);
				}
				
			} catch (SemanticError e) {
				errorHandler(e);
			}
		}
		// Build class symbol tables
		for ( ICClass c : program.getClasses() ){
			c.accept(this);
		}
		
		// Add the library symbol table
		setLibraryTable( (SymbolTable) program.getLibrary().accept(this) );
		
		// Build symbol tables for methods and code-blocks + scope checking
		isFieldMethodInserted = true;
		for ( ICClass c : program.getClasses() ){
			c.accept(this);
		}

		// Make sure no unresolved calls were made
		try {
			checkUnresolved();
		} catch (SemanticError e) {
			errorHandler(e);
		}

		return globalTable;
	}

	/**
	 * @throws SemanticError 
	 * 
	 */
	private void checkUnresolved() throws SemanticError {
//		setUnresolvedChecking();
//		Map<Call, SymbolTable> oldUnRes = getUnresolved();
//		setUnresolved(new HashMap<Call, SymbolTable>() );
//		for ( Call call : oldUnRes.keySet() ){
//			SymbolTable env = oldUnRes.get(call);
//			setCurrentTable(env);
//			call.accept(this);
//		}
		if ( getUnresolved().size() > 0 ){
			Call unresCall = getUnresolved().keySet().iterator().next();
			throw new SemanticError( unresCall.getLine(), 
					"Unresolved call to: " + unresCall);
		}
	}

//	private void setUnresolvedChecking() {
//		unresCheck  = true;
//	}

	@Override
	public Object visit(ICClass icClass) {
		if ( ! isFieldMethodInserted ) {
			SymbolTable table = new SymbolTable(icClass.getName(), Kind.CLASS);
			TypeTable.getUserType(icClass.getName()).setSymbolTable(table);
			if ( icClass.hasSuperClass() ) {
//				Symbol parentSym = getGlobal().lookup(icClass.getSuperClassName() );
//				if ( parentSym == null )
//					errorHandler(new SemanticError(icClass.getLine(), "Class not defined: " + icClass.getSuperClassName()));
//				SymbolTable pst = parentSym.getRelatedSymTab();
				SymbolTable pst = TypeTable.getClassSymTab(icClass.getSuperClassName());
				if ( pst == null )
					errorHandler(new SemanticError(icClass.getLine(), "Unknown super class: " + icClass.getSuperClassName()));
				pst.getChilds().add(table);
				table.setParentSymbolTable(pst);
			} else {
				getGlobal().getChilds().add(table);
				table.setParentSymbolTable(getGlobal());
			}
			icClass.setEnclosingScope(table);
			setCurrentTable(table);
			for ( Field f : icClass.getFields() ){
				f.accept(this);
			}
			for ( Method m : icClass.getMethods() ){
				m.accept(this);
			}
			setCurrentTable(table.getParentSymbolTable());
			return table;
		} else {
			SymbolTable table = TypeTable.getClassSymTab(icClass.getName());
			setCurrentTable(table);
//			for ( Field f : icClass.getFields() ){
//				f.accept(this);
//			}
			for ( Method m : icClass.getMethods() ){
				m.accept(this);
			}
			setCurrentTable(table.getParentSymbolTable());
			return table;
		}
	}

	@Override
	public Object visit(Library library) {
		SymbolTable table = new SymbolTable("Library", Kind.CLASS);
		if ( getGlobal().lookup("Library") != null )
			errorHandler(new SemanticError(library.getLine(),"Can't override Library class"));
		getGlobal().getChilds().add(table);
		table.setParentSymbolTable(getGlobal());
		setCurrentTable(table);
		library.setEnclosingScope(table);
		for ( LibraryMethod m : library.getMethodList() ){
			m.accept(this);
		}
		setCurrentTable(table.getParentSymbolTable());
		return table;
	}


	@Override
	public Object visit(Field field) {
		// make sure the field is not already declared in the class hierarchy
		if ( getCurrentTable().lookup(field.getName()) != null )
			errorHandler(new SemanticError(field.getLine(), "Identifier already declared: "+field.getName()));
		else {
			Symbol fSymbol = null;
			try {
				String uid = field.getName() + "_" + getUniqueID();
				fSymbol = new Symbol(field.getName(), TypeTable.astType(field.getType()),
						Kind.FIELD, field.getLine(), uid);
				getCurrentTable().insertSymbol(fSymbol);		
			} catch (SemanticError e) {
				errorHandler(e);
			}
		}
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		if ( ! isFieldMethodInserted ) {
			
			
			MethodType mt = TypeTable.methodType(method);
			
			Symbol prevDec = getCurrentTable().lookup(method.getName());
			if ( prevDec != null ) { 
				// mothod has been declared in the class hierarchy. verify correct overriding
				if ( prevDec.isStatic() ) 
					errorHandler(new SemanticError(method.getLine(), 
							"Previous decleration of " + method.getName() + " is static"));
				else if ( prevDec.getType().getID() != mt.getID() )
					errorHandler(new SemanticError(method.getLine(), 
							"Cannot overload previous decleration of " + method.getName()));
				
			}
			Symbol fSymbol = null;
			try {
				String uid = method.getName() +"_" + getUniqueID();
				fSymbol = new MethodSymbol(method.getName(), mt, Kind.METHOD, method.getLine(), method.getFormals(), uid);
				getCurrentTable().insertSymbol(fSymbol);		
			} catch (SemanticError e) {
				errorHandler(e);
			}
		} else {
			SymbolTable table = new SymbolTable(method.getName(), Kind.METHOD);
			SymbolTable superScope = getCurrentTable().getParentSymbolTable();
			method.setEnclosingScope(table);
			getCurrentTable().getChilds().add(table);
			table.setParentSymbolTable(currentTable);
			String thisTypeName = getCurrentTable().getId();
			setCurrentTable(table);
			
			// Add 'this' entry
			try {
				Type thisType = TypeTable.getUserType(thisTypeName);
				table.insertSymbol(new Symbol("this", thisType, Kind.AUTOMATIC, method.getLine(), "this"));
			} catch (SemanticError e) {
				errorHandler(e);
			}
			
			// If necessary add '$ret' entry
			try {
				Type retType = TypeTable.astType(method.getType());
				if ( retType != TypeTable.voidType ){
						table.insertSymbol(new Symbol("$ret", retType, Kind.AUTOMATIC, method.getLine(), "$ret"));
				}
			} catch (SemanticError e) {
				errorHandler(e);
			}
				
			MethodSymbol prevDec = (MethodSymbol) superScope.lookup(method.getName());
			if ( prevDec != null ) {
				method.setFormalUIDs( prevDec.getFormalUIDs() );
			}
			for ( Formal f : method.getFormals() ){
				f.accept(this);
			}
			
			for(Statement s : method.getStatements()){
				s.accept(this);
			}
			setCurrentTable(table.getParentSymbolTable());
		}
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		if ( ! isFieldMethodInserted ) {
//			boolean libMethod = getCurrentTable().getId().equals("Library");
//			if(libMethod)
//				mt.setLibraryMethod(true);
			Symbol fSymbol = null;
			MethodType mt = TypeTable.methodType(method);
			Symbol prevDec = getCurrentTable().lookup(method.getName());
			if ( prevDec != null ) {
				// mothod has been declared in the class hierarchy. verify correct hiding
				if ( ! prevDec.isStatic() ) 
					errorHandler(new SemanticError(method.getLine(), 
							"Previous decleration of " + method.getName() + " is virtual"));
				else if ( prevDec.getType().getID() != mt.getID() )
					errorHandler(new SemanticError(method.getLine(), 
							"Cannot overload previous decleration of " + method.getName()));
			}
			try {
				String uid = method.getName() +"_" + getUniqueID();
				fSymbol = new MethodSymbol(method.getName(), mt, Kind.METHOD, method.getLine(), method.getFormals(), uid);
				fSymbol.setStatic(true);
				getCurrentTable().insertSymbol(fSymbol);		
			} catch (SemanticError e) {
				errorHandler(e);
			}
		} else {
			SymbolTable table = new SymbolTable(method.getName(), Kind.METHOD);
			//fSymbol.setRelatedSymTab(table);
			method.setEnclosingScope(table);
			getCurrentTable().getChilds().add(table);
			table.setParentSymbolTable(getGlobal());
			SymbolTable oldCurrentTable = getCurrentTable();
			setCurrentTable(table);

			// If necessary add '$ret' entry
			try {
				Type retType = TypeTable.astType(method.getType());
				if ( retType != TypeTable.voidType ){
						table.insertSymbol(new Symbol("$ret", retType, Kind.AUTOMATIC, method.getLine(), "$ret"));
				}
			} catch (SemanticError e) {
				errorHandler(e);
			}

			for ( Formal f : method.getFormals() ){
				f.accept(this);
			}
			
			for(Statement s : method.getStatements()){
				s.accept(this);
			}
			setCurrentTable(oldCurrentTable);
		}
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) {
		if ( ! isFieldMethodInserted ) {
			Symbol fSymbol = null;
			try {
				MethodType mt = TypeTable.methodType(method);
				mt.setLibraryMethod(true);
				String uid = method.getName() + "_" + getUniqueID();
				fSymbol = new Symbol(method.getName(), mt, Kind.METHOD, method.getLine(), uid);
				fSymbol.setStatic(true);
				getCurrentTable().insertSymbol(fSymbol);		
			} catch (SemanticError e) {
				errorHandler(e);
			}
		} else {
			SymbolTable table = new SymbolTable(method.getName(), Kind.METHOD);
			//fSymbol.setRelatedSymTab(table);
			method.setEnclosingScope(table);
			getCurrentTable().getChilds().add(table);
			table.setParentSymbolTable(getGlobal());
			SymbolTable oldCurrentTable = getCurrentTable();
			setCurrentTable(table);

			// If necessary add '$ret' entry
			try {
				Type retType = TypeTable.astType(method.getType());
				if ( retType != TypeTable.voidType ){
						table.insertSymbol(new Symbol("$ret", retType, Kind.AUTOMATIC, method.getLine(), "$ret"));
				}
			} catch (SemanticError e) {
				errorHandler(e);
			}

			for ( Formal f : method.getFormals() ){
				f.accept(this);
			}
			setCurrentTable(oldCurrentTable);
		}
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		formal.setEnclosingScope(getCurrentTable());
		Symbol sym = null;
		try {
			String uid;
			if ( formal.getLirName() != null )
				uid = formal.getLirName();
			else
				uid = formal.getName() + "_" + getUniqueID();
			sym = new Symbol(formal.getName(), TypeTable.astType(formal.getType()), 
					Kind.PARAMETER, formal.getLine(), uid);
			formal.setLirName(uid);
			getCurrentTable().insertSymbol(sym);
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return sym;
	}

	@Override
	public Object visit(PrimitiveType type) {
//		if ( type.getDimension() > 0 ){
//			try {
//				TypeTable.arrayType(TypeTable.astType(type), type.getDimension());
//			} catch (SemanticError e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		return null;
	}

	@Override
	public Object visit(UserType type) {
//		if ( type.getDimension() > 0 ){
//			try {
//				TypeTable.arrayType(TypeTable.astType(type), type.getDimension());
//			} catch (SemanticError e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		assignment.setEnclosingScope(getCurrentTable());
		assignment.getExp().accept(this);
		assignment.getVariable().accept(this);
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		callStatement.setEnclosingScope(getCurrentTable());
		callStatement.getCall().accept(this);
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		returnStatement.setEnclosingScope(getCurrentTable());
		
		if ( returnStatement.hasValue() ) {
			Expression exp = returnStatement.getValue();
			exp.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.setEnclosingScope(getCurrentTable());
		ifStatement.getCondition().accept(this);
		ifStatement.getOperation().accept(this);
		if ( ifStatement.hasElse() )
			ifStatement.getElseOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		whileStatement.setEnclosingScope(getCurrentTable());
		whileStatement.getCondition().accept(this);
		whileStatement.getOperation().accept(this);
		
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		breakStatement.setEnclosingScope(getCurrentTable());
		// We've been through this... 
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		continueStatement.setEnclosingScope(getCurrentTable());
		// We've been through this... 
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		SymbolTable table = new SymbolTable("statement block in " + currentTable.getId(), Kind.BLOCK);
		statementsBlock.setEnclosingScope(table);
		getCurrentTable().getChilds().add(table);
		table.setParentSymbolTable(getCurrentTable());
		try {
			String uid = "Block_"+(++BlockNumber);
			Symbol sym = new Symbol(uid, null, Kind.BLOCK, statementsBlock.getLine(), uid);
			getCurrentTable().insertSymbol(sym);
		} catch (SemanticError e) {
			errorHandler(e);
		}
		setCurrentTable(table);
		for(Statement s : statementsBlock.getStatements()){
			s.accept(this);
		}
		setCurrentTable(table.getParentSymbolTable());
		return table;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		localVariable.setEnclosingScope(getCurrentTable());
		if ( localVariable.hasInitValue() )
			localVariable.getInitValue().accept(this);
		Symbol sym = null;
		try {
			Type varType = TypeTable.astType(localVariable.getType());
			if ( varType == null )
				throw new SemanticError(localVariable.getLine(), "Unknown type: "+ localVariable.getType().getName());
			String uid = localVariable.getName() + "_" + getUniqueID();
			sym = new Symbol(localVariable.getName(), varType, 
					Kind.VARIABLE, localVariable.getLine(), uid );
			localVariable.setUID(uid);
			getCurrentTable().insertSymbol(sym);
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return sym;
	}

	private int getUniqueID() {
		
		return ++uid ;
	}

	@Override
	public Object visit(VariableLocation location) {
		//location.setEnclosingScope(getCurrentTable());
		Symbol locSym = null;
		if ( location.isExternal() ) {
			location.getLocation().accept(this);
			SymbolTable leftSymTab = location.getLocation().getTypeScope();
			if ( leftSymTab == null ) {
				errorHandler(new SemanticError(location.getLine(), "Cannot find field in external location"));
			}
			locSym = location.getLocation().getTypeScope().lookup(location.getName());
		} else {
			locSym = getCurrentTable().lookup(location.getName());
		}
		if ( locSym == null )
			errorHandler(new SemanticError(location.getLine(), "location not found: " + location.getName()));
		location.setEnclosingScope(getCurrentTable().lookForScope(locSym));
		SymbolTable locationTypeSymTab = TypeTable.getClassSymTab(locSym.getType().getName());
		//location.setEnclosingScope(locationTypeSymTab);
		Type t = locSym.getType();
		while ( t instanceof ArrayType ) 
			t = ((ArrayType)t).getElemType();
		
		if ( t instanceof ClassType ) {
			location.setTypeScope( ((ClassType)t).getSymbolTable() );
		}
		
		return locationTypeSymTab;
	}

	@Override
	public Object visit(ArrayLocation location) {
		location.setEnclosingScope(getCurrentTable());
		location.getArray().accept(this);
		location.getIndex().accept(this);
		location.setTypeScope(location.getArray().getTypeScope());
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		call.setEnclosingScope(getCurrentTable());
		for ( Expression exp : call.getArguments() )
			exp.accept(this);

		SymbolTable classST;
		if ( ! call.getClassName().equals( "Library" ) ) {
			
			if ( getGlobal().lookup(call.getClassName()) == null ) {
				//errorHandler(new SemanticError(call.getLine(), "Class not found: " + call.getClassName()));
				getUnresolved().put(call, currentTable);
				return null;
			}
			classST = TypeTable.getClassSymTab(call.getClassName());
			
		} 
		else {
			classST = getLibraryTable();
		}
		
		if ( (classST == null ) || (classST.lookup(call.getName()) == null) ) {
			getUnresolved().put(call, currentTable);
		} else 
			if( (classST.lookup(call.getName()) != null) 
					&& (!classST.lookup(call.getName()).isStatic())){
			errorHandler(new SemanticError(call.getLine(), "Could find static method: " + call.getClassName() + "." +call.getName()));
		}
		
		call.setTypeScope( TypeTable.getClassSymTab(
				((MethodType)classST.lookup(call.getName())
						.getType()).getReturnType().getName() ) );
		return null;
	}

//	private boolean isInCheckUnRes() {
//		return unresCheck;
//	}

	@Override
	public Object visit(VirtualCall call) {
		call.setEnclosingScope(getCurrentTable());

		// propagating to the location and arguments
		if ( call.isExternal() )
			call.getLocation().accept(this);
		for ( Expression exp : call.getArguments() )
			exp.accept(this);
		
		// Scope checking
		SymbolTable declerationTable = null;
		if ( call.isExternal() ) {
			declerationTable = call.getLocation().getTypeScope();
			Symbol methodSym = declerationTable.lookup(call.getName());
			if ( methodSym == null )
				getUnresolved().put(call, currentTable);
			else if ( methodSym.isStatic() ){
				errorHandler(new SemanticError(call.getLine(), "Cannot call static method: "+call.getName()+", as a virtual method."));				 
			}
		} else  {
			declerationTable = currentTable;
			Symbol methodSymbol = currentTable.lookup(call.getName());
			if ( methodSymbol == null )
				getUnresolved().put(call, currentTable);
			else if ( methodSymbol.isStatic() ) 
				errorHandler(new SemanticError(call.getLine(), "Cannot call static method: "+call.getName()+", as a virtual method."));
		}
		
		if(declerationTable.lookup(call.getName()) == null)
			errorHandler(new SemanticError(call.getLine(), "Virtual method: "+call.getName()+" not found"));
			
		call.setTypeScope( TypeTable.getClassSymTab(
				((MethodType)declerationTable.lookup(call.getName())
						.getType()).getReturnType().getName() ) );
	
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		Symbol thisSym = getCurrentTable().lookup("this");
		if ( thisSym == null )
			errorHandler(new SemanticError(thisExpression.getLine(), "'this' object is undefined here"));
		SymbolTable thisTypeSymTab = TypeTable.getClassSymTab(thisSym.getType().getName()); 
		thisExpression.setEnclosingScope(thisTypeSymTab);
		thisExpression.setTypeScope(thisTypeSymTab);
		return thisTypeSymTab;
	}

	@Override
	public Object visit(NewClass newClass) {
		newClass.setEnclosingScope(getCurrentTable());
		if ( TypeTable.getUserType(newClass.getName()) == null )
			errorHandler(new SemanticError(newClass.getLine(), "No such type: " + newClass.getName()));
		newClass.setTypeScope(TypeTable.getUserType(newClass.getName()).getSymbolTable());
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		newArray.setEnclosingScope(getCurrentTable());
		newArray.getSize().accept(this);
		ArrayType arrt = null;
		try {
			arrt = TypeTable.arrayType(TypeTable.astType(newArray.getType()), newArray.getType().getDimension());
		} catch (SemanticError e) {
			errorHandler(e);
		}
		while ( arrt.getElemType() instanceof ArrayType )
			arrt = (ArrayType) arrt.getElemType();
		if ( arrt.getElemType() instanceof ClassType ) {
			newArray.setTypeScope( ((ClassType)arrt.getElemType()).getSymbolTable() );
		}
		return null;
	}

	@Override
	public Object visit(Length length) {
		length.getArray().accept(this);
		length.setEnclosingScope(getCurrentTable());
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		binaryOp.setEnclosingScope(getCurrentTable());
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		binaryOp.setEnclosingScope(getCurrentTable());
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		unaryOp.setEnclosingScope(getCurrentTable());
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		unaryOp.setEnclosingScope(getCurrentTable());
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		literal.setEnclosingScope(getCurrentTable());
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		expressionBlock.setEnclosingScope(getCurrentTable());
		expressionBlock.getExpression().accept(this);
		expressionBlock.setTypeScope(expressionBlock.getExpression().getTypeScope());
		return null;
	}

	@Override
	public Object visit(ICVoid icVoid) {
		icVoid.setEnclosingScope(getCurrentTable());
		return null;
	}

	public SymbolTable getCurrentTable() {
		return currentTable;
	}

	public void setCurrentTable(SymbolTable currentTable) {
		this.currentTable = currentTable;
	}

	public SymbolTable getGlobal() {
		return global;
	}

	public void setGlobal(SymbolTable global) {
		this.global = global;
	}

	public Map<Call, SymbolTable> getUnresolved() {
		return unresolved;
	}

	public void setUnresolved(Map<Call, SymbolTable> unresolved) {
		this.unresolved = unresolved;
	}

	public SymbolTable getLibraryTable() {
		return libraryTable;
	}

	public void setLibraryTable(SymbolTable libraryTable) {
		this.libraryTable = libraryTable;
	}

}
