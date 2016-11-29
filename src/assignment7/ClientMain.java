/* CHAT ROOM ClientMain.java
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

public class ClientMain {
	private BufferedReader reader;
	private PrintWriter writer;
	private JTextArea incoming;
	private JTextField outgoing;
	private JFrame frame;
	private JFrame logframe;
	private String userName;
	private String lastMsg;
	private Socket sock;
	private JLabel labelE;
	private JTabbedPane tabs;
	private HashMap<String, JComboBox<String>> boxs = new HashMap<String, JComboBox<String>>();
	private JLabel friendErr;
	private ArrayList<String> friends = new ArrayList<String>();
	private ArrayList<String> pchats = new ArrayList<String>();
	private HashMap<String, JTextArea> privateChats = new HashMap<String, JTextArea>();
	private ArrayList<String> gchats = new ArrayList<String>();
	private HashMap<String, JTextArea> groupChats = new HashMap<String, JTextArea>();

	public static void main(String[] args) {
		try {
			@SuppressWarnings("unused")
			ClientMain client = new ClientMain();
		} catch (Exception e) {

		}
	}

	public ClientMain() throws Exception {
		ChatClient chat = new ChatClient();
		chat.initView();
		chat.setUpNetworking();
		chat.frinit();
	}

	public class ChatClient {
		private void setUpNetworking() throws Exception {
			InetAddress ip = InetAddress.getByName("10.164.0.152");
			sock = new Socket(ip, 8000);
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());
			Thread readerThread = new Thread(new IncomingReader());
			readerThread.start();
		}

		class IncomingReader implements Runnable {
			public void run() {
				String message;
				String chk;
				try {
					do {
						chk = reader.readLine();
						labelE.setText(chk);
					} while (!chk.equals("K"));
					userName = lastMsg;
					logframe.setVisible(false);
					frame.setLocation(logframe.getX(), logframe.getY());
					logframe.dispose();
					frame.setVisible(true);
					frame.setTitle(userName + "'s Chat Client");
					while (!(message = reader.readLine()).equals("dumped")) {
						incoming.append(message + "\n");
					}
				} catch (IOException e1) {
				}
				try {
					while ((message = reader.readLine()) != null) {
						if (message.equals("exit")) {
							System.exit(1);
						} else if (message.equals("private")) {
							message = reader.readLine();
							if (!friends.contains(message)) {
								friends.add(message);
								for(HashMap.Entry<String, JComboBox<String>> box : boxs.entrySet()){
									box.getValue().addItem(message);
								}
								newChat(message);
							}
							JTextArea textbox = privateChats.get(message);
							message = reader.readLine();
							textbox.append(message + "\n");
						} else if (message.equals("strt_private")) {
							message = reader.readLine();
							if (!friends.contains(message)) {
								friends.add(message);
								for(HashMap.Entry<String, JComboBox<String>> box : boxs.entrySet()){
									box.getValue().addItem(message);
								}
							}
							if (!pchats.contains(message)) {
								newChat(message);
							}
						} 
						else if (message.equals("new_group")){
							message = reader.readLine();
							newGroup(message);
						}
						else if (message.equals("group")){
							message = reader.readLine();
							if(!gchats.contains(message)){
								gchats.add(message);
								newGroup(message);
							}
							JTextArea textbox = groupChats.get(message);
							message = reader.readLine();
							textbox.append(message + "\n");
						}
						else if (message.equals("msg")) {
							message = reader.readLine();
							if (message.contains(userName)) {
								incoming.append(message + "\n");
							} else {
								incoming.append(message + "\n");
							}
						} else if (message.equals("invite")) {
							message = reader.readLine();
							if (message.equals("F")) {
								message = reader.readLine();
								friendErr.setText(message);
							} else if (message.equals("accept")) {
								message = reader.readLine();
								for(HashMap.Entry<String, JComboBox<String>> box : boxs.entrySet()){
									box.getValue().addItem(message);
								}
								friends.add(message);
							} else if (message.equals("recieved")) {
								message = reader.readLine();
								if (friends.contains(message)) {
									writer.println("accept");
									writer.flush();
									writer.println(message);
									writer.flush();
								} else {
									inviteR(message);
								}
							}
						}
					}
				} catch (IOException e) {
				}
			}

		}
		
		public void newGroup(String group) {
			gchats.add(group);
			JPanel pchat = new JPanel();
			JTextArea chat = new JTextArea(15, 50);
			chat.setName(group);
			chat.setLineWrap(true);
			chat.setWrapStyleWord(true);
			chat.setEditable(false);
			JButton sendButton = new JButton("Send");
			JButton addMem = new JButton("Add User");
			JScrollPane qScroller = new JScrollPane(chat);
			JComboBox<String> box = new JComboBox<String>();
			boxs.put(group, box);
			JComboBox<String> ogbox = boxs.get("account");
			for(int i = 0; i < ogbox.getItemCount(); i++){
				box.addItem(ogbox.getItemAt(i));
			}
			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			JTextField field = new JTextField(20);
			pchat.add(qScroller);
			pchat.add(field);
			pchat.add(sendButton);
			pchat.add(box);
			pchat.add(addMem);
			field.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendButton.doClick();
				}
			});
			addMem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writer.println("add_mem");
					writer.flush();
					writer.println(group);
					writer.flush();
					writer.println(box.getSelectedItem());
					writer.flush();
				}
			});
			sendButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writer.println("group");
					writer.flush();
					writer.println(group);
					writer.flush();
					writer.println(field.getText());
					writer.flush();
					field.setText("");
					field.requestFocus();
				}
			});
			tabs.add(group, pchat);
			groupChats.put(group, chat);
		}
		
		public void newChat(String person) {
			pchats.add(person);
			JPanel pchat = new JPanel();
			JTextArea chat = new JTextArea(15, 50);
			chat.setName(person);
			chat.setLineWrap(true);
			chat.setWrapStyleWord(true);
			chat.setEditable(false);
			JButton sendButton = new JButton("Send");
			JScrollPane qScroller = new JScrollPane(chat);
			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			JTextField field = new JTextField(20);
			pchat.add(qScroller);
			pchat.add(field);
			pchat.add(sendButton);
			field.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendButton.doClick();
				}
			});
			sendButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writer.println("private");
					writer.flush();
					writer.println(chat.getName());
					writer.flush();
					writer.println(field.getText());
					writer.flush();
					field.setText("");
					field.requestFocus();
				}
			});
			tabs.add(person, pchat);
			privateChats.put(person, chat);
		}

		public void inviteR(String sender) {
			JFrame YN = new JFrame();
			JPanel pane = new JPanel();
			JLabel send = new JLabel(sender + " sent you a friend request.");
			JButton Y = new JButton("accept");
			JButton N = new JButton("decline");
			pane.add(send);
			pane.add(Y);
			pane.add(N);
			YN.getContentPane().add(BorderLayout.CENTER, pane);
			YN.setSize(300, 200);
			YN.setVisible(true);
			YN.setLocation(frame.getX(), frame.getY());
			Y.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(HashMap.Entry<String, JComboBox<String>> box : boxs.entrySet()){
						box.getValue().addItem(sender);
					}
					friends.add(sender);
					writer.println("accept");
					writer.flush();
					writer.println(sender);
					writer.flush();
					YN.setVisible(false);
					YN.dispose();
				}
			});
			N.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writer.println("decline");
					writer.flush();
					writer.println(sender);
					writer.flush();
					YN.setVisible(false);
					YN.dispose();
				}
			});
			YN.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					writer.println("decline");
					writer.flush();
					writer.println(sender);
					writer.flush();
				}
			});
		}

		private void initView() {
			frame = new JFrame("Chat Client");
			logframe = new JFrame("Login");
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					writer.println("exit");
					writer.flush();
				}
			});
			logframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			JPanel mainPanel = new JPanel();
			JPanel logPanel = new JPanel();
			GroupLayout layout = new GroupLayout(logPanel);
			logPanel.setLayout(layout);
			incoming = new JTextArea(15, 50);
			incoming.setLineWrap(true);
			incoming.setWrapStyleWord(true);
			incoming.setEditable(false);
			outgoing = new JTextField(20);
			JButton sendButton = new JButton("Send");
			sendButton.addActionListener(new SendButtonListener());
			outgoing.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					lastMsg = outgoing.getText();
					writer.println("msg");
					writer.flush();
					writer.println(lastMsg);
					writer.flush();
					outgoing.setText("");
					outgoing.requestFocus();
				}
			});
			JScrollPane qScroller = new JScrollPane(incoming);
			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			JTextField username = new JTextField(20);
			JPasswordField password = new JPasswordField(20);
			username.setMaximumSize(username.getPreferredSize());
			password.setMaximumSize(password.getPreferredSize());
			JLabel labelU = new JLabel("Username:");
			JLabel labelP = new JLabel("Password:");
			labelE = new JLabel("");
			JButton logButton = new JButton("Login/Register");
			username.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					logButton.doClick();
				}
			});
			password.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					logButton.doClick();
				}
			});
			logButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					lastMsg = username.getText();
					writer.println(lastMsg);
					writer.flush();
					writer.println(password.getPassword());
					writer.flush();
				}
			});
			mainPanel.add(qScroller);
			mainPanel.add(outgoing);
			mainPanel.add(sendButton);
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(labelU)
							.addComponent(labelP))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(username)
							.addComponent(password).addComponent(logButton).addComponent(labelE)));
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelU)
							.addComponent(username))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelP)
							.addComponent(password))
					.addComponent(logButton).addComponent(labelE));
			tabs = new JTabbedPane();
			tabs.addTab("Lobby", mainPanel);
			frame.getContentPane().add(BorderLayout.CENTER, tabs);
			frame.setSize(650, 500);
			frame.setVisible(false);
			logframe.getContentPane().add(BorderLayout.CENTER, logPanel);
			logframe.setSize(650, 500);
			logframe.setVisible(true);
		}

		public void frinit() {
			JPanel mainPanel = new JPanel();
			JComboBox<String> box = new JComboBox<String>();
			boxs.put("account", box);
			JButton chat = new JButton("Start Chat");
			JTextField friendB = new JTextField(20);
			JPasswordField newPass = new JPasswordField(20);
			JButton changePass = new JButton("Change Password");
			JButton invite = new JButton("Add Friend");
			JButton newChat = new JButton("New Groupchat");
			friendErr = new JLabel("");
			mainPanel.add(box);
			mainPanel.add(chat);
			mainPanel.add(friendB);
			mainPanel.add(invite);
			mainPanel.add(friendErr);
			mainPanel.add(newPass);
			mainPanel.add(changePass);
			mainPanel.add(newChat);
			tabs.addTab("Account", mainPanel);
			friendB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					invite.doClick();
				}
			});
			newChat.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writer.println("strt_group");
					writer.flush();
				}
			});
			changePass.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writer.println("password");
					writer.flush();
					writer.println(newPass.getPassword());
					writer.flush();
					newPass.setText("");
				}
			});
			invite.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (friends.contains(friendB.getText())) {
						friendErr.setText("Already Friends");
					} else {
						writer.println("invite");
						writer.flush();
						writer.println(friendB.getText());
						writer.flush();
						friendErr.setText("Invite Sent");
					}
					friendB.setText("");
				}
			});
			chat.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox<String> box = boxs.get("account");
					if (box.getSelectedItem() == null || box.getSelectedItem().equals("")) {
						friendErr.setText("Select A Friend");
					} else if (pchats.contains(box.getSelectedItem())) {
						friendErr.setText("Chat Already Started");
					} else {
						writer.println("strt_private");
						writer.flush();
						writer.println(box.getSelectedItem());
						writer.flush();
					}
				}
			});
		}

		class SendButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent ev) {
				lastMsg = outgoing.getText();
				writer.println("msg");
				writer.flush();
				writer.println(lastMsg);
				writer.flush();
				outgoing.setText("");
				outgoing.requestFocus();
			}
		}

	}
}
