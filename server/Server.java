package com.snakegame.server;

import com.snakegame.model.Game;
import com.snakegame.model.GameMode;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class Server {

    public static Game game;
    public static int connectedPlayers = 0;
    public static HashMap<String, Integer> playersIDs = new HashMap<>();
    public static ArrayList<Thread> playerThreads;
    public static ArrayList<DatagramSocket> playerSockets;
    public static int serverStartPort;
    private static Thread connectThread;
    private static DatagramSocket connectSocket;

    public static void main(String args[]) throws Exception {
        serverStartPort = parseInt(args[0]);
        int width = parseInt(args[1]);
        int height = parseInt(args[2]);
        if(width < 3 || height < 3)
            throw new IllegalArgumentException();
        int delay = parseInt(args[3]);
        GameMode.loadGameMods();
        GameMode mode = GameMode.gameMods.get(args[4]);
        game = new Game(width, height, delay, mode, null, null);
        connectSocket = new DatagramSocket(serverStartPort);
        connectThread = new Thread(new ConnectSocket(connectSocket, args[4], game));
        connectThread.start();
    }

    public static void close() {
        for(Thread t: playerThreads)
            t.interrupt();
        for(DatagramSocket s: playerSockets)
            s.close();
        connectThread.interrupt();
        connectSocket.close();
    }
}
class ConnectSocket implements Runnable {
    private DatagramSocket socket = null;
    private Game game;
    private String modeName;

    public ConnectSocket(DatagramSocket socket, String modeName, Game game) {
        this.socket = socket;
        this.modeName = modeName;
        this.game = game;
    }

    public void run() {
        byte[] receiveData = new byte[10];
        byte[] sendData;
        GameMode gameMode = game.gameMode;
        while (true) {
            if(Thread.currentThread().isInterrupted())
                return;
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
                if((gameMode.supportAddingPlayers && Server.connectedPlayers >= gameMode.maxPlayers) ||
                        (!gameMode.supportAddingPlayers && Server.connectedPlayers >= gameMode.snakeCount))
                    sendData = "not".getBytes();
                else {
                    if(!Server.playersIDs.containsKey(ipPort))
                        Server.playersIDs.put(ipPort, Server.connectedPlayers++);
                    sendData = (game.board.getWidth() + " " +
                            game.board.getHeight() + " " +
                            game.delay  + " " +
                            Server.playersIDs.get(ipPort) + " " +
                            modeName + " ").getBytes();
                    try {
                        DatagramSocket playerSocket = new DatagramSocket(Server.serverStartPort + Server.connectedPlayers);
                        Thread thread = new Thread(new Responder(playerSocket, Server.connectedPlayers - 1));
                        thread.start();
                        Server.playerThreads.add(thread);
                        Server.playerSockets.add(playerSocket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else if(data.startsWith("dis")) {
                if(Server.playersIDs.containsKey(ipPort)) {
                    int playerID = Server.playersIDs.get(ipPort);
                    Server.playersIDs.remove(playerID);
                    --Server.connectedPlayers;
                    Thread thread = Server.playerThreads.get(playerID);
                    Server.playerThreads.remove(playerID);
                    thread.interrupt();
                    DatagramSocket playerSocket = Server.playerSockets.get(playerID);
                    Server.playerSockets.remove(playerID);
                    playerSocket.close();
                    sendData = "ok".getBytes();
                }
                else
                    sendData = "bad player".getBytes();
            }
            else
                sendData = "bad response".getBytes();
            try {
                socket.send(new DatagramPacket(sendData, sendData.length, ip, clientPort));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Responder implements Runnable {
    private DatagramSocket socket = null;
    private int playerID;

    public Responder(DatagramSocket socket, int playerID) {
        this.socket = socket;
        this.playerID = playerID;
    }
    public void run() {
        byte[] receiveData = new byte[10];
        byte[] sendData;
        Game game = Server.game;
        while (true) {
            if(Thread.currentThread().isInterrupted())
                return;
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
            if(Server.playersIDs.get(ipPort) != playerID)
               continue;
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
            packet = new DatagramPacket(sendData, sendData.length, ip, clientPort);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
