package com.snakegame.server;

import com.snakegame.model.Game;
import com.snakegame.model.GameMode;
import com.snakegame.model.Snake;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.snakegame.server.Server.playerNames;
import static com.snakegame.server.Server.playersIDs;
import static com.snakegame.server.Server.snakeNumberByID;
import static java.lang.Integer.parseInt;

public class Server {

    public static Game game;
    public static int connectedPlayers = 0;
    public static HashMap<String, Integer> playersIDs = new HashMap<>();
    public static ArrayList<Thread> playerThreads = new ArrayList<>();
    public static ArrayList<DatagramSocket> playerSockets = new ArrayList<>();
    public static int serverStartPort;
    public static ArrayList<Integer> freeIDs = new ArrayList<Integer>();
    public static HashMap<Integer, Integer> snakeNumberByID = new HashMap<>();
    public static HashMap<Integer, String> playerNames = new HashMap<>();
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
                    try {
                        int id = Server.connectedPlayers;
                        String[] splitedData = data.split(" ");
                        String name = splitedData.length > 1 ? data.split(" ")[1] : "Player";
                        if(Server.freeIDs.size() > 0) {
                            id = Collections.min(Server.freeIDs);
                        }
                        playerNames.put(id, name);
                        if(!Server.playersIDs.containsKey(ipPort))
                            Server.playersIDs.put(ipPort, id);
                        if(gameMode.supportAddingPlayers) {
                            game.board.snakes.add(new Snake(3, id));
                            snakeNumberByID.put(id, game.board.snakes.size() - 1);
                        }
                        sendData = (game.board.getWidth() + " " +
                                game.board.getHeight() + " " +
                                game.delay  + " " +
                                id + " " +
                                modeName + " " + snakeNumberByID.get(id) + " ").getBytes();
                        DatagramSocket playerSocket = new DatagramSocket(Server.serverStartPort + id + 1);
                        Thread thread = new Thread(new Responder(playerSocket, id));
                        Server.connectedPlayers++;
                        thread.start();
                        Server.playerThreads.add(thread);
                        Server.playerSockets.add(playerSocket);
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendData = "error".getBytes();
                    }
                }
            }
            else if(data.startsWith("dis")) {
                if(Server.playersIDs.containsKey(ipPort)) {
                    int playerID = Server.playersIDs.get(ipPort);
                    Server.freeIDs.add(playerID);
                    Server.playersIDs.remove(playerID);
                    snakeNumberByID.remove(playerID);
                    playerNames.remove(playerID);
                    --Server.connectedPlayers;
                    Thread thread = Server.playerThreads.get(playerID);
                    Server.playerThreads.remove(playerID);
                    thread.interrupt();
                    DatagramSocket playerSocket = Server.playerSockets.get(playerID);
                    Server.playerSockets.remove(playerID);
                    playerSocket.close();
                    game.board.snakes.remove(playerID);
                    sendData = "ok".getBytes();
                    for(Integer i: snakeNumberByID.keySet()) {
                        if(i > playerID) {
                            int snakeNumber = snakeNumberByID.get(i);
                            snakeNumberByID.put(i,  snakeNumber- 1);
                            game.board.snakes.get(snakeNumber).number--;
                        }
                    }
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
            //if(Server.playersIDs.get(ipPort) != playerID)
            //   continue;
            try {
                String[] splitedData = data.split(" ");
                int x = parseInt(splitedData[0]);
                int y = parseInt(splitedData[1]);
                game.board.snakes.get(snakeNumberByID.get(playerID)).setDirection(new Point(x, y));
                String mes = game.board.snakes.size() + "&";
                for (int i = 0; i != game.board.snakes.size(); ++i) {
                    for (Point p : game.board.snakes.get(i).snakePoints)
                        mes += p.x + "," + p.y + ' ';
                    mes += game.board.snakes.get(i).score + "'";
                }
                mes += game.board.fruitPos.x + "," + game.board.fruitPos.y + "," + game.board.fruit.name +"'";
                for(Integer i: playersIDs.values())
                    mes += playerNames.get(i) + "%";
                mes = mes.substring(0, mes.length() - 1) + "'";
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
