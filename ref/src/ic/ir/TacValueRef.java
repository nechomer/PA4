package ic.ir;

public class TacValueRef {
	public static enum Kind {
		IMMEDIATE, LOCAL, LABEL
	}

	public final Kind kind;
	public final int val; // for IMMEDIATE, LOCAL
	public final String name; // for LABEL

	public TacValueRef(Kind kind, int value_or_index) {
		this.kind = kind;
		this.val = value_or_index;
		this.name = null;
	}

	public TacValueRef(Kind kind, String name) {
		this.kind = kind;
		this.val = 0;
		this.name = name;
	}

	@Override
	public String toString() {
		switch (kind) {
		case IMMEDIATE:
			return String.format("%d", val);
		case LOCAL:
			return String.format("$%d", val);
		case LABEL:
			return name == null ? String.format(":%d", val) : ":" + name;
		default:
			return "?";
		}
	}

	// Convenience

	public static TacValueRef imm(int literalValue) {
		return new TacValueRef(Kind.IMMEDIATE, literalValue);
	}

	public static TacValueRef loc(int index) {
		return new TacValueRef(Kind.LOCAL, index);
	}

	public static TacValueRef lbl(int index) {
		return new TacValueRef(Kind.LABEL, index);
	}

	public static TacValueRef lbl(String name) {
		return new TacValueRef(Kind.LABEL, name);
	}
}
