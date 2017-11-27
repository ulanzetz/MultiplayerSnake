package com.snakegame.tests;

import com.snakegame.client.Client;
import com.snakegame.model.Direction;
import com.snakegame.model.Snake;
import com.snakegame.server.Server;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;


@RunWith(Enclosed.class)
public class ClientServerTests {
    public static class MultiplayerInfSnakes {

        Server server = null;
        @Before
        public void testData() throws Exception {
            server =  new Server(new String[]{"9866", "20", "20", "40", "multiplayerinfsnakes"});
        }

        @Test(expected = NumberFormatException.class)
        public void testNotConnectTwoClientWithSameNick() throws Exception {
            Client client1 = new Client("127.0.0.1", "9866", "Player");
            Client client2 = new Client("127.0.0.1", "9866", "Player");
        }

        @Test
        public void testClientDisconnect() throws Exception {
            Client client1 = new Client("127.0.0.1", "9866", "Player1");
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
            Client client2 = new Client("127.0.0.1", "9866", "Player2");
            client1.close();
            Thread.sleep(1000);
            client1 = new Client("127.0.0.1", "9866", "Player1");
            Thread.sleep(1000);
            assertEquals(server.connectedPlayers, 2);
            assertEquals(server.game.board.snakes.size(), 2);
        }

        @Test
        public void clientHavePlayersNames() throws Exception {
            String[] testNames = {"testname1", "TestName2", "*/Specific_Player_Name=3`"};
            Client client1 = new Client("127.0.0.1", "9866", testNames[0]);
            Client client2 = new Client("127.0.0.1", "9866", testNames[1]);
            Client client3 = new Client("127.0.0.1", "9866", testNames[2]);
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
            Client client2 = new Client("127.0.0.1", "9866", "Player2");
            client1.infoChange();
            client2.infoChange();
            Thread.sleep(1000);
            ArrayList<Snake> clientSnakes = client1.game.board.snakes;
            clientSnakes.get(0).setDirection(Direction.Right);
            assertNotEquals(clientSnakes.get(1).getDirection(), clientSnakes.get(0).getDirection());
        }

        @Test
        public void incorrectClientPackets() throws Exception{

        }

    }
}