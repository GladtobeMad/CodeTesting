import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Created by VladOleksenko on 3/17/17.
 */
public class MainWindow extends Frame {

    private JTabbedPane tabs;
    private JComponent leaderboardPanel, actionsPanel;
    private JTable leaderboardTable;
    private JTabbedPane submissionPanel;
    private FileDialog fileChooser = new FileDialog(this);
    private final MainWindow window = this;
    private String file = null;

    private HashMap<String, ClientHandler> handlers = new HashMap<>();
    private HashMap<String, Integer> teamScores = new HashMap<>();
    private HashMap<String, Integer> teamPenalties = new HashMap<>();
    private HashMap<String, JTable> teamTables = new HashMap<>();
    private HashMap<String, File> teamFolders = new HashMap<>();

    private File source;
    private File problems;

    MainWindow(InetAddress address) {
        super("Code Judging (" + address.getHostAddress() + ")");

        Server server = new Server(window);

        tabs = new JTabbedPane();

        submissionPanel = new JTabbedPane();
        tabs.add("Submissions", submissionPanel);

        /*
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Submission Name");
        model.addColumn("Submission Time");
        model.addColumn("Status");
        runsTable = new JTable(model);
        runsTable.setEnabled(false);
        runsTable.getTableHeader().setEnabled(false);
        runsPanel = new JScrollPane(runsTable);
        tabs.add("Submissions", runsPanel);
        */

        try {
            source = new File (MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/Judging_Session_" + System.currentTimeMillis());
            if (!source.mkdir()) {
                JOptionPane.showMessageDialog(window, "Unable to create a source folder. Please restart the application.", "Unable to create a source folder", JOptionPane.ERROR_MESSAGE);
            }
            problems = new File(source.getPath() + "/problems");
            if (!problems.mkdir()) {
                JOptionPane.showMessageDialog(window, "Unable to create a problems folder. Please restart the application.", "Unable to create a problems folder", JOptionPane.ERROR_MESSAGE);
            }
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(window, "Unable to locate the source file. Please restart the application.", "Unable to locate the source file", JOptionPane.ERROR_MESSAGE);
        }


        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Team Name");
        model.addColumn("Score");
        model.addColumn("Penalty");
        leaderboardTable = new JTable(model);
        leaderboardTable.setEnabled(false); // TODO: DEAL WITH THIS
        leaderboardTable.getTableHeader().setEnabled(false);
        leaderboardPanel = new JScrollPane(leaderboardTable);
        tabs.add("Leaderboard", leaderboardPanel);

        actionsPanel = new JPanel();
        tabs.add("Actions", actionsPanel);

        this.add(tabs);
        pack();
        setSize(640, 480);
        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // TODO: INFORM CLIENTS ABOUT CLOSING
                dispose();
                System.exit(0);
            }
        });
    }

    public boolean addTeam(String name, ClientHandler handler) {
        if (!teamScores.containsKey(name) && !teamPenalties.containsKey(name) && !name.contains(" ")) {
            teamScores.put(name, 0);
            teamPenalties.put(name, 0);
            handlers.put(name, handler);

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Submission Name");
            model.addColumn("Submission Time");
            model.addColumn("Status");
            model.addColumn("Test");
            JTable teamTable = new JTable(model);
            teamTable.setEnabled(false); // TODO: DEAL WITH THIS
            teamTable.getTableHeader().setEnabled(false);
            JComponent teamPanel = new JScrollPane(teamTable);
            tabs.add("Submissions", teamPanel);

            teamTables.put(name, teamTable);
            submissionPanel.addTab(name, teamPanel);
            submissionPanel.validate();
            submissionPanel.repaint();

            File teamFolder = new File(source.getPath() + "/" + name);

            if (teamFolder.mkdir()) {
                teamFolders.put(name, teamFolder);
                return true;
            }
                JOptionPane.showMessageDialog(window, "Unable to create a team folder for team \"" + name + "\". Please restart the application.", "Unable to create a team folder", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }


    public void updateAll() {

        String[][] data = new String[teamScores.size()][3];

        int x = 0;
        for (String team : teamScores.keySet()) {
            data[x][0] = team;
            data[x][1] = String.valueOf(teamScores.get(team));
            data[x][2] = String.valueOf(teamPenalties.get(team));
            x++;
        }

        for (int i = 0; i < data.length - 1; i++) {
            for (int j = i + 1; j < data.length; j++) {
                if (Integer.parseInt(data[j][1]) > Integer.parseInt(data[i][1])) {
                    String[] temp = data[i];
                    data[i] = data[j];
                    data[j] = temp;
                } else if ((Integer.parseInt(data[j][1]) == Integer.parseInt(data[i][1]) && (Integer.parseInt(data[j][2]) < Integer.parseInt(data[i][2])))) {
                    String[] temp = data[i];
                    data[i] = data[j];
                    data[j] = temp;
                } else if ((Integer.parseInt(data[j][1]) == Integer.parseInt(data[i][1]) && (Integer.parseInt(data[j][2]) == Integer.parseInt(data[i][2]))) && (data[i][0].compareTo(data[j][0]) > 0)) {
                    String[] temp = data[i];
                    data[i] = data[j];
                    data[j] = temp;
                }
            }
        }

        DefaultTableModel model = (DefaultTableModel) leaderboardTable.getModel();

        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        for (int i = 0; i < data.length; i++) {
            model.addRow(data[i]);
        }

        for (ClientHandler handler : handlers.values()) {
            handler.updateLeaderboard(data);
        }
    }

    public File getSource() {
        return source;
    }

    public File getProblems() {
        return problems;
    }

    public File getTeamFolder(String team) {
        return teamFolders.get(team);
    }

    public void addTeamSubmission(String team, String name, long id, String status) {
        DefaultTableModel model = (DefaultTableModel) teamTables.get(team).getModel();
        String time = new SimpleDateFormat("h:mm a").format(new Timestamp(id));
        model.addRow(new Object[]{name, time, status, new JButton("Test")});
    }

}
