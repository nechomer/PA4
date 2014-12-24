package ic.lexer;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

public class Lexer {

    public static void process(Reader rd, Collection<Token> outTokens)
            throws LexerException, IOException {
        Token token;
        Scanner scanner = new Scanner(rd);

        while ((token = scanner.yylex()) != null)
            outTokens.add(token);
    }

}