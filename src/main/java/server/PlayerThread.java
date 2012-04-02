package server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class PlayerThread extends Thread {
    private Socket player;
    private Server server;

    public PlayerThread(Socket player, Server server) {
        this.player = player;
        this.server = server;
        start();
    }
    @Override
    public void run() {
        try {
            Scanner in = new Scanner(new InputStreamReader(player.getInputStream()));
            String str;
            while (true) {
                str = in.nextLine();
                if (str.split("\\;")[0].equals("@exit")) {
                    server.getServerCommand().run(player, str);
                    break;
                } else {
                    server.getServerCommand().run(player, str);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
