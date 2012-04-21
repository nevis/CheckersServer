package server;

import checkers.CheckersCommand;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter out;
    private ServerCommand serverCommand = new ServerCommand(this);
    private CheckersCommand checkersCommand = new CheckersCommand(this);

    public static void main(String [] args) {
        new Server();
    }
    public Server() {
        try {
            serverSocket = new ServerSocket(5869);
            System.out.println("Server started.");
            while(true) {
                socket = serverSocket.accept();
                new PlayerThread(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void response(Socket s, String message) {
        try {
            out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
            out.println(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public ServerCommand getServerCommand() {
        return serverCommand;
    }
    public CheckersCommand getCheckersCommand() {
        return checkersCommand;
    }
    public Player getPlayer(List<Player> players, Socket socket) {
        Player pl = null;
        for (int i = 0; i < players.size(); i++) {
            pl = players.get(i);
            if ((pl != null) && pl.getSocket() == socket) {
                players.remove(pl);
                break;
            }
        }
        return pl;
    }
    public Player getPlayer(List<Player> players, int hashCode) {
        Player pl = null;
        for (int i = 0; i < players.size(); i++) {
            pl = players.get(i);
            if ((pl != null) && pl.getHashCode() == hashCode) {
                players.remove(pl);
                break;
            }
        }
        return pl;
    }
}
