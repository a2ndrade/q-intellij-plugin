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
%eof{  return;
%eof}

EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE={LINE_WS}+
NEWLINE=\r|\n|\r\n

COMMENT1="/" [^\r\n]* {EOL}?
COMMENT2={WHITE_SPACE}+ {COMMENT1}

DIRECTORY={WHITE_SPACE}* "\\d" {WHITE_SPACE}+ [._a-zA-Z0-9]+ ({WHITE_SPACE}|{EOL})+
IDENTIFIER=[.a-zA-Z][._a-zA-Z0-9]*
IDENTIFIER_SYS="_" [._a-zA-Z0-9]*|[0-6] ":"
ID={IDENTIFIER}|{IDENTIFIER_SYS}
ID_START=[_.][a-zA-Z]

NUMBER=-?((0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?|0[iInN])
NUMBER_VECTOR={NUMBER}({WHITE_SPACE}{NUMBER})+
C=([^\\\"]|\\[^\ \t])
CHAR=\"{C}\"
CHAR_VECTOR=\"{C}*\"
SYMBOL="`"([._a-zA-Z0-9]+|{CHAR_VECTOR}|({NEWLINE}|{WHITE_SPACE}*)+)
SYMBOL_VECTOR={SYMBOL} ({WHITE_SPACE}?{SYMBOL})+
VERB=[!#$%&*+,-.<=>?@\^_|~]
ADVERB="/" | "/:" | "\\" | "\\:" | "'" | "':"

// function composition
COMPOSED_MONAD={VERB}+ ":"

// higher-order functions
DERIVED_VERB=({ID}|{VERB})+{ADVERB}+

// Is Next Minus Token a Dyad
%state INMTD

%%

<INMTD> {
  "-"                          { yybegin(YYINITIAL); return VERB;}
}

<YYINITIAL> {

  {NEWLINE}+                   { return NEWLINE; }
  ^{DIRECTORY}                 { return DIRECTORY; }
  {NUMBER_VECTOR}              { return NUMBER_VECTOR; }
  {COMPOSED_MONAD}             { return COMPOSED_MONAD; }
  {DERIVED_VERB}               { return DERIVED_VERB; }
  {SYMBOL_VECTOR}              { return SYMBOL_VECTOR; }
  {SYMBOL}                     { return SYMBOL; }
//  "`"$                         { return SYMBOL; }
  {WHITE_SPACE}                { return com.intellij.psi.TokenType.WHITE_SPACE; }
  ^{COMMENT1}                  { return COMMENT; }
  {COMMENT2}                   { return COMMENT; }

  "."/"["                      { return DOT; }
  "@"/"["                      { return AT; }

  {VERB}/{ID_START}            { return VERB;}
  {VERB}/-[0-9]                { return VERB;}
  {VERB}                       { return VERB;}

  "("                          { return OPEN_PAREN; }
  ")"/-                        { yybegin(INMTD); return CLOSE_PAREN; }
  ")"                          { return CLOSE_PAREN; }
  ";"                          { return SEMICOLON; }
  "["                          { return OPEN_BRACKET; }
  "]"/-                        { yybegin(INMTD); return CLOSE_BRACKET; }
  "]"                          { return CLOSE_BRACKET; }
  "{"                          { return OPEN_BRACE; }
  "}"                          { return CLOSE_BRACE; }
  "if"/"["                     { return IF; }
  "do"/"["                     { return DO; }
  "while"/"["                  { return WHILE; }

  {IDENTIFIER_SYS}/-           { yybegin(INMTD); return IDENTIFIER_SYS; }
  {IDENTIFIER_SYS}             { return IDENTIFIER_SYS; }
  {IDENTIFIER}/-               { yybegin(INMTD); return IDENTIFIER; }
  {IDENTIFIER}                 { return IDENTIFIER; }
  {NUMBER}/-                   { yybegin(INMTD); return NUMBER; }
  {NUMBER}                     { return NUMBER; }
  {CHAR}                       { return CHAR; }
  {CHAR_VECTOR}                { return STRING; }

  ":"                          { return COLON; }
  "`"                          { return SYMBOL; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
