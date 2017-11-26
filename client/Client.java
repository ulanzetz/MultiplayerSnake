package com.snakegame.client;

import com.snakegame.gui.GameForm;
import com.snakegame.gui.JFrameExtentions;
import com.snakegame.gui.Panel;
import com.snakegame.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Client extends JFrame {
    private int id;
    public Game game;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private InetAddress ip;
    private int port;
    private int connectPort;
    public boolean debugMode = false;
    private int snakeID;
    public String[] playerNames;

    public Client()  {
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

        JLabel nameLabel = new JLabel("Name");
        panel.add(nameLabel);
        nameLabel.setLocation(5, 90);
        nameLabel.setSize(80, 30);

        JTextField nameBox = new JTextField("Player");
        panel.add(nameBox);
        nameBox.setLocation(85, 90);
        nameBox.setSize(80, 30);


        JButton startButton = new JButton("Start");
        panel.add(startButton);
        startButton.setLocation(200, 230);
        startButton.setSize(80, 30);

        startButton.addActionListener(e -> {
            try {
                initializeClient(ipBox.getText(), serverPortBox.getText(),  nameBox.getText(), true);
            } catch (IOException e1) {
                JFrameExtentions.infoBox("Incorrect format of data", "Error");
            }
        });
    }

    public Client(String ipString, String portString) {
        try {
            debugMode = true;
            initializeClient(ipString, portString, "",false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeClient(String ipString, String portString, String name, boolean useGui) throws IOException {
        ip = InetAddress.getByName(ipString);
        port = Integer.parseInt(portString);
        connectPort = port;
        socket = new DatagramSocket();
        packet = new DatagramPacket(("con "  + name + " ").getBytes(), 5 + name.length(), ip, port);
        socket.send(packet);
        byte[] receiveData = new byte[50];
        packet = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(packet);
        String answer = new String(receiveData);
        String args[] = answer.split(" ");
        int width = Integer.parseInt(args[0]);
        int height = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);
        id = Integer.parseInt(args[3]);
        port += 1 + id;
        GameMode.loadGameMods();
        Fruit.loadFruits();
        GameMode mode = GameMode.gameMods.get(args[4]);
        snakeID = Integer.parseInt(args[5]);
        Panel panel1 = new Panel(width, height, delay, mode, this);

        if(useGui) {
            GameForm form = new GameForm();
            form.setSize(width * 30 + 20, height * 30 + 30);
            JFrameExtentions.SetLocationToCenter(form);
            form.add(panel1);
            form.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    super.windowClosing(e);
                }
            });
            setVisible(false);
            dispose();
        }
    }

    public void infoChange() throws IOException {
        while (game.board.snakes.size() > 0 && snakeID >= game.board.snakes.size())
            snakeID--;
        Point dir = game.board.snakes.size() > 0 ? game.board.snakes.get(snakeID).getDirection() : Direction.Down;
        byte[] mes = (dir.x + " " + dir.y + " ").getBytes();
        packet = new DatagramPacket(mes, mes.length, ip, port);
        socket.send(packet);
        byte[] receiveData = new byte[256];
        packet = new DatagramPacket(receiveData, receiveData.length);
        socket.setSoTimeout(1000);
        socket.receive(packet);
        String data[] = new String(receiveData).split("&");
        int snakeCount = Integer.parseInt(data[0]);
        if(snakeCount != game.board.snakes.size()) {
            game.board.snakes = new ArrayList<Snake>(snakeCount);
            for(int i = 0; i != snakeCount; ++i)
                game.board.snakes.add(new Snake(3, i));
        }
        String args[] = data[1].split("'");
        for (int i = 0; i != snakeCount; ++i) {
            String points[] = args[i].split(" ");
            Snake snake = game.board.snakes.get(i);
            ArrayList<Point> snPoints = new ArrayList<>();
            for (int j = 0; j != points.length - 1; ++j) {
                String cords[] = points[j].split(",");
                int x = Integer.parseInt(cords[0]);
                int y = Integer.parseInt(cords[1]);
                snPoints.add(new Point(x, y));
            }
            snake.snakePoints = snPoints;
            snake.score = Integer.parseInt(points[points.length - 1]);
        }
        String fruitCords[] = args[snakeCount].split(",");
        int x = Integer.parseInt(fruitCords[0]);
        int y = Integer.parseInt(fruitCords[1]);
        game.board.fruitPos = new Point(x, y);
        game.board.fruit = Fruit.fruits.get(fruitCords[2]);
        playerNames = args[snakeCount + 1].split("%");
    }

    public void close() throws IOException {
        socket.send(new DatagramPacket("dis".getBytes(), 3, ip, connectPort));
    }

    public static void main(String[] args) {
        new Client();
    }

    public Game getGame() {
        return game;
    }

    public int getId() {
        return snakeID;
    }
}
