/* Package */
package example;

import java.io.*;
import example.Token;

/* JFlex Options */
%%
%class Lexer
%unicode
%public
%line
%column
%type Token

%{
    // Token counter
    private int tokenCount = 0;

    public int getTokenCount() {
        return tokenCount;
    }
%}

/* Regular Expressions */
LineTerminator = \r|\n|\r\n
Whitespace = [ \t\f]
InputCharacter = [^\r\n]
Comment = {TraditionalComment} | {EndOfLineComment}
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

Identifier = [a-zA-Z_][a-zA-Z0-9_]*
Number = [0-9]+
StringLit = \"([^\"\\]|\\.)*\"
Keyword = "int"|"return"|"void"|"if"|"else"|"while"|"for"|"printf"
Operator = "="|"+"|"-"|"*"|"/"
Punctuator = [(){};,]

%%

/* Ignore whitespace and comments */
{LineTerminator}    { /* ignore */ }
{Whitespace}       { /* ignore */ }
{Comment}          { /* ignore */ }

/* Return tokens */
{Keyword}          { tokenCount++; return new Token(Token.KEYWORD, yytext()); }
{Identifier}       { tokenCount++; return new Token(Token.IDENTIFIER, yytext()); }
{Number}           { tokenCount++; return new Token(Token.NUMBER, yytext()); }
{StringLit}        { tokenCount++; return new Token(Token.STRING, yytext()); }
{Operator}         { tokenCount++; return new Token(Token.OPERATOR, yytext()); }
{Punctuator}       { tokenCount++; return new Token(Token.PUNCTUATOR, yytext()); }

/* Handle end of file */
<<EOF>>            { return new Token(Token.EOF, null); }

/* Handle unrecognized characters */
[^]                { 
    System.out.println("Warning: Skipping unrecognized character '" + yytext() + "' at line " + (yyline+1) + ", column " + (yycolumn+1));
    return null;
}