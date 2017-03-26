import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by VladOleksenko on 3/17/17.
 */
public class MainWindow extends Frame {

    private JTabbedPane tabs;
    private JComponent leaderboardPanel, actionsPanel;
    private JPanel problemAddPanel;
    private JButton addProblemButton, selectInputButton, selectOutputButton, removeProblemButton;
    private JLabel inputFileLabel, outputFileLabel;
    private JTextField nameField;
    private JList problemList;
    private JTable leaderboardTable;
    private JTabbedPane submissionPanel;
    private FileDialog fileChooser = new FileDialog(this);
    private final MainWindow window = this;
    private String inputFile = null, outputFile = null;

    private HashMap<String, ClientHandler> handlers = new HashMap<>();
    private HashMap<String, Integer> teamScores = new HashMap<>();
    private HashMap<String, Integer> teamPenalties = new HashMap<>();
    private HashMap<String, JTable> teamTables = new HashMap<>();
    private HashMap<String, File> teamFolders = new HashMap<>();

    private HashMap<String, File> inputFiles = new HashMap<>();
    private HashMap<String, File> outputFiles = new HashMap<>();

    private File source;
    private File problems;

    MainWindow(InetAddress address) {
        super("Code Judging (" + address.getHostAddress() + ")");

        new Server(window);

        tabs = new JTabbedPane();

        submissionPanel = new JTabbedPane();
        tabs.add("Submissions", submissionPanel);

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
        actionsPanel.setLayout(new BorderLayout());
        actionsPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                    problemList.setSelectedIndex(-1);
                    problemList.clearSelection();
                    actionsPanel.requestFocus();
            }
        });


        problemAddPanel = new JPanel();
        problemAddPanel.setLayout(new FlowLayout());
        problemAddPanel.setPreferredSize(new Dimension(320, 120));

        inputFileLabel = new JLabel(" ");
        inputFileLabel.setPreferredSize(new Dimension(150, 30));
        inputFileLabel.setHorizontalAlignment(JLabel.CENTER);
        selectInputButton = new JButton("Select Input File");
        selectInputButton.setPreferredSize(new Dimension(150, 30));
        selectInputButton.addActionListener(l -> {
            fileChooser.setVisible(true);
            String file = fileChooser.getFile();
            String directory = fileChooser.getDirectory();
            if (file != null && directory != null) {
                inputFile = directory + "/" + file;
                inputFileLabel.setText(file);
            }
        });
        outputFileLabel = new JLabel(" ");
        outputFileLabel.setPreferredSize(new Dimension(150, 30));
        outputFileLabel.setHorizontalAlignment(JLabel.CENTER);
        selectOutputButton = new JButton("Select Output File");
        selectOutputButton.setPreferredSize(new Dimension(150, 30));
        selectOutputButton.addActionListener(l -> {
            fileChooser.setVisible(true);
            String file = fileChooser.getFile();
            String directory = fileChooser.getDirectory();
            if (file != null && directory != null) {
                outputFile = directory + "/" + file;
                outputFileLabel.setText(file);
            }
        });

        problemList = new JList(new DefaultListModel<>());
        problemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        problemList.setLayoutOrientation(JList.VERTICAL);

        JScrollPane problemScroller = new JScrollPane(problemList);
        problemScroller.setPreferredSize(new Dimension(300, 220));

        removeProblemButton = new JButton("Remove Problem");
        removeProblemButton.addActionListener(l -> {
            if (problemList.getSelectedIndex() != -1) {
                String problem = (String) problemList.getSelectedValue();
                DefaultListModel m = (DefaultListModel) problemList.getModel();
                m.removeElementAt(problemList.getSelectedIndex());
                inputFiles.remove(problem).delete();
                outputFiles.remove(problem).delete();
                File folder = new File(problems.getPath() + "/" + problem);
                folder.delete();
                problemList.clearSelection();
                problemList.setSelectedIndex(-1);
            }
        });

        addProblemButton = new JButton("Add Problem");
        addProblemButton.setPreferredSize(new Dimension(150, 30));
        addProblemButton.addActionListener(l -> {
            if (inputFile != null && outputFile != null && !nameField.getText().trim().equals("")) {
                String problemName = nameField.getText().trim();
                if (!inputFiles.containsKey(problemName) && !outputFiles.containsKey(problemName)) {
                    File iFile = new File(inputFile);
                    File oFile = new File(outputFile);
                    if (!iFile.getName().equals(oFile.getName())) {
                        File problem = new File(problems.getPath() + "/" + problemName);
                        if (problem.exists()) {
                            deleteFolder(problem);
                        }
                        if (!problem.mkdir()) {
                            JOptionPane.showMessageDialog(window, "Unable to create problem folder!", "Error creating a problem", JOptionPane.ERROR_MESSAGE);
                        } else {
                            File problemInputFile = null, problemOutputFile = null;
                            try {
                                problemInputFile = Files.copy(iFile.toPath(), (new File(problem.getPath() + "/" + iFile.getName())).toPath()).toFile();
                                problemOutputFile = Files.copy(oFile.toPath(), (new File(problem.getPath() + "/" + oFile.getName())).toPath()).toFile();
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(window, "An error occurred while copying files!", "Error creating a problem", JOptionPane.ERROR_MESSAGE);
                            }
                            if (problemInputFile != null && problemOutputFile != null) {
                                inputFiles.put(problemName, problemInputFile);
                                outputFiles.put(problemName, problemOutputFile);
                                nameField.setText("");
                                inputFile = null;
                                outputFile = null;
                                inputFileLabel.setText(" ");
                                outputFileLabel.setText(" ");
                                DefaultListModel m = (DefaultListModel) problemList.getModel();
                                m.addElement(problemName);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(window, "Input file and output file can't have the same name!", "Error creating a problem", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(window, "Problem " + problemName + " already exists!", "Error creating a problem", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JLabel nameLabel = new JLabel("Problem Name:");
        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        nameLabel.setPreferredSize(new Dimension(150, 30));

        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(150, 30));

        problemAddPanel.add(inputFileLabel);
        problemAddPanel.add(selectInputButton);
        problemAddPanel.add(outputFileLabel);
        problemAddPanel.add(selectOutputButton);
        problemAddPanel.add(nameLabel);
        problemAddPanel.add(nameField);
        problemAddPanel.add(addProblemButton);
        problemAddPanel.add(problemScroller);
        problemAddPanel.add(removeProblemButton);

        actionsPanel.add(problemAddPanel, BorderLayout.LINE_START);

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

    public ArrayList<String> getProblemList() {
        ArrayList<String> toReturn = new ArrayList<>();
        for (int i = 0; i < problemList.getModel().getSize(); i++) {
            toReturn.add((String) problemList.getModel().getElementAt(i));
        }
        return toReturn;
    }

    public File getInputFile(String problem) {
        return inputFiles.get(problem);
    }

    public File getOutputFile(String problem) {
        return outputFiles.get(problem);
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
            teamTable.getColumn("Test").setCellRenderer(new ButtonRenderer());
            teamTable.addMouseListener(new JTableButtonMouseListener(teamTable));
            teamTable.setEnabled(false);
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

    public File getTeamFolder(String team) {
        return teamFolders.get(team);
    }

    public void addTeamSubmission(String team, File file, long id, String status) {
        DefaultTableModel model = (DefaultTableModel) teamTables.get(team).getModel();
        String time = new SimpleDateFormat("h:mm a").format(new Timestamp(id));
        model.insertRow(0, new Object[]{file.getName(), time, status, "Test"});
        JButton button = new JButton("Test");
        //idToButton.put(id, button);
        button.addActionListener(l -> {
            if (this.getProblemList().size() > 0) {
                new CodeTestingWindow(file, window, button);
                this.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(window, "Problem list is empty!", "Unable to test code", JOptionPane.ERROR_MESSAGE);
            }
        });
        model.setValueAt(button, 0, 3);
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files != null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        public JComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            if (value instanceof JButton) {
                setText(((JButton) value).getText());
            } else {
                setText((value == null) ? "" : value.toString());
            }
            return this;
        }
    }

    private static class JTableButtonMouseListener extends MouseAdapter {
        private final JTable table;

        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }

        public void mouseClicked(MouseEvent e) {
            int column = table.getColumnModel().getColumnIndexAtX(e.getX());
            int row    = e.getY()/table.getRowHeight();

            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                Object value = table.getValueAt(row, column);
                if (value instanceof JButton) {
                    ((JButton)value).doClick();
                }
            }
        }
    }


}
