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
USER_IDENTIFIER=[.a-zA-Z][._a-zA-Z0-9]*
SYSTEM_IDENTIFIER="_" [._a-zA-Z0-9]+
N_COLON=[0-6] ":"
ID={USER_IDENTIFIER}|{SYSTEM_IDENTIFIER}
ID_START=[_.][a-zA-Z]

NUMBER=-?((0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?|0[iInN])
NUMBER_VECTOR={NUMBER}({WHITE_SPACE}{NUMBER})+
C=([^\\\"]|\\[^\ \t])
CHAR=\"{C}\"
CHAR_VECTOR=\"{C}*\"
SYMBOL="`"([._a-zA-Z0-9]+|{CHAR_VECTOR}|({NEWLINE}|{WHITE_SPACE}*)+)
SYMBOL_VECTOR={SYMBOL} ({WHITE_SPACE}*{SYMBOL})+
VERB=[!#$%&*+,-.<=>?@\^_|~]
MONADIC_AND_DYADIC_ADVERB="/" | \\ | '
DYADIC_ONLY_ADVERB="/": | \\: | ':
ADVERB={MONADIC_AND_DYADIC_ADVERB}|{DYADIC_ONLY_ADVERB}

// function composition
SIMPLE_COMPOSED_MONAD=(({VERB}{WHITE_SPACE}*)+|{N_COLON}){WHITE_SPACE}* ":"
COMPOSED_MONAD={SIMPLE_COMPOSED_MONAD} {MONADIC_AND_DYADIC_ADVERB}*

// higher-order functions
DERIVED_VERB=({ID}|({VERB}|{N_COLON})){ADVERB}+

%state INFIX
%state DERIVED_LAMBDA
%state ESCAPE
%state COMMAND

%%

<INFIX> {
  {SYSTEM_IDENTIFIER}          { yybegin(YYINITIAL);  return SYSTEM_IDENTIFIER; }
  {DERIVED_VERB}               { yybegin(YYINITIAL); return DERIVED_VERB;}
  {VERB}/":["                  { yybegin(YYINITIAL); return VERB;}
  {VERB}/{COMPOSED_MONAD}      { yybegin(YYINITIAL); return VERB;}
  {VERB}                       { yybegin(YYINITIAL); return VERB;}
}

<DERIVED_LAMBDA> {
  {ADVERB}                     { yybegin(YYINITIAL); return ADVERB;}
}

<ESCAPE> {
  {ANY}                        { yybegin(YYINITIAL); return COMMENT; }
}

<COMMAND> {
  "^"                          { yybegin(YYINITIAL); return CARET; }
  {USER_IDENTIFIER}            { yybegin(YYINITIAL); return USER_IDENTIFIER; }
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
  {ADVERB}+/"["                { return ADVERB; }
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
  ")"/{VERB}                   { yybegin(INFIX); return CLOSE_PAREN; }
  ")"                          { return CLOSE_PAREN; }
  ";"                          { return SEMICOLON; }
  "["                          { return OPEN_BRACKET; }
  "]"/{ADVERB}                 { yybegin(DERIVED_LAMBDA); return CLOSE_BRACKET; }
  "]"/{VERB}                   { yybegin(INFIX); return CLOSE_BRACKET; }
  "]"                          { return CLOSE_BRACKET; }
  "{"                          { return OPEN_BRACE; }
  "}"/{ADVERB}                 { yybegin(DERIVED_LAMBDA); return CLOSE_BRACE; }
  "}"                          { return CLOSE_BRACE; }

  {SYSTEM_IDENTIFIER}/{VERB}   { yybegin(INFIX); return SYSTEM_IDENTIFIER; }
  {SYSTEM_IDENTIFIER}          { return SYSTEM_IDENTIFIER; }
  {USER_IDENTIFIER}/{VERB}     { yybegin(INFIX); return USER_IDENTIFIER; }
  {USER_IDENTIFIER}            { return USER_IDENTIFIER; }
  {NUMBER}/{VERB}              { yybegin(INFIX); return NUMBER; }
  {NUMBER}                     { return NUMBER; }
  {CHAR}/{VERB}                { yybegin(INFIX); return CHAR; }
  {CHAR}                       { return CHAR; }
  {CHAR_VECTOR}/{VERB}         { yybegin(INFIX); return STRING; }
  {CHAR_VECTOR}                { return STRING; }

  ":"                          { return COLON; }
  "'"                          { return TICK; }
  "\\"/{NEWLINE}               { yybegin(ESCAPE); return COMMENT; }
  "\\"                         { return BACK_SLASH; }
  "`"                          { return SYMBOL; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
