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

SIMPLE_COMMAND="\\"(
  [dafv_ib] // ({WHITE_SPACE}+{USER_IDENTIFIER})?
 |([ls12x]|"kr") // {WHITE_SPACE}+{USER_IDENTIFIER}
 |[egopPSTwWz] // ({WHITE_SPACE}+{NUMBER})? // 't' is not included b/c in K3 it takes an expression
 |[bBrsu\\wm]
 |[cC] //{NUMBER_VECTOR}
  )
COMPLEX_COMMAND="\\"{USER_IDENTIFIER} // takes OS command and/or arbitrary expression as argument
USER_IDENTIFIER=[.a-zA-Z][._a-zA-Z0-9]*
N_COLON=[0-6] ":"
ID={USER_IDENTIFIER}|{K3_SYSTEM_FUNCTION}|{Q_SYSTEM_FUNCTION}
ID_START=[_.][a-zA-Z]

K3_SYSTEM_FUNCTION=(_a|_abs|_acos|_asin|_atan|_bd|_bin|_binl|_ci|_cos|_cosh|_d|_db|_di|_div|_dot|_draw|_dv
        |_dvl|_exit|_exp|_f|_floor|_getenv|_gtime|_h|_host|_i|_ic|_in|_inv|_jd|_k|_lin|_log|_lsq|_lt|_mul|_n
        |_p|_sin|_sinh|_sm|_sqr|_sqrt|_ss|_ssr|_sv|_T|_t|_t|_tan|_tanh|_u|_v|_w)

// q functions
Q_SYSTEM_FUNCTION=(abs|acos|aj|aj0|all|and|any|asc|asin|asof|atan|attr|avg|avgs|bin|binr|by|ceiling|cols|cor|cos|count
       |cov|cross|csv|cut|delete|deltas|desc|dev|differ|distinct|div|dsave|each|ej|ema|enlist|eval|except
       |exec|exit|exp|fby|fills|first|fkeys|flip|floor|from|get|getenv|group|gtime|hclose|hcount|hdel|hopen
       |hsym|iasc|idesc|ij|in|insert|inter|inv|key|keys|last|like|lj|ljf|load|log|lower|lsq|ltime|ltrim|mavg
       |max|maxs|mcount|md5|mdev|med|meta|min|mins|mmax|mmin|mmu|mod|msum|neg|next|not|null|or|over|parse
       |peach|pj|prd|prds|prev|prior|rand|rank|ratios|raze|read0|read1|reciprocal|reverse|rload|rotate|rsave
       |rtrim|save|scan|scov|sdev|select|set|setenv|show|signum|sin|sqrt|ss|ssr|string|sublist|sum|sums|sv
       |svar|system|tables|tan|til|trim|type|uj|ungroup|union|update|upper|upsert|value|var|view|views|vs
       |wavg|where|within|wj|wj1|wsum|ww|xasc|xbar|xcol|xcols|xdesc|xexp|xgroup|xkey|xlog|xprev|xrank)

INT_TYPE=[ihjepuvt]
FLOAT_TYPE=[fen]
Q_DATETIME_TYPE=[mdz]
TYPE={INT_TYPE}|{FLOAT_TYPE}|{Q_DATETIME_TYPE}
// only positive numbers
Q_NIL_POSITIVE="0N"({INT_TYPE}|{FLOAT_TYPE}|"g")
Q_BINARY=[01]"b"
Q_HEX_CHAR=[[:digit:]A-Fa-f]
Q_HEX_NUMBER="0x"{Q_HEX_CHAR}{Q_HEX_CHAR}?
NUMBER_POSITIVE={Q_NIL_POSITIVE}|{Q_BINARY}|{Q_HEX_NUMBER}
// positive & negative
NIL_OR_INFINITY=0[iInNwW] // iI are from k3; wW are from k4; nN are from both
Q_MONTH=[:digit:]+\.[:digit:]{2}
Q_DATE={Q_MONTH}+\.[:digit:]{2}
Q_TIME=[:digit:]+(\:[:digit:]{2}(\:[0-5][:digit:](\.[:digit:]+)?)?)?
Q_DATETIME={Q_DATE}"T"{Q_TIME}
NUMBER_POSITIVE_NEGATIVE=(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?|{NIL_OR_INFINITY}
// all numbers
NUMBER_NO_TYPE=-?({NUMBER_POSITIVE_NEGATIVE})|{NUMBER_POSITIVE}|-?({Q_DATE}|{Q_TIME}|{Q_DATETIME}|{Q_MONTH})
NUMBER={NUMBER_NO_TYPE}{TYPE}?
BINARY_VECTOR=[01][01]+"b"|"0x"{Q_HEX_CHAR}{2}{Q_HEX_CHAR}+
NUMBER_VECTOR={NUMBER_NO_TYPE}({WHITE_SPACE}{NUMBER_NO_TYPE})+{TYPE}?|{BINARY_VECTOR}
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
DERIVED_VERB=({ID}|({VERB}|{N_COLON}|":")){ADVERB}+

%state INFIX
%state DERIVED_LAMBDA
%state ESCAPE
%state COMMAND

%%

<INFIX> {
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
  "^"                          { yybegin(YYINITIAL); return USER_IDENTIFIER; }
  "."                          { yybegin(YYINITIAL); return USER_IDENTIFIER; }
  {WHITE_SPACE}                { return com.intellij.psi.TokenType.WHITE_SPACE; }
  {USER_IDENTIFIER}            { yybegin(YYINITIAL); return USER_IDENTIFIER; }
  {NUMBER}                     { yybegin(YYINITIAL); return NUMBER; }
  {NUMBER_VECTOR}              { yybegin(YYINITIAL); return NUMBER_VECTOR; }
}

<YYINITIAL> {
  {NEWLINE}+                   { return NEWLINE; }
  ^"\\d"                       { yybegin(COMMAND); return CURRENT_NAMESPACE; }
  ^{SIMPLE_COMMAND}            { yybegin(COMMAND); return SIMPLE_COMMAND; }
  ^{COMPLEX_COMMAND}           { return COMPLEX_COMMAND; }
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

  {K3_SYSTEM_FUNCTION}/{VERB}  { yybegin(INFIX); return K3_SYSTEM_FUNCTION; }
  {K3_SYSTEM_FUNCTION}         { return K3_SYSTEM_FUNCTION; }
  {Q_SYSTEM_FUNCTION}/{VERB}   { yybegin(INFIX); return Q_SYSTEM_FUNCTION; }
  {Q_SYSTEM_FUNCTION}          { return Q_SYSTEM_FUNCTION; }
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
