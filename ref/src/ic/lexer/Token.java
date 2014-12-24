package ic.lexer;
public class Token extends fun.grammar.Word {
    public int column;
    public int line;
    public String value;

    public Token(int line, int column, String tag, String value) {
        super(tag);
        
        this.line = line;
        this.column = column;
        this.value = value;
    }
}
