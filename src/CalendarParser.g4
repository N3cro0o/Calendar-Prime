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
    | EXPORT_W FROM_W what = file   #def_export
    ;

select
    : NEW_W     #select_new
    | FROM_W target = file       #select_from
    ;

change
    : TITLE_W new = STR         #title
    | DESC_W new = STR          #desc
    | LOCAT_W new = STR         #locat
    | START_W datetime    #start
    | END_W datetime       #end
    | ALL_DAY_W check = BOOL        #all_day
    | REPEAT_W repeat           #repeat_event
    ;

repeat
    : val = (INT | INT_TWO | INT_FOUR)       #repeat_every_num
    | REPEAT_WEEKLY_W PAREN_OPEN (when += (MONDAY_W | TUESDAY_W | WEDNESDAY_W | THURSDAY_W | FRIDAY_W | SATURDAY_W | SUNDAY_W))+ PAREN_CLOSE    #repeat_cycle_week
    | cycle = (REPEAT_NONE_W
    | REPEAT_DAILY_W
    | REPEAT_WEEKLY_W
    | REPEAT_MONTHLY_W
    | REPEAT_YEARLY_W)      #repeat_cycle
    | END_W repeat_end      #repeat_othr
    | WITHOUT_W without     #repeat_without
    ;

without
    : date    #without_date // Returns unix timestamp
    | RANGE_W from = date RANGE_SEPARATOR to = date       #without_range
    | REPEAT_NONE_W        #without_reset
    ;

repeat_end
    : INF_W     #inf_repeat
    | END_AFTER_W val = (INT | INT_TWO | INT_FOUR)       #num_repeat
    | END_ON_W date    #date_repeat
    ;

file
    : val = (INT | INT_TWO | INT_FOUR)       #file_id
    | STR       #file_dir
    | CURRENT_FILE_W        #current
    ;

date : year = INT_FOUR month = INT_TWO day = INT_TWO ;

datetime : year = INT_FOUR month = INT_TWO day = INT_TWO hour = INT_TWO min = INT_TWO ;