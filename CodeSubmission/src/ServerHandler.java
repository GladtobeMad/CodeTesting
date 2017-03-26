import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by VladOleksenko on 3/20/17.
 */
public class ServerHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private MainWindow window;

    ServerHandler(Socket socket, BufferedReader in, PrintWriter out, MainWindow window) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.window = window;
        new Thread(this).start();
    }

    public void run() {
        try {
        String line;

            while ((line = in.readLine()) != null) {
                if(line.startsWith("UPDATELEADERBOARD ")) {
                    Scanner read = new Scanner(line.substring(18));
                    int num = read.nextInt();
                    String[][] data = new String[num][3];
                    for (int i = 0; i < num; i++) {
                        line = in.readLine();
                        Scanner r = new Scanner(line);
                        data[i][0] = r.next();
                        data[i][1] = r.next();
                        data[i][2] = r.next();
                    }
                    window.updateLeaderboard(data);
                } else if (line.startsWith("FILESUBMISSION ")) {
                    Scanner read = new Scanner(line.substring(15));
                    String name = read.next();
                    long id = read.nextLong();
                    String status = in.readLine();
                    window.addSubmission(name, id, status);
                } else if (line.startsWith("FILEUPDATE ")) {
                    Scanner read = new Scanner(line.substring(11));
                    long id = read.nextLong();
                    String status = in.readLine();
                    window.updateSubmission(id, status);
                }
            }

        } catch (IOException e) {
        } finally {
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {

            }
        }
    }

    public boolean submitFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            out.println("SENDFILE " + file.getName() + " " + lines.size());
            for (String l : lines) {
                out.println(l);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }


}
