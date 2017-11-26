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

import static com.snakegame.server.Server.*;
import static java.lang.Integer.parseInt;

public class Server {

    public static Game game;
    public static int connectedPlayers = 0;
    public static HashMap<String, Integer> playersIDs = new HashMap<>();
    public static ArrayList<Integer> usingIDs = new ArrayList<>();
    public static HashMap<Integer, Thread> playerThreads = new HashMap<>();
    public static HashMap<Integer, DatagramSocket> playerSockets = new HashMap<>();
    public static int serverStartPort;
    public static ArrayList<Integer> freeIDs = new ArrayList<Integer>();
    public static HashMap<Integer, Integer> snakeNumberByID = new HashMap<>();
    public static HashMap<Integer, String> playerNames = new HashMap<>();
    public static HashMap<String, Integer> nameScoreCounter = new HashMap<>();
    public static Thread connectThread;
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
        for(Thread t: playerThreads.values())
            t.interrupt();
        for(DatagramSocket s: playerSockets.values())
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
        byte[] receiveData = new byte[30];
        byte[] sendData;
        GameMode gameMode = game.gameMode;
        while (true) {
            if(Server.connectThread.isInterrupted())
                return;
            receiveData = new byte[receiveData.length];
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
            String[] splitedData = data.split(" ");
            String name = splitedData.length > 1 ? splitedData[1] : "Player";
            if(data.startsWith("con")) {
                if((gameMode.supportAddingPlayers && Server.connectedPlayers >= gameMode.maxPlayers) ||
                        (!gameMode.supportAddingPlayers && Server.connectedPlayers >= gameMode.snakeCount) ||
                        playerNames.containsValue(name))
                    sendData = "not".getBytes();
                else {
                    try {
                        int id = Server.connectedPlayers;
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
                        Server.playerThreads.put(id, thread);
                        Server.playerSockets.put(id, playerSocket);
                        Server.usingIDs.add(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendData = "error".getBytes();
                    }
                }
            }
            else if(data.startsWith("dis")) {
                if(Server.playersIDs.containsKey(ipPort)) {
                    try {
                        int playerID = Server.playersIDs.get(ipPort);
                        Server.freeIDs.add(playerID);
                        Server.playersIDs.remove(playerID);
                        playerNames.remove(playerID);
                        --Server.connectedPlayers;
                        Thread playerThread = Server.playerThreads.get(playerID);
                        Server.playerThreads.remove(playerID);
                        playerThread.interrupt();
                        DatagramSocket playerSocket = Server.playerSockets.get(playerID);
                        Server.playerSockets.remove(playerID);
                        Server.usingIDs.remove((Object) playerID);
                        playerSocket.close();
                        int playerSnakeNumber = snakeNumberByID.get(playerID);
                        Snake playerSnake = null;
                        for (Snake snake : game.board.snakes)
                            if (snake.number == playerSnakeNumber)
                                playerSnake = snake;
                        if (playerSnake != null)
                            game.board.snakes.remove(playerSnake);
                        snakeNumberByID.remove(playerID);
                        sendData = null;
                        for (Integer i : snakeNumberByID.keySet()) {
                            if (i > playerID) {
                                int snakeNumber = snakeNumberByID.get(i);
                                snakeNumberByID.put(i, snakeNumber - 1);
                                game.board.snakes.get(snakeNumber - 1).number--;
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        sendData = null;
                    }
                }
                else
                    sendData = "bad player".getBytes();
            }
            else
                sendData = "bad response".getBytes();
            try {
                if(sendData != null) {
                    socket.send(new DatagramPacket(sendData, sendData.length, ip, clientPort));
                }
            } catch (Exception e) {
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
                for(Snake snake: game.board.snakes)
                    if(snake.number == playerID)
                        snake.setDirection(new Point(x, y));
                String mes = game.board.snakes.size() + "&";
                for (int i = 0; i != game.board.snakes.size(); ++i) {
                    for (Point p : game.board.snakes.get(i).snakePoints)
                        mes += p.x + "," + p.y + ' ';
                    mes += game.board.snakes.get(i).score + "'";
                }
                mes += game.board.fruitPos.x + "," + game.board.fruitPos.y + "," + game.board.fruit.name +"'";
                for(Integer i: usingIDs)
                    mes += playerNames.get(i) + "%";
                mes = mes.substring(0, mes.length() - 1) + "'";
                sendData = mes.getBytes();
            }
            catch (Exception e) {
                e.printStackTrace();
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
