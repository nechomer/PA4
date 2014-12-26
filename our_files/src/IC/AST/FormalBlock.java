package IC.AST;

import java.util.ArrayList;
import java.util.List;

public class FormalBlock extends ASTNode {

	private List<Formal> formals = new ArrayList<Formal>();
	public FormalBlock(int line, Formal formal) {
		super(line);
		formals.add(formal);
		// TODO Auto-generated constructor stub
	}

	public void addFormal(Formal formal) {
		formals.add(formal);
	}
	
	@Override
	public Object accept(Visitor visitor) {
		// TODO Auto-generated method stub
		return null;
	}

}
