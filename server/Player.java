package com.snakegame.server;

import com.snakegame.model.Snake;

import java.net.DatagramSocket;

public class Player {
    Thread thread;
    DatagramSocket socket;
    int ID;
    String name;
    public Snake snake;

    Player(int ID, DatagramSocket socket, String name, Snake snake)
    {
        this.ID = ID;
        this.socket = socket;
        this.name = name;
        this.snake = snake;
    }
}
