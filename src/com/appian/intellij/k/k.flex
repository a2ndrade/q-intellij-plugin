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
WHITE_SPACE=({LINE_WS}|{EOL})+

COMMENT1="/" [^\r\n]* {EOL}?
COMMENT2={WHITE_SPACE}+ {COMMENT1}

IDENTIFIER=[a-zA-Z][._a-zA-Z0-9]*
SYSFUNCTION="_" {IDENTIFIER}
NUMBER=-?((0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?|0[iInN])
STRING=\"(\\\"|[^\"])*\"
SYMBOL="`"([._a-zA-Z0-9]+|{STRING}|{WHITE_SPACE}+)

%%
<YYINITIAL> {

  {WHITE_SPACE}      { return com.intellij.psi.TokenType.WHITE_SPACE; }
  ^{COMMENT1}        { return COMMENT; }
  {COMMENT2}         { return COMMENT; }
  {SYMBOL}           { return SYMBOL; }

  "!"                { return BANG; }
  "\""               { return QUOTE; }
  "#"                { return HASH; }
  "$"                { return DOLLAR; }
  "%"                { return PERCENT; }
  "&"                { return AMPERSAND; }
  "'"                { return TICK; }
  "("                { return OPEN_PAREN; }
  ")"                { return CLOSE_PAREN; }
  "*"                { return ASTERISK; }
  "+"                { return PLUS; }
  ","                { return COMMA; }
  "-"                { return DASH; }
  "."                { return PERIOD; }
  "/"                { return SLASH; }
  ":"                { return COLON; }
  ";"                { return SEMICOLON; }
  "<"                { return LESS_THAN; }
  "="                { return EQUALS; }
  ">"                { return GREATER_THAN; }
  "?"                { return QUESTION_MARK; }
  "@"                { return AT; }
  "["                { return OPEN_BRACKET; }
  "\\"               { return BACK_SLASH; }
  "]"                { return CLOSE_BRACET; }
  "^"                { return CARET; }
  "_"                { return UNDERSCORE; }
  "`"                { return BACK_TICK; }
  "{"                { return OPEN_BRACE; }
  "|"                { return PIPE; }
  "}"                { return CLOSE_BRACE; }
  "~"                { return TILDE; }
  "/:"               { return SLASH_COLON; }
  "\\:"              { return BACK_SLASH_COLON; }
  "':"               { return TICK_COLON; }
  "_n"               { return NIL; }
  "0:"               { return ZEROCOLON; }
  "1:"               { return ONECOLON; }
  "2:"               { return TWOCOLON; }
  "3:"               { return THREECOLON; }
  "4:"               { return FOURCOLON; }
  "5:"               { return FIVECOLON; }
  "6:"               { return SIXCOLON; }
  "if"               { return IF; }
  "do"               { return DO; }
  "while"            { return WHILE; }

  {SYSFUNCTION}      { return SYSFUNCTION; }
  {IDENTIFIER}       { return IDENTIFIER; }
  {NUMBER}           { return NUMBER; }
  {STRING}           { return STRING; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
