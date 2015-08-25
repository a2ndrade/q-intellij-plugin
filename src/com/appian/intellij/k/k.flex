package com.appian.intellij.k;

import static com.appian.intellij.k.psi.KTypes.*;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

%%

%class KLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return NEWLINE;
%eof}

//EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE={LINE_WS}+
NEWLINE=\r|\n|\r\n

ANY=({NEWLINE}|.)*

COMMENT1="/" [^\r\n]* {NEWLINE}?
COMMENT2={WHITE_SPACE}+ {COMMENT1}

COMMAND_NAME={WHITE_SPACE}*"\\"[dl]
IDENTIFIER=[.a-zA-Z][._a-zA-Z0-9]*
IDENTIFIER_SYS="_" [._a-zA-Z0-9]*
N_COLON=[0-6] ":"
ID={IDENTIFIER}|{IDENTIFIER_SYS}
ID_START=[_.][a-zA-Z]

NUMBER=-?((0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?|0[iInN])
NUMBER_VECTOR={NUMBER}({WHITE_SPACE}{NUMBER})+
C=([^\\\"]|\\[^\ \t])
CHAR=\"{C}\"
CHAR_VECTOR=\"{C}*\"
SYMBOL="`"([._a-zA-Z0-9]+|{CHAR_VECTOR}|({NEWLINE}|{WHITE_SPACE}*)+)
SYMBOL_VECTOR={SYMBOL} ({WHITE_SPACE}*{SYMBOL})+
VERB=[!#$%&*+,-.<=>?@\^_|~]
ADVERB="/" | "/:" | "\\" | "\\:" | "'" | "':"

// function composition
COMPOSED_MONAD=(({VERB}|{N_COLON}){WHITE_SPACE}*) ":"

// higher-order functions
DERIVED_VERB=({ID}|(({VERB}|{N_COLON})":"?)){ADVERB}+

// Is Next Minus Token a Dyad
%state MINUS
%state DERIVED_LAMBDA
%state ESCAPE
%state COMMAND

%%

<MINUS> {
  "-"/":["                     { yybegin(YYINITIAL); return VERB;}
  "-:"                         { yybegin(YYINITIAL); return COMPOSED_MONAD;}
  "-" (":"?) {ADVERB}+         { yybegin(YYINITIAL); return DERIVED_VERB;}
  "-"                          { yybegin(YYINITIAL); return VERB;}
}

<DERIVED_LAMBDA> {
  {ADVERB}                     { yybegin(YYINITIAL); return ADVERB;}
}

<ESCAPE> {
  {ANY}                        { yybegin(YYINITIAL); return COMMENT; }
}

<COMMAND> {
  "^"                          { yybegin(YYINITIAL); return CARET; }
  {IDENTIFIER}                 { yybegin(YYINITIAL); return IDENTIFIER; }
  {WHITE_SPACE}                { return com.intellij.psi.TokenType.WHITE_SPACE; }
  {NEWLINE}                    { yybegin(YYINITIAL); return NEWLINE; }
}

<YYINITIAL> {

  {NEWLINE}+                   { return NEWLINE; }
  ^{COMMAND_NAME}              { yybegin(COMMAND); return COMMAND_NAME; }
  {NUMBER_VECTOR}              { return NUMBER_VECTOR; }
  {N_COLON}/[^\[]              { return N_COLON; }
  ":"/"["                      { return COLON; }
  "if"/"["                     { return IF; }
  "do"/"["                     { return DO; }
  "while"/"["                  { return WHILE; }
  {ADVERB}/"["                 { return ADVERB; }
  {DERIVED_VERB}               { return DERIVED_VERB; }
  {COMPOSED_MONAD}/[^\[]       { return COMPOSED_MONAD; }
  {SYMBOL_VECTOR}/{LINE_WS}"/" { return SYMBOL_VECTOR; }
  {SYMBOL_VECTOR}              { return SYMBOL_VECTOR; }
  {SYMBOL}/{LINE_WS}"/"        { return SYMBOL; }
  {SYMBOL}                     { return SYMBOL; }
  {WHITE_SPACE}                { return com.intellij.psi.TokenType.WHITE_SPACE; }
  ^{COMMENT1}                  { return COMMENT; }
  {COMMENT2}/{NEWLINE}         { return COMMENT; }
  {COMMENT2}                   { return COMMENT; }


  {VERB}/{ID_START}            { return VERB;}
  {VERB}/-[0-9]                { return VERB;}
  {VERB}                       { return VERB;}

  "("                          { return OPEN_PAREN; }
  ")"/{ADVERB}                 { yybegin(DERIVED_LAMBDA); return CLOSE_PAREN; }
  ")"/-                        { yybegin(MINUS); return CLOSE_PAREN; }
  ")"                          { return CLOSE_PAREN; }
  ";"                          { return SEMICOLON; }
  "["                          { return OPEN_BRACKET; }
  "]"/{ADVERB}                 { yybegin(DERIVED_LAMBDA); return CLOSE_BRACKET; }
  "]"/-                        { yybegin(MINUS); return CLOSE_BRACKET; }
  "]"                          { return CLOSE_BRACKET; }
  "{"                          { return OPEN_BRACE; }
  "}"/{ADVERB}                 { yybegin(DERIVED_LAMBDA); return CLOSE_BRACE; }
  "}"                          { return CLOSE_BRACE; }

  {IDENTIFIER_SYS}/-           { yybegin(MINUS); return IDENTIFIER_SYS; }
  {IDENTIFIER_SYS}             { return IDENTIFIER_SYS; }
  {IDENTIFIER}/-               { yybegin(MINUS); return IDENTIFIER; }
  {IDENTIFIER}                 { return IDENTIFIER; }
  {NUMBER}/-                   { yybegin(MINUS); return NUMBER; }
  {NUMBER}                     { return NUMBER; }
  {CHAR}                       { return CHAR; }
  {CHAR_VECTOR}                { return STRING; }

  ":"                          { return COLON; }
  "'"                          { return TICK; }
  "\\"/{NEWLINE}               { yybegin(ESCAPE); return COMMENT; }
  "\\"                         { return BACK_SLASH; }
  "`"                          { return SYMBOL; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
