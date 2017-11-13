package com.snakegame.server;

import com.snakegame.model.Game;
import com.snakegame.model.GameMode;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import static java.lang.Integer.getInteger;
import static java.lang.Integer.parseInt;

public class Server {

    public static void main(String args[]) throws Exception {
        int serverStartPort = parseInt(args[0]);
        int width = parseInt(args[1]);
        int height = parseInt(args[2]);
        if(width < 3 || height < 3)
            throw new IllegalArgumentException();
        int delay = parseInt(args[3]);
        GameMode.loadGameMods();
        GameMode mode = GameMode.gameMods.get(args[4]);
        Game game = new Game(width, height, delay, mode, null, null);

        for(int i = 0; i != game.gameMode.snakeCount; ++i) {
            DatagramSocket socket = new DatagramSocket(serverStartPort + i);
            new Thread(new Responder(socket, game, args[4], i)).start();
        }
    }

}
class Responder implements Runnable {
    private DatagramSocket socket = null;
    private Game game;
    private static HashMap<String, Integer> playersIDs = new HashMap<>();
    private static int connectedPlayers = 0;
    private String modeName;
    private int playerID;

    public Responder(DatagramSocket socket, Game game, String modeName, int playerID) {
        this.socket = socket;
        this.game = game;
        this.modeName = modeName;
        this.playerID = playerID;
    }
    public void run() {
        byte[] receiveData = new byte[10];
        byte[] sendData;
        while (true) {
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String data = new String(packet.getData());
            InetAddress ip = packet.getAddress();
            int clientPort = packet.getPort();
            String ipPort = ip.toString() + ":" + clientPort;
            if(data.startsWith("con")) {
                if(connectedPlayers == game.gameMode.snakeCount) {
                    sendData = "not".getBytes();
                }
                else {
                    if(!playersIDs.containsKey(ipPort))
                        playersIDs.put(ipPort, connectedPlayers++);
                    sendData = (game.board.getWidth() + " " +
                            game.board.getHeight() + " " +
                            game.delay  + " " +
                            playersIDs.get(ipPort) + " " +
                            modeName + " ").getBytes();
                }
            }
            else {
                try {
                    String[] splitedData = data.split(" ");
                    int x = parseInt(splitedData[0]);
                    int y = parseInt(splitedData[1]);
                    game.board.snakes[playerID].setDirection(new Point(x, y));
                    String mes = "";
                    for (int i = 0; i != game.gameMode.snakeCount; ++i) {
                        for (Point p : game.board.snakes[i].snakePoints)
                            mes += p.x + "," + p.y + ' ';
                        mes += game.board.snakes[i].score + "'";
                    }
                    mes += game.board.fruitPos.x + "," + game.board.fruitPos.y + "'";
                    sendData = mes.getBytes();
                }
                catch (Exception e) {
                    continue;
                }
            }
            packet = new DatagramPacket(sendData, sendData.length, ip, clientPort);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
