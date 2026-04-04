lexer grammar CalendarLexer;
fragment INT_SINGLE: [0-9] ;

IN_W : 'in' ;
SELECT_W : 'select' ;
NEW_W : 'new' ;
FROM_W : 'from' ;
DELETE_W : 'delete' ;

LIST_W : 'list' ;
ALL_W : 'all' ;
CHANGE_W : 'change';
TITLE_W : 'title';
DESC_W : 'description' | 'desc';
LOCAT_W : 'location';
START_W : 'start';
END_W : 'end';
INF_W : 'inf' ;
ALL_DAY_W : 'allDay';
REPEAT_W : 'repeat';
WITHOUT_W : 'without';
RANGE_W : 'range' ;
REPEAT_NONE_W : 'none';
REPEAT_DAILY_W : 'daily' | 'd';
REPEAT_WEEKLY_W : 'weekly' | 'w';
REPEAT_MONTHLY_W : 'monthly' | 'm';
REPEAT_YEARLY_W : 'yearly' | 'y';
CURRENT_FILE_W : 'current' ;
PRINT_W : 'print' ;
EXPORT_W : 'export' ;
TO_W : 'to' ;

END_AFTER_W : 'after';
END_ON_W : 'on';

BOOL : 'true' | 'false' ;
INT_TWO : INT_SINGLE INT_SINGLE ;
INT_FOUR: INT_SINGLE INT_SINGLE INT_SINGLE INT_SINGLE ;
INT : (INT_SINGLE+)  ;
STR : '"' .*? '"' ;

SEPARATOR : ';' ;
RANGE_SEPARATOR : '-' ;
COMMA : ',' ;
EOL : '\n';
WS: [ \t\r\f]+ -> skip ;