package com.snakegame.server;

import com.snakegame.model.Game;
import com.snakegame.model.GameMode;
import com.snakegame.model.Snake;

import java.net.DatagramSocket;
import java.util.*;

import static java.lang.Integer.parseInt;

public class Server {
    private static HashSet<Server> activeServers = new HashSet<>();
    public Game game;
    public int connectedPlayers = 0;
    int serverStartPort;
    ArrayList<Integer> freeIDs = new ArrayList<>();
    HashMap<String, Player> players;
    Thread connectThread;
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
        game.board.server = this;
        connectSocket = new DatagramSocket(serverStartPort);
        connectThread = new Thread(new ConnectSocket(connectSocket, args[4], game, this));
        connectThread.start();
        activeServers.add(this);
        players = new HashMap<>();
    }

    private void close() {
        for(Player p: players.values())
        {
            p.socket.close();
            p.thread.interrupt();
        }
        connectThread.interrupt();
        connectSocket.close();
        connectedPlayers = 0;
    }

    boolean playerNamesContains(String name) {
        for(Player p: players.values())
            if(Objects.equals(p.name, name))
                return true;
        return false;
    }

    public Player playerBySnake(Snake snake) {
        for(Player p: players.values())
            if(Objects.equals(p.snake, snake))
                return p;
        return null;
    }

    boolean playerIpPortsContains(String ipPort) {
        return players.containsKey(ipPort);
    }

    public static void main(String args[]) throws Exception { new Server(args); }
}
