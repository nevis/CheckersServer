package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter out;
    private ServerCommand serverCommand;
    private PlayerThread playerThread;
    private PlayersData playersData = new PlayersData();

    public static void main(String [] args) {
        new Server();
    }
    public Server() {
        try {
            serverSocket = new ServerSocket(5869);
            serverCommand = new ServerCommand(this, playersData);
            System.out.println("Server started.");
            while(true) {
                socket = serverSocket.accept();
                playerThread = new PlayerThread(socket, this);
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
}
