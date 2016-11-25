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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
			@SuppressWarnings("resource")
			Socket sock = new Socket("127.0.0.1", 8000);
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
					} while (chk.equals("F"));
					logframe.setVisible(false);
					frame.setLocation(logframe.getX(), logframe.getY());
					frame.setVisible(true);
				} catch (IOException e1) {
				}
				try {
					while ((message = reader.readLine()) != null) {
						incoming.append(message + "\n");
					}
				} catch (IOException e) {
				}
			}

		}

		private void initView() {
			frame = new JFrame("Chat Client");
			logframe = new JFrame("Login");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			JPanel mainPanel = new JPanel();
			JPanel logPanel = new JPanel();
			incoming = new JTextArea(15, 50);
			incoming.setLineWrap(true);
			incoming.setWrapStyleWord(true);
			incoming.setEditable(false);
			outgoing = new JTextField(20);
			JButton sendButton = new JButton("Send");
			sendButton.addActionListener(new SendButtonListener());
			outgoing.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writer.println(outgoing.getText());
					writer.flush();
					outgoing.setText("");
					outgoing.requestFocus();
				}
			});
			JScrollPane qScroller = new JScrollPane(incoming);
			qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			JTextField username = new JTextField(20);
			JButton logButton = new JButton("Login");
			logButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writer.println(username.getText());
					writer.flush();
				}
			});
			mainPanel.add(qScroller);
			mainPanel.add(outgoing);
			mainPanel.add(sendButton);
			logPanel.add(username);
			logPanel.add(logButton);
			frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
			frame.setSize(650, 500);
			frame.setVisible(false);
			logframe.getContentPane().add(BorderLayout.CENTER, logPanel);
			logframe.setSize(650, 500);
			logframe.setVisible(true);
		}

		class SendButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent ev) {
				writer.println(outgoing.getText());
				writer.flush();
				outgoing.setText("");
				outgoing.requestFocus();
			}
		}

	}
}
