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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

public class ServerMain extends Observable {

	private ArrayList<String> users;
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
		users = new ArrayList<String>();
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
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(8000);
		while (true) {
			Socket clientSocket = serverSock.accept();
			ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
			String IP = clientSocket.getRemoteSocketAddress().toString();
			IP = IP.substring(1);
			Thread t = new Thread(new ClientHandler(clientSocket, writer, IP));
			t.start();
			output.append("got a connection to: " + IP + "\n");
		}
	}

	class ClientHandler implements Runnable {
		private BufferedReader reader;
		private ClientObserver writer;
		private String IP;

		public ClientHandler(Socket clientSocket, ClientObserver w, String address) throws IOException {
			IP = address;
			writer = w;
			Socket sock = clientSocket;
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		}

		public void run() {
			String message;
			String user;
			try {
				user = reader.readLine();
				while (users.contains(user)) {
					writer.println("F");
					writer.flush();
					user = reader.readLine();
				}
				users.add(user);
				writer.println("K");
				writer.flush();
				output.append(user + " has connected from: " + IP + "\n");
				user += ": ";
				addObserver(writer);
				while ((message = reader.readLine()) != null) {
					output.append(user + message + "\n");
					setChanged();
					notifyObservers(user + message);
				}
			} catch (IOException e) {

			}
		}
	}
}
