package IC.SemanticChecks;

import IC.AST.ASTNode;

public class SemanticException extends Error  {
    private String msg;
    
    public SemanticException(String message) {
        msg = message;
    }
    
    public SemanticException(ASTNode node, String message) {
        msg = node.getLine() + ": semantic error; " + message;
    }
    
    @Override
    public String getMessage() {
        return msg;        
    }
}
