import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by VladOleksenko on 3/17/17.
 */
public class Main {

    public static void main(String[] args) {

        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        new MainWindow(address);

    }

}
