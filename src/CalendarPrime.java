public class CalendarPrime extends CalendarParserBaseVisitor<Integer>{
    CalendarFileHandler fileHandler;
    private Integer selectedID = null;

    public CalendarPrime(CalendarFileHandler fh) {
        fileHandler = fh;
        System.out.println("Visitor ready");
    }

    @Override
    public Integer visitDef_change(CalendarParser.Def_changeContext ctx) {
        if (ctx.selected == null) return 1;
        selectedID = visit(ctx.selected);

        return selectedID;
    }

    @Override
    public Integer visitCurrent(CalendarParser.CurrentContext ctx) {
        if (fileHandler.isLoaded) return 0;
        return null;
    }

    @Override
    public Integer visitProg_start(CalendarParser.Prog_startContext ctx) {
        if (ctx.stat().isEmpty()) return 1;
        int result = 0;
        for (CalendarParser.StatContext stat : ctx.stat()) {
            if (stat.def().isEmpty()) {
                result = 2;
                break;
            }
            int o = visit(stat.def());
            if (o != 0) {
                result = 3;
                break;
            }
        }
        return result;
    }

    @Override
    public Integer visitDef_select(CalendarParser.Def_selectContext ctx) {
        if (ctx.selected == null) return 1;
        return visit(ctx.selected);
    }

    @Override
    public Integer visitSelect_new(CalendarParser.Select_newContext ctx) {
        fileHandler.newEmpty();
        return 0;
    }
}
