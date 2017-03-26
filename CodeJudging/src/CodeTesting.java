import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by VladOleksenko on 3/23/17.
 */
public class CodeTesting {

    private File toTest, input, output;
    private boolean compileError, timeoutError, runtimeError;
    private long timeout;

    CodeTesting (File toTest, File input, File output, long timeout) {
        this.toTest = toTest;
        this.input = input;
        this.output = output;
        this.timeout = timeout;
    }

    public String test() { // TODO: IMPROVE THIS. MAKE IT MORE INTERACTIVE
        StringWriter write = new StringWriter();
        PrintWriter printWriter = new PrintWriter(write);
        compileError = false;
        try {
            Process pro1 = Runtime.getRuntime().exec("javac " + toTest.getPath());
            BufferedReader errorReader1 = new BufferedReader(new InputStreamReader(pro1.getErrorStream()));
            String line;
            try {
                while ((line = errorReader1.readLine()) != null) {
                    printWriter.println(line);
                    compileError = true;
                }
                errorReader1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pro1.waitFor();

            Process pro2 = Runtime.getRuntime().exec("java -cp " + toTest.getParent() + " " + toTest.getName().substring(0, toTest.getName().length()-5)); // Removes the .java extension

            BufferedReader outputReader = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(pro2.getErrorStream()));
            BufferedReader inputReader = new BufferedReader(new FileReader(input));
            BufferedReader outputChecker = new BufferedReader(new FileReader(output));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(pro2.getOutputStream()));
            StringWriter stringWriter = new StringWriter();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String line;
                    try {
                        while ((line = outputReader.readLine()) != null) {
                            stringWriter.write(line + "\n");
                            stringWriter.flush();
                        }
                        outputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String line;
                    try {
                        while ((line = errorReader.readLine()) != null) {
                            stringWriter.write(line + "\n");
                            stringWriter.flush();
                            runtimeError = true;
                        }
                        errorReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            while ((line = inputReader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                writer.flush();
            }
            pro2.waitFor(timeout, TimeUnit.MILLISECONDS);
            if (pro2.isAlive()) {
                timeoutError = true;
                pro2.destroy();
            }

            String output = stringWriter.toString();
            Scanner reader = new Scanner(output);

            int count = 1;
            int errorCount = 0;
            //HashMap<Integer, String> errors1 = new HashMap<>();
            //HashMap<Integer, String> errors2 = new HashMap<>();

            while ((line = outputChecker.readLine()) != null) {
                if(reader.hasNextLine()) {
                    String line2 = reader.nextLine();
                    if (line.equals(line2)) {
                        printWriter.println(count + "\t   " + line2);
                    } else {
                        printWriter.println(count + "\t-> " + line2);
                        printWriter.println("\t   " + line);
                        errorCount++;
                        //errors1.put(count, line);
                        //errors2.put(count, line2);
                    }
                } else {
                    printWriter.println(count + "\t-> ");
                    printWriter.println("\t   " + line);
                    errorCount++;
                    //errors1.put(count, line);
                    //errors2.put(count, "");
                }
                count++;
            }


            printWriter.println("\nSucessfully finished testing.");
            if (compileError) {
                printWriter.println("The program resulted in a compile error.");
            } else if (timeoutError) {
                printWriter.println("The program resulted in a timeout error.");
            } else if (runtimeError) {
                printWriter.println("The program resulted in a runtime error.");
            } else {
                printWriter.println("There was a total of " + errorCount + " errors.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return write.toString();
    }


}
