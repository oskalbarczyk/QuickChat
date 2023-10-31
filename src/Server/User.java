package Server;

//import java.util.ArrayList;
import java.util.UUID;

public class User {
    private UUID id;
    private String nickname;
    private String password;
    private boolean loggedIn = false;

    //ArrayList<User> friends;

    public User( String nickname, String password) {
        id = UUID.randomUUID();
        this.nickname = nickname;
        this.password = password;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public UUID getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }
}
