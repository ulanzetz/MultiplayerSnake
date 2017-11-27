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
import java.util.HashSet;

import static com.snakegame.server.Server.*;
import static java.lang.Integer.parseInt;

public class Server {
    public static HashSet<Server> activeServers = new HashSet<>();
    public Game game;
    public int connectedPlayers = 0;
    public HashMap<String, Integer> playersIDs = new HashMap<>();
    public ArrayList<Integer> usingIDs = new ArrayList<>();
    public HashMap<Integer, Thread> playerThreads = new HashMap<>();
    public HashMap<Integer, DatagramSocket> playerSockets = new HashMap<>();
    public int serverStartPort;
    public ArrayList<Integer> freeIDs = new ArrayList<Integer>();
    public HashMap<Integer, String> playerNames = new HashMap<>();
    public Thread connectThread;
    private DatagramSocket connectSocket;

    public Server(String args[]) throws Exception {
        serverStartPort = parseInt(args[0]);
        for(Server s : activeServers) {
            if (s.serverStartPort == serverStartPort) {
                s.close();
            }
        }
        int width = parseInt(args[1]);
        int height = parseInt(args[2]);
        if(width < 3 || height < 3)
            throw new IllegalArgumentException();
        int delay = parseInt(args[3]);
        GameMode.loadGameMods();
        GameMode mode = GameMode.gameMods.get(args[4]);
        game = new Game(width, height, delay, mode, null, null);
        connectSocket = new DatagramSocket(serverStartPort);
        connectThread = new Thread(new ConnectSocket(connectSocket, args[4], game, this));
        connectThread.start();
        activeServers.add(this);
    }

    public void close() {
        for(Thread t: playerThreads.values())
            t.interrupt();
        for(DatagramSocket s: playerSockets.values())
            s.close();
        connectThread.interrupt();
        connectSocket.close();
        connectedPlayers = 0;

    }

    public static void main(String args[]) throws Exception { new Server(args); }
}
class ConnectSocket implements Runnable {
    private DatagramSocket socket = null;
    private Game game;
    private String modeName;
    private Server server;

    public ConnectSocket(DatagramSocket socket, String modeName, Game game, Server server) {
        this.socket = socket;
        this.modeName = modeName;
        this.game = game;
        this.server = server;
    }

    public void run() {
        byte[] receiveData = new byte[30];
        byte[] sendData;
        GameMode gameMode = game.gameMode;
        while (true) {
            receiveData = new byte[receiveData.length];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                continue;
            }
            if(server.connectThread.isInterrupted())
                return;
            String data = new String(packet.getData());
            InetAddress ip = packet.getAddress();
            int clientPort = packet.getPort();
            String ipPort = ip.toString() + ":" + clientPort;
            String[] splitedData = data.split(" ");
            String name = splitedData.length > 1 ? splitedData[1] : "Player";
            if(data.startsWith("con")) {
                if((gameMode.supportAddingPlayers && server.connectedPlayers >= gameMode.maxPlayers) ||
                        (!gameMode.supportAddingPlayers && server.connectedPlayers >= gameMode.snakeCount) ||
                        server.playerNames.containsValue(name))
                    sendData = "not".getBytes();
                else {
                    try {
                        int id = server.connectedPlayers;
                        if(server.freeIDs.size() > 0) {
                            id = Collections.min(server.freeIDs);
                        }
                        server.playerNames.put(id, name);
                        if(!server.playersIDs.containsKey(ipPort))
                            server.playersIDs.put(ipPort, id);
                        if(gameMode.supportAddingPlayers) {
                            game.board.snakes.add(new Snake(3, id));
                        }
                        sendData = (game.board.getWidth() + " " +
                                game.board.getHeight() + " " +
                                game.delay  + " " +
                                id + " " +
                                modeName + " ").getBytes();
                        DatagramSocket playerSocket = new DatagramSocket(server.serverStartPort + id + 1);
                        Thread thread = new Thread(new Responder(playerSocket, id, server));
                        server.connectedPlayers++;
                        thread.start();
                        server.playerThreads.put(id, thread);
                        server.playerSockets.put(id, playerSocket);
                        server.usingIDs.add(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendData = "error".getBytes();
                    }
                }
            }
            else if(data.startsWith("dis")) {
                if(server.playersIDs.containsKey(ipPort)) {
                    try {
                        int playerID = server.playersIDs.get(ipPort);
                        server.freeIDs.add(playerID);
                        server.playersIDs.remove(playerID);
                        server.playerNames.remove(playerID);
                        --server.connectedPlayers;
                        Thread playerThread = server.playerThreads.get(playerID);
                        server.playerThreads.remove(playerID);
                        playerThread.interrupt();
                        DatagramSocket playerSocket = server.playerSockets.get(playerID);
                        server.playerSockets.remove(playerID);
                        server.usingIDs.remove((Object) playerID);
                        playerSocket.close();
                        Snake playerSnake = null;
                        for (Snake snake : game.board.snakes)
                            if (snake.number == playerID)
                                playerSnake = snake;
                        if (playerSnake != null)
                            game.board.snakes.remove(playerSnake);
                        sendData = null;
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
    private Server server;

    public Responder(DatagramSocket socket, int playerID, Server server) {
        this.socket = socket;
        this.playerID = playerID;
        this.server = server;
    }
    public void run() {
        byte[] receiveData = new byte[10];
        byte[] sendData;
        Game game = server.game;
        while (true) {
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                continue;
            }
            if(Thread.currentThread().isInterrupted())
                return;
            String data = new String(packet.getData());
            InetAddress ip = packet.getAddress();
            int clientPort = packet.getPort();
            String ipPort = ip.toString() + ":" + clientPort;
            try {
                String[] splitedData = data.split(" ");
                int x = parseInt(splitedData[0]);
                int y = parseInt(splitedData[1]);
                for(Snake snake: game.board.snakes)
                    if(snake.number == playerID)
                        snake.setDirection(new Point(x, y));
                String mes = getIdsString() + "&";
                for (int i = 0; i != game.board.snakes.size(); ++i) {
                    for (Point p : game.board.snakes.get(i).snakePoints)
                        mes += p.x + "," + p.y + ' ';
                    mes += game.board.snakes.get(i).score + "'";
                }
                mes += game.board.fruitPos.x + "," + game.board.fruitPos.y + "," + game.board.fruit.name +"'";
                for(Integer i: server.usingIDs)
                    mes += server.playerNames.get(i) + "%";
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

    private String getIdsString() {
        String ids = "";
        for(Snake snake: server.game.board.snakes)
            ids += snake.number+"-";
        return ids.substring(0, ids.length() - 1);
    }

}
