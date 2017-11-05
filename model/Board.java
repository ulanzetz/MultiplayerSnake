package com.snakegame.model;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Board implements Serializable{
    private final int width, height;
    private final int snakeStartSize;
    public Point fruitPos;
    public Fruit fruit;
    private GameMode gameMode;
    public Snake[] snakes;
    public boolean finished;
    public int score;
    public int loserNum;

    public Board(int w, int h, int snakeSize, GameMode mode) {
        width = w;
        height = h;
        snakeStartSize = snakeSize;
        loserNum = -1;
        snakes = new Snake[mode.snakeCount];
        for (int i = 0; i != mode.snakeCount; ++i)
            snakes[i] = new Snake(snakeSize, i);
        for(int i = mode.snakeCount - mode.botCounts; i != mode.snakeCount; ++i)
            snakes[i].bot = true;
        gameMode = mode;
        dropFruit();
    }

    public void setFruitPos(Point point){
        fruitPos = point;
    }
    public int getWidth(){
        return width;
    }
    public int getHeight(){
        return height;
    }
    public GameMode getGameMode(){
        return gameMode;
    }

    public void checkCollisions() {
        for(int i = 0; i != gameMode.snakeCount; ++i)
            checkCollision(i);
    }

    public void moveSnakes() {
        for(int i = 0; i != gameMode.snakeCount; ++i)
            snakes[i].move(fruitPos);
    }

    private void checkCollision(int snakeNumber) {
        Snake snake = snakes[snakeNumber];
        Point head = snake.getHead();
        if(head.equals(fruitPos)) {
            int points = fruit.givenScore;
            for(int i = 0; i != points; ++i)
                snake.addLength();
            snake.score += points;
            score += points;
            dropFruit();
        }
        if(head.x < 0 || head.y < 0 || head.x >= width || head.y >= height) {
            if(!gameMode.infMode) {
                loserNum = snakeNumber;
                finished = true;
                return;
            }
            snakes[snakeNumber] = new Snake(snakeStartSize, snake.number, snakes[snakeNumber].score);
        }
        int size = snake.snakePoints.size();
        for(int i = 0; i != gameMode.snakeCount; ++i)
            for(int j = 0; j != snakes[i].getSize(); ++j) {
                if(head.equals(snakes[i].snakePoints.get(j))) {
                    if(j == 0 && i == snakeNumber) continue;
                    if(i == snakeNumber){
                        if (!gameMode.infMode) {
                            loserNum = i;
                            finished = true;
                            return;
                        }
                        snake.snakePoints.subList(j - 1, size - 1).clear();
                        return;
                    }
                    if(!gameMode.infMode) {
                        loserNum = snakeNumber;
                        finished = true;
                    }
                }
            }
    }

    public void dropFruit() {
        ArrayList<Point> allPoints = new ArrayList<>();
        for(int x = 0; x != width; ++x)
            for(int y = 0; y != height; ++y)
                allPoints.add(new Point(x, y));
        List<Point> emptyPoints = allPoints.stream()
                .filter(i ->
                        {
                            for(int j = 0; j != gameMode.snakeCount; ++j)
                                if(snakes[j].snakePoints.contains(i))
                                    return false;
                            return true;
                        })
                .collect(Collectors.toList());
        Random rand = new Random();
        fruitPos = emptyPoints.get(rand.nextInt(emptyPoints.size()));
        fruit = gameMode.usingFruits[rand.nextInt(gameMode.usingFruits.length)];
    }

    public Point getFruitPos() {
        return fruitPos;
    }

}
