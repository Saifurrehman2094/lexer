package parser;

import lexer.*;
import symboltable.SymbolTable;
import errorhandler.ErrorHandler;
import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int pos;
    private SymbolTable symbolTable;

    public Parser() {
        symbolTable = new SymbolTable();
    }

    public void parse(List<Token> tokens) {
        this.tokens = tokens;
        pos = 0;
        try {
            parseProgram();
            System.out.println("Parsing completed successfully.");
            symbolTable.display();
        } catch (Exception e) {
            ErrorHandler.reportError(e.getMessage());
        }
    }

    private Token currentToken() {
        if (pos < tokens.size()) return tokens.get(pos);
        return null;
    }

    private Token peekToken() {
        if (pos + 1 < tokens.size()) return tokens.get(pos + 1);
        return null;
    }

    private void consume() {
        pos++;
    }

    // Program -> Type main "(" ")" "{" Statements "}"
    private void parseProgram() throws Exception {
        String type = parseType();
        Token mainToken = currentToken();
        if (mainToken == null || mainToken.getType() != TokenType.KEYWORD || 
            !mainToken.getValue().equals("main")) {
            throw new Exception("Program must start with main");
        }
        consume(); // consume "main"
        expect(TokenType.PUNCTUATION, "(");
        expect(TokenType.PUNCTUATION, ")");
        expect(TokenType.PUNCTUATION, "{");
        parseStatements();
        expect(TokenType.PUNCTUATION, "}");
    }

    private String parseType() throws Exception {
        Token t = currentToken();
        if (t == null || t.getType() != TokenType.KEYWORD) {
            throw new Exception("Expected a type but found " + (t == null ? "EOF" : t.getValue()));
        }
        if (!(t.getValue().equals("int") || t.getValue().equals("float") ||
              t.getValue().equals("char") || t.getValue().equals("bool"))) {
            throw new Exception("Invalid type: " + t.getValue());
        }
        consume();
        return t.getValue();
    }

    // Statements -> Statement*
    private void parseStatements() throws Exception {
        while (currentToken() != null && !currentToken().getValue().equals("}")) {
            parseStatement();
        }
    }

    // Statement -> DeclarationStmt | AssignmentStmt | PrintStmt | IfStmt
    private void parseStatement() throws Exception {
        Token t = currentToken();
        if (t == null) throw new Exception("Unexpected end of input in statement");
        // Declaration: starts with a type keyword.
        if (t.getType() == TokenType.KEYWORD && 
           (t.getValue().equals("int") || t.getValue().equals("float") ||
            t.getValue().equals("char") || t.getValue().equals("bool"))) {
            parseDeclarationStmt();
        } else if (t.getType() == TokenType.KEYWORD && t.getValue().equals("print")) {
            parsePrintStmt();
        } else if (t.getType() == TokenType.KEYWORD && t.getValue().equals("fi")) {
            parseIfStmt();
        } else if (t.getType() == TokenType.IDENTIFIER) {
            // Assignment statement: Identifier "=" Expression ";"
            Token next = peekToken();
            if (next != null && next.getType() == TokenType.OPERATOR && next.getValue().equals("=")) {
                parseAssignmentStmt();
            } else {
                throw new Exception("Unexpected token in statement: " + t.getValue());
            }
        } else {
            throw new Exception("Unexpected token in statement: " + t.getValue());
        }
    }

    // DeclarationStmt -> Type Identifier "=" Expression ";"
    private void parseDeclarationStmt() throws Exception {
        String type = parseType();
        Token id = currentToken();
        if (id == null || id.getType() != TokenType.IDENTIFIER) {
            throw new Exception("Expected identifier after type " + type);
        }
        if (id.getValue().equals("main")) {
            throw new Exception("Cannot declare 'main' inside function body");
        }
        consume(); // consume identifier
        symbolTable.addSymbol(id.getValue(), type);
        expect(TokenType.OPERATOR, "=");
        parseExpression();
        expect(TokenType.PUNCTUATION, ";");
    }

    // AssignmentStmt -> Identifier "=" Expression ";"
    private void parseAssignmentStmt() throws Exception {
        Token id = currentToken();
        if (id == null || id.getType() != TokenType.IDENTIFIER) {
            throw new Exception("Expected identifier for assignment");
        }
        if (!symbolTable.contains(id.getValue())) {
            throw new Exception("Identifier " + id.getValue() + " not declared");
        }
        consume(); // consume identifier
        expect(TokenType.OPERATOR, "=");
        parseExpression();
        expect(TokenType.PUNCTUATION, ";");
    }

    // PrintStmt -> "print" "(" Expression ")" ";"
    private void parsePrintStmt() throws Exception {
        expect(TokenType.KEYWORD, "print");
        expect(TokenType.PUNCTUATION, "(");
        parseExpression();
        expect(TokenType.PUNCTUATION, ")");
        expect(TokenType.PUNCTUATION, ";");
    }

    // IfStmt -> "fi" "(" RelationalExpression ")" Block [ "esle" Block ]
    private void parseIfStmt() throws Exception {
        expect(TokenType.KEYWORD, "fi");
        expect(TokenType.PUNCTUATION, "(");
        parseRelationalExpression();
        expect(TokenType.PUNCTUATION, ")");
        parseBlock();
        Token t = currentToken();
        if (t != null && t.getType() == TokenType.KEYWORD && t.getValue().equals("esle")) {
            consume();
            parseBlock();
        }
    }

    private void parseBlock() throws Exception {
        expect(TokenType.PUNCTUATION, "{");
        parseStatements();
        expect(TokenType.PUNCTUATION, "}");
    }

    // RelationalExpression -> Expression [ ( ">" | "<" | "==" | "!=" | ">=" | "<=" ) Expression ]
    private void parseRelationalExpression() throws Exception {
        parseExpression();
        Token t = currentToken();
        if (t != null && t.getType() == TokenType.OPERATOR && 
           (t.getValue().equals(">") || t.getValue().equals("<") ||
            t.getValue().equals("==") || t.getValue().equals("!=") ||
            t.getValue().equals(">=") || t.getValue().equals("<="))) {
            consume();
            parseExpression();
        }
    }

    // Expression -> Term { ("+" | "-") Term }
    private void parseExpression() throws Exception {
        parseTerm();
        while (currentToken() != null && currentToken().getType() == TokenType.OPERATOR &&
              (currentToken().getValue().equals("+") || currentToken().getValue().equals("-"))) {
            consume();
            parseTerm();
        }
    }

    // Term -> Factor { ("*" | "/") Factor }
    private void parseTerm() throws Exception {
        parseFactor();
        while (currentToken() != null && currentToken().getType() == TokenType.OPERATOR &&
              (currentToken().getValue().equals("*") || currentToken().getValue().equals("/"))) {
            consume();
            parseFactor();
        }
    }

    // Factor -> Literal | Identifier | Number | Boolean | "(" Expression ")"
    private void parseFactor() throws Exception {
        Token t = currentToken();
        if (t == null) {
            throw new Exception("Expected factor but found EOF");
        }
        if (t.getType() == TokenType.LITERAL || t.getType() == TokenType.INTEGER ||
            t.getType() == TokenType.FLOAT || t.getType() == TokenType.BOOLEAN) {
            consume();
        } else if (t.getType() == TokenType.IDENTIFIER) {
            if (!symbolTable.contains(t.getValue())) {
                throw new Exception("Identifier " + t.getValue() + " not declared");
            }
            consume();
        } else if (t.getValue().equals("(")) {
            expect(TokenType.PUNCTUATION, "(");
            parseExpression();
            expect(TokenType.PUNCTUATION, ")");
        } else {
            throw new Exception("Invalid factor: " + t.getValue());
        }
    }

    private void expect(TokenType expectedType, String expectedValue) throws Exception {
        Token t = currentToken();
        if (t == null || t.getType() != expectedType || !t.getValue().equals(expectedValue)) {
            throw new Exception("Expected '" + expectedValue + "' but found " + (t == null ? "EOF" : t.getValue()));
        }
        consume();
    }
}
