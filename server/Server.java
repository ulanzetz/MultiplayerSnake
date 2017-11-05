package com.snakegame.server;

import com.snakegame.model.Board;
import com.snakegame.model.Game;
import com.snakegame.model.GameMode;
import com.snakegame.model.Snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class Server {
    public static void main(String args[]) throws Exception {
        int serverPort = parseInt(args[0]);
        int width = parseInt(args[1]);
        int height = parseInt(args[2]);
        if(width < 3 || height < 3)
            throw new IllegalArgumentException();
        int delay = parseInt(args[3]);
        GameMode.loadGameMods();
        GameMode mode = GameMode.gameMods.get(args[4]);
        Game game = new Game(width, height, delay, mode, null, null);

        HashMap<InetAddress, Integer> playersIDs = new HashMap<InetAddress, Integer>();
        DatagramSocket socket = new DatagramSocket(serverPort);
        byte[] receiveData = new byte[10];
        byte[] sendData;
        int connectedPlayers = 0;

        while (true) {
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet);
            String data = new String(packet.getData());
            InetAddress ip = packet.getAddress();
            int clientPort = packet.getPort();
            if(data.startsWith("con")) {
                if(connectedPlayers == mode.snakeCount) {
                    sendData = "not".getBytes();
                }
                else {
                    if(!playersIDs.containsKey(ip))
                        playersIDs.put(ip, connectedPlayers++);
                    sendData = (args[1] + " " + args[2] + " " + args[3] + " " + playersIDs.get(ip) + " " +
                             args[4] + " ").getBytes();
                }
            }
            else {
                String[] splitedData = data.split(" ");
                int x = parseInt(splitedData[0]);
                int y = parseInt(splitedData[1]);
                int playerID = playersIDs.get(ip);
                game.board.snakes[playerID].setDirection(new Point(x, y));
                String mes = "";
                for(int i = 0; i != game.gameMode.snakeCount; ++i) {
                    for(Point p: game.board.snakes[i].snakePoints)
                        mes += p.x + "," + p.y + ' ';
                    mes += game.board.snakes[i].score + "'";
                }
                mes += game.board.fruitPos.x + "," + game.board.fruitPos.y + "'";
                sendData = mes.getBytes();
            }
            packet = new DatagramPacket(sendData, sendData.length, ip, clientPort);
            socket.send(packet);
        }
    }

}
