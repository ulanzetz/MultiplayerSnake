package com.snakegame.server;

import com.snakegame.model.Snake;

import java.net.DatagramSocket;

class Player {
    Thread thread;
    DatagramSocket socket;
    int ID;
    String name;
    Snake snake;

    Player(int ID, DatagramSocket socket, String name, Snake snake)
    {
        this.ID = ID;
        this.socket = socket;
        this.name = name;
    }
}
