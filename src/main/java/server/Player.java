package server;

import java.net.Socket;

public class Player implements PlayersObserver {
    private Server server;
    private Socket socket;
    private String name;
    private int hashCode;
    private int chipNumber;

    public Player(Server server, Socket socket, String name, int hashCode) {
        this.server = server;
        this.socket = socket;
        this.name = name;
        this.hashCode = hashCode;
    }
    public String getName() {
        return name;
    }
    public int getHashCode() {
        return hashCode;
    }
    public Socket getSocket() {
        return socket;
    }
    public void setChipNumber(int chipNumber) {
        this.chipNumber = chipNumber;
    }
    public int getChipNumber() {
        return chipNumber;
    }
    @Override
    public void update(Player player, String command) {
        server.response(getSocket(), command + player.getName() + ";" + player.getHashCode() + ";");
    }
}