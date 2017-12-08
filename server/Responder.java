package com.snakegame.server;

import com.snakegame.model.Game;
import com.snakegame.model.Snake;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static java.lang.Integer.parseInt;

class Responder implements Runnable {
    private Player player;
    private Server server;

    public Responder(Player player, Server server) {
        this.player = player;
        this.server = server;
        player.thread = Thread.currentThread();
    }
    public void run() {
        byte[] receiveData = new byte[10];
        byte[] sendData;
        DatagramSocket socket = player.socket;
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
            try {
                String[] splitedData = data.split(" ");
                int x = parseInt(splitedData[0]);
                int y = parseInt(splitedData[1]);
                player.snake.setDirection(new Point(x, y));
                sendData = serializeGame(game, server);
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

    private byte[] serializeGame(Game game, Server server) {
        StringBuilder mes = new StringBuilder(getIdsString() + "&");
        for (int i = 0; i != game.board.snakes.size(); ++i) {
            for (Point p : game.board.snakes.get(i).snakePoints)
                mes.append(p.x).append(",").append(p.y).append(' ');
            mes.append(game.board.snakes.get(i).score).append("'");
        }
        mes.append(game.board.fruitPos.x).append(",").append(game.board.fruitPos.y).append(",").
                append(game.board.fruit.name).append("'");
        for(Player p: server.players.values())
            mes.append(p.name).append("%");
        mes = new StringBuilder(mes.substring(0, mes.length() - 1) + "'");
        return mes.toString().getBytes();
    }

}