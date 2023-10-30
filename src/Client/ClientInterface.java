package Client;

public interface ClientInterface {
    void shutdown();
    void sendMessage(String message);
    void login(String username, String password);
    void register(String username, String password);
}
