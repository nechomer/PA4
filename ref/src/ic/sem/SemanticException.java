package ic.sem;

public class SemanticException extends Error  {
    private String msg;
    
    public SemanticException(String message) {
        msg = message;
    }
    
    public SemanticException(ic.ast.Node node, String message) {
        msg = node.getLine() + ": semantic error; " + message;
    }
    
    @Override
    public String getMessage() {
        return msg;        
    }
}
