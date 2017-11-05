package com.snakegame.client;

import com.snakegame.gui.GameForm;
import com.snakegame.gui.JFrameExtentions;
import com.snakegame.gui.Panel;
import com.snakegame.model.*;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Client extends JFrame {
    private int id;
    private Game game;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private InetAddress ip;
    private int port;

    public Client() {
        setTitle("Choose settings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
        setResizable(false);
        JFrameExtentions.SetLocationToCenter(this);
        setVisible(true);
        JPanel panel = new JPanel(null);
        add(panel);

        JLabel ipLabel = new JLabel("Server IP:");
        panel.add(ipLabel);
        ipLabel.setLocation(5, 10);
        ipLabel.setSize(80, 30);

        JTextField ipBox = new JTextField("127.0.0.1");
        panel.add(ipBox);
        ipBox.setLocation(85, 10);
        ipBox.setSize(80, 30);

        JLabel serverPort = new JLabel("Server Port:");
        panel.add(serverPort);
        serverPort.setLocation(5, 50);
        serverPort.setSize(80, 30);

        JTextField serverPortBox = new JTextField("9866");
        panel.add(serverPortBox);
        serverPortBox.setLocation(85, 50);
        serverPortBox.setSize(80, 30);

        JButton startButton = new JButton("Start");
        panel.add(startButton);
        startButton.setLocation(200, 230);
        startButton.setSize(80, 30);

        startButton.addActionListener(e -> {
            try {
                ip = InetAddress.getByName(ipBox.getText());
                port = Integer.parseInt(serverPortBox.getText());
                socket = new DatagramSocket();
                packet = new DatagramPacket("con".getBytes(), 3, ip, port);
                socket.send(packet);
                byte[] receiveData = new byte[30];
                packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                String answer = new String(receiveData);
                String args[] = answer.split(" ");
                int width = Integer.parseInt(args[0]);
                int height = Integer.parseInt(args[1]);
                int delay = Integer.parseInt(args[2]);
                id = Integer.parseInt(args[3]);
                GameMode.loadGameMods();
                GameMode mode = GameMode.gameMods.get(args[4]);
                GameForm form = new GameForm();
                form.setSize(width * 30 + 20, height * 30 + 30);
                JFrameExtentions.SetLocationToCenter(form);

                Panel panel1 = new Panel(width, height, delay, mode, this);
                form.add(panel1);
                setVisible(false);
                dispose();
                game = panel1.game;

            } catch (Exception exp) {
                JFrameExtentions.infoBox("Incorrect format of data", "Error");
            }
        });
    }

    public void infoChange() throws IOException {
        Point dir = game.board.snakes[id].getDirection();
        byte[] mes = (dir.x + " " + dir.y + " ").getBytes();
        packet = new DatagramPacket(mes, mes.length, ip, port);
        socket.send(packet);
        byte[] receiveData = new byte[256];
        packet = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(packet);
        String args[] = new String(receiveData).split("'");
        for(int i = 0; i != game.gameMode.snakeCount; ++i) {
            String points[] = args[i].split(" ");
            Snake snake = game.board.snakes[i];
            ArrayList<Point> snPoints = new ArrayList<>();
            for(int j = 0; j != points.length - 1; ++j) {
                String cords[] = points[j].split(",");
                int x = Integer.parseInt(cords[0]);
                int y = Integer.parseInt(cords[1]);
                snPoints.add(new Point(x, y));
            }
            snake.snakePoints = snPoints;
            snake.score = Integer.parseInt(points[points.length - 1]);
        }
        String fruitCords[] = args[game.gameMode.snakeCount].split(",");
        int x = Integer.parseInt(fruitCords[0]);
        int y = Integer.parseInt(fruitCords[1]);
        game.board.fruitPos = new Point(x, y);
    }

    public static void main(String[] args) {
        new Client();
    }
}
