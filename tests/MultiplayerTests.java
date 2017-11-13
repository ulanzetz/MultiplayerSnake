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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.InetSocketAddress;

import static org.junit.Assert.*;


@RunWith(Enclosed.class)
public class MultiplayerTests {
    public static class TwoSnakesInfTests{
        private Server server;
        private Client client1;
        private Client client2;
        private DatagramSocket socket;
        private DatagramPacket packet;

        @Before
        public void testData() throws Exception {
            Server.main(new String[]{"9866", "20", "20", "40", "twosnakesinf"});
            client1 = new Client("127.0.0.1","9866");
            client2 = new Client("127.0.0.1", "9867");
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
        public void testSecondSnakeNotMove() throws IOException {
            Snake[] clientSnakes = client1.getGame().board.snakes;
            clientSnakes[0].setDirection(Direction.Right);
            clientSnakes[1].setDirection(Direction.Right);
            client1.infoChange();
            Snake[] serverSnakes = Server.getGame().board.snakes;

            assertEquals(serverSnakes[0].getDirection(), Direction.Right);
            assertEquals(serverSnakes[1].getDirection(), Direction.Down);
        }

        @Test
        public void testChangeDirectionOnBothSnakes() throws IOException {
            Point prevHeadPos = client1.getGame().board.snakes[0].getHead();
            Point dir = client1.getGame().board.snakes[0].getDirection();
            client1.infoChange();
//            client1.infoChange();
            //123
            assertEquals(new Point(prevHeadPos.x+2*dir.x,prevHeadPos.y + 2*dir.y),
                            client1.getGame().board.snakes[0].getHead());

        }

        @Test
        public void testPacketLostTimeOut(){

        }

        @Test
        public void testWrongPacket(){}
    }
}
