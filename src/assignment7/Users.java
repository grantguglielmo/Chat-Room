/* CHAT ROOM User.java
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

import java.io.*;

public class Users {
	
	public static Object lock = new Object();

	public static boolean checkStatus(String user) {
		String line;
		try {
			FileReader fileReader = new FileReader("Users.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				if (line.equals(user)) {
					line = bufferedReader.readLine();
					line = bufferedReader.readLine();
					if (line.equals("OFF")) {
						bufferedReader.close();
						return false;
					}
					bufferedReader.close();
					return true;
				}
				line = bufferedReader.readLine();
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean check(String user) {
		String line;
		try {
			FileReader fileReader = new FileReader("Users.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				if (line.equals(user)) {
					line = bufferedReader.readLine();
					bufferedReader.close();
					return true;
				}
				line = bufferedReader.readLine();
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean checkP(String user, String pass) {
		String line;
		try {
			FileReader fileReader = new FileReader("Users.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				if (line.equals(user)) {
					line = bufferedReader.readLine();
					if (line.equals(pass)) {
						bufferedReader.close();
						return true;
					}
				} else {
					line = bufferedReader.readLine();
				}
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static void mark(String user, String stat) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("Users.txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("Users_temp.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equals(user)) {
					bw.write(line + "\n");
					line = br.readLine();
					bw.write(line + "\n");
					line = br.readLine();
					bw.write(stat + "\n");
				} else {
					bw.write(line + "\n");
					line = br.readLine();
					bw.write(line + "\n");
					line = br.readLine();
					bw.write(line + "\n");
				}
			}
			br.close();
			bw.close();
			File oldFile = new File("Users.txt");
			oldFile.delete();
			File newFile = new File("Users_temp.txt");
			newFile.renameTo(oldFile);
		} catch (Exception e) {

		}
	}
	
	public static void pass(String user, String password) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("Users.txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("Users_temp.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equals(user)) {
					bw.write(line + "\n");
					line = br.readLine();
					bw.write(password + "\n");
					line = br.readLine();
					bw.write(line + "\n");
				} else {
					bw.write(line + "\n");
					line = br.readLine();
					bw.write(line + "\n");
					line = br.readLine();
					bw.write(line + "\n");
				}
			}
			br.close();
			bw.close();
			File oldFile = new File("Users.txt");
			oldFile.delete();
			File newFile = new File("Users_temp.txt");
			newFile.renameTo(oldFile);
		} catch (Exception e) {

		}
	}

	public static void add(String user, String pass) {
		if (check(user)) {
			mark(user, "ON");
		} else {
			try {
				FileWriter file = new FileWriter("Users.txt", true);
				BufferedWriter bw = new BufferedWriter(file);
				bw.write(user);
				bw.newLine();
				bw.write(pass);
				bw.newLine();
				bw.write("ON");
				bw.newLine();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
