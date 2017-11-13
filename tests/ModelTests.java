package com.snakegame.tests;

import com.snakegame.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.awt.*;
import java.lang.reflect.*;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class ModelTests {
    public static class ModelTestsClassic {
        private Board gameBoard;
        private Snake snake;

        @Before
        public void testData() {
            GameMode.loadGameMods();
            gameBoard = new Board(10, 10, 2, GameMode.gameMods.get("classic"));
            snake = gameBoard.snakes[0];
        }

        @Test
        public void testInitializationWidthHeightScoreSize() throws NoSuchFieldException, IllegalAccessException {
            Board anotherGameBoard = new Board(10,11,3,GameMode.gameMods.get("classic"));

            int width = anotherGameBoard.getWidth();
            int height = anotherGameBoard.getHeight();

            assertEquals(width, 10);
            assertEquals(height, 11);
            assertEquals(anotherGameBoard.score, 0);
            assertEquals(anotherGameBoard.snakes[0].snakePoints.size(), 3);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testWrongInitialization(){
            Board anotherGameBoard = new Board(0, 0, 0, GameMode.gameMods.get("classic"));
        }

        @Test
        public void testInitializationFruitGameModeSnakeAndfinished() throws NoSuchFieldException, IllegalAccessException {
            Board anotherGameBoard = new Board(10,11,3,GameMode.gameMods.get("classic"));
            GameMode gameMode = anotherGameBoard.getGameMode();

            assertNotNull(anotherGameBoard.fruit);
            assertFalse(anotherGameBoard.finished);
            assertNotNull(anotherGameBoard.snakes);
            assertEquals(gameMode, GameMode.gameMods.get("classic"));
        }

        @Test
        public void testSimpleSingleMove() {
            Point prevPosition = snake.getHead();

            snake.move();

            assertEquals(new Point
                    (prevPosition.x + snake.getDirection().x, prevPosition.y + snake.getDirection().y),
                    snake.getHead());
        }

        @Test
        public void testSimpleDoubleMove() {
            Point prevPosition = snake.getHead();

            snake.move();
            snake.move();

            assertEquals(new Point
                            (prevPosition.x + 2 *snake.getDirection().x,
                                    prevPosition.y + 2 * snake.getDirection().y),
                    snake.getHead());
        }

        @Test
        public void testMoveWithChangingDirection() throws Exception {
            Point prevPoint = snake.getHead();
            Point startingDirection = snake.getDirection();

            snake.move();
            snake.setDirection(Direction.Right);
            snake.move();
            prevPoint.translate(startingDirection.x,startingDirection.y);
            prevPoint.translate(snake.getDirection().x, snake.getDirection().y);

            assertEquals(prevPoint, snake.getHead());
        }

        @Test
        public void testHitInWallGameOver() {
            gameBoard.snakes[0] = new Snake(0, 0, Direction.Left, 3, 0);
            Snake snake = gameBoard.snakes[0];

            snake.move();
            gameBoard.checkCollisions();

            assertEquals(true, gameBoard.finished);
        }

        @Test
        public void testGrowthSnakeAndCorrectEating() throws NoSuchFieldException, IllegalAccessException {
            int prevSnakeSize = snake.snakePoints.size();
            int prevScore = snake.score;
            Point prevFruitPos = gameBoard.getFruitPos();

            gameBoard.setFruitPos(new Point(snake.getHead().x + snake.getDirection().x,
                    snake.getHead().y + snake.getDirection().y));
            snake.move();
            gameBoard.checkCollisions();

            assertEquals(prevSnakeSize + 1, snake.snakePoints.size());
            assertNotEquals(prevScore, snake.score);
            assertNotEquals(prevFruitPos, gameBoard.getFruitPos());
        }

        @Test
        public void testIncreaseScore() throws NoSuchFieldException, IllegalAccessException {
            int prevScore = gameBoard.score;
            int bonusPoints = gameBoard.fruit.givenScore;

            gameBoard.setFruitPos(new Point(snake.getHead().x, snake.getHead().y));
            gameBoard.checkCollisions();

            assertEquals(prevScore+bonusPoints, gameBoard.score);
        }

        @Test
        public void testEatingItselfGameOver() {
            gameBoard.snakes[0] = new Snake(Direction.Left,0,
                    new Point(5,5),
                    new Point(5,4),
                    new Point(4,4),
                    new Point(4,5),
                    new Point(4,6));
            Snake snake = gameBoard.snakes[0];

            snake.move();
            gameBoard.checkCollisions();

            assertEquals(true, gameBoard.finished);
        }

        @Test
        public void testRandomRespawningFruit() throws NoSuchFieldException {
            Point prevFruitPos = gameBoard.getFruitPos();
            gameBoard.setFruitPos( new Point(snake.getHead().x+snake.getDirection().x,
                    snake.getHead().y + snake.getDirection().y));
            snake.move();
            gameBoard.checkCollisions();

            assertNotEquals(prevFruitPos, gameBoard.getFruitPos());
        }

        @Test
        public void test_reverse_direction_Down() throws Exception {
            gameBoard.snakes[0] = new Snake(5,5,Direction.Down,3,0);
            snake = gameBoard.snakes[0];
            Point originalDirection = snake.getDirection();

            gameBoard.checkCollisions();
            snake.setDirection(Direction.Up);

            assertEquals("The course should not change on the opposite", originalDirection, gameBoard.snakes[0].getDirection());
        }

        @Test
        public void test_reverse_direction_Up() throws Exception {
            gameBoard.snakes[0] = new Snake(5,5,Direction.Up,3,0);
            snake = gameBoard.snakes[0];
            Point originalDirection = snake.getDirection();

            gameBoard.checkCollisions();
            snake.setDirection(Direction.Down);

            assertEquals("The course should not change on the opposite", originalDirection, gameBoard.snakes[0].getDirection());
        }

        @Test
        public void test_reverse_direction_Right() throws Exception {
            gameBoard.snakes[0] = new Snake(5,5,Direction.Right,3,0);
            snake = gameBoard.snakes[0];
            Point originalDirection = snake.getDirection();

            gameBoard.checkCollisions();
            snake.setDirection(Direction.Left);

            assertEquals("The course should not change on the opposite", originalDirection, gameBoard.snakes[0].getDirection());
        }

        @Test
        public void test_reverse_direction_Left() throws Exception {
            gameBoard.snakes[0] = new Snake(5,5,Direction.Left,3,0);
            snake = gameBoard.snakes[0];
            Point originalDirection = snake.getDirection();

            gameBoard.checkCollisions();
            snake.setDirection(Direction.Right);

            assertEquals("The course should not change on the opposite", originalDirection, gameBoard.snakes[0].getDirection());
        }
    }

    public static class ModelTestsInfinitive {
        private Board gameBoard;
        private Snake snake;

        @Before
        public void testData() {
            GameMode.loadGameMods();
            gameBoard = new Board(20, 20, 6, GameMode.gameMods.get("infinite"));
            snake = gameBoard.snakes[0];
        }

        @Test
        public void testEatingItselfInInfinite() throws Exception {
            gameBoard.snakes[0] = new Snake(10, 10, Direction.Down, 6,0);
            snake = gameBoard.snakes[0];
            snake.move();
            snake.setDirection(Direction.Right);
            snake.move();
            snake.setDirection(Direction.Up);
            snake.move();
            snake.setDirection(Direction.Left);
            snake.move();
            gameBoard.checkCollisions();

            assertEquals(false, gameBoard.finished);
            assertEquals(4, snake.snakePoints.size());
        }

        @Test
        public void testEatingFruitInInfinite() throws NoSuchFieldException, IllegalAccessException {
            int prevScore = gameBoard.score;
            Point prevFruitPos = gameBoard.getFruitPos();

            gameBoard.setFruitPos(new Point(snake.getHead().x + snake.getDirection().x,
                                                snake.getHead().y + snake.getDirection().y));
            snake.move();
            gameBoard.checkCollisions();

            assertNotEquals(prevScore, gameBoard.score);
            assertNotEquals(prevFruitPos, gameBoard.getFruitPos());
        }

        @Test
        public void testHitInWallInInfiniteLeadToSnakeRespawn() {
            gameBoard.snakes[0] = new Snake(gameBoard.getWidth() - 1,gameBoard.getHeight() - 1, Direction.Right, 7,0);
            snake = gameBoard.snakes[0];
            Point prevHead = snake.getHead();
            int prevSnakeSize = snake.snakePoints.size();
            snake.move();
            gameBoard.checkCollisions();
            int currentSnakeSize = gameBoard.snakes[0].snakePoints.size();

            assertNotEquals(prevHead, gameBoard.snakes[0].getHead());
            assertTrue(currentSnakeSize < prevSnakeSize);
            assertEquals(false, gameBoard.finished);
        }
     }

    public static class ModelTestsMultiplayerInfinite {
        private Board gameBoard;
        private Snake snake1;
        private Snake snake2;

        @Before
         public void testData(){
            GameMode.loadGameMods();
            gameBoard = new Board(20,20, 3, GameMode.gameMods.get("twosnakesinf"));
            snake1 = gameBoard.snakes[0];
            snake2 = gameBoard.snakes[1];
        }

        @Test
        public void testSnakesSpawnNotInOnePlace(){
             for(int i = 0; i< snake1.getSize(); i++){
                 for(int j = 0; j< snake2.getSize();j++){
                     assertNotEquals(snake1.snakePoints.get(i),snake2.snakePoints.get(j));
                 }
             }
        }

        @Test
        public void testSecondSnakeEatingFruit() throws NoSuchFieldException, IllegalAccessException {
            gameBoard.setFruitPos(new Point(snake2.getHead().x + snake2.getDirection().x,
                    snake2.getHead().y + snake2.getDirection().y));
            Point oldFruit = gameBoard.getFruitPos();
            int prevSnake2size = snake2.snakePoints.size();
            int prevSnake1size = snake1.snakePoints.size();

            snake2.move();
            gameBoard.checkCollisions();

            assertNotEquals(oldFruit, gameBoard.getFruitPos() );
            assertEquals(gameBoard.snakes[0].snakePoints.size(), prevSnake1size);
            assertNotEquals(prevSnake2size, gameBoard.snakes[1].snakePoints.size());
        }

        @Test
        public void testOnlySecondSnakeChangeDirection() throws Exception {
            Point prevSnake1Dir = snake1.getDirection();
            Point prevSnake2Dir = snake2.getDirection();
            snake2.setDirection(Direction.Right);

            assertEquals(snake1.getDirection(), prevSnake1Dir);
            assertNotEquals(snake2.getDirection(), prevSnake2Dir);
        }
    }
}


