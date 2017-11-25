package com.snakegame.model;

import java.io.Serializable;
import java.util.HashMap;

public class GameMode implements Serializable{
    public Fruit[] usingFruits;
    public boolean infMode;
    public int snakeCount;
    public int botCounts;
    public boolean supportAddingPlayers = false;
    public int maxPlayers = 4;

    private GameMode(Fruit[] fruits, boolean inf, int count, int bCount) {
        usingFruits = fruits;
        infMode = inf;
        snakeCount = count;
        botCounts = bCount;
    }
    public static void loadGameMods() {

        GameMode classic = new GameMode(new Fruit[]{Fruit.apple}, false, 1, 0);
        GameMode infinitive = new GameMode(new Fruit[]{Fruit.apple, Fruit.pear}, true, 1, 0);
        GameMode twoSnakesInf = new GameMode(new Fruit[]{Fruit.apple}, true, 2, 0);
        GameMode twoSnakesCls = new GameMode(new Fruit[]{Fruit.apple}, false, 2, 0);
        GameMode infWithBot = new GameMode(new Fruit[]{Fruit.apple}, true, 2, 1);
        GameMode multiplayerInfSnakes = new GameMode(new Fruit[]{Fruit.apple, Fruit.pear}, true, 0, 0);
        multiplayerInfSnakes.supportAddingPlayers = true;

        gameMods.put("classic", classic);
        gameMods.put("infinite", infinitive);
        gameMods.put("twosnakesinf", twoSnakesInf);
        gameMods.put("twosnakesclassic", twoSnakesCls);
        gameMods.put("infwithbot", infWithBot);
        gameMods.put("multiplayerinfsnakes", multiplayerInfSnakes);
    }
    public static HashMap<String, GameMode> gameMods = new HashMap<>();
}
