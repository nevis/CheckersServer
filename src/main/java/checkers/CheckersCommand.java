package checkers;

import server.Player;
import server.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class CheckersCommand {
    private Server server;
    private CheckersPlayersData checkersPlayersData = new CheckersPlayersData();
    private List<Player> invitePlayerList = new Vector<Player>();
    private List<CheckersGameData> checkersGameList = new Vector<CheckersGameData>();

    public CheckersCommand(Server server) {
        this.server = server;
    }
    public CheckersPlayersData getCheckersPlayersData() {
        return checkersPlayersData;
    }
    private enum Command {
        Invite("@invite"),
        AcceptInvite("@acceptinvite"),
        RejectInvite("@rejectinvite"),
        Turn("@turn"),
        Kill("@kill"),
        Exit("@exit");

        private String command;
        Command (String command) {
            this.command = command;
        }
        public String getCommand() {
            return command;
        }
    }
    private Command getCommandFromClient(String s) {
        if (s.equals(Command.Invite.getCommand())) return Command.Invite;
        else if (s.equals(Command.AcceptInvite.getCommand())) return Command.AcceptInvite;
        else if (s.equals(Command.RejectInvite.getCommand())) return Command.RejectInvite;
        else if (s.equals(Command.Turn.getCommand())) return Command.Turn;
        else if (s.equals(Command.Kill.getCommand())) return Command.Kill;
        else if (s.equals(Command.Exit.getCommand())) return Command.Exit;
        else throw new AssertionError("Unknown command: " + s);
    }
    public void run(Socket socket, String in) throws IOException {
        String command = in.split("\\;")[0];
        switch (getCommandFromClient(command)) {
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
            case Turn: {
                turn(socket, in);
                break;
            }
            case Kill: {
                kill(socket, in);
                break;
            }
            case Exit: {
                disconnect(socket);
                break;
            }
        }
    }
    public String checkersPlayerList() {
        String playerList = "@playerlist;";
        for (Player p : checkersPlayersData.getPlayers()) {
            playerList += p.getName() + ";" + p.getHashCode() + ";";
        }
        return playerList;
    }
    private void invite(Socket socket, String in) {
        try {
            Player pl1 = checkersPlayersData.getPlayer(socket);
            Player pl2 = checkersPlayersData.getPlayer(Integer.parseInt(in.split("\\;")[2]));
            Socket socket2 = pl2.getSocket();
            checkersPlayersData.removeObserver(pl1);
            checkersPlayersData.removeObserver(pl2);
            invitePlayerList.add(pl1);
            invitePlayerList.add(pl2);
            pl1.update(pl2, "@removeplayer;");
            server.response(socket2, "@invite;" + pl1.getName() + ";" + pl1.getHashCode() + ";");
        } catch (Exception e) {
            server.response(socket, "@nofind;" + in.split("\\;")[1] + ";");
        }
    }
    private void acceptInvite(Socket socket, String in) {
        Player pl1 = server.getPlayer(invitePlayerList, socket);
        Player pl2 = server.getPlayer(invitePlayerList, Integer.parseInt(in.split("\\;")[2]));
        Socket socket2 = pl2.getSocket();
        //start game
        server.response(socket, "@accept;1;");
        server.response(socket2, "@accept;2;");
        CheckersGameData gd = new CheckersGameData(2);
        gd.addObserver(pl1);
        gd.addObserver(pl2);
        checkersGameList.add(gd);

    }
    private void rejectInvite(Socket socket, String in) {
        Player pl2 = server.getPlayer(invitePlayerList, Integer.parseInt(in.split("\\;")[2]));
        Socket socket2 = pl2.getSocket();
        Player pl1 = server.getPlayer(invitePlayerList, socket);
        checkersPlayersData.addObserver(pl1);
        checkersPlayersData.addObserver(pl2);
        pl2.update(pl1, "@addplayer;");
        server.response(socket2, "@rejectinvite;" + pl1.getName() + ";");
    }
    private void turn(Socket socket, String in) {
        CheckersGameData checkersGameData = getCheckersGameData(socket);
        Player pl = checkersGameData.getPlayer(socket);
        String s [] = in.split("\\;");
        checkersGameData.notifyObservers(pl, "@turn;" + s[1] + ";" + s[2] + ";" + s[3] + ";" + s[4] + ";"
                + s[5] + ";");
    }
    private void kill(Socket socket, String in) {
        CheckersGameData checkersGameData = getCheckersGameData(socket);
        Player pl = checkersGameData.getPlayer(socket);
        String s [] = in.split("\\;");
        checkersGameData.notifyObservers(pl, "@kill;" + s[1] + ";" + s[2] + ";" + s[3] + ";" + s[4] + ";"
                + s[5] + ";" + s[6] + ";" + s[7] + ";" + s[8] + ";");
        for (Player p : checkersGameData.getPlayers()) {
            if (p.getHashCode() != pl.getHashCode()) p.setChipNumber(p.getChipNumber() - 1);
            if (p.getChipNumber() <= 0) {
                server.response(p.getSocket(), "@lose;");
                server.response(socket, "@win;");
                checkersGameData.removeObserver(p);
                checkersGameData.removeObserver(pl);
                server.response(p.getSocket(), checkersPlayerList());
                checkersPlayersData.addObserver(p);
                server.response(pl.getSocket(), checkersPlayerList());
                checkersPlayersData.addObserver(pl);
                break;
            }
        }
    }
    private void disconnect(Socket socket) {
        Player p = server.getCheckersCommand().getCheckersPlayersData().getPlayer(socket);
        if (p != null) server.getCheckersCommand().getCheckersPlayersData().removeObserver(p);
        else {
            CheckersGameData checkersGameData = server.getCheckersCommand().getCheckersGameData(socket);
            p = checkersGameData.getPlayer(socket);
            checkersGameData.removeObserver(p);
            server.getCheckersCommand().enemyLeftGame(checkersGameData);
        }
        System.out.println("Connection close: [" + p.getHashCode() + "], by name [" + p.getName() + "].");
    }
    private void enemyLeftGame(CheckersGameData checkersGameData) {
        for (int i = 0; i < checkersGameData.getPlayers().size(); i++) {
            Player p = checkersGameData.getPlayers().get(i);
            if (p != null) {
                server.response(p.getSocket(), "@enemyOff;");
                checkersGameData.removeObserver(p);
                server.response(p.getSocket(), checkersPlayerList());
                checkersPlayersData.addObserver(p);
            }
        }
    }
    private CheckersGameData getCheckersGameData(Socket socket) {
        CheckersGameData checkersGameData = null;
        for (int i = 0; i < checkersGameList.size(); i++) {
            Player p = checkersGameList.get(i).getPlayer(socket);
            if (p != null) {
                checkersGameData = checkersGameList.get(i);
            }
        }
        return checkersGameData;
    }
}
