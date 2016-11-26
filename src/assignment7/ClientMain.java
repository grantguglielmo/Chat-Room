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
import java.net.Socket;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
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
	}

	public class ChatClient {
		private void setUpNetworking() throws Exception {
			sock = new Socket("127.0.0.1", 8000);
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
					frame.setVisible(true);
				} catch (IOException e1) {
				}
				try {
					while ((message = reader.readLine()) != null) {
						if (message.equals("exit")) {
							System.exit(1);
						} else if (message.equals("msg")) {
							message = reader.readLine();
							if (message.contains(userName)) {
								incoming.append(message + "\n");
							} else {
								incoming.append(message + "\n");
							}
						}
					}
				} catch (IOException e) {
				}
			}

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
			JButton logButton = new JButton("Login");
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

			frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
			frame.setSize(650, 500);
			frame.setVisible(false);
			logframe.getContentPane().add(BorderLayout.CENTER, logPanel);
			logframe.setSize(650, 500);
			logframe.setVisible(true);
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
