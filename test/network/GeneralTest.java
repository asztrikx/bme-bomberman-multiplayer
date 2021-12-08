package test.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import di.DI;
import helper.Config;
import network.Connect;
import network.Listen;
import network.Network.Connection;

public class GeneralTest {

	@BeforeAll
	public static void beforeAll() {
		DI.init(Config.defaultConfigFileName);
	}

	/**
	 * @formatter:off
	 * Test Listen (L) and Connect (C) communication:
	 * C -> L: hello
	 * L -> C: hello
	 * L -> C: ping
	 * C -> L: pong
	 * L closes C
	 * Test should wait until C is closed
	 * @formatter:on
	 * @throws Exception
	 */
	@Test
	public void Send() throws Exception {
		final String helloMsg = "Hello!";
		final String pongMsg = "Pong!";
		final String pingMsg = "Ping?";

		Listen listen = new Listen();
		Connect connect = new Connect();

		listen.listen(Config.defaultPort, (connection) -> {
			try {
				// Receive hello, send hello
				String hello = (String) listen.receive(connection.objectInputStream);
				assertEquals(hello, helloMsg);
				listen.send(connection.objectOutputStream, helloMsg);

				// send ping
				listen.send(connection.objectOutputStream, pingMsg);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			return true;
		}, (final Connection connection, final Object object) -> {
			// receive pong
			String pong = (String) object;
			assertEquals(pong, pongMsg);

			// close client
			try {
				connect.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, (final Connection connection) -> {
		});

		connect.connect((final Connection connection) -> {
			try {
				// send hello
				connect.send(helloMsg);

				// receive hello
				String hello = (String) connect.receive();
				assertEquals(hello, helloMsg);
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			return true;
		}, (final Object object) -> {
			// receive ping
			String ping = (String) object;
			assertEquals(ping, pingMsg);

			// send pong
			try {
				connect.send(pongMsg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, Config.defaultIP, Config.defaultPort);

		connect.join();
		listen.close();
	}
}
