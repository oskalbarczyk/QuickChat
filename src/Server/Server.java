package Server;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.net.*;
import java.io.*;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final ArrayList<ConnectionHandler> connections;
    private final ArrayList<User> users;
    private ServerSocket server;
    private boolean running;
    private ExecutorService pool;

    public Server() {
        running = true;
        connections = new ArrayList<>();
        users = new ArrayList<>();

    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (running) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            running = false;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(String password, String nickname) {
        users.add(new User(nickname, password));
    }

    public User login(String nickname, String password) {
        for (User user : users) {
            if (user.nickname.equals(nickname) && user.password.equals(password)) {
                return user;
            }
        }
        return null;
    }

    class ConnectionHandler implements Runnable {
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private boolean loggedIn;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                System.out.println("Client " + client.getLocalAddress()  + " connected");
                User user = null;
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Type number to: ");
                out.println("1. Login");
                out.println("2. Register");
                String choice = in.readLine();
                while (!loggedIn) {
                    if (choice.equals("1")) {
                        out.println("Enter nickname: ");
                        String nickname = in.readLine();
                        System.out.println(nickname);
                        out.println("Enter password: ");
                        String password = in.readLine();
                        System.out.println(password);
                        user = login(nickname, password);
                        if (user != null) {
                            loggedIn = true;
                            out.println("You are now logged in");
                            System.out.println("User " + nickname + " logged in");
                        } else {
                            out.println("Wrong username or password");
                            System.out.println("User " + nickname + " failed to log in");
                        }
                    }
                }
                if(user == null){
                    shutdown();
                }
                out.println("Enter message: ");
                String message = in.readLine();
                String nickname = user.getNickname();
                while (message != null) {
                    broadcast(nickname + ": " + message);
                    System.out.println(nickname + ": " + message);
                    message = in.readLine();
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
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

    }

    public static void main(String[] args) {
        new Server().run();
    }
}

