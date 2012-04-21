package checkers;

import server.Player;
import server.GameSubject;

import java.net.Socket;
import java.util.Vector;

public class CheckersGameData implements GameSubject {
    private int maxNumberOfPlayers;
    private int currentNumberOfPlayers = 0;
    private Vector<Player> players = new Vector<Player>();

    public CheckersGameData(int maxNumberOfPlayers) {
        this.maxNumberOfPlayers = maxNumberOfPlayers;
    }
    public Vector<Player> getPlayers() {
        return players;
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
       for (int i = 0; i < players.size(); i ++) {
           if (players.get(i).getSocket() == socket) pl = players.get(i);
       }
       return pl;
   }
}
