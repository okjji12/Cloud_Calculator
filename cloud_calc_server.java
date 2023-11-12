package calc_s;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class cloud_calc_server {
	public static String answer;   // static variable representing the result of the formula from client

	// Method defining response status for client
	public static String responseType(int resNo) {
		String response = "";
		if(resNo == 401) {
			response = "401 variable type";
		}else if(resNo == 402) {
			response = "402 too many";
		}else if (resNo == 403) {
			response = "403 div zero";
		}else if (resNo == 404) {
			response = "404 too little";
		}else if (resNo == 405) {
			response = "405 undef err";
		}else if(resNo == 200) {
			response = "200 ok " + answer;
		}
		return response;
	}
	
	// Method determining whether an expression contains non - integer elements , associated with Error 401
	public static boolean isInteger(String strValue) {
		try {
			Integer.parseInt(strValue);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	// Method of determining the response type for the client and returning it in string form
	public static String calc(String exp) {
		StringTokenizer st = new StringTokenizer(exp, " ");
		// Determine if the length of the formula is appropriate
		if (st.countTokens() != 3) {
			if (st.countTokens() < 3) {
				return "Error 404";
			} else if (st.countTokens() > 3) {
				return "Error 402";
			}
		}
		String[] array = exp.split(" ");
		// The part that determines if it causes Error 401
		if (isInteger(array[1]) == false || isInteger(array[2]) == false) {
			return "Error 401";
		}
		String res = "";
		// The process of calculating by splitting each element of the client's request format
		String opcode = st.nextToken();
		int op1 = Integer.parseInt(st.nextToken());
		int op2 = Integer.parseInt(st.nextToken());
		try {
			switch (opcode) {
			case "ADD":
				answer = Integer.toString(op1 + op2);
				break;
			case "SUB":
				answer = Integer.toString(op1 - op2);
				break;
			case "MUL":
				answer = Integer.toString(op1 * op2);
				break;
			case "DIV":
				answer = Integer.toString(op1 / op2);
				break;
			default:
				res = "Error 405";
				return res;
			}
		} catch (ArithmeticException e) {
			return "Error 403";
		}
		// Return the message that the calculation was successful unless there was a separate error
		res = "200 OK";
		return res;
	}

	// In a state of being able to receive requests from multiple clients by applying thread
	private static class Calc_Server_Thread implements Runnable {
		private Socket socket;

		Calc_Server_Thread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {

			BufferedReader in = null;
			BufferedWriter out = null;

			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				while (true) {
					String inputMessage = in.readLine();
					if (inputMessage.equalsIgnoreCase("bye")) {
						System.out.println("Client terminated the connection");
						System.exit(0); // Shut down when server get "bye" from the client
					}
					System.out.println(inputMessage); // Outputs received messages to the screen
					String res = calc(inputMessage); // res indicates the response status type
					switch (res) {
					case "Error 401":
						res = responseType(401);
						break;
					case "Error 402":
						res = responseType(402);
						break;
					case "Error 403":
						res = responseType(403);
						break;
					case "Error 404":
						res = responseType(404);
						break;
					case "Error 405":
						res = responseType(405);
						break;
					case "200 OK":
						res = responseType(200);
						break;
					default:
						break;
					}
					out.write(res + "\n"); // Send calculation result string
					out.flush();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} finally {
				try {
					if (socket != null)
						socket.close(); // Close the communication socket
				} catch (IOException e) {
					System.out.println("An error occurred while chatting with the client.");
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		ServerSocket listener = null;
		listener = new ServerSocket(9999); // Creating a Server Socket
		System.out.println("Waiting for the connection.....");
		ExecutorService pool = Executors.newFixedThreadPool(10);
		try {
			Socket sock = listener.accept(); // Waiting for connection requests from clients
			System.out.println("Connected.");
			pool.execute(new Calc_Server_Thread(sock));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (listener != null) {
					listener.close(); // Close the socket of the server
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}

	}
}
