/* CHAT ROOM ServerMain.java
 * EE422C Project 7 submission by
 * Grant Guglielmo
 * gg25488
 * 16470
 * Mohit Joshi
 * msj696
 * 16475
 * Slip days used: 0
 * Fall 2016
 */
package assignment7;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

public class ServerMain {

	private ArrayList<PrintWriter> clientOutputStreams;
	private JTextArea output;

	public static void main(String[] args) {
		try {
			@SuppressWarnings("unused")
			ServerMain server = new ServerMain();
		} catch (IOException e) {

		}
	}

	public ServerMain() throws IOException {
		serverInit();
		setUpNetworking();
	}

	public void serverInit() {
		JFrame frame = new JFrame("Server");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JPanel mainPanel = new JPanel();
		output = new JTextArea(15, 50);
		output.setLineWrap(true);
		output.setWrapStyleWord(true);
		output.setEditable(false);
		JScrollPane qScroller = new JScrollPane(output);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		mainPanel.add(qScroller);
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.setSize(650, 500);
		frame.setVisible(true);
	}

	private void setUpNetworking() throws IOException {
		clientOutputStreams = new ArrayList<PrintWriter>();
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(8000);
		while (true) {
			Socket clientSocket = serverSock.accept();
			PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
			Thread t = new Thread(new ClientHandler(clientSocket));
			t.start();
			clientOutputStreams.add(writer);
			output.append("got a connection\n");
		}
	}

	class ClientHandler implements Runnable {
		private BufferedReader reader;

		public ClientHandler(Socket clientSocket) throws IOException {
			Socket sock = clientSocket;
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		}

		public void run() {
			String message;
			String user;
			try {
				user = reader.readLine();
				output.append(user + " has connected \n");
				user += ": ";
				while ((message = reader.readLine()) != null) {
					output.append(user + message + "\n");
					notifyClients(user + message);
				}
			} catch (IOException e) {

			}
		}
	}

	private void notifyClients(String message) {
		for (PrintWriter writer : clientOutputStreams) {
			writer.println(message);
			writer.flush();
		}
	}
}
