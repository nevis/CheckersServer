package server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class PlayerThread extends Thread {
    private Socket player;
    private Server server;
    private int gameType = 0;
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
                switch (gameType) {
                    case 0: {  //server
                        server.getServerCommand().run(player, str);
                        break;
                    } case 1: {  //checkers
                        server.getCheckersCommand().run(player, str);
                        break;
                    }
                }
                if (str.split("\\;")[0].equals("@game")) {
                    gameType = Integer.parseInt(str.split("\\;")[1]);
                } else if (str.split("\\;")[0].equals("@exit")) {
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
