import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class CalendarDesktopApp extends JFrame {

    private JTextField titleField, locationField, startField, endField;
    private JTextArea descArea;
    private JTextArea consoleOutputArea;

    public CalendarDesktopApp() {
        setTitle("Calendar ICS Generator - ANTLR4");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Główny layout (podział na lewą i prawą stronę)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);

// --- PANEL LEWY: FORMULARZ  ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Używamy GridBagLayout dla ułożenia pól
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Dane Nowego Wydarzenia"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // Dodawanie pól po kolei
        addFormField(formPanel, "Tytuł:", titleField = new JTextField(20), gbc, 0);
        addFormField(formPanel, "Lokalizacja:", locationField = new JTextField(20), gbc, 1);
        addFormField(formPanel, "Start (RRRR MM DD GG MM):", startField = new JTextField("2026 04 20 10 00"), gbc, 2);
        addFormField(formPanel, "Koniec (RRRR MM DD GG MM):", endField = new JTextField("2026 04 20 12 00"), gbc, 3);

        // Opis (JTextArea w JScrollPane)
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Opis:"), gbc);
        gbc.gridx = 1;
        descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        formPanel.add(new JScrollPane(descArea), gbc);

        leftPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Sekcja przycisków
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JButton executeBtn = new JButton("Zapisz i Eksportuj");
        executeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        executeBtn.setBackground(new Color(54, 219, 0));
        executeBtn.addActionListener(e -> generateAndRunScript("new"));

        JButton listBtn = new JButton("Pokaż wszystkie zapisane");
        listBtn.addActionListener(e -> generateAndRunScript("list"));
        buttonPanel.add(executeBtn);
        buttonPanel.add(listBtn);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

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


    // metoda do dodawania etykiet i pól
    private void addFormField(JPanel panel, String label, JTextField field, GridBagConstraints gbc, int y) {
        gbc.gridx = 0; gbc.gridy = y;
        gbc.weightx = 0.1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        panel.add(field, gbc);
    }



    // generowanie skryptu
    private void generateAndRunScript(String action) {
        StringBuilder script = new StringBuilder();

        if (action.equals("new")) {
            script.append("select new;\n");
            script.append("in current change ");
            script.append("title \"").append(titleField.getText()).append("\", ");
            script.append("description \"").append(descArea.getText().replace("\n", " ")).append("\", ");
            script.append("location \"").append(locationField.getText()).append("\", ");
            script.append("start ").append(startField.getText()).append(", ");
            script.append("end ").append(endField.getText()).append(";\n");
            script.append("export from current;\n");
            script.append("print current;\n");
        } else if (action.equals("list")) {
            script.append("list all;\n");
        }

        executeAntlrGrammar(script.toString());
    }

    private void executeAntlrGrammar(String code) {
        consoleOutputArea.setText("");
        System.out.println("--- Generowanie skryptu ANTLR ---");
        System.out.println(code); // console print

        try {
            CharStream input = CharStreams.fromString(code);
            CalendarMain prog = new CalendarMain(input);
            prog.setup();
            prog.run();
            System.out.println("--- Operacja zakończona ---");
        } catch (Exception ex) {
            System.err.println("Błąd wykonania:");
            ex.printStackTrace();
        }
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) { updateTextArea(String.valueOf((char) b)); }
            @Override
            public void write(byte[] b, int off, int len) { updateTextArea(new String(b, off, len)); }
            private void updateTextArea(final String text) {
                SwingUtilities.invokeLater(() -> {
                    consoleOutputArea.append(text);
                    consoleOutputArea.setCaretPosition(consoleOutputArea.getDocument().getLength());
                });
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new CalendarDesktopApp().setVisible(true);
        });
    }
}