import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.regex.*;

public class ProxyApp {
	// Default port to listen to for incoming clients
	ServerSocket clientConnect;
	protected static Socket destConnect;
	String destination = "NULL";
	int port = 3000;
	// Max size of a message, 10 000 characters (bytes) long
	int MaxMsg = 10000;
	// List of messages (not yet dealt with) sent by client
	List<String> messagesFromClient = new ArrayList<String>();
	// List of messages (not yet dealt with) received from server (destination)
	List<String> messagesToClient = new ArrayList<String>();

	// As currently only one server connection is supported, this server
	// connection handler only waits on a destination name before starting
	//up and forwarding out messages to the intended server	
	public class ServerConnectionHandler implements Runnable {

		public void run() {
			Boolean session = false;
			Boolean waitForClients = true;
			String currentDestination = "";
			
			while(currentDestination.equalsIgnoreCase("")){
				currentDestination=getDestination();
			}
			System.out.println("Current dest after: "+currentDestination);
			
//			while () {
				try {
					// Split destination into hostname and port
					String[] destInfo = currentDestination.split("\\:");
					System.out.println("Destination determined to be: " + destInfo[0]);
					// Assign hostname and port to local variables to avoid too
					// many array accesses
					String destString = destInfo[0];
					int destPort = Integer.parseInt(destInfo[1]);

					InetAddress local_address = InetAddress.getByName("127.0.0.1");

					destConnect = new Socket(destString, destPort, local_address, 3006);

					DataInputStream destIn = new DataInputStream(destConnect.getInputStream());
					OutputStreamWriter destOut = new OutputStreamWriter(destConnect.getOutputStream());
					byte[] b;
					String message = "";
					byte temp = 0;
					session = true;
					// Session with client
					while (session) {
						System.out.println("Listening for commands");
						// Listen for input from server
						int byteEst = destIn.available();
						System.out.println("Bytes to be read: "+byteEst);
						while (byteEst != 0) {
							temp = destIn.readByte();
							System.out.println(temp);
							if (temp == 10) {
								temp = destIn.readByte();
								if (temp == 13) {
									temp = destIn.readByte();
									if (temp == 10) {
										System.out.print("Response received: ");
										System.out.println(message);
										// Add message to list of messages that
										// are to go to the client
										addMessageToList(message);
										break;
									}
								}
							}
							String first_char = String.valueOf((char) temp);
							System.out.println(first_char);
							message += first_char;
						}
						String messageToSend = removeTopMessage();
						while (messageToSend != "") {
							try {
								destOut.write(messageToSend.length());
								destOut.write(messageToSend);
								destOut.flush();
							} catch (Exception e) {
								System.out.println("Could not send message to the server: " + e.getMessage());
								e.printStackTrace();
								return;
							}
							messageToSend = removeTopMessage();
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
//			}
		}

		public synchronized String removeTopMessage() {
			if (messagesFromClient.isEmpty()) {
				return "";
			} else {
				return messagesFromClient.remove(0);
			}
		}

		public synchronized String getDestination() {
			return destination;
		}

		public synchronized List<String> getNumberMessages() {
			return new ArrayList<String>();
		}

		public synchronized void addMessageToList(String message) {
			messagesToClient.add(message);
		}
	}

	public class ClientConnectionHandler implements Runnable {

		public void run() {
			Boolean session = false;
			Boolean waitForClients = true;
			while (waitForClients) {
				try {
					System.out.println("Waiting for incoming clients");
					Socket clientReq = clientConnect.accept();
					System.out.println("A client has connected");
					DataInputStream clientIn = new DataInputStream(clientReq.getInputStream());
					DataOutputStream clientOut = new DataOutputStream(clientReq.getOutputStream());
					byte[] b;
					String message = "";
					byte temp = 0;
					session = true;
					// Session with client
					while (session) {
						System.out.println("Listening for commands");
						// Listen for input from client
						while (true) {
							temp = clientIn.readByte();
							System.out.println(temp);
							if (temp == 10) {
								temp = clientIn.readByte();
								if (temp == 13) {
									temp = clientIn.readByte();
									if (temp == 10) {
										break;
									}
								}
							}
							String first_char = String.valueOf((char) temp);
							System.out.println(first_char);
							// first_char.concat(message);
							message += first_char;
						}
						System.out.print("Command received: ");
						System.out.println(message);
						String[] components = message.split("\\s+");

						for (int i = 0; i < components.length; i++) {
							System.out.println(components[i]);
						}

						String request_type = components[0];
						switch (request_type) {
						case ("GET"): {
							b = "ready".getBytes();
							temp = 0;
							for (int i = 0; i < b.length; i++) {
								temp = b[i];
								clientOut.write(temp);
								clientOut.flush();
							}
							temp = 4;
							clientOut.write(temp);
							clientOut.flush();
						}
							break;
						case ("CONNECT"): {
							System.out.println("Got to connect! Components[1] " + components[1]);
							setDestination(components[1]);
						}
							break;
						default:
							continue;
						}
					}
					// End of Try
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public synchronized void setDestination(String dest) {
			destination = dest;
		}
	}

	//The proxy app starts up the main process that will spawn the ClientConnectionHandler and ServerConnectionHandler
	//It will also create and initalize the socket that will be used by the ClientConnectionHandler on a specific port
	//based on the command line arguement passed in
	public ProxyApp(int n) {		
		this.port = n;
		// Try making a ServerSocket to the given port
		System.out.println("Now attempting to bind to port: " + n);
		try {
			clientConnect = new ServerSocket(n);
		} catch (Exception e) {
			System.out.println("Could not connect to port: " + n + "\n" + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		} // No errors, assume binding of server-app to port was successful
		System.out.println("Proxy initalized ServerSocket app listening for incoming clients on port: " + n);
		this.port = n;
	}

	public static void main(String args[]) {
		//default port value
		int defPort = 3004;
		//Initalization for port value passed in
		int arg1=0;
		try {
			//If there is exactly one arguement, assume that it's the port. 
			if (args.length == 1) {
				arg1 = Integer.parseInt(args[0]);
				//Ensure that the port # (passed in through the command line) is within a sane range
				//If it is not valid, use a default value defined in the constructor
				if (arg1 < 1 && arg1 > 65535) {
					System.out.println("Port specified is not valid, using default: " + defPort);
					arg1 = defPort;
				}
			//If no arguement passed in, or too many arguements are passed in, print usage and continue using the default port
			}else{
				System.out.println("Incorrect number of arguements. Please speficy the port that the proxy should wait for client connections on.");
				System.out.println("Specify it in the form of ProxyApp <port>");
				arg1=defPort;
			}
			ProxyApp proxy = new ProxyApp(arg1);
			ClientConnectionHandler clientHandeler = proxy.new ClientConnectionHandler();
			Thread clientThread = new Thread(clientHandeler);
			clientThread.start();
			ServerConnectionHandler serverHandeler = proxy.new ServerConnectionHandler();
			Thread serverThread = new Thread(serverHandeler);
			serverThread.start();
			System.out.println("Proxy waiting for clients on port" + arg1);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}

	/*
	 * public void setDestination(DataInputStream in) throws IOException {
	 * String result = ""; int temp = 0; this.setDestination(in);
	 * System.out.println("Waiting for destination address"); while(temp!=4){
	 * temp=in.read(); System.out.println(temp); result+=(char)temp; }
	 * System.out.println("Address received: "); System.out.println(result);
	 * this.destination=result; }
	 */
}