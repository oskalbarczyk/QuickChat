package Server;

//import java.util.ArrayList;
import java.util.UUID;

public class User {
    UUID id;
    String nickname;
    String password;
    //ArrayList<User> friends;

    public User( String nickname, String password) {
        id = UUID.randomUUID();
        this.nickname = nickname;
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }
}
