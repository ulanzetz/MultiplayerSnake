package com.snakegame.server;

import com.snakegame.model.Game;
import com.snakegame.model.GameMode;
import com.snakegame.model.Snake;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;

class ConnectSocket implements Runnable {
    private DatagramSocket socket = null;
    private Game game;
    private String modeName;
    private Server server;

    ConnectSocket(DatagramSocket socket, String modeName, Game game, Server server) {
        this.socket = socket;
        this.modeName = modeName;
        this.game = game;
        this.server = server;
    }

    public void run() {
        byte[] receiveData = new byte[30];
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
            if(data.startsWith("con")) {
                String[] splitedData = data.split(" ");
                String name = splitedData.length > 1 ? splitedData[1] : "Player";
                String passwd = splitedData.length > 2 ? splitedData[2] : "";
                connect(name, passwd, ip, clientPort);
            }
            else if(data.startsWith("dis"))
                disconnect(ip, clientPort);
            else
            {
                byte[] response = "bad response".getBytes();
                try {
                    socket.send(new DatagramPacket(response, response.length, ip, clientPort));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void connect(String name, String passwd, InetAddress ip, int clientPort) {
        byte[] sendData;
        String ipPort = ip.toString() + ":" + clientPort;
        Integer score = loadScore(name, passwd);
        if(score == null)
            sendData = "not".getBytes();
        else {
            try {
                int id = server.connectedPlayers;
                if(server.freeIDs.size() > 0) {
                    id = Collections.min(server.freeIDs);
                }
                sendData = (game.board.getWidth() + " " +
                        game.board.getHeight() + " " +
                        game.delay  + " " +
                        id + " " +
                        modeName + " ").getBytes();
                DatagramSocket playerSocket = new DatagramSocket(server.serverStartPort + id + 1);
                Snake snake = new Snake(3, id, score);
                game.board.snakes.add(snake);
                Player player = new Player(id, playerSocket, name, snake);
                Thread thread = new Thread(new Responder(player, server));
                player.thread = thread;
                server.connectedPlayers++;
                thread.start();
                server.players.put(ipPort, player);
            } catch (Exception e) {
                e.printStackTrace();
                sendData = "error".getBytes();
            }
        }
        try {
            socket.send(new DatagramPacket(sendData, sendData.length, ip, clientPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private Integer loadScore(String name, String passwd) {
        GameMode gameMode = game.gameMode;
        if((gameMode.supportAddingPlayers && server.connectedPlayers >= gameMode.maxPlayers) ||
                (!gameMode.supportAddingPlayers && server.connectedPlayers >= gameMode.snakeCount) ||
                server.playerNamesContains(name))
            return null;
        try {
            return DbHandler.dbHandler.loadPlayerScore(name, passwd);
        }
        catch (Exception e) {
            return null;
        }
    }

    private void disconnect(InetAddress ip, int clientPort) {
        String ipPort = ip.toString() + ":" + clientPort;
        if(server.playerIpPortsContains(ipPort)) {
            try {
                Player player = server.players.get(ipPort);
                DbHandler.dbHandler.updatePlayer(player.name, player.snake.score);
                server.freeIDs.add(player.ID);
                --server.connectedPlayers;
                player.thread.interrupt();
                player.socket.close();
                game.board.snakes.remove(player.snake);
                server.players.remove(ipPort);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}