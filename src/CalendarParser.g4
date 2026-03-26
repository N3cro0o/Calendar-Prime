parser grammar CalendarParser;
options { tokenVocab=CalendarLexer; }

prog_start : stat* ;

stat : def (EOL | SEPARATOR)* ;

def
    : SELECT_W selected = select    #def_select
    | LIST_W ALL_W      #def_list
    | DELETE_W to_del = file        #def_delete
    | IN_W selected = file CHANGE_W what += change (COMMA what += change)*     #def_change
    | PRINT_W selected = file       #def_print
    ;

select
    : NEW_W     #select_new
    | FROM_W target = file       #select_from
    ;

change
    : TITLE_W new = STR         #title
    | DESC_W new = STR          #desc
    | LOCAT_W new = STR         #locat
    | START_W year = INT_FOUR month = INT_TWO day = INT_TWO hour = INT_TWO min = INT_TWO    #start
    | END_W year = INT_FOUR month = INT_TWO day = INT_TWO hour = INT_TWO min = INT_TWO       #end
    | ALL_DAY_W check = BOOL        #all_day
    | REPEAT_W repeat           #repeat_event
    ;

repeat
    : INT       #repeat_every_num
    | cycle = (REPEAT_NONE_W
    | REPEAT_DAILY_W
    | REPEAT_WEEKLY_W
    | REPEAT_MONTHLY_W
    | REPEAT_YEARLY_W)      #repeat_cycle
    | END_W repeat_end      #repeat_othr
    ;

repeat_end
    : INF_W     #inf_repeat
    | END_AFTER_W INT       #num_repeat
    | END_ON_W year = INT_FOUR month = INT_TWO day = INT_TWO    #date_repeat
    ;

file
    : INT       #file_id
    | STR       #file_dir
    | CURRENT_FILE_W        #current
    ;