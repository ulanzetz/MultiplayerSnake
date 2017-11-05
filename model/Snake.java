package com.snakegame.model;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Snake implements Serializable {
    public ArrayList<Point> snakePoints = new ArrayList<Point>();
    private Point direction;
    public int number;
    public int score = 0;
    public boolean bot = false;

    public Snake(int size, int num){
        number = num;
        for(int i = size - 1; i != -1; --i)
            snakePoints.add(new Point(number * size, i));
        direction = Direction.Down;
    }
    public Snake(int size, int num, int scr){
        number = num;
        score = scr;
        for(int i = size - 1; i != -1; --i)
            snakePoints.add(new Point(number * size, i));
        direction = Direction.Down;
    }

    public Snake(int x, int y, Point snakeDirection, int size, int snakeNumber){
        number = snakeNumber;
        direction = snakeDirection;
        for(int i = 0; i != size; ++i)
            snakePoints.add(new Point(x + number * 3, y - direction.y * i));
    }

    public Snake(Point dir, int snakeNumber, Point... points) {
        direction = dir;
        number = snakeNumber;
        for (Point p: points)
            snakePoints.add(p);
    }

    public void move(){
        for(int i = snakePoints.size() - 1; i != 0; --i)
            snakePoints.set(i, snakePoints.get(i - 1));
        Point head = snakePoints.get(0);
        snakePoints.set(0, new Point(head.x + direction.x, head.y + direction.y));
    }

    public void move(Point fruitPos) {
        if(bot) botMove(fruitPos);
        move();
    }

    public void botMove(Point fruitPos) {
        Point head = getHead();
        head = new Point(fruitPos.x - head.x , fruitPos.y - head.y);
        if(head.x != 0) direction = new Point((int)Math.signum(head.x), 0);
        else direction = new Point(0, (int)Math.signum(head.y));
    }

    public void addLength(){
        int length = snakePoints.size();
        Point firstTail = snakePoints.get(length - 1);
        Point secondTail = snakePoints.get(length - 2);
        snakePoints.add(
                new Point(2 * firstTail.x - secondTail.x,
                        2 * firstTail.y - secondTail.y));
    }

    public void setDirection(Point newDir){
        if(newDir.equals(new Point(-direction.x, -direction.y)))
            return;
        direction = newDir;
    }

    public Point getHead(){
        return snakePoints.get(0);
    }
    
    public Point getDirection(){ 
        return direction; 
    }

    public int getSize() { return snakePoints.size(); }

}
