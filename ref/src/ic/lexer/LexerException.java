package ic.lexer;
@SuppressWarnings("serial")
public class LexerException extends Error {
    public LexerException(int line, int column, String message) {
        super(line + ":" + column + " : lexical error; " + message);
    }
}
