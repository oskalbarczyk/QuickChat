package Client;

import javax.swing.*;

public class Visual extends JFrame {

    private final ClientInterface client;

    private JPanel panel;
    private JTextField textField;
    private JButton sendButton;

    public Visual(ClientInterface client){
        super("QuickChat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500,500);
        setVisible(true);

        this.client = client;

    }
}
