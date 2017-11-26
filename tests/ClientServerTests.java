package com.snakegame.tests;

import com.snakegame.client.Client;
import com.snakegame.server.Server;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;

import java.io.IOException;

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

        }

    }
}
