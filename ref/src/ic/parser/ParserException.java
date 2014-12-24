package ic.parser;

@SuppressWarnings("serial")
public class ParserException extends RuntimeException {
    private String msg;
    
    public ParserException(String message) {
        msg = message;
    }
    
    public ParserException(int line, int column, String message) {
        if (line == -1 && column == -1)
            msg = "at end of input";
        else
            msg = line + ":" + column;
        
        msg += " : syntax error; " + message;
    }
    
    @Override
    public String getMessage() {
        return msg;        
    }

}
