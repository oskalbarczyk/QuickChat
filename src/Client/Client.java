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
            client = new Socket("127.0.0.1", 8080);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread inputThread = new Thread(inHandler);
            inputThread.start();

            while (!done) {
                if(loggedIn){
                    String message = in.readLine();
                    System.out.println(message);
                }

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
        out.println(message);
    }

    @Override
    public void login(String nickname, String password) {
        out.println("login");  // Send the choice
        out.println(nickname);  // Send the username
        out.println(password);  // Send the password
    }

    @Override
    public void register(String nickname, String password) {
        out.println("register");  // Send the choice
        out.println(nickname);  // Send the username
        out.println(password);  // Send the password
    }



    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("You are not logged in. Please login or register.");
                System.out.println("Enter /login or /register: ");
                while (!done) {
                    String message;
                    while (!loggedIn) {
                         message = inReader.readLine();
                        if (message.equals("/login")) {
                            System.out.println("Enter your nickname: ");
                            String nickname = inReader.readLine();
                            System.out.println("Enter your password: ");
                            String password = inReader.readLine();
                            login(nickname, password);
                            String response = in.readLine();
                            if(response.equals("logged in")){
                                loggedIn = true;
                                System.out.println("You are logged in.");
                            }else{
                                loggedIn = false;
                                System.out.println("Login failed. Type /login to try again or /register to register.");
                            }
                        } else if (message.equals("/register")) {
                            System.out.println("Enter your nickname: ");
                            String nickname = inReader.readLine();
                            System.out.println("Enter your password: ");
                            String password = inReader.readLine();
                            register(nickname, password);
                            System.out.println("Registering...");
                            String response = in.readLine();
                            if (response.equals("registered")) {
                                loggedIn = true;
                                System.out.println("Registration successful. You are now logged in.");
                            } else {
                                loggedIn = false;
                                System.out.println("Registration failed. Type /login to login or /register to try again.");
                            }
                        } else {
                            System.out.println("Invalid command.");
                        }
                    }
                    System.out.println("Enter your message: ");
                    message = inReader.readLine();

                    if (message.equals("/exit")) {
                        shutdown();
                    }
                    sendMessage(message);

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