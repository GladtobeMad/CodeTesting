import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Created by Vlad on 3/25/17.
 */
public class CodeTestingWindow extends JFrame {

    private File toTest;
    private MainWindow window;

    CodeTestingWindow(File toTest, MainWindow window) {

        super ("Testing file " + toTest.getName());

        this.toTest = toTest;
        this.window = window;

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
