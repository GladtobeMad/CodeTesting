import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Created by Vlad on 3/25/17.
 */
public class CodeTestingWindow extends JFrame {

    private File toTest;
    private MainWindow window;
    private long id;
    private String teamName;

    private JTextField timeoutField;
    private JComboBox problemComboBox, resultComboBox;
    private JButton testButton, finishButton;
    private JTextPane testResultPane;



    CodeTestingWindow(File test, MainWindow w, long i, String name) {

        super ("Testing file " + test.getName());

        this.toTest = test;
        this.window = w;
        this.id = i;
        this.teamName = name;

        this.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(400, 300));
        panel.setLayout(new FlowLayout());

        JLabel timeoutLabel = new JLabel("Timeout in ms:");
        timeoutLabel.setHorizontalAlignment(JLabel.CENTER);
        timeoutLabel.setPreferredSize(new Dimension(150, 30));

        JLabel problemLabel = new JLabel("Problem:");
        problemLabel.setHorizontalAlignment(JLabel.CENTER);
        problemLabel.setPreferredSize(new Dimension(150, 30));

        timeoutField = new JTextField("5000");
        timeoutField.setPreferredSize(new Dimension(150, 30));

        problemComboBox = new JComboBox();
        problemComboBox.addItem("");
        for (String problem : window.getProblemList()) {
            problemComboBox.addItem(problem);
        }

        String probableSelection = null;
        for (String problem : window.getProblemList()) {
                if (toTest.getName().endsWith(".java")) {
                    if (toTest.getName().substring(0, toTest.getName().length()-5).equals(problem)) {
                        probableSelection = problem;
                    }
                }
        }
        if (probableSelection != null) {
            problemComboBox.setSelectedItem(probableSelection);
        } else {
            problemComboBox.setSelectedItem("");
        }

        problemComboBox.setPreferredSize(new Dimension(150, 30));

        testButton = new JButton("Test");
        testButton.setPreferredSize(new Dimension(150, 30));
        testButton.addActionListener(l -> {
            if (!problemComboBox.getSelectedItem().equals("")) {
                Integer timeout = null;
                try {
                    timeout = Integer.parseInt(timeoutField.getText());
                } catch (NumberFormatException e) {

                }
                if (timeout != null) {
                    File input = window.getInputFile((String) problemComboBox.getSelectedItem());
                    File output = window.getOutputFile((String) problemComboBox.getSelectedItem());
                    CodeTesting testing = new CodeTesting(toTest, input, output, timeout);
                    testResultPane.setText(testing.test());
                } else {
                    JOptionPane.showMessageDialog(this, "Timeout must be an integer!", "Unable to test code", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        resultComboBox = new JComboBox();
        resultComboBox.addItem("");
        resultComboBox.addItem("Compile Error");
        resultComboBox.addItem("Runtime Error");
        resultComboBox.addItem("Timeout Error");
        resultComboBox.addItem("Incorrect");
        resultComboBox.addItem("Correct");
        resultComboBox.setPreferredSize(new Dimension(150, 30));

        testResultPane = new JTextPane();
        testResultPane.setEditable(false);
        JScrollPane textScroll = new JScrollPane(testResultPane);
        textScroll.setPreferredSize(new Dimension(380, 200));

        finishButton = new JButton("Finish");
        finishButton.setPreferredSize(new Dimension(150, 30));
        finishButton.addActionListener(l -> {
            if (resultComboBox.getSelectedItem() != "") {
                window.getTeamHandler(teamName).fileUpdate((String) resultComboBox.getSelectedItem(), id);
                int penalty = 0;
                int solved = 0;
                String problemName = (String) problemComboBox.getSelectedItem();
                if (window.getProblemPenalties(teamName) != null) {
                    if (window.getProblemPenalties(teamName).containsKey(problemName)) {
                        penalty = window.getProblemPenalties(teamName).get(problemName);
                    }
                }
                if (window.getProblemScores(teamName) != null) {
                    if (window.getProblemScores(teamName).containsKey(problemName)) {
                        solved = window.getProblemScores(teamName).get(problemName);
                    }
                }

                if (resultComboBox.getSelectedItem().equals("Correct")) {
                    if (solved == 0) {
                        penalty+=20;
                        solved = 1;
                        penalty += ((id - window.getBeginTime())/1000);
                        window.setProblemScore(teamName, problemName, solved);
                        window.setProblemPenalty(teamName, problemName, penalty);
                        window.addTeamScore(teamName, solved);
                        window.addTeamPenalty(teamName, penalty);
                    } else {
                        window.setProblemPenalty(teamName, problemName, penalty + 20);
                        window.addTeamPenalty(teamName, 20);
                    }
                } else {
                    penalty += 20;
                    window.setProblemPenalty(teamName, problemName, penalty);
                }
                dispose();
                window.setEnabled(true);
            }
        });

        panel.add(timeoutLabel);
        panel.add(problemLabel);
        panel.add(timeoutField);
        panel.add(problemComboBox);
        panel.add(testButton);
        panel.add(textScroll);
        panel.add(resultComboBox);
        panel.add(finishButton);

        add(panel, BorderLayout.LINE_START);

        pack();
        setSize(400, 400);
        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                window.setEnabled(true);
            }
        });
    }



}
