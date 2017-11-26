package com.snakegame.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.plaf.synth.ColorType;

import com.snakegame.client.Client;
import com.snakegame.model.*;

public class Panel extends JPanel  {
    private HashMap<Fruit, Image> fruitSprites = new HashMap<Fruit, Image>();
    private JLabel[] scoreLabels;
    public Game game;
    private Client client;

    private static Color[] snakeColors =
            {Color.blue, Color.green, Color.red, Color.magenta};

    private static int[][] playersControls =
            {
                    {KeyEvent.VK_D, KeyEvent.VK_A, KeyEvent.VK_W, KeyEvent.VK_S},
                    {KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT, KeyEvent.VK_UP, KeyEvent.VK_DOWN}
            };

    public Panel(int w, int h, int del, GameMode mode, Client client) throws IOException {
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(new TAdapter());
        scoreLabels = new JLabel[mode.maxPlayers];
        for(int i = 0; i != mode.maxPlayers; ++i) {
            scoreLabels[i] = new JLabel("Score: ");
            scoreLabels[i].setForeground(snakeColors[i]);
            scoreLabels[i].setLocation(300, 300 + i * 30);
            add(scoreLabels[i]);
        }
        game = new Game(w, h, del, mode, this, client);
        if(client != null) {
            client.game = game;
            client.infoChange();
            this.client = client;
        }
        LoadImages();
    }

    private void LoadImages() {
        GameMode gameMode = game.gameMode;
        for(Fruit fruit : gameMode.usingFruits)
            fruitSprites.put(fruit,
                    new ImageIcon(getClass().getResource(fruit.name + ".png")).getImage());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        Board board = game.board;
        g.drawImage(fruitSprites.get(board.fruit), board.getFruitPos().x * 30,  board.getFruitPos().y * 30, this);
        for(Snake snake: board.snakes) {
            if(client == null)
                scoreLabels[snake.number].setText("Score: " + snake.score);
            for (Point point : snake.snakePoints) {
                //g.drawImage(snake_circle, point.x * 30, point.y * 30, this);
                g.setColor(snakeColors[snake.number]);
                g.drawOval(point.x * 30, point.y * 30, 30, 30);
                g.fillOval(point.x * 30, point.y * 30, 30, 30);
            }
        }
        if(client != null) {
            int snakesCount = board.snakes.size();
            for (int i = 0; i != scoreLabels.length; ++i) {
                if (i < snakesCount)
                    scoreLabels[i].setText(client.playerNames[i] + ": " + board.snakes.get(i).score);
                else
                    scoreLabels[i].setText("");
            }
        }
        //Toolkit.getDefaultToolkit().sync();
    }

    public void actionPerformed() {
        repaint();
        Board board = game.board;
        if(board.finished) {
            String infoMes = "Game finised!\n";
            for(int i = 0; i != board.snakes.size(); ++i) {
                infoMes += "Player " + i + ". Score: " + board.snakes.get(i).score;
                if(board.loserNum == i) infoMes += ". Loser";
                infoMes += "\n";
            }
            JFrameExtentions.closeInfoBox(infoMes, "Game Over!");

        }
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            Board board = game.board;
            GameMode gameMode = game.gameMode;
            int key = e.getKeyCode();
            Client client = game.getClient();
            if(client != null)
            {
                int id = client.getId();
                for(int j = 0; j != 4; ++j)
                    if(playersControls[0][j] == key) {
                        try {
                            board.snakes.get(id).setDirection(Direction.getDirection(j));
                        } catch (Exception e1) {
                            continue;
                        }
                        return;
                    }
            }
            for (int i = 0; i != gameMode.snakeCount; ++i)
                for(int j = 0; j != 4; ++j)
                    if(playersControls[i][j] == key) {
                        try {
                            board.snakes.get(i).setDirection(Direction.getDirection(j));

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        return;
                    }
        }
    }
}
