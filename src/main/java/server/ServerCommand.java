package server;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class ServerCommand {
    private static final String GAME_LIST = "Checkers;";
    private Server server;
    private List<Player> connectionPlayer = new Vector<Player>();

    public ServerCommand(Server server) {
        this.server = server;
    }
    private enum Command {
        Connection("@connect"),
        ChooseGame("@game"),
        Exit("@exit");
        private String command;
        Command(String command) {
            this.command = command;
        }
        public String getCommand() {
            return command;
        }
    }
    private Command getCommandFromClient(String s) {
        if (s.equals(Command.Connection.getCommand())) return Command.Connection;
        else if (s.equals(Command.ChooseGame.getCommand())) return Command.ChooseGame;
        else if (s.equals(Command.Exit.getCommand())) return Command.Exit;
        else throw new AssertionError("Unknown command: " + s);
    }
    public void run(Socket socket, String in) throws IOException {
        String [] input = in.split("\\;");
        switch (getCommandFromClient(input[0])) {
            case Connection: {
                connection(socket, input);
                break;
            }
            case ChooseGame: {
                chooseGame(socket, input);
                break;
            }
            case Exit: {
                disconnect(socket);
                break;
            }
        }
    }
    private void connection(Socket socket, String [] in) {
        server.response(socket, "@connected;" + GAME_LIST); //send list of games
        connectionPlayer.add(new Player(server, socket, in[1], socket.hashCode())); // in[1] - client name
        System.out.println("New Connection: [" + socket.hashCode() + "], by name [" + in[1] + "].");
    }
    private void chooseGame(Socket socket, String [] in) {
        Player player = server.getPlayer(connectionPlayer, socket);
        switch (Integer.parseInt(in[1])) {
            case 1: {   // Checkers
                server.response(socket, server.getCheckersCommand().checkersPlayerList());
                connectionPlayer.remove(player);
                server.getCheckersCommand().getCheckersPlayersData().addObserver(player);
                break;
            } default: {
                connectionPlayer.remove(player);
                break;
            }
        }
    }
    private void disconnect(Socket socket) {
        Player p = server.getPlayer(connectionPlayer, socket);
        System.out.println("Connection close: [" + p.getHashCode() + "], by name [" + p.getName() + "].");
    }
}