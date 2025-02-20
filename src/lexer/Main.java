package lexer;

import lexer.*;
import parser.Parser;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Sample source code with:
        // - Declaration of main function.
        // - Declaration of variables.
        // - An assignment statement with arithmetic operators.
        // - A conditional statement with relational operator and blocks.
        // Note: In our language, 'print' is used in place of cout, and 'fi' and 'esle' replace if/else.
        String sourceCode = "int main() { int a = 5; float b = 3.14; bool c = 1; char d = a; print(a); fi(a>0){print(a);} esle{print(b);} }";

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(sourceCode);

        System.out.println("Tokens:");
        for (Token t : tokens) {
            System.out.println(t);
        }
        System.out.println("Total Tokens:"+(tokens.size()));
        Parser parser = new Parser();
        parser.parse(tokens);
    }
}
