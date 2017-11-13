package com.snakegame.tests;

import com.snakegame.client.Client;
import com.snakegame.server.Server;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

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
            server = new Server();
//            client1 = new Client("127.0.0.1","9894");
//            client2 = new Client("127.0.0.2", "9895");
        }

        @Test
        public void testThirdConnection() throws Exception {
            server.main(new String[]{"9866", "20", "20", "40", "twosnakesinf"});
            client1 = new Client("127.0.0.1","9866");
            client2 = new Client("127.0.0.1", "9867");
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
        public void testFirstConnectionAndWait(){
            
        }

        @Test
        public void testSecondConnectionAndStart(){

        }

        @Test
        public void testSecondSnakeMove(){

        }
    }
}
