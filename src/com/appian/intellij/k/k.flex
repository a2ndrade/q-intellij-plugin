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

eol="\r"|"\n"|"\r\n"
lineWhitespace=[\ \t\f]
whitespace=({lineWhitespace}|{eol})+
inputCharacter=[^\r\n]

digit=[0-9]
alpha=[a-zA-Z]
idRest=[._] | {alpha} | {digit}
identifier={alpha} {idRest}*
system=[_] {idRest}*

lineComment="/" {inputCharacter}* {eol}?
endOfLineComment={whitespace}+ {lineComment}

integer={digit}+ | "-" {digit}+ | "0N" | "0I"
integerVector="!0" | "," {integer} | {integer} ({lineWhitespace}+ {integer})+

float=({digit}+ | "-" {digit}+) "." {digit}*
floatVector="0#0.0" | "," {float} | {float} ({lineWhitespace}+ {float})+

charVector=\" (\\\" | [^\"])* \"
string=\` {charVector}

%%
<YYINITIAL> {

  {whitespace}       { return com.intellij.psi.TokenType.WHITE_SPACE; }

  // comments
  ^{lineComment}     { System.out.println("c: " + yytext()); }
  {endOfLineComment} { System.out.println("c: " + yytext()); }

  {charVector}       { System.out.println("cv: " + yytext()); }

  {string}           { System.out.println("s: " + yytext()); }

  // verbs
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

  // adverbs
  "/:"               { return SLASH_COLON; }
  "\\:"              { return BACK_SLASH_COLON; }
  "':"               { return TICK_COLON; }

  // data
  "_n"               { return NIL; }
  "0I"               { return INTINFINITY; }
  "0N"               { return INTNAN; }
  "0i"               { return FLOATINFINITY; }
  "0n"               { return FLOATNAN; }
  {integer}          { System.out.println("i: " + yytext());}
  {float}            { System.out.println("f: " + yytext());}

  // i/o, dynamic load and client/server
  "0:"               { return ZEROCOLON; }
  "1:"               { return ONECOLON; }
  "2:"               { return TWOCOLON; }
  "3:"               { return THREECOLON; }
  "4:"               { return FOURCOLON; }
  "5:"               { return FIVECOLON; }
  "6:"               { return SIXCOLON; }

  // assign, define, control and debug
  "if"               { return IF; }
  "do"               { return DO; }
  "while"            { return WHILE; }

  // names
  {identifier}       { return IDENTIFIER; }

  // system verbs and nouns
  {system}           { return SYSTEM; }

  // vectors
  {integerVector}    { System.out.println("iv: " + yytext()); }
  {floatVector}      { System.out.println("fv: " + yytext()); }

  // punctuation
  \\n                { return NEWLINE; }
}

  [^]                { return com.intellij.psi.TokenType.BAD_CHARACTER; }
