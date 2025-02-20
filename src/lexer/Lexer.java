package lexer;

import automata.*;
import errorhandler.ErrorHandler;
import java.util.*;

public class Lexer {
    private Map<TokenType, String> tokenPatterns;

    public Lexer() {
        // Order matters.
        tokenPatterns = new LinkedHashMap<>();
        tokenPatterns.put(TokenType.KEYWORD, "int|float|char|bool|main|print|fi|esle");
        tokenPatterns.put(TokenType.FLOAT, "[0-9]+\\.[0-9]{1,5}");
        tokenPatterns.put(TokenType.INTEGER, "[0-9]+");
        tokenPatterns.put(TokenType.IDENTIFIER, "[a-z]+");
        tokenPatterns.put(TokenType.PUNCTUATION, "[(){};]");
        tokenPatterns.put(TokenType.OPERATOR, "[+\\-*/=><]");
        tokenPatterns.put(TokenType.BOOLEAN, "true|false");
        tokenPatterns.put(TokenType.CHARACTER, "[a-z]");  
        tokenPatterns.put(TokenType.LITERAL, "\"[^\"]*\"");
        tokenPatterns.put(TokenType.COMMENT, "//.*|/\\*.*?\\*/");
    }

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int position = 0;
        while (position < input.length()) {
            if (Character.isWhitespace(input.charAt(position))) {
                position++;
                continue;
            }
            boolean matched = false;
            for (Map.Entry<TokenType, String> entry : tokenPatterns.entrySet()) {
                TokenType type = entry.getKey();
                String pattern = entry.getValue();
                NFA nfa = new NFA(pattern);
                DFA dfa = nfa.toDFA();
                int startPos = position;
                State currentState = dfa.getStartState();
                State lastAcceptingState = null;
                int lastAcceptingPos = startPos;
                while (position < input.length()) {
                    State nextState = dfa.move(currentState, input.charAt(position));
                    if (nextState == null) break;
                    currentState = nextState;
                    position++;
                    if (dfa.isAccepting(currentState)) {
                        lastAcceptingState = currentState;
                        lastAcceptingPos = position;
                    }
                }
                if (lastAcceptingState != null) {
                    String tokenValue = input.substring(startPos, lastAcceptingPos);
                    tokens.add(new Token(type, tokenValue));
                    position = lastAcceptingPos;
                    matched = true;
                    break;
                } else {
                    position = startPos;
                }
            }
            if (!matched) {
                ErrorHandler.reportWarning("Skipping unrecognized char '" + input.charAt(position) 
                        + "' at position " + position);
                position++;
            }
        }
        return tokens;
    }
}
