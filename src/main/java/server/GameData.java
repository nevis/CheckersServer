package server;

import java.net.Socket;
import java.util.Vector;

public class GameData implements PlayersGameSubject {
    private String gameName;
    private int maxNumberOfPlayers;
    private int currentNumberOfPlayers = 0;

    private Vector<Player> players = new Vector<Player>();

    public GameData(String gameName, int maxNumberOfPlayers) {
        this.gameName = gameName;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
    }
    public String getGameName() {
        return gameName;
    }
    public Vector<Player> getPlayers() {
        return players;
    }
    public int getCurrentNumberOfPlayers() {
        return currentNumberOfPlayers;
    }
    public int getMaxNumberOfPlayers() {
        return maxNumberOfPlayers;
    }
    @Override
    public void addObserver(Player observer) {
        if (currentNumberOfPlayers < maxNumberOfPlayers) {
            players.add(observer);
            observer.setChipNumber(12);
            currentNumberOfPlayers += 1;
        }
    }
    @Override
    public void removeObserver(Player observer) {
        players.remove(observer);
        currentNumberOfPlayers -= 1;
    }
    @Override
    public void notifyObservers(Player player, String command) {
        for (Player p : players) {
            if (p.getHashCode() != player.getHashCode()) p.update(player, command);
        }
    }
    public Player getPlayer(Socket socket) {
        Player pl = null;
        for (Player p : players) {
            if (p.getSocket() == socket) pl = p;
        }
        return pl;
    }
}
