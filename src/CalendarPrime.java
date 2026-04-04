import org.antlr.v4.runtime.Token;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class CalendarPrime extends CalendarParserBaseVisitor<Long>{
    CalendarFileHandler fileHandler;
    Long currentID;
    Long selectedID;

    final ZoneOffset timeOffset;

    public CalendarPrime(CalendarFileHandler fh) {
        super();
        fileHandler = fh;
        timeOffset = ZoneId.of("Europe/Warsaw").getRules().getOffset(LocalDateTime.now());
        System.out.println("Visitor ready");
    }

    @Override
    public Long visitProg_start(CalendarParser.Prog_startContext ctx) {
        if (ctx.stat().isEmpty()) return -1L;
        long result = 0;
        for (CalendarParser.StatContext stat : ctx.stat()) {
            if (stat.def().isEmpty()) {
                result = -2;
                break;
            }
            long o = visit(stat.def());
            if (o != 0) {
                System.err.println(o);
                result = -3;
                break;
            }
        }
        return result;
    }

    @Override
    public Long visitDef_select(CalendarParser.Def_selectContext ctx) {
        if (ctx.selected == null) return -1L;
        currentID = visit(ctx.selected);
        return 0L;
    }

    @Override
    public Long visitDef_change(CalendarParser.Def_changeContext ctx) {
        if (ctx.selected == null) return 1L;
        selectedID = visit(ctx.selected);
        if (selectedID == null || selectedID < 0) return 1L;
        fileHandler.loadFile(selectedID);
        if (ctx.what.isEmpty()) return 2L;
        for (CalendarParser.ChangeContext change : ctx.what){
            if (visit(change) != 0) return 3L;
        }
        fileHandler.saveCurrent();
        return 0L;
    }

    @Override
    public Long visitDef_print(CalendarParser.Def_printContext ctx) {
        if (ctx.selected == null) return 1L;
        selectedID = visit(ctx.selected);
        if (selectedID == null || selectedID < 0) return 1L;
        fileHandler.loadFile(selectedID);
        System.out.println(fileHandler.currentToString());
        return 0L;
    }

    @Override
    public Long visitDef_export(CalendarParser.Def_exportContext ctx) {
        if (ctx.what == null) return 1L;
        selectedID = visit(ctx.what);
        if (selectedID == null || selectedID < 0) return 1L;
        fileHandler.loadFile(selectedID);
        fileHandler.exportICS();
        return 0L;
    }

    @Override
    public Long visitTitle(CalendarParser.TitleContext ctx) {
        if (ctx.STR() == null) return -1L;
        var title = ctx.STR().toString();
        if (title.isEmpty()) return -2L;
        fileHandler.updateCurrentTitle(title.substring(1, title.length() - 1));
        return 0L;
    }

    @Override
    public Long visitDesc(CalendarParser.DescContext ctx) {
        if (ctx.STR() == null) return -1L;
        var desc = ctx.STR().toString();
        if (desc.isEmpty()) return -2L;
        fileHandler.updateCurrentDesc(desc.substring(1, desc.length() - 1));
        return 0L;
    }

    @Override
    public Long visitLocat(CalendarParser.LocatContext ctx) {
        if (ctx.STR() == null) return -1L;
        var locat = ctx.STR().toString();
        if (locat.isEmpty()) return -2L;
        fileHandler.updateCurrentLocat(locat.substring(1, locat.length() - 1));
        return 0L;
    }

    @Override
    public Long visitStart(CalendarParser.StartContext ctx) {
        var time = visitToLocalDateTime(visit(ctx.datetime()));
        fileHandler.updateCurrentStart(time);
        return 0L;
    }

    @Override
    public Long visitEnd(CalendarParser.EndContext ctx) {
        var time = visitToLocalDateTime(visit(ctx.datetime()));
        fileHandler.updateCurrentEnd(time);
        return 0L;
    }

    private LocalDateTime createLocalDateTime(Token year2, Token month2, Token day2, Token hour2, Token min2) {
        long hour = Long.parseLong(hour2.getText());
        long min = Long.parseLong(min2.getText());
        LocalDateTime time = createLocalDateTime(year2, month2, day2);
        time = time.withHour((int) hour).withMinute((int) min);
        return time;
    }

    private LocalDateTime createLocalDateTime(Token year2, Token month2, Token day2) {
        long year = Long.parseLong(year2.getText());
        long month = Long.parseLong(month2.getText());
        long day = Long.parseLong(day2.getText());
        LocalDateTime time = LocalDateTime.now();
        time = time.withYear((int) year).withMonth((int) month).withDayOfMonth((int) day);
        return time;
    }

    private LocalDateTime visitToLocalDateTime(Long output) {
        Instant instant = Instant.ofEpochSecond(output);
        return LocalDateTime.ofInstant(instant, timeOffset);
    }

    @Override
    public Long visitAll_day(CalendarParser.All_dayContext ctx) {
        boolean b = Boolean.parseBoolean(ctx.check.getText());
        fileHandler.updateCurrentAllDay(b);
        return 0L;
    }

    @Override
    public Long visitRepeat_event(CalendarParser.Repeat_eventContext ctx) {
        return visit(ctx.repeat());
    }

    @Override
    public Long visitRepeat_every_num(CalendarParser.Repeat_every_numContext ctx) {
        var repeat_data = fileHandler.getCurrentRepeatData();
        long i = Long.parseLong(ctx.INT().getText());
        repeat_data.setRepeatEvery(i);
        return 0L;
    }

    @Override
    public Long visitRepeat_cycle_week(CalendarParser.Repeat_cycle_weekContext ctx) {
        int mask = 0b00000000;
        var repeat_data = fileHandler.getCurrentRepeatData();
        for (Token weekday : ctx.when) {
            switch (weekday.getType()) {
                case CalendarLexer.MONDAY_W -> mask = mask ^ CalendarData.RepeatData.MONDAY;
                case CalendarLexer.TUESDAY_W -> mask = mask ^ CalendarData.RepeatData.TUESDAY;
                case CalendarLexer.WEDNESDAY_W -> mask = mask ^ CalendarData.RepeatData.WEDNESDAY;
                case CalendarLexer.THURSDAY_W -> mask = mask ^ CalendarData.RepeatData.THURSDAY;
                case CalendarLexer.FRIDAY_W -> mask = mask ^ CalendarData.RepeatData.FRIDAY;
                case CalendarLexer.SATURDAY_W -> mask = mask ^ CalendarData.RepeatData.SATURDAY;
                case CalendarLexer.SUNDAY_W -> mask = mask ^ CalendarData.RepeatData.SUNDAY;
            }
        }
        repeat_data.setRepeatCycle(CalendarData.RepeatData.RepeatCycle.WEEKLY);
        repeat_data.setWeekdayMask(mask);
        return 0L;
    }

    @Override
    public Long visitRepeat_cycle(CalendarParser.Repeat_cycleContext ctx) {
        var repeat_data = fileHandler.getCurrentRepeatData();
        switch (ctx.cycle.getType()) {
            case CalendarLexer.REPEAT_DAILY_W:
                repeat_data.setRepeatCycle(CalendarData.RepeatData.RepeatCycle.DAILY);
                break;
            case CalendarLexer.REPEAT_WEEKLY_W:
                repeat_data.setRepeatCycle(CalendarData.RepeatData.RepeatCycle.WEEKLY);
                break;
            case CalendarLexer.REPEAT_MONTHLY_W:
                repeat_data.setRepeatCycle(CalendarData.RepeatData.RepeatCycle.MONTHLY);
                break;
            case CalendarLexer.REPEAT_YEARLY_W:
                repeat_data.setRepeatCycle(CalendarData.RepeatData.RepeatCycle.YEARLY);
                break;
            case CalendarLexer.REPEAT_NONE_W:
                repeat_data.setRepeatCycle(CalendarData.RepeatData.RepeatCycle.NONE);
                break;
        }
        return 0L;
    }

    @Override
    public Long visitRepeat_othr(CalendarParser.Repeat_othrContext ctx) {
        return visit(ctx.repeat_end());
    }

    @Override
    public Long visitInf_repeat(CalendarParser.Inf_repeatContext ctx) {
        var repeat_data = fileHandler.getCurrentRepeatData();
        repeat_data.setRepeatEnd(CalendarData.RepeatData.RepeatEnd.INF);
        return 0L;
    }

    @Override
    public Long visitNum_repeat(CalendarParser.Num_repeatContext ctx) {
        var repeat_data = fileHandler.getCurrentRepeatData();
        long i = Long.parseLong(ctx.val.getText());
        repeat_data.setRepeatEnd(CalendarData.RepeatData.RepeatEnd.AFTER);
        repeat_data.setRepeatAfter(i);
        return 0L;
    }

    @Override
    public Long visitDate_repeat(CalendarParser.Date_repeatContext ctx) {
        var repeat_data = fileHandler.getCurrentRepeatData();
        var date = visitToLocalDateTime(visit(ctx.date()));
        repeat_data.setRepeatEnd(CalendarData.RepeatData.RepeatEnd.ON);
        repeat_data.setRepeatOn(date);
        return 0L;
    }

    @Override
    public Long visitFile_id(CalendarParser.File_idContext ctx) {
        if (ctx.INT() == null) return -1L;
        long id = Long.parseLong(ctx.INT().toString());
        if (fileHandler.checkForFile(id)) {
            CalendarData output = fileHandler.loadFile(id);
            if (output != null) {
                System.out.printf("Loaded file ID: %d\n", output.ID);
                return output.ID;
            }
        }
        return -2L;
    }

    @Override
    public Long visitWithout_date(CalendarParser.Without_dateContext ctx) {
        var date = visitToLocalDateTime(visit(ctx.date()));
        fileHandler.pushWithoutDates(date);
        return 0L;
    }

    @Override
    public Long visitWithout_range(CalendarParser.Without_rangeContext ctx) {
        var date_from = visitToLocalDateTime(visit(ctx.from));
        var date_to = visitToLocalDateTime(visit(ctx.to));
        fileHandler.pushWithoutDates(date_from, date_to);
        return 0L;
    }

    @Override
    public Long visitWithout_reset(CalendarParser.Without_resetContext ctx) {
        fileHandler.resetWithoutDates();
        return 0L;
    }

    @Override
    public Long visitCurrent(CalendarParser.CurrentContext ctx) {
        if (currentID == null) return -1L;
        return currentID;
    }

    @Override
    public Long visitSelect_new(CalendarParser.Select_newContext ctx) {
        Long id = fileHandler.newEmpty();
        if (id == null) return -1L;
        System.out.printf("New file ID: %d\n", id);
        return id;
    }

    @Override
    public Long visitSelect_from(CalendarParser.Select_fromContext ctx) {
        return super.visitSelect_from(ctx);
    }

    @Override
    public Long visitDate(CalendarParser.DateContext ctx) {
        var time = createLocalDateTime(ctx.year, ctx.month, ctx.day);
        return time.toEpochSecond(timeOffset);
    }

    @Override
    public Long visitDatetime(CalendarParser.DatetimeContext ctx) {
        var time = createLocalDateTime(ctx.year, ctx.month, ctx.day, ctx.hour, ctx.min);
        return time.toEpochSecond(timeOffset);
    }
}
