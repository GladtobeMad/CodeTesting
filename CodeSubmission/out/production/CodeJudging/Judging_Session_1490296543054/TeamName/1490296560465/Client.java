import java.io.*;
import java.net.Socket;

/**
 * Created by VladOleksenko on 3/17/17.
 */
public class Client implements Runnable {

    Socket socket;
    InputStream in;
    OutputStream out;

    public Client(String ip, int port) {
        socket = null;
        try {
            this.socket = new Socket(ip, port);
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (socket != null)
            new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            String line;
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            while ((line = read.readLine()) != null) {

            }
        } catch (IOException ioe) {

        } finally {
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }

    public boolean submitFile(File file, long time, int id) {
        DataOutputStream stream = new DataOutputStream(out);
        BufferedInputStream readFile;
        try {
            readFile = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return false;
        }

        try {
            stream.writeLong(file.length());
            stream.writeUTF(file.getName());
            stream.writeLong(time);
            stream.writeInt(id);
            byte[] buff = new byte[(int) file.length()];
            readFile.read(buff, 0, buff.length);
            stream.write(buff);
            stream.flush();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
