import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class CalendarDesktopApp extends JFrame {

    private JTextArea inputCodeArea;
    private JTextArea consoleOutputArea;

    public CalendarDesktopApp() {
        setTitle("Calendar ICS Generator - ANTLR4");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Wyśrodkowanie okna na ekranie

        // Główny layout (podział na lewą i prawą stronę)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // --- PANEL LEWY: EDYTOR KODU ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        inputCodeArea = new JTextArea();
        inputCodeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputCodeArea.setTabSize(4);

        // Przykładowy skrypt testujący gramatykę
        inputCodeArea.setText("select new;\n" +
                "in current change" +
                " title \"Spotkanie Zespolu\",\n" +
                "    description \"Wazne omowienie projektu\",\n" +
                "    location \"Sala konferencyjna\",\n" +
                "    start 2026 04 20 10 00,\n" +
                "    end 2026 04 20 12 00;\n\n" +
                "print current;\n" +
                "export from current;\n" +
                "list all;\n");

        JScrollPane inputScroll = new JScrollPane(inputCodeArea);
        inputScroll.setBorder(new TitledBorder("Edytor skryptów kalendarza"));
        leftPanel.add(inputScroll, BorderLayout.CENTER);

        JButton executeBtn = new JButton("Uruchom skrypt (Execute)");
        executeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        executeBtn.setBackground(new Color(54, 219, 0));
        executeBtn.setOpaque(true);
        executeBtn.setBorderPainted(false);
        executeBtn.setFocusPainted(false);

        executeBtn.addActionListener(e -> executeAntlrGrammar());
        leftPanel.add(executeBtn, BorderLayout.SOUTH);

        // --- PANEL PRAWY: KONSOLA WYNIKOWA ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        consoleOutputArea = new JTextArea();
        consoleOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleOutputArea.setBackground(new Color(30, 30, 30));
        consoleOutputArea.setForeground(new Color(200, 200, 200));
        consoleOutputArea.setEditable(false);

        JScrollPane consoleScroll = new JScrollPane(consoleOutputArea);
        consoleScroll.setBorder(new TitledBorder("Konsola wyników (System.out)"));
        rightPanel.add(consoleScroll, BorderLayout.CENTER);

        // Dodanie paneli do okna
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane);

        // Przekierowanie strumieni wyjścia, aby `System.out.println` pisało po JTextArea
        redirectSystemStreams();
    }

    private void executeAntlrGrammar() {
        // Czyszczenie konsoli przed każdym nowym uruchomieniem
        consoleOutputArea.setText("");
        System.out.println("--- Rozpoczynam parsowanie i wykonywanie ---");

        String code = inputCodeArea.getText();
        if (code.trim().isEmpty()) {
            System.err.println("Błąd: Edytor jest pusty!");
            return;
        }

        try {
            // Zamiana tekstu z interfejsu (JTextArea) na CharStream dla ANTLR
            CharStream input = CharStreams.fromString(code);

            // Wywołanie głównej logiki interpretera z wcześniej przygotowanej klasy
            CalendarMain prog = new CalendarMain(input);
            prog.setup();
            prog.run();

            System.out.println("--- Zakończono pomyślnie ---");
        } catch (Exception ex) {
            System.err.println("Wystąpił błąd podczas kompilacji/wykonania:");
            ex.printStackTrace();
        }
    }

    // Klasa pomocnicza do przekierowania logów z System.out / System.err do JTextArea
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateTextArea(new String(b, off, len));
            }

            private void updateTextArea(final String text) {
                SwingUtilities.invokeLater(() -> {
                    consoleOutputArea.append(text);
                    // Automatyczne przewijanie w dół (scroll)
                    consoleOutputArea.setCaretPosition(consoleOutputArea.getDocument().getLength());
                });
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    // --- BRAKUJĄCY ELEMENT: METODA MAIN ---
    public static void main(String[] args) {
        // Interfejsy Swing muszą być uruchamiane w wątku obsługi zdarzeń (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // Opcjonalnie: ustawienie natywnego wyglądu (Look and Feel) dla systemu
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Zignoruj błędy L&F i użyj domyślnego
            }

            CalendarDesktopApp app = new CalendarDesktopApp();
            app.setVisible(true); // Wyświetlenie okna
        });
    }
}