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
IDENTIFIER_SYS="_" [._a-zA-Z0-9]*
ID={IDENTIFIER}|{IDENTIFIER_SYS}
ID_START=[_.][a-zA-Z]

NUMBER=-?((0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?|0[iInN])
NUMBER_VECTOR={NUMBER}({WHITE_SPACE}{NUMBER})+
CHAR=\"(\\\"|[^\"])\"
CHAR_VECTOR=\"(\\\"|[^\"])*\"
SYMBOL="`"([._a-zA-Z0-9]+|{CHAR_VECTOR}|({NEWLINE}|{WHITE_SPACE})+)
SYMBOL_VECTOR={SYMBOL} {SYMBOL}+
VERB=[!#$%&*+,-.<=>?@\^_|~]
ADVERB="/" | "/:" | "\\" | "\\:" | "'" | "':"

// function composition
COMPOSED_VERB={VERB} {VERB}+
COMPOSED_MONAD={VERB}+ ":"

// higher-order functions
DERIVED_VERB=({ID}|{VERB})+{ADVERB}+

// Is Next Minus Token a Dyad
%state INMTD

%%

<INMTD> {
  "-"                         { yybegin(YYINITIAL); return VERB;}
}

<YYINITIAL> {

  {NEWLINE}+                   { return NEWLINE; }
  ^{DIRECTORY}                 { return DIRECTORY; }
  {NUMBER_VECTOR}              { return NUMBER_VECTOR; }
  {COMPOSED_MONAD}             { return COMPOSED_MONAD; }
  {COMPOSED_VERB}/{ID_START}   { return COMPOSED_VERB; }
  {COMPOSED_VERB}/-[0-9]       { return COMPOSED_VERB; }
  {COMPOSED_VERB}              { return COMPOSED_VERB; }
  {DERIVED_VERB}               { return DERIVED_VERB; }
  {WHITE_SPACE}                { return com.intellij.psi.TokenType.WHITE_SPACE; }
  ^{COMMENT1}                  { return COMMENT; }
  {COMMENT2}                   { return COMMENT; }
  {SYMBOL_VECTOR}              { return SYMBOL_VECTOR; }
  {SYMBOL}                     { return SYMBOL; }
  {VERB}/{ID_START}            { return VERB;}
  {VERB}/-[0-9]                { return VERB;}
  {VERB}                       { return VERB;}

  "("                          { return OPEN_PAREN; }
  ")"/-                        { yybegin(INMTD); return CLOSE_PAREN; }
  ")"                          { return CLOSE_PAREN; }
  ":"                          { return COLON; }
  ";"                          { return SEMICOLON; }
  "["                          { return OPEN_BRACKET; }
  "]"/-                        { yybegin(INMTD); return CLOSE_BRACKET; }
  "]"                          { return CLOSE_BRACKET; }
  "{"                          { return OPEN_BRACE; }
  "}"                          { return CLOSE_BRACE; }
  "0:"                         { return ZEROCOLON; }
  "1:"                         { return ONECOLON; }
  "2:"                         { return TWOCOLON; }
  "3:"                         { return THREECOLON; }
  "4:"                         { return FOURCOLON; }
  "5:"                         { return FIVECOLON; }
  "6:"                         { return SIXCOLON; }
  "if"                         { return IF; }
  "do"                         { return DO; }
  "while"                      { return WHILE; }

  {IDENTIFIER_SYS}/-           { yybegin(INMTD); return IDENTIFIER_SYS; }
  {IDENTIFIER_SYS}             { return IDENTIFIER_SYS; }
  {IDENTIFIER}/-               { yybegin(INMTD); return IDENTIFIER; }
  {IDENTIFIER}                 { return IDENTIFIER; }
  {NUMBER}/-                   { yybegin(INMTD); return NUMBER; }
  {NUMBER}                     { return NUMBER; }
  {CHAR}                       { return CHAR; }
  {CHAR_VECTOR}                { return STRING; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
