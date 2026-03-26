import org.antlr.v4.runtime.Token;
import java.time.LocalDateTime;

public class CalendarPrime extends CalendarParserBaseVisitor<Integer>{
    CalendarFileHandler fileHandler;
    Integer currentID;
    Integer selectedID;

    public CalendarPrime(CalendarFileHandler fh) {
        fileHandler = fh;
        System.out.println("Visitor ready");
    }

    @Override
    public Integer visitProg_start(CalendarParser.Prog_startContext ctx) {
        if (ctx.stat().isEmpty()) return -1;
        int result = 0;
        for (CalendarParser.StatContext stat : ctx.stat()) {
            if (stat.def().isEmpty()) {
                result = -2;
                break;
            }
            int o = visit(stat.def());
            if (o != 0) {
                result = -3;
                break;
            }
        }
        return result;
    }

    @Override
    public Integer visitDef_select(CalendarParser.Def_selectContext ctx) {
        if (ctx.selected == null) return -1;
        currentID = visit(ctx.selected);
        return 0;
    }

    @Override
    public Integer visitDef_change(CalendarParser.Def_changeContext ctx) {
        if (ctx.selected == null) return 1;
        selectedID = visit(ctx.selected);
        if (selectedID == null || selectedID < 0) return 1;
        fileHandler.loadFile(selectedID);
        if (ctx.what.isEmpty()) return 2;
        for (CalendarParser.ChangeContext change : ctx.what){
            if (visit(change) != 0) return 3;
        }
        fileHandler.saveCurrent();
        return 0;
    }

    @Override
    public Integer visitDef_print(CalendarParser.Def_printContext ctx) {
        if (ctx.selected == null) return 1;
        selectedID = visit(ctx.selected);
        if (selectedID == null || selectedID < 0) return 1;
        fileHandler.loadFile(selectedID);
        System.out.println(fileHandler.currentToString());
        return 0;
    }

    @Override
    public Integer visitTitle(CalendarParser.TitleContext ctx) {
        if (ctx.STR() == null) return -1;
        var title = ctx.STR().toString();
        if (title.isEmpty()) return -2;
        fileHandler.updateCurrentTitle(title.substring(1, title.length() - 1));
        return 0;
    }

    @Override
    public Integer visitDesc(CalendarParser.DescContext ctx) {
        if (ctx.STR() == null) return -1;
        var desc = ctx.STR().toString();
        if (desc.isEmpty()) return -2;
        fileHandler.updateCurrentDesc(desc.substring(1, desc.length() - 1));
        return 0;
    }

    @Override
    public Integer visitLocat(CalendarParser.LocatContext ctx) {
        if (ctx.STR() == null) return -1;
        var locat = ctx.STR().toString();
        if (locat.isEmpty()) return -2;
        fileHandler.updateCurrentLocat(locat.substring(1, locat.length() - 1));
        return 0;
    }

    @Override
    public Integer visitStart(CalendarParser.StartContext ctx) {
        var time = createLocalDateTime(ctx.year, ctx.month, ctx.day, ctx.hour, ctx.min);
        fileHandler.updateCurrentStart(time);
        return 0;
    }

    @Override
    public Integer visitEnd(CalendarParser.EndContext ctx) {
        var time = createLocalDateTime(ctx.year, ctx.month, ctx.day, ctx.hour, ctx.min);
        fileHandler.updateCurrentEnd(time);
        return 0;
    }

    private LocalDateTime createLocalDateTime(Token year2, Token month2, Token day2, Token hour2, Token min2) {
        int hour = Integer.parseInt(hour2.getText());
        int min = Integer.parseInt(min2.getText());
        LocalDateTime time = createLocalDateTime(year2, month2, day2);
        time = time.withHour(hour).withMinute(min);
        return time;
    }

    private LocalDateTime createLocalDateTime(Token year2, Token month2, Token day2) {
        int year = Integer.parseInt(year2.getText());
        int month = Integer.parseInt(month2.getText());
        int day = Integer.parseInt(day2.getText());
        LocalDateTime time = LocalDateTime.now();
        time = time.withYear(year).withMonth(month).withDayOfMonth(day);
        return time;
    }

    @Override
    public Integer visitAll_day(CalendarParser.All_dayContext ctx) {
        Boolean b = Boolean.parseBoolean(ctx.check.getText());
        fileHandler.updateCurrentAllDay(b);
        return 0;
    }

    @Override
    public Integer visitRepeat_event(CalendarParser.Repeat_eventContext ctx) {
        return visit(ctx.repeat());
    }

    @Override
    public Integer visitRepeat_every_num(CalendarParser.Repeat_every_numContext ctx) {
        var repeat_data = fileHandler.getCurrentRepeatData();
        Integer i = Integer.parseInt(ctx.INT().getText());
        repeat_data.setRepeatEvery(i);
        return 0;
    }

    @Override
    public Integer visitRepeat_cycle(CalendarParser.Repeat_cycleContext ctx) {
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
        return 0;
    }

    @Override
    public Integer visitRepeat_othr(CalendarParser.Repeat_othrContext ctx) {
        return visit(ctx.repeat_end());
    }

    @Override
    public Integer visitInf_repeat(CalendarParser.Inf_repeatContext ctx) {
        var repeat_data = fileHandler.getCurrentRepeatData();
        repeat_data.setRepeatEnd(CalendarData.RepeatData.RepeatEnd.INF);
        return 0;
    }

    @Override
    public Integer visitNum_repeat(CalendarParser.Num_repeatContext ctx) {
        var repeat_data = fileHandler.getCurrentRepeatData();
        Integer i = Integer.parseInt(ctx.INT().getText());
        repeat_data.setRepeatEnd(CalendarData.RepeatData.RepeatEnd.AFTER);
        repeat_data.setRepeatAfter(i);
        return 0;
    }

    @Override
    public Integer visitDate_repeat(CalendarParser.Date_repeatContext ctx) {
        var repeat_data = fileHandler.getCurrentRepeatData();
        var date = createLocalDateTime(ctx.year, ctx.month, ctx.day);
        repeat_data.setRepeatEnd(CalendarData.RepeatData.RepeatEnd.ON);
        repeat_data.setRepeatOn(date);
        return 0;
    }

    @Override
    public Integer visitFile_id(CalendarParser.File_idContext ctx) {
        if (ctx.INT() == null) return -1;
        int id = Integer.parseInt(ctx.INT().toString());
        if (fileHandler.checkForFile(id)) {
            CalendarData output = fileHandler.loadFile(id);
            if (output != null) {
                System.out.printf("Loaded file ID: %d\n", output.ID);
                return output.ID;
            }
        }
        return -2;
    }

    @Override
    public Integer visitCurrent(CalendarParser.CurrentContext ctx) {
        if (currentID == null) return -1;
        return currentID;
    }

    @Override
    public Integer visitSelect_new(CalendarParser.Select_newContext ctx) {
        Integer id = fileHandler.newEmpty();
        if (id == null) return -1;
        System.out.printf("New file ID: %d\n", id);
        return id;
    }

    @Override
    public Integer visitSelect_from(CalendarParser.Select_fromContext ctx) {
        return super.visitSelect_from(ctx);
    }

}
