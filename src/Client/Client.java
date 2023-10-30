package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable, ClientInterface {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private boolean loggedIn;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }

        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void login(String nickname, String password) {
        out.println(nickname);  // Send the username
        out.println(password);  // Send the password
    }

    @Override
    public void register(String nickname, String password) {
        out.println(nickname);  // Send the username
        out.println(password);  // Send the password
    }

    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    while (!loggedIn) {
                        System.out.println(in.readLine());
                        System.out.println(in.readLine());
                        System.out.println(in.readLine());
                        String choice = inReader.readLine();
                        out.println(choice);
                        if (choice.equals("1")) {
                            System.out.println("Enter nickname: ");
                            String nickname = inReader.readLine();
                            System.out.println("Enter password: ");
                            String password = inReader.readLine();
                            login(nickname, password);
                        } else if (choice.equals("2")) {
                            System.out.println("Enter nickname: ");
                            String nickname = inReader.readLine();
                            System.out.println("Enter password: ");
                            String password = inReader.readLine();
                            register(nickname, password);
                        }

                    }


                    String serverMessage = in.readLine();
                    System.out.println(serverMessage);

                    String message = inReader.readLine();
                    if (message.equals("/quit")) {
                        out.println(message);
                        inReader.close();
                        shutdown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        //Visual visual = new Visual(client);
        client.run();
    }
}