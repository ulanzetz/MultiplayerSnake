package com.snakegame.model;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;

public class Fruit implements Serializable {
    public int givenScore;
    public int timeToDestroy;
    public String name;

    private Fruit(int score, int time, String n) {
        givenScore = score;
        timeToDestroy = time;
        name = n;
    }
    public static Fruit apple = new Fruit(1, 90 * 1000, "apple");
    public static Fruit pear = new Fruit(5, 15 * 1000, "pear");

    public static HashMap<String, Fruit> fruits = new HashMap<>();
    public static void loadFruits() {
        fruits.put("apple", apple);
        fruits.put("pear", pear);
    }
}
