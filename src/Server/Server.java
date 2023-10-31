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
            server = new ServerSocket(8080);
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

    public void broadcast(String message, ConnectionHandler sender){
        for (ConnectionHandler ch : connections) {
            if (ch != null && ch.isLoggedIn() && ch != sender) {
                System.out.println("broadcasting: " + message + " to: " + ch.user.getNickname());
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

    public User register(String nickname, String password){

        for (User user : users) {
            if (user.getNickname().equals(nickname)) {
                return null;
            }
        }

        User user = new User(nickname, password);
        users.add(user);
        return user;
    }

    public User login(String nickname, String password) {
        for (User user : users) {
            if (user.getNickname().equals(nickname) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    class ConnectionHandler implements Runnable {
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private boolean loggedIn = false;
        private User user;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                System.out.println("Client " + client.getLocalAddress() + " connected");
                if(!loggedIn){
                    System.out.println("Client " + client.getLocalAddress() + " is not logged in");
                }

                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while (!loggedIn) {
                    String choice = in.readLine();
                    if (choice.equals("login")) {
                        String nickname = in.readLine();
                        String password = in.readLine();
                        user = login(nickname, password);
                        if(user != null){
                            loggedIn = true;
                            out.println("logged in");
                            System.out.println("Client " + client.getLocalAddress() + " logged in");
                        }else{
                            loggedIn = false;
                            out.println("failed");
                            System.out.println("Client " + client.getLocalAddress() + " failed to log in");

                        }

                    } else if (choice.equals("register")) {
                        String nickname = in.readLine();
                        String password = in.readLine();
                        user = register(nickname, password);
                        if(user != null){
                            user.setLoggedIn(true);
                            loggedIn = true;
                            user.setLoggedIn(true);
                            out.println("registered");
                            System.out.println("Client " + client.getLocalAddress() + " registered as: " + nickname);
                        }else{
                            loggedIn = false;
                            out.println("failed");
                            System.out.println("Client " + client.getLocalAddress() + " failed to register");
                        }
                    }
                }

                String nickname = user.getNickname();

                while (loggedIn) {
                    String inMessage = in.readLine();
                    if (inMessage.equals("/exit")) {
                        shutdown();
                        break;
                    } else {
                        System.out.println("revived message from: " + nickname + ": " + inMessage);
                        broadcast(nickname + ": " + inMessage,this);
                    }
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
                user.setLoggedIn(false);
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
                connections.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isLoggedIn() {
            return loggedIn;
        }
    }

    public static void main(String[] args) {
        new Server().run();
    }
}

