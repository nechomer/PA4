package IC.Parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

public class Scanner {

    public static void process(Reader rd, Collection<Token> tokens)
            throws LexicalError, IOException {
        Lexer lexer = new Lexer(rd);
        Token token;
        while ((token = lexer.next_token()) != null)
            tokens.add(token);
    }

}