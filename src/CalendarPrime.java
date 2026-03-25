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
        fileHandler.updateCurrentTitle(title.substring(1, title.length() - 1));
        return 0;
    }

    @Override
    public Integer visitDesc(CalendarParser.DescContext ctx) {
        if (ctx.STR() == null) return -1;
        var desc = ctx.STR().toString();
        fileHandler.updateCurrentDesc(desc.substring(1, desc.length() - 1));
        return 0;
    }

    @Override
    public Integer visitLocat(CalendarParser.LocatContext ctx) {
        if (ctx.STR() == null) return -1;
        var locat = ctx.STR().toString();
        fileHandler.updateCurrentLocat(locat.substring(1, locat.length() - 1));
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
