package com.snakegame.tests;

import com.snakegame.client.Client;
import com.snakegame.model.Board;
import com.snakegame.model.Direction;
import com.snakegame.model.Snake;
import com.snakegame.server.Server;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.awt.*;
import java.io.IOException;
import java.net.*;

import static org.junit.Assert.*;


@RunWith(Enclosed.class)
public class MultiplayerTests {
    public static class TwoSnakesInfTests{
        private Server server;
        private Client client1;
        private Client client2;
        private DatagramSocket socket;
        private DatagramPacket packet;
        private InetAddress ip;

        @Before
        public void testData() throws Exception {
            Server.main(new String[]{"9866", "20", "20", "40", "twosnakesinf"});
            client1 = new Client("127.0.0.1","9866");
            client2 = new Client("127.0.0.1", "9867");
            socket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName("127.0.0.1");
        }

        @Test
        public void testThirdConnection() throws Exception {
            InetAddress ip = InetAddress.getByName("127.0.0.1");
            socket = new DatagramSocket( 13000);
            packet = new DatagramPacket("con".getBytes(), 3, ip, 9866);

            socket.send(packet);
            byte[] receiveData = new byte[30];
            packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet);

            assertTrue(new String(receiveData).startsWith("not"));
        }

        @Test
        public void testSecondSnakeNotMove() throws Exception {
            Snake[] clientSnakes = (Snake[]) client1.getGame().board.snakes.toArray();
            clientSnakes[0].setDirection(Direction.Right);
            clientSnakes[1].setDirection(Direction.Right);
            client1.infoChange();
            Snake[] serverSnakes = (Snake[]) Server.game.board.snakes.toArray();

            assertEquals(serverSnakes[0].getDirection(), Direction.Right);
            assertEquals(serverSnakes[1].getDirection(), Direction.Down);
        }


        @Test(expected = Exception.class)
        public void testWrongDirectionFailure() throws Exception {
            client1.getGame().board.snakes.get(0).setDirection(new Point(-100, -100));
            client1.infoChange();
            socket.setSoTimeout(1000);
            byte[] receiveData = new byte[30];
            packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet);
        }

        @Test(expected = SocketTimeoutException.class)
        public void testServerDownGameOver() throws IOException, InterruptedException {
            Server.close();
            client1.infoChange();
            client1.infoChange();
        }

        @Test(expected = SocketTimeoutException.class)
        public void testWrongPacket() throws IOException {
            InetAddress ip = InetAddress.getByName("127.0.0.1");
            socket = new DatagramSocket();
            packet = new DatagramPacket("abcd".getBytes(), 4, ip, 9866);
            socket.send(packet);
            socket.setSoTimeout(1000);
            socket.receive(packet);
        }
    }
}
