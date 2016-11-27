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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class ServerMain extends Observable {

	private JTextArea output;
	private ArrayList<String> online = new ArrayList<String>();
	private HashMap<String, ClientObserver> map = new HashMap<String, ClientObserver>();
	private HashMap<String, ArrayList<ClientObserver>> Gmap = new HashMap<String, ArrayList<ClientObserver>>();
	private HashMap<String, ArrayList<String>> pending = new HashMap<String, ArrayList<String>>();
	private BufferedWriter bw;
	private int groupnum = 1;

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
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				for (String u : online) {
					Users.mark(u, "OFF");
				}
				try {
					bw.close();
					BufferedWriter end = new BufferedWriter(new FileWriter("history.txt"));
					end.write("");
					end.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(1);
			}
		});
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
		try {
			bw = new BufferedWriter(new FileWriter("history.txt", true));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
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
		private Socket sock;

		public ClientHandler(Socket clientSocket, ClientObserver w, String address) throws IOException {
			IP = address;
			writer = w;
			sock = clientSocket;
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		}

		public void run() {
			String message;
			String user;
			String pass;

			try {
				user = reader.readLine();
				pass = reader.readLine();
				while (Users.check(user) || user.equals("")) {
					if (pass.equals(null) || pass.equals("") || Users.checkP(user, pass)) {
						if (!Users.checkStatus(user)) {
							break;
						} else {
							writer.println("User Already Online");
							writer.flush();
						}
					} else {
						writer.println("Incorrect Password");
						writer.flush();
					}
					user = reader.readLine();
					pass = reader.readLine();
				}
				Users.add(user, pass);
				online.add(user);
				map.put(user, writer);
				pending.put(user, new ArrayList<String>());
				writer.println("K");
				writer.flush();
				output.append(user + " has connected from: " + IP + "\n");
				addObserver(writer);
				String line;
				BufferedReader br = new BufferedReader(new FileReader("history.txt"));
				while ((line = br.readLine()) != null) {
					writer.println(line);
					writer.flush();
				}
				writer.println("dumped");
				writer.flush();
				br.close();
				while ((message = reader.readLine()) != null) {
					if (message.equals("exit")) {
						Users.mark(user, "OFF");
						online.remove(user);
						writer.println("exit");
						writer.flush();
						output.append(user + " has disconnected\n");
					} else if (message.equals("private")) {
						message = reader.readLine();
						String other = message;
						if (!Users.checkStatus(message)) {
							writer.println("private");
							writer.flush();
							writer.println(message);
							writer.flush();
							writer.println(message + " is Offline");
							writer.flush();
						}
						ClientObserver f = map.get(message);
						message = reader.readLine();
						writer.println("private");
						writer.flush();
						writer.println(other);
						writer.flush();
						writer.println(user + ": " + message);
						writer.flush();
						f.println("private");
						f.flush();
						f.println(user);
						f.flush();
						f.println(user + ": " + message);
						f.flush();
						output.append(user + " @ " + other + ": " + message + "\n");
					} else if (message.equals("strt_private")) {
						message = reader.readLine();
						if (!Users.checkStatus(message)) {
							writer.println("invite");
							writer.flush();
							writer.println("F");
							writer.flush();
							writer.println("User Offline");
							writer.flush();
						} else {
							ClientObserver f = map.get(message);
							writer.println("strt_private");
							writer.flush();
							writer.println(message);
							writer.flush();
							f.println("strt_private");
							f.flush();
							f.println(user);
							f.flush();
						}
					} else if (message.equals("strt_group")) {
						writer.println("new_group");
						writer.flush();
						writer.println("Group " + groupnum);
						writer.flush();
						ArrayList<ClientObserver> newG = new ArrayList<ClientObserver>();
						newG.add(writer);
						Gmap.put("Group " + groupnum, newG);
						groupnum++;
					} else if (message.equals("add_mem")) {
						message = reader.readLine();
						String g = message;
						ArrayList<ClientObserver> groupLis = Gmap.get(message);
						message = reader.readLine();
						if (!Users.checkStatus(message)) {
							writer.println("group");
							writer.flush();
							writer.println(g);
							writer.flush();
							writer.println(message + " is offline");
							writer.flush();
						} else if (groupLis.contains(map.get(message))) {
							writer.println("group");
							writer.flush();
							writer.println(g);
							writer.flush();
							writer.println(message + " is already in the group");
							writer.flush();
						} else {
							groupLis.add(map.get(message));
							map.get(message).println("new_group");
							map.get(message).flush();
							map.get(message).println(g);
							map.get(message).flush();
							for (ClientObserver o : groupLis) {
								o.println("group");
								o.flush();
								o.println(g);
								o.flush();
								o.println(message + " has joined");
								o.flush();
							}
						}
					} else if (message.equals("group")) {
						message = reader.readLine();
						String g = message;
						ArrayList<ClientObserver> groupLis = Gmap.get(g);
						message = reader.readLine();
						for (ClientObserver o : groupLis) {
							o.println("group");
							o.flush();
							o.println(g);
							o.flush();
							o.println(user + ": " + message);
							o.flush();
						}
						output.append(user + " @ " + g + ": " + message + "\n");
					} else if (message.equals("password")) {
						message = reader.readLine();
						Users.pass(user, message);
					} else if (message.equals("msg")) {

						setChanged();
						notifyObservers("msg");
						message = reader.readLine();
						output.append(user + " @ Lobby: " + message + "\n");
						setChanged();
						notifyObservers(user + ": " + message);
						bw.write(user + ": " + message + "\n");
						bw.flush();
					} else if (message.equals("accept")) {
						message = reader.readLine();
						ArrayList<String> p = pending.get(user);
						p.remove(message);
						p = pending.get(message);
						p.remove(user);
						ClientObserver f = map.get(message);
						f.println("invite");
						f.flush();
						f.println("accept");
						f.flush();
						f.println(user);
						f.flush();
						output.append(user + " <3 " + message + "\n");
					} else if (message.equals("decline")) {
						message = reader.readLine();
					} else if (message.equals("invite")) {
						message = reader.readLine();
						if (!Users.check(message)) {
							writer.println("invite");
							writer.flush();
							writer.println("F");
							writer.flush();
							writer.println("No Such User");
							writer.flush();
						} else if (!Users.checkStatus(message)) {
							writer.println("invite");
							writer.flush();
							writer.println("F");
							writer.flush();
							writer.println("User Offline");
							writer.flush();
						} else if (pending.get(user).contains(message)) {
							writer.println("invite");
							writer.flush();
							writer.println("F");
							writer.flush();
							writer.println("Invite Pending");
							writer.flush();
						} else if (message.equals(user)) {
							writer.println("invite");
							writer.flush();
							writer.println("F");
							writer.flush();
							writer.println("Are You Lonely");
							writer.flush();
						} else {
							ArrayList<String> p = pending.get(user);
							p.add(message);
							p = pending.get(message);
							p.add(user);
							ClientObserver f = map.get(message);
							f.println("invite");
							f.flush();
							f.println("recieved");
							f.flush();
							f.println(user);
							f.flush();
						}
					}
				}
			} catch (IOException e) {

			}
		}
	}
}
