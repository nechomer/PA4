package IC.AST;

import java.util.List;


public class Library extends ASTNode {

	private List<LibraryMethod> dList;
	
	public Library(int line, List<LibraryMethod> dList) {
		super(line);
		this.dList = dList;
	}

	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	public List<LibraryMethod> getMethodList() {
		return dList;
	}

}
