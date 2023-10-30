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
        System.out.println("Server started");
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(80);
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

    public User register(String password, String nickname) {
        User user = new User(nickname, password);
        users.add(user);
        return user;
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
                System.out.println("Client " + client.getLocalAddress() + " connected");
                User user = null;
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String choice = in.readLine();
                while (!loggedIn) {
                    if (choice.equals("1")) {
                        String nickname = in.readLine();
                        String password = in.readLine();
                        user = login(nickname, password);
                        if (user != null) {
                            loggedIn = true;
                            out.println("logged in");
                        } else {
                            out.println("login failed");
                        }
                    } else if (choice.equals("2")) {
                        String nickname = in.readLine();
                        String password = in.readLine();
                        user = register(password, nickname);
                        System.out.println("Added user: " + user.nickname + " " + user.id);
                        loggedIn = true;
                        out.println("logged in");

                    }
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

