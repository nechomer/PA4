package ic.ir;

import java.util.Arrays;

public class TacInstruction
{
	String op;
	TacValueRef[] args;
	
	
	public TacInstruction(String op, TacValueRef[] args)
	{
		this.op = op;
		this.args = args;
	}

	public String getOp() { return op; }
	public TacValueRef[] getArguments() { return args; }
	
	@Override
	public String toString()
	{
		return String.format("%s %s", op,
				join(" ", Arrays.asList(args)));
	}
	
	private String join(String sep, Iterable<?> bs)
	{
		StringBuilder j = new StringBuilder();
		for (Object item : bs) {
			if (j.length() > 0) j.append(sep);
			j.append(item);
		}
		return j.toString();
	}
	
	// Convenience (for Java 6)
	
	public TacInstruction(String op)
	{
		this(op, new TacValueRef[] {});
	}

	public TacInstruction(String op, TacValueRef arg0)
	{
		this(op, new TacValueRef[] {arg0});
	}

	public TacInstruction(String op, TacValueRef arg0, TacValueRef arg1)
	{
		this(op, new TacValueRef[] {arg0, arg1});
	}

	public TacInstruction(String op, TacValueRef arg0, TacValueRef arg1, TacValueRef arg2)
	{
		this(op, new TacValueRef[] {arg0, arg1, arg2});
	}

	
}
