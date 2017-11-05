package com.snakegame.model;

import com.snakegame.client.Client;
import com.snakegame.gui.Panel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;

public class Game implements ActionListener, Serializable {
    public Board board;
    public GameMode gameMode;
    private int fruitTimer;
    private int delay;
    private Panel panel;
    private Client client;

    public Game(int w, int h, int del, GameMode mode, Panel pan, Client c) {
        board = new Board(w, h, 3, mode);
        gameMode = mode;
        delay = del;
        panel = pan;
        client = c;
        new Timer(delay, this).start();
        fruitTimer = board.fruit.timeToDestroy;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(panel != null) {
            if(client != null) {
                try {
                    client.infoChange();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                panel.repaint();
                return;
            }
            panel.actionPerformed();
        }
        int score = board.score;
        board.moveSnakes();
        board.checkCollisions();
        if(board.score != score)
            fruitTimer = board.fruit.timeToDestroy;
        else {
            fruitTimer -= delay;
            if(fruitTimer < 0) {
                board.dropFruit();
                fruitTimer = board.fruit.timeToDestroy;
            }
        }
    }
}
