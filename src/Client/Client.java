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
    private boolean loggedIn = false;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 80);
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
        out.println("1");  // Send the choice
        out.println(nickname);  // Send the username
        out.println(password);  // Send the password
    }

    @Override
    public void register(String nickname, String password) {
        out.println("2");  // Send the choice
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
                        System.out.println("Press key to: ");
                        System.out.println("1. Login: ");
                        System.out.println("2. Register: ");
                        String choice = inReader.readLine();
                        if (choice.equals("1")) {
                            System.out.println("Enter nickname: ");
                            String nickname = inReader.readLine();
                            System.out.println("Enter password: ");
                            String password = inReader.readLine();
                            login(nickname, password);
                            String response = in.readLine();
                            if (response.equals("logged in")) {
                                loggedIn = true;
                            }
                        } else if (choice.equals("2")) {
                            System.out.println("Enter nickname: ");
                            String nickname = inReader.readLine();
                            System.out.println("Enter password: ");
                            String password = inReader.readLine();
                            register(nickname, password);

                            String response = in.readLine();
                            if (response.equals("logged in")) {
                                loggedIn = true;
                            }
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