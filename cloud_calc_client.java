package calc_c;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class cloud_calc_client {
	
	// Method for determining response type from server
	public static String resHandle(int errNo) {
		String respon = "";
		if (errNo == 401) {
			respon = "Error - Variable type error";
		} else if (errNo == 402) {
			respon = "Error - too many arguments";
		} else if (errNo == 403) {
			respon = "Error - divided by zero";
		} else if (errNo == 404) {
			respon = "Error - too little arguments";
		} else if (errNo == 405) {
			respon = "Error - Undefined error";
		}
		return respon;
	}

	// Methods for defining request forms
	public static String reqType(String oper) {
		String request = "";
		if (oper == "+") {
			request = "ADD";
		} else if (oper == "-") {
			request = "SUB";
		} else if (oper == "*") {
			request = "MUL";
		} else if (oper == "/") {
			request = "DIV";
		}
		return request;
	}

	public static void main(String[] args) {
		BufferedReader in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		Socket socket = null;
		Scanner scanner = new Scanner(System.in);
		// Default value in case the file does not exist
		String ipAddress = "localhost";
		String portNum = "9999";
		try {
			// Text file that contains the information about server IP address and port number
			String fileName = "src/TextFile/server_info.txt";
			FileReader fr = new FileReader(fileName);
			br = new BufferedReader(fr);
			// Assigning an IP address and a port number by reading two lines of a text file
			ipAddress = br.readLine();
			portNum = br.readLine();
			// Convert the data type of "portNum" to integer
			socket = new Socket(ipAddress, Integer.parseInt(portNum)); 
			fr.close();
			br.close();

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			while (true) {
				System.out.print("Formula (enter in blank spaces, e.g. 24 + 42)>>"); // Input Guidelines
				String outputMessage = scanner.nextLine(); // Read formula from keyboard
				StringTokenizer st = new StringTokenizer(outputMessage, " ");
				if (st.countTokens() == 3) {
					String[] array = outputMessage.split(" ");
					String opercd = array[1];
					// Formatting messages sent to the server differently depending on the entered arithmetic operation
					switch (opercd) {
					case "+":
						outputMessage = reqType("+") + " " + array[0] + " " + array[2];
						break;
					case "-":
						outputMessage = reqType("-") + " " + array[0] + " " + array[2];
						break;
					case "*":
						outputMessage = reqType("*") + " " + array[0] + " " + array[2];
						break;
					case "/":
						outputMessage = reqType("/") + " " + array[0] + " " + array[2];
						break;
					default:
						break;
					}
				} else if (outputMessage.equalsIgnoreCase("bye")) {
					out.write(outputMessage + "\n"); // Send "bye" string
					out.flush();
					break; // If the user enters "bye", send it to the server and terminate the connection
				}
				out.write(outputMessage + "\n"); // Send formula string read from keyboard
				out.flush();
				String inputMessage = in.readLine(); // Receive calculation results from the server
				String sub_inputM[] = inputMessage.split(" ");

				// Use the resHandle method to determine the meaning of the response sent by the server
				switch (inputMessage) {
				case "401 variable type":
					inputMessage = resHandle(401);
					break;
				case "402 too many":
					inputMessage = resHandle(402);
					break;
				case "403 div zero":
					inputMessage = resHandle(403);
					break;
				case "404 too little":
					inputMessage = resHandle(404);
					break;
				case "405 undef err":
					inputMessage = resHandle(405);
					break;
				default:
					inputMessage = sub_inputM[2];
				}

				System.out.println("Result: " + inputMessage);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				scanner.close();
				if (socket != null)
					socket.close(); // Close the socket of the client
			} catch (IOException e) {
				System.out.println("An error occurred while chatting with the server.");
			}
		}
	}
}
