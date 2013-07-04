grammar Tiger;

// reserved words

FUNCTION
    : 'function'
    ;
IF
    : 'if'
    ;
THEN
    : 'then'
    ;
ELSE
    : 'else'
    ;
WHILE
    : 'while'
    ;
FOR
    : 'for'
    ;
DO
    : 'do'
    ;
BREAK
    : 'break'
    ;
LET
    : 'let'
    ;
IN
    : 'in'
    ;
END
    : 'end'
    ;
OF
    : 'of'
    ;
TO
    : 'to'
    ;
TYPE
    : 'type'
    ;
VAR
    : 'var'
    ;
ARRAY
    : 'array'
    ;
COLON
    : ':'
    ;
LEFTBRACE
    : '{'
    ;
RIGHTBRACE
    : '}'
    ;
LEFTBRACKET
    : '['
    ;
RIGHTBRACKET
    : ']'
    ;
LEFTPARENTHESIS
    : '('
    ;
RIGHTPARENTHESIS
    : ')'
    ;
EQUAL
    : '='
    ;
MINUS
    : '-'
    ;
PLUS
    : '+'
    ;
TIMES
    : '*'
    ;
DIV
    : '/'
    ;
ASSIGN
    : ':='
    ;
COMMA
    : ','
    ;
QUOTE
    : '"'
    ;
GT
    : '>'
    ;
NE
    : '<>'
    ;
LT
    : '<'
    ;
GE
    : '>='
    ;
LE
    : '<='
    ;
AND
    : '&'
    ;
OR
    : '|'
    ;
SEMICOLON
    : ';'
    ;
DOT
    : '.'
    ;
BACKSLASH
    : '\\'
    ;
TYPEINT
    : 'int'
    ;
TYPESTRING
    : 'string'
    ;
NIL
    : 'nil'
    ;

//program

prog
    : exp
    ;

//declarations

decs
    : dec*
    ;
dec
    : tydecs
    | vardec
    | fundecs
    ;
tydecs
    : tydec+
    ;
tydec
    : TYPE typeid EQUAL ty
    ;
ty
    : typeid
    | LEFTBRACE tyfields RIGHTBRACE
    | ARRAY OF typeid
    ;
tyfields
    : ID COLON typeid (COMMA ID COLON typeid)*
    |
    ;
typeid
    : TYPEINT
    | TYPESTRING
    | ID
    ;
vardec
    : VAR ID COLON typeid ASSIGN exp
    | VAR ID ASSIGN exp
    ;
fundecs
    : fundec+
    ;
fundec
    : FUNCTION ID LEFTPARENTHESIS tyfields RIGHTPARENTHESIS EQUAL exp
    | FUNCTION ID LEFTPARENTHESIS tyfields RIGHTPARENTHESIS COLON typeid EQUAL exp
    ;

// varibales and expressions

lvalue
    : lvalue LEFTBRACKET exp RIGHTBRACKET
    | lvalue DOT ID
    | ID
    ;
exp
    : lvalue
    | NIL
    | INT
    | STRING
    | LEFTPARENTHESIS exp (SEMICOLON exp)* RIGHTPARENTHESIS
    | LEFTPARENTHESIS RIGHTPARENTHESIS
    | functioncall
    | recordcreation
    | arraycreation
    | assignexp
    | ifexp
    | whileexp
    | forexp
    | breakexp
    | letexp
    | MINUS exp
    | exp (TIMES|DIV) exp
    | exp (PLUS|MINUS) exp
    | exp (EQUAL|NE|GT|LT|GE|LE) exp
    | exp AND exp
    | exp OR exp
    ;
functioncall
    : ID LEFTPARENTHESIS exp (COMMA exp)* RIGHTPARENTHESIS
    | ID LEFTPARENTHESIS RIGHTPARENTHESIS
    ;
recordcreation
    : typeid LEFTBRACE ID EQUAL exp (COMMA ID EQUAL exp)* RIGHTBRACE
    | typeid LEFTBRACE RIGHTBRACE
    ;
arraycreation
    : typeid LEFTBRACKET exp RIGHTBRACKET OF exp
    ;
assignexp
    : lvalue ASSIGN exp
    ;
ifexp
    : IF exp THEN exp ELSE exp
    | IF exp THEN exp
    ;
whileexp
    : WHILE exp DO exp
    ;
forexp
    : FOR ID ASSIGN exp TO exp DO exp
    ;
breakexp
    : BREAK
    ;
letexp
    : LET decs IN (exp (SEMICOLON exp)*)? END
    ;

// lexer

INT
    : DIGIT+
    ;
fragment DIGIT
    : '0'..'9'
    ;
STRING
    : QUOTE ((~('\\'|'"'))|ESCAPEDSEQ)* QUOTE
    ;
fragment ESCAPEDSEQ
    : BACKSLASH 'n'
    | BACKSLASH 't'
    | BACKSLASH '^c'
    | BACKSLASH DIGIT DIGIT DIGIT
    | BACKSLASH QUOTE
    | BACKSLASH BACKSLASH
    | IGNORE
    ;
fragment IGNORE
    : ('\\' (' '|'\t'|'\r'|'\n'|'\f')+ '\\') -> skip
    ;
ID
    : PRINTABLECHAR (PRINTABLECHAR|DIGIT)*
    ;
fragment PRINTABLECHAR
    : 'a'..'z'
    | 'A'..'Z'
    | '_'
    ;
FORMATCHAR
    : (' '|'\t'|'\r'|'\n'|'\f') -> skip
    ;
COMMENT
    : '/*' (~('/')|(('/')~('*')))*? (COMMENT (~('/')|(('/')~('*')))*?)* '*/' -> skip
    ;