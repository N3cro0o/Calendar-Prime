import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class CalendarMain {
    private CharStream input;
    private CalendarLexer lexer;
    private CommonTokenStream tokenStream = null;
    private CalendarParser parser;
    private ParseTree tree = null;

    private CalendarPrime visitor;
    private CalendarFileHandler fileHandler;

    public CalendarMain(CharStream i) {
        fileHandler = new CalendarFileHandler();
        visitor = new CalendarPrime(fileHandler);
        input = i;
    }

    public void setup(){
        lexer = new CalendarLexer(input);
        tokenStream = new CommonTokenStream(lexer);
        parser = new CalendarParser(tokenStream);
        tree = parser.prog_start();
    }

    public void run(){
        var output = visitor.visit(tree);
        System.out.println(output);
    }

    //in current change title "NEW"

    static void main(String[] args) throws Exception {
//        CharStream input = CharStreams.fromString("select from 1; print current; in current change repeat without range 2026 05 01 - 2026 05 15; select from 0; print 1");
//        CharStream input = CharStreams.fromString("select new");
        CharStream input = CharStreams.fromString("print 1; export from 1;");
        var prog = new CalendarMain(input);
        prog.setup();
        prog.run();
    }
}