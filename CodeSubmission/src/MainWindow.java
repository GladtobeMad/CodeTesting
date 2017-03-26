import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by VladOleksenko on 3/17/17.
 */
public class MainWindow extends Frame {

    private JTabbedPane tabs;
    private JComponent submissionPanel, leaderboardPanel, runsPanel;
    private JTable runsTable, leaderboardTable;
    private FileDialog fileChooser = new FileDialog(this);
    private JButton submitFileButton = new JButton("Submit");
    private JButton chooseFileButton = new JButton("Choose File");
    private JLabel submitFileLabel = new JLabel(" ");
    private final Frame window = this;
    private String file = null;
    private JTextField ipField, teamNameField;
    private JButton connectButton;
    private ServerHandler handler;
    private ArrayList<Long> submissionIds = new ArrayList<>();


    public MainWindow() {

        super("Code Submission");

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        teamNameField = new JTextField();
        teamNameField.setMaximumSize(new Dimension(250,20));
        this.add(teamNameField);

        ipField = new JTextField();
        ipField.setMaximumSize(new Dimension(250,20));
        this.add(ipField);


        connectButton = new JButton("Connect");
        connectButton.addActionListener(l -> {
            Socket socket;
            if (ipField.getText().isEmpty() || teamNameField.getText().isEmpty()) {
                return;
            }
            try {
                socket = new Socket(ipField.getText(), 51234);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.println("TEAMNAME " + teamNameField.getText());
                out.flush();
                String response = in.readLine();
                if (response.equals("TEAMACCEPT")) {
                    this.start(teamNameField.getText());
                    handler = new ServerHandler(socket, in, out, this);
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to connect to " + ipField.getText() + "! Please choose a different team name.", "Unable to Connect", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Unable to connect to " + ipField.getText() + "! Make sure the address is correct.", "Unable to Connect", JOptionPane.ERROR_MESSAGE);
            }
        });
        this.add(connectButton);

        tabs = new JTabbedPane();

        submissionPanel = new JPanel();
        submissionPanel.setLayout(new BoxLayout(submissionPanel, BoxLayout.PAGE_AXIS));
        submissionPanel.add(submitFileLabel);
        submissionPanel.add(Box.createHorizontalGlue());

        chooseFileButton.addActionListener(e -> {
            fileChooser.setFile("*.java");
            fileChooser.setFilenameFilter((dir, file) -> file.endsWith(".java"));
            fileChooser.setVisible(true);
            String filename = fileChooser.getFile();
            String directory = fileChooser.getDirectory();
            if (filename != null && directory != null) {
                submitFileLabel.setText(filename);
                file = directory + filename;
                submitFileButton.setEnabled(true);
            }
        });
        chooseFileButton.setEnabled(false);

        submitFileButton.setEnabled(false);
        submitFileButton.addActionListener(e -> {
            if (file != null) {
                File code = new File(file);
                file = null;
                submitFileButton.setEnabled(false);
                if (handler.submitFile(code)) {
                    tabs.setSelectedIndex(1);
                } else {
                    JOptionPane.showMessageDialog(window, "Unable to submit file.", "Unable to submit file", JOptionPane.ERROR_MESSAGE);
                }
                submitFileLabel.setText(" ");

            }
        });

        submissionPanel.add(chooseFileButton);
        submissionPanel.add(Box.createHorizontalGlue());
        submissionPanel.add(submitFileButton);
        tabs.add("Sumbit Code", submissionPanel);

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Submission Name");
        model.addColumn("Submission Time");
        model.addColumn("Status");
        runsTable = new JTable(model);
        runsTable.setEnabled(false);
        runsTable.getTableHeader().setEnabled(false);
        runsPanel = new JScrollPane(runsTable);
        tabs.add("Submissions", runsPanel);


        DefaultTableModel lmodel = new DefaultTableModel();
        lmodel.addColumn("Team");
        lmodel.addColumn("Problems Solved");
        lmodel.addColumn("Penalty");
        leaderboardTable = new JTable(lmodel);
        leaderboardTable.setEnabled(false);
        leaderboardTable.getTableHeader().setEnabled(false);
        leaderboardPanel = new JScrollPane(leaderboardTable);
        tabs.add("Leaderboard", leaderboardPanel);

        pack();
        setSize(640, 480);
        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

    }
    public void start(String teamName) {
        window.setTitle("Code Submission - " + teamName);
        window.remove(ipField);
        window.remove(teamNameField);
        window.remove(connectButton);
        window.add(tabs);
        window.validate();
        window.repaint();
    }


    public void updateLeaderboard(String[][] data) {
        DefaultTableModel model = (DefaultTableModel) leaderboardTable.getModel();

        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        for (int i = 0; i < data.length; i++) {
            model.addRow(data[i]);
        }

    }

    public void addSubmission(String name, long id, String status) {
        DefaultTableModel model = (DefaultTableModel) runsTable.getModel();
        String time = new SimpleDateFormat("h:mm a").format(new Timestamp(id));
        model.insertRow(0, new Object[]{name, time, status});
        submissionIds.add(0, id);
    }

    public void updateSubmission(long id, String status) {
        int index = submissionIds.indexOf(id);
        if (index != -1) {
            DefaultTableModel model = (DefaultTableModel) runsTable.getModel();
            model.setValueAt(status, index, 2);
        }
    }

    public void startSession() {
        chooseFileButton.setEnabled(true);
    }

    public void endSession() {
        chooseFileButton.setEnabled(false);
        submitFileButton.setEnabled(false);
    }
}
