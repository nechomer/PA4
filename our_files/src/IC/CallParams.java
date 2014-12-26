package IC;

import java.util.List;

import IC.AST.Expression;

public class CallParams {

	private Expression location = null;
	private String className = null;
	private String methodName = null;
	private List<Expression> arguments = null;
	private int line = -1;
	
	public CallParams() {
		
	}

	public Expression getLocation() {
		return location;
	}

	public void setLocation(Expression location) {
		this.location = location;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	public void setArguments(List<Expression> arguments) {
		this.arguments = arguments;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

}
