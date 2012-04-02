package server;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class ServerCommand {
    private enum Command {
        Connection("@connect"),
        Invite("@invite"),
        AcceptInvite("@acceptinvite"),
        RejectInvite("@rejectinvite"),
        Exit("@exit"),
        Turn("@turn"),
        Kill("@kill");
        
        private String command;
        Command(String command) {
            this.command = command;
        }
        public String getCommand() {
            return command;
        }
    }
    private Server server;
    private PlayersData playersData;
    private List<Player> invitePlayerList = new Vector<Player>();
    private List<GameData> gameList = new Vector<GameData>();
    ServerCommand(Server server, PlayersData playersData) {
        this.server = server;
        this.playersData = playersData;
    }
    private Command getCommandFromClient(String s) {
        if (s.equals(Command.Connection.getCommand())) return Command.Connection;
        else if (s.equals(Command.Invite.getCommand())) return Command.Invite;
        else if (s.equals(Command.AcceptInvite.getCommand())) return Command.AcceptInvite;
        else if (s.equals(Command.RejectInvite.getCommand())) return Command.RejectInvite;
        else if (s.equals(Command.Exit.getCommand())) return Command.Exit;
        else if (s.equals(Command.Turn.getCommand())) return Command.Turn;
        else if (s.equals(Command.Kill.getCommand())) return Command.Kill;
        else throw new AssertionError("Unknown command: " + s);
    }
    public void run(Socket socket, String in) throws IOException {
        String command = in.split("\\;")[0];
        switch (getCommandFromClient(command)) {
            case Connection: {
                connection(socket, in);
                break;
            }
            case Invite: {
                invite(socket, in);
                break;
            }
            case AcceptInvite: {
                acceptInvite(socket, in);
                break;
            }
            case RejectInvite: {
                rejectInvite(socket, in);
                break;
            }
            case Exit: {
                disconnect(socket);
                break;
            }
            case Turn: {
                turn(socket, in);
                break;
            }
            case Kill: {
                kill(socket, in);
                break;
            }
        }
    }
    private void kill(Socket socket, String in) {
        GameData gameData = getGameData(socket);
        Player pl = gameData.getPlayer(socket);
        String s [] = in.split("\\;");
        gameData.notifyObservers(pl, "@kill;" + s[1] + ";" + s[2] + ";" + s[3] + ";" + s[4] + ";"
                + s[5] + ";" + s[6] + ";");
        for (Player p : gameData.getPlayers()) {
            if (p.getHashCode() != pl.getHashCode()) p.setChipNumber(p.getChipNumber() - 1);
            if (p.getChipNumber() <= 0) {
                server.response(p.getSocket(), "@lose;");
                server.response(socket, "@win;");
                gameData.removeObserver(p);
                gameData.removeObserver(pl);
                server.response(p.getSocket(), playerList());
                playersData.addObserver(p);
                server.response(pl.getSocket(), playerList());
                playersData.addObserver(pl);
                break;
            }
        }
    }
    private void turn(Socket socket, String in) {
        GameData gameData = getGameData(socket);
        Player pl = gameData.getPlayer(socket);
        String s [] = in.split("\\;");
        gameData.notifyObservers(pl, "@turn;" + s[1] + ";" + s[2] + ";" + s[3] + ";" + s[4] + ";");
    }
    private void connection(Socket socket, String in) {
        server.response(socket, "@connected;");
        String name = in.split("\\;")[1];
        server.response(socket, playerList());
        Player player = new Player(server, socket, name, socket.hashCode());
        playersData.addObserver(player);
        System.out.println("New Connection: [" + socket.hashCode() + "], by name [" + name + "].");
    }
    private String playerList() {
        String playerList = "@playerlist;";
        for (Player p : playersData.getPlayers()) {
            playerList += p.getName() + ";" + p.getHashCode() + ";";
        }
        return playerList;
    }
    private void invite(Socket socket, String in) {
        try {
            Player pl1 = playersData.getPlayer(socket);
            Player pl2 = playersData.getPlayer(Integer.parseInt(in.split("\\;")[2]));
            Socket socket2 = pl2.getSocket();
            playersData.removeObserver(pl1);
            playersData.removeObserver(pl2);
            invitePlayerList.add(pl1);
            invitePlayerList.add(pl2);
            pl1.update(pl2, "@removeplayer;");
            server.response(socket2, "@invite;" + pl1.getName() + ";" + pl1.getHashCode() + ";");
        } catch (Exception e) {
            server.response(socket, "@nofind;" + in.split("\\;")[1] + ";");
        }
    }
    private void acceptInvite(Socket socket, String in) {
        Player pl1 = getPlayerFromInvite(socket);
        Player pl2 = getPlayerFromInvite(Integer.parseInt(in.split("\\;")[2]));
        Socket socket2 = pl2.getSocket();
        //start game
        server.response(socket, "@accept;1;");
        server.response(socket2, "@accept;2;");
        GameData gd = new GameData("game_" + pl2.getName() + "_" + pl2.getHashCode(), 2);
        gd.addObserver(pl1);
        gd.addObserver(pl2);
        gameList.add(gd);

    }
    private void rejectInvite(Socket socket, String in) {
        Player pl2 = getPlayerFromInvite(Integer.parseInt(in.split("\\;")[2]));
        Socket socket2 = pl2.getSocket();
        Player pl1 = getPlayerFromInvite(socket);
        playersData.addObserver(pl1);
        playersData.addObserver(pl2);
        pl2.update(pl1, "@addplayer;");
        server.response(socket2, "@rejectinvite;" + pl1.getName() + ";");
    }
    private void disconnect(Socket socket) {
        Player p = playersData.getPlayer(socket);
        if (p == null) {
            GameData gameData = getGameData(socket);
            p = gameData.getPlayer(socket);
            gameData.removeObserver(p);
            enemyLeftGame(gameData);
        } else {
            playersData.removeObserver(p);
        }
        System.out.println("Connection close: [" + p.getHashCode() + "], by name [" + p.getName() + "].");
    }
    private GameData getGameData(Socket socket) {
        GameData gameData = null;
        for (GameData gd : gameList) {
            Player p = gd.getPlayer(socket);
            if (p != null) {
                gameData = gd;
            }
        }
        return gameData;
    }
    private void enemyLeftGame(GameData gameData) {
        for (int i = 0; i < gameData.getPlayers().size(); i++) {
            Player p = gameData.getPlayers().get(i);
            if (p != null) {
                server.response(p.getSocket(), "@enemyOff;");
                gameData.removeObserver(p);
                server.response(p.getSocket(), playerList());
                playersData.addObserver(p);
            }
        }
    }
    public Player getPlayerFromInvite(Socket socket) {
        Player pl = null;
        for (int i = 0; i < invitePlayerList.size(); i++) {
            pl = invitePlayerList.get(i);
            if ((pl != null) && pl.getSocket() == socket) {
                invitePlayerList.remove(pl);
                break;
            }
        }
        return pl;
    }
    public Player getPlayerFromInvite(int hashCode) {
        Player pl = null;
        for (int i = 0; i < invitePlayerList.size(); i++) {
            pl = invitePlayerList.get(i);
            if ((pl != null) && pl.getHashCode() == hashCode) {
                invitePlayerList.remove(pl);
                break;
            }
        }
        return pl;
    }
}