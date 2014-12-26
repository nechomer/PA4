package IC.AST;

import java.util.ArrayList;
import java.util.List;

public class LibraryMethodBlock extends ASTNode {

	private List<Method> methods = new ArrayList<Method>();
	
	public LibraryMethodBlock(int line, Method startingMethod) {
		super(line);
		methods.add(startingMethod);
		// TODO Auto-generated constructor stub
	}
	
	public void addMethod(Method method){
		methods.add(method);
	}

	@Override
	public Object accept(Visitor visitor) {
		// TODO Auto-generated method stub
		return null;
	}

}
