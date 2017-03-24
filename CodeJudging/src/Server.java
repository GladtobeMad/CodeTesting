import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by VladOleksenko on 3/17/17.
 */
public class Server implements Runnable {

    private MainWindow window;
    private ServerSocket serverSocket;

    public static final int SERVER_PORT = 51234;


    public Server(MainWindow window) {
        this.window = window;
        new Thread(this).start();
    }

    @Override
    public void run() {

        serverSocket = null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {

        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, window);
            } catch (IOException e) {

            }
        }

    }


}
