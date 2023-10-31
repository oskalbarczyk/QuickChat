package Server;

public enum ConsoleColor{
    GREEN("\u001B[34m"),
    YELLOW("\u001B[33m"),
    RESET("\u001B[0m"),
    RED("\u001B[31m"),
    WHITE("\u001B[37m");

    private final String color;

    ConsoleColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
