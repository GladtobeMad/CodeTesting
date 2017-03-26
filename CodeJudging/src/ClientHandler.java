import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by VladOleksenko on 3/17/17.
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private MainWindow window;
    private BufferedReader in;
    private PrintWriter out;

    private String teamName;

    private HashMap<Long, String> submissionFiles = new HashMap<>();
    private HashMap<Long, String> submissionStatuses = new HashMap<>();

    ClientHandler(Socket socket, MainWindow window) {
        this.socket = socket;
        this.window = window;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            return;
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {

            String command = in.readLine();
            if (command.startsWith("TEAMNAME ")) {
                if (window.addTeam(command.substring(9), this)) {
                    out.println("TEAMACCEPT");
                    out.flush();
                    this.teamName = command.substring(9);
                    window.updateAll();
                } else {
                    out.println("TEAMDENY");
                    out.flush();
                    // TODO: CLEAN OUT THE GARBAGE
                    return;
                }
            } else {
                out.println("TEAMDENY");
                out.flush();
                // TODO: CLEAN OUT THE GARBAGE
                return;
            }

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("SENDFILE ")) {
                    line = line.substring(9);
                    Scanner read = new Scanner(line);
                    String filename = read.next();
                    int size = read.nextInt();
                    File folder = window.getTeamFolder(teamName);
                    long id = System.currentTimeMillis();
                    File f = new File(folder.getPath() + "/" + id);
                    if (!f.mkdir()) System.out.println("UNABLE TO CREATE FOLDER");
                    File file = new File(f.getPath() + "/" + filename);
                    if (file.createNewFile()) {
                        PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
                        for (int i = 0; i < size; i++) {
                            output.println(in.readLine());
                        }
                        output.flush();
                        fileSubmission(file, id, "Being Judged");
                    } else {
                        System.out.println("Unable to create file.");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {

            }
        }
    }

    public void updateLeaderboard(String[][] data) {
        out.println("UPDATELEADERBOARD " + data.length);
        for (int i = 0; i < data.length; i++) {
            out.println(data[i][0] + " " + data[i][1] + " " + data[i][2]);
        }
        out.flush();
    }

    public void fileSubmission(File file, long id, String status) {
        submissionFiles.put(id, file.getName());
        submissionStatuses.put(id, status);
        out.println("FILESUBMISSION " + file.getName() + " " + id);
        out.println(status);
        window.addTeamSubmission(this.teamName, file, id, status);
        out.flush();
    }

    public void fileUpdate(String status, long id) {
        int index = window.getTeamRowIndex(teamName, id);
        if (index != -1) {
            JTable table = window.getTeamTable(teamName);
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setValueAt(status, index, 2);
            model.setValueAt("Done", index, 3);

        }
        out.println("FILEUPDATE " + id);
        out.println(status);
        out.flush();
    }

    public String getTeamName() {
        return teamName;
    }

}
