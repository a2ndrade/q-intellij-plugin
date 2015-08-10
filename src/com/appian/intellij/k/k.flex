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

lineComment="/" {inputCharacter}* {eol}?
endOfLineComment={whitespace}+ {lineComment}

%%
<YYINITIAL> {

  {whitespace}       { return com.intellij.psi.TokenType.WHITE_SPACE; }
  ^{lineComment}     { System.out.println("lineComment"); }
  {endOfLineComment} { System.out.println("endOfLineComment"); }

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

  {digit}            { return DIGIT; }
  {alpha}            { return ALPHA; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
