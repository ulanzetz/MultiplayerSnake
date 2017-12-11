package com.snakegame.tests;

import com.snakegame.client.Client;
import com.snakegame.model.Direction;
import com.snakegame.model.Snake;
import com.snakegame.server.Server;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;


@RunWith(Enclosed.class)
public class ClientServerTests {
    public static class MultiplayerInfSnakes {

        Server server = null;
        @Before
        public void testData() throws Exception {
            server =  new Server(new String[]{"9866", "20", "20", "40", "multiplayerinfsnakes"});
        }

        @Test(expected = IOException.class)
        public void testNotConnectTwoClientWithSameNick() throws Exception {
            new Client("127.0.0.1", "9866", "Player1");
            new Client("127.0.0.1", "9866", "Player1");
        }

        @Test
        public void testClientDisconnect() throws Exception {
            new Client("127.0.0.1", "9866", "Player1");
            Client client2 = new Client("127.0.0.1", "9866", "Player2");
            client2.close();
            Thread.sleep(1000);
            assertEquals(server.connectedPlayers, 1);
            assertEquals(server.game.board.snakes.size(), 1);
        }

        @Test
        public void testAllClientsDisconnect() throws Exception {
            Client client1 = new Client("127.0.0.1", "9866", "Player1");
            Client client2 = new Client("127.0.0.1", "9866", "Player2");
            client1.close();
            client2.close();
            Thread.sleep(1000);
            assertEquals(server.connectedPlayers, 0);
            assertEquals(server.game.board.snakes.size(), 0);
        }

        @Test
        public void testClientReconnect() throws Exception {
            Client client1 = new Client("127.0.0.1", "9866", "Player1");
            new Client("127.0.0.1", "9866", "Player2");
            client1.close();
            Thread.sleep(1000);
            new Client("127.0.0.1", "9866", "Player1");
            Thread.sleep(1000);
            assertEquals(server.connectedPlayers, 2);
            assertEquals(server.game.board.snakes.size(), 2);
        }

        @Test
        public void clientHavePlayersNames() throws Exception {
            String[] testNames = {"testname1", "TestName2", "*/Specific_Player_Name=3`"};
            Client client1 = new Client("127.0.0.1", "9866", testNames[0]);
            new Client("127.0.0.1", "9866", testNames[1]);
            new Client("127.0.0.1", "9866", testNames[2]);
            Thread.sleep(1000);
            client1.infoChange();
            assertArrayEquals(client1.playerNames, testNames);
        }

        @Test
        public void clientCantCheat() throws Exception {
            Client client1 = new Client("127.0.0.1", "9866", "Player1");
            Client client2 = new Client("127.0.0.1", "9866", "Player2");
            client1.infoChange();
            client2.infoChange();
            Thread.sleep(1000);
            ArrayList<Snake> clientSnakes = client1.game.board.snakes;
            clientSnakes.get(0).score = 1337;
            clientSnakes.get(1).score = -322;
            client1.infoChange();
            client2.infoChange();
            Thread.sleep(1000);
            ArrayList<Snake> serverSnakes = server.game.board.snakes;
            assertEquals(serverSnakes.get(0).score, clientSnakes.get(0).score);
            assertEquals(serverSnakes.get(1).score, clientSnakes.get(1).score);
        }

        @Test
        public void clientCantChangeNotItsDirection() throws Exception{
            Client client1 = new Client("127.0.0.1", "9866", "Player1");
            new Client("127.0.0.1", "9866", "Player2");
            client1.infoChange();
            Thread.sleep(1000);
            ArrayList<Snake> clientSnakes = client1.game.board.snakes;
            clientSnakes.get(0).setDirection(Direction.Right);
            client1.infoChange();
            Thread.sleep(1000);
            assertEquals(server.game.board.snakes.get(1).getDirection(), Direction.Down);
        }

        @Test
        public void testWrongPacket() throws IOException {
            InetAddress ip = InetAddress.getByName("127.0.0.1");
            DatagramSocket socket = new DatagramSocket();
            String message = "some wrong message";
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), ip, 9866);
            socket.send(packet);
            socket.setSoTimeout(1000);
            socket.receive(packet);
        }
    }

    public static class DbTests {
        Server server = null;
        @Before
        public void testData() throws Exception {
            server =  new Server(new String[]{"9866", "20", "20", "40", "multiplayerinfsnakes"});
        }

        @Test(expected = IOException.class)
        public void testCantLoginWithBadPasswd() throws IOException {
            new Client("127.0.0.1", "9866", "testPlayer", "pass");
            new Client("127.0.0.1", "9866", "testPlayer", "annotherpass");
        }

        @Test
        public void testSaveScore() throws IOException, InterruptedException {
            Client client = new Client("127.0.0.1", "9866", "testPlayer", "pass");
            int rand = new Random().nextInt(1000) + 1;
            server.game.board.snakes.get(0).score = rand;
            client.close();
            client = new Client("127.0.0.1", "9866", "testPlayer", "pass");
            client.infoChange();
            Thread.sleep(1000);
            assertEquals(client.game.board.snakes.get(0).score, rand);
        }

        @Test(expected = IOException.class)
        public void testCantSQLInjection() throws IOException {
            String name = "name' OR 1 = 1 --";
            new Client("127.0.0.1", "9866", name, "pass");
        }

        @Test
        public void testScoreSaveAfterServerReboot() throws Exception {
            Client client = new Client("127.0.0.1", "9866", "testPlayer", "pass");
            int rand = new Random().nextInt(1000) + 1;
            server.game.board.snakes.get(0).score = rand;
            client.close();
            server =  new Server(new String[]{"9866", "20", "20", "40", "multiplayerinfsnakes"});
            client = new Client("127.0.0.1", "9866", "testPlayer", "pass");
            assertEquals(client.game.board.snakes.get(0).score, rand);
        }

        @Test
        public void testFourPlayers() throws IOException {
            for(int i = 0; i != 4; ++i) {
                Client client = new Client("127.0.0.1", "9866","testPlayer" + i, "pass" + i);
                server.game.board.snakes.get(0).score = 100 * i;
                client.close();
            }
            for(int i = 0; i != 4; ++i) {
                Client client = new Client("127.0.0.1", "9866","testPlayer" + i, "pass" + i);
                assertEquals(server.game.board.snakes.get(0).score, 100 * i);
                client.close();
            }
        }

        @Test(expected = IOException.class)
        public void testShortPassword() throws IOException {
            new Client("127.0.0.1", "9886", "testPlayer", "12");
        }


    }
}