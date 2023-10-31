package Server;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.net.*;
import java.io.*;
import java.util.concurrent.Executors;

import static Server.ConsoleColor.*;
public class Server implements Runnable {
    private final ArrayList<ConnectionHandler> connections;
    private final ArrayList<User> users;
    private ServerSocket server;
    private boolean running;
    private ExecutorService pool;
    private final FileHandler fileHandler;


    public Server() {
        running = true;
        connections = new ArrayList<>();
        users = new ArrayList<>();
        fileHandler = new FileHandler();
        System.out.println("Server started");
    }

    @Override
    public void run() {
        users.addAll(fileHandler.loadUsers());
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
            e.printStackTrace();
            shutdown();
        }
    }

    public void broadcast(String message, ConnectionHandler sender) {
        for (ConnectionHandler ch : connections) {
            if (ch != null && ch.isLoggedIn() && ch != sender) {
                System.out.println("broadcasting: " + message + " to: " + ch.user.getNickname());
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            fileHandler.saveUsers(users);
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

    public User register(String nickname, String password) {

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
                System.out.println(GREEN.getColor() + "Client: " + client.getLocalAddress() + " connected" + RESET.getColor());
                if (!loggedIn) {
                    System.out.println(YELLOW.getColor() + "Client: " + client.getLocalAddress() + " is not logged in" + RESET.getColor());
                }

                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while (!loggedIn) {
                    String choice = in.readLine();
                    if (choice.equals("login")) {
                        String nickname = in.readLine();
                        String password = in.readLine();
                        user = login(nickname, password);
                        if (user != null) {
                            loggedIn = true;
                            out.println("logged in");
                            System.out.println("Client " + client.getLocalAddress() + " logged in");
                        } else {
                            loggedIn = false;
                            out.println("failed");
                            System.out.println(RED.getColor() + "Client " + client.getLocalAddress() + " failed to log in" + RESET.getColor());

                        }

                    } else if (choice.equals("register")) {
                        String nickname = in.readLine();
                        String password = in.readLine();
                        user = register(nickname, password);
                        if (user != null) {
                            user.setLoggedIn(true);
                            loggedIn = true;
                            user.setLoggedIn(true);
                            out.println("registered");
                            System.out.println(GREEN.getColor()+ "Client " + client.getLocalAddress() + " registered as: " + nickname);
                        } else {
                            loggedIn = false;
                            out.println("failed");
                            System.out.println(RED.getColor() + "Client " + client.getLocalAddress() + " failed to register" + RESET.getColor());
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
                        System.out.println(WHITE.getColor() + "revived message from: " + nickname + ": " + inMessage + RESET.getColor());
                        broadcast(nickname + ": " + inMessage, this);
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

    static class FileHandler implements Serializable {
        public void saveUsers(ArrayList<User> users) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream("users.ser");
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(users);
                objectOutputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public ArrayList<User> loadUsers() {
            ArrayList<User> users = new ArrayList<>();
            try {
                FileInputStream fileInputStream = new FileInputStream("users.ser");
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                users = (ArrayList<User>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return users;
        }

    }


    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}

