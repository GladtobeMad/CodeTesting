import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Vlad on 3/11/17.
 */
public class CodeTester {
    public static void main(String[] args) {
        Scanner read = new Scanner(System.in);

        while(true) {
            String command = read.nextLine();
            if (command.trim().equals("")) {
            } else if (command.startsWith("test ")) {
                String[] arguments = command.split(" ");
                try {
                    Process pro1 = Runtime.getRuntime().exec("javac " + arguments[1] + ".java");
                    pro1.waitFor();
                    Process pro = Runtime.getRuntime().exec("java " + arguments[1]);
                    System.out.println("Successfully ran the program");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (command.equals("exit")) {
                System.out.println("Exiting ...");
                System.exit(0);
            } else {
                System.out.println("Error: Invalid command " + command);
            }
        }

    }
}
