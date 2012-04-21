package checkers;

import server.Player;
import server.PlayersSubject;

import java.net.Socket;
import java.util.Vector;

public class CheckersPlayersData implements PlayersSubject {
    private Vector<Player> players;
    private Player player;

    public CheckersPlayersData() {
        players = new Vector<Player>();
    }
    @Override
    public void addObserver(Player observer) {
        player = observer;
        players.add(player);
        notifyObservers("@addplayer;");
    }
    @Override
    public void removeObserver(Player observer) {
        player = observer;
        players.remove(observer);
        notifyObservers("@removeplayer;");
    }
    @Override
    public void notifyObservers(String command) {
        for (Player p : players) {
            if (p.getHashCode() != player.getHashCode()) p.update(player, command);
        }
    }
    public Vector<Player> getPlayers() {
        return players;
    }
    public Player getPlayer(Socket socket) {
        Player pl = null;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getSocket() == socket) pl = players.get(i);
        }
        return pl;
    }
    public Player getPlayer(int hashCode) {
        Player pl = null;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getHashCode() == hashCode) pl = players.get(i);
        }
        return pl;
    }
}