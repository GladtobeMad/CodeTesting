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
    private JTextField timeoutField;
    private JComboBox problemComboBox;
    private JButton testButton, finishButton;



    CodeTestingWindow(File test, MainWindow w) {

        super ("Testing file " + test.getName());

        this.toTest = test;
        this.window = w;

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
                File input = window.getInputFile((String) problemComboBox.getSelectedItem());
                File output = window.getOutputFile((String) problemComboBox.getSelectedItem());
                CodeTesting testing = new CodeTesting(toTest, input, output);
                System.out.println(testing.test());
            }
        });

        panel.add(timeoutLabel);
        panel.add(problemLabel);
        panel.add(timeoutField);
        panel.add(problemComboBox);
        panel.add(testButton);

        add(panel, BorderLayout.LINE_START);

        pack();
        setSize(400, 300);
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
