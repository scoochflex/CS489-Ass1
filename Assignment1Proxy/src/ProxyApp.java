import java.net.*;
import java.nio.ByteOrder;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.regex.*;

public class ProxyApp {
	// Default port to listen to for incoming clients
	ServerSocket clientConnect;
	Socket destConnect;
	String destination = "";
	int port;
	// Max size of a message, 10 000 characters (bytes) long
	int MaxMsg = 10000;
	// List of messages (not yet dealt with) sent by client
	List<String> messagesFromClient = new ArrayList<String>();
	// List of messages (not yet dealt with) received from server (destination)
	List<String> messagesToClient = new ArrayList<String>();

	// As currently only one server connection is supported, this server
	// connection handler only waits on a destination name before starting
	// up and forwarding out messages to the intended server	
	public class ServerConnectionHandler implements Runnable {

		public void run() {
			Boolean session = false;
			String currentDestination = "";
			
			while(currentDestination.isEmpty()){
				currentDestination=getDestination();
			}
			
			System.out.println("(Server Thread) Current dest after: " + currentDestination);
			
//			while () {
				try {
					// Split destination into hostname and port
					// Assign hostname and port to local variables to avoid too
					// many array accesses					
					String destString = "";
					// Assign hostname and port to local variables to avoid too
					// many array accesses
					destString = currentDestination.substring(0,currentDestination.lastIndexOf(":"));
					
					int destPort = Integer.parseInt(currentDestination.substring(currentDestination.lastIndexOf(":")+1,currentDestination.length()));
					

					InetAddress local_address = InetAddress.getByName("127.0.0.1");
					InetAddress remote_address = InetAddress.getByName(destString);
					System.out.println("(Server Thread) Destination determined to be: " + destString + " : " + destPort + " remote_address: " + remote_address.toString());

					destConnect = new Socket(remote_address, destPort, local_address, 3002);
//					destConnect = new Socket("http://httpbin.org", 80, local_address, 3002);

					DataInputStream destIn = new DataInputStream(destConnect.getInputStream());
					OutputStreamWriter destOut = new OutputStreamWriter(destConnect.getOutputStream());
					byte[] b;
					String message = "";
					session = true;
					// Session with client
					while (session) {
						// Listen for input from server
						int byteEst = 0;
						byteEst = destIn.available();
						while (byteEst != 0) {
							b = new byte[byteEst];
							destIn.readFully(b, 0, byteEst);
							for(int i =0; i<b.length;i++){
								String first_char = String.valueOf((char) b[i]);
								message += first_char;
							}
							byteEst = destIn.available();
						}
						System.out.println("(Server Thread) Message receieved: " + message);
						String messageToSend = removeTopMessage();
						while (messageToSend != "") {
							System.out.println("(Server Thread) There is a message waiting to be sent: " + messageToSend);
							try {
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
					System.exit(0);
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
			if(destination.isEmpty()){
				return "";
			}else{	
				System.out.println("Dest is: Set!");
				return destination;
			}
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
					System.out.println("(Client Thread) Waiting for incoming clients...");
					Socket clientReq = clientConnect.accept();
					System.out.println("(Client Thread) A client has connected");
					DataInputStream clientIn = new DataInputStream(clientReq.getInputStream());
					OutputStreamWriter clientOut = new OutputStreamWriter(clientReq.getOutputStream());
					String message = "";
					byte [] b = null;
					session = true;
					// Session with client
					while (session) {
//						System.out.println("Listening for commands");
						// Listen for input from server
						int byteEst = 0;
						byteEst = clientIn.available();
						while (byteEst != 0) {
							b = new byte[byteEst];
							clientIn.readFully(b, 0, byteEst);
							for(int i =0; i<b.length;i++){
								String first_char = String.valueOf((char) b[i]);
								message += first_char;
							}
							byteEst = clientIn.available();
						}
						System.out.println("(Client Thread) Message receieved: " + message);
						String[] components = message.split("\\s+");

						String request_type = components[0];
						
						if(request_type.equalsIgnoreCase("CONNECT")){
							if(!destinationEqualityCheck(components[1])){
								System.out.println("(Client Thread) Got to connect! Setting dest to Components[1] : " + components[1]);
								setDestination(components[1]);
							}
							addMessageToList(message);
						}else{
							addMessageToList(message);
						}
						
						String messageToSend = removeTopMessage();
						while (messageToSend != "") {
							System.out.println("(Client Thread) There is a message to be sent to the client : " + messageToSend);
							try {
								clientOut.write(messageToSend);
								clientOut.flush();
							} catch (Exception e) {
								System.out.println("Could not send message to the server: " + e.getMessage());
								e.printStackTrace();
								return;
							}
							messageToSend = removeTopMessage();
						}
//						switch (request_type) {
//						case ("GET"): {
//							b = "ready".getBytes();
//							temp = 0;
//							for (int i = 0; i < b.length; i++) {
//								temp = b[i];
//								clientOut.write(temp);
//								clientOut.flush();
//							}
//							temp = 4;
//							clientOut.write(temp);
//							clientOut.flush();
//						}
//							break;
//						case ("CONNECT"): {
//							System.out.println("Got to connect! Components[1] " + components[1]);
//							setDestination(components[1]);
//						}
//							break;
//						default:
//							continue;
//						}
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
		
		public synchronized boolean destinationEqualityCheck(String check){
			return check.equalsIgnoreCase(destination);
		}
		
		public synchronized String removeTopMessage() {
			if (messagesToClient.isEmpty()) {
				return "";
			} else {
				return messagesToClient.remove(0);
			}
		}

		public synchronized void addMessageToList(String message) {
			messagesFromClient.add(message);
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
		System.out.println("Proxy initalized, ServerSocket clientConnect listening for incoming clients on port: " + n);
		this.port = n;
	}

	public static void main(String args[]) {
		//default port value
		int defPort = 3001;
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