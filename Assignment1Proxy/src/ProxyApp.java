import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyApp {
	// Default port to listen to for incoming clients
	ServerSocket clientConnect;
	Socket destConnect;
	String destination = "";
	DataInputStream destIn;
	DataOutputStream clientOut;
	int port;
	
	/*  The below List<String> data types are used to store messages
	 *  to/from the client and server so that they can be dealt 
	 *  with as soon as the client/server connection handler is ready.
	 *  As messages are sent out, they are removed from these lists
	 *  The lists are effectively ordered as queues so that messages
	 *  are dealt with in a FIFO manner
	 */
	
	// List of messages (not yet dealt with) sent by client
	List<String> messagesFromClient = new ArrayList<String>();

	// As  only one server connection is supported, this server
	// connection handler only waits on a destination name before starting
	// up and forwarding out messages to the intended server	
	public class ServerConnectionHandler implements Runnable{

		//Main run method of the server connection handler
		public void run() {
			//Default port to try to connect to remote resource					

			//Boolean controlling weather or not the server connection handler waits for messages from/to a specified destination
			Boolean session = true;
			//The current destination is empty until the client receives a GET that specifies a remote location
			String currentDestination = "";
			String destName;
			
			//Outer loop ensuring that if a session is no longer in place it will wait for a new destination to be set before setting up a connection
			while(true){
			int destPort = 80;
			String [] parts = null;
			//Ensures that ServerConnectionHandler will block until a destination is determined and set by ClientConnectionHandler in a thread safe manner
			while(currentDestination.isEmpty()){
				//Get the destination to be checked by the while
				currentDestination=getDestination();
			}
			session=true;
			destName=currentDestination;
			if(currentDestination.split(":").length>=2){
				parts = currentDestination.split(":");
				destName = parts[0];
				destPort = Integer.parseInt(parts[1]);
			}
			//Feedback indicating that the server is trying to connect to the destination on the default port
			System.out.println("(Server Thread) Current destination: " + destName + " the port: " + destPort + " will be used to establish the connection.");
			
				try {
					try{
					//Create a socket based around the desired destination and over the default port
					destConnect = new Socket(destName, destPort);
					}catch (UnknownHostException e) {
						e.printStackTrace();
						System.out.println("(Server Thread) Could not connect to server at: " + currentDestination);
						sendErrorToClient();
						setDestination("");
						currentDestination="";
						session=false;
					}
					//Get the underlying stream writer for forwarding messages to the destination as they come in from the client
					
				
					while (session) {
						OutputStreamWriter destOut  = new OutputStreamWriter(destConnect.getOutputStream());					
						destIn = new DataInputStream(destConnect.getInputStream());
						String messageToSend = removeTopMessage();
					while (messageToSend!="") {
						System.out.println("(Server Thread) There is a message waiting to be sent: " + messageToSend);
						try {
							destOut.write(messageToSend);
							destOut.flush();
						} catch (Exception e) {
							System.out.println("(Server Thread)Could not send message to the server: " + e.getMessage());
							e.printStackTrace();
							return;
						}
						messageToSend = removeTopMessage();
					}

					byte[] b = new byte[30000];
						// Listen for input from server
					    int byteEst = destIn.available();
						while (byteEst > 0) {
							byteEst = destIn.read(b, 0, 30000);
							if(byteEst>0){
								clientOut.write(Arrays.copyOf(b, byteEst));
							}
							if(!getDestination().equalsIgnoreCase(currentDestination)){
								currentDestination=getDestination();
								session = false;
								break;
							}
						    byteEst = destIn.available();
						}
						
						if(!getDestination().equalsIgnoreCase(currentDestination)){
							currentDestination=getDestination();
							session = false;
							break;
						}
						
					//while(session loop end)
					}
				//Try end IOException 
				}
				catch (Exception e) {
					e.printStackTrace();
					System.out.println("(Server Thread) disconnected from server at: " + currentDestination);
					sendErrorToClient();
					setDestination("");
					currentDestination="";
				}
				//while(true) loop end
			}
		}
		
		//Sets the destination variable to be used by the server thread t
		public synchronized void setDestination(String dest) {
			destination = dest;
		}
		
		public synchronized String getDestination() {
			if(destination.isEmpty()){
				return "";
			}else{	
				return destination;
			}	
		}		
		
		//A thread safe way to take the top element of the messagesToClient List<String>
		public synchronized String removeTopMessage() {
			if (messagesFromClient.isEmpty()) {
				return "";
			} else {
				return messagesFromClient.remove(0);
			}
		}
		
		public void sendErrorToClient(){
			try{
				String message = "Could not connect to host";
				clientOut.write(message.getBytes(),0,message.length());
				clientOut.flush();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public class ClientConnectionHandler implements Runnable {

		public void run() {
			int defPort = 0;
			Boolean session = false;
			Boolean waitForClients = true;
			while (waitForClients) {
				try {
					System.out.println("(Client Thread) Waiting for incoming clients...");
					if(clientConnect.isClosed()){
						clientConnect = new ServerSocket(defPort);
					}else{
						defPort = clientConnect.getLocalPort();
					}
					Socket clientReq = clientConnect.accept();
//					clientReq.setSoLinger(true, 10);
					System.out.println("(Client Thread) A client has connected");
					DataInputStream clientIn = new DataInputStream(clientReq.getInputStream());
					clientOut = new DataOutputStream(clientReq.getOutputStream());
					byte [] b = null;
					session = true;
					// Session with client
					while (session) {
						String message = "";
						// Listen for input from client
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
						if(!message.isEmpty()){
							if(message.equalsIgnoreCase("Message Receieved. Close Connection")){
								session=false;
								break;
							}else if(destinationEqualityCheck("")){
								checkIncomingMessageForHostName(message);
							}
							System.out.print("(Client Thread) Message receieved: " + message);
							addMessageToList(message);
						}
					}
					setDestination("");
					clientConnect.close();
					System.out.println("(Client Thread) Response receieved. Terminating connection with client...");					
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("(Client Thread) Client at " + destination + " disconnected...");					
				}
			}
		}
		
		public boolean messageIsResponse(String message){
			String [] lines = message.split("\n");
			boolean responseSent = false;
			Matcher m;
			Pattern statusCode = Pattern.compile("[^ ]+\\s([\\d]{3}+)\\s.+[\r\n]*");
			m = statusCode.matcher(lines[0]);
			
			if(m.matches()){
				String code = m.group(1);
				int codeNum = Integer.parseInt(code);
				/* Checking to see if the codeNumber is a success or a failure message
				 * If so we terminate the connection with the client upon sending the message
				 * If further redirection is required we (e.g. code in the 300's) 
				 * we do not mark it as a terminating response message
				 */
				if(codeNum<300 || codeNum>400){
					responseSent=true;
				}		
			}
			return responseSent;
		}
		
		//This function uses a regular expression to parse the text given and look for a GET message from the client
		//Upon receiving the GET it extracts the hostname and sets destination to this value
		public void checkIncomingMessageForHostName(String message) throws Exception{
			String [] lines = message.split("\n");
			boolean getReceieved = false;
			String hostName = "";
			String port = "";
			Matcher m;
			Pattern requestType = Pattern.compile("(\\w+)\\s([^ ]*).*[\r\n]*");
			Pattern hostname = Pattern.compile("^[Hh]ost:\\s*(.*)[\r\n]*");
			Pattern portCheck = Pattern.compile("([^:]+(:[^/]*)/)*.*[\r\n]*");
			int numLinesCheck = lines.length > 6 ? 6 : lines.length;
			for(int i=0; i<numLinesCheck;i++){
				m = requestType.matcher(lines[i]); 
				if(m.matches() && m.group(1).equalsIgnoreCase("GET")){
					System.out.println("(Client Thread) dest is: " + m.group(2));					
					URL tmp = new URL(m.group(2));
					port = tmp.getPort()==-1 ? "" : String.valueOf(tmp.getPort());
					getReceieved = true;
				}
				if(getReceieved){
				m = hostname.matcher(lines[i]);
					if(m.matches()){
						hostName = m.group(1);
					}
					m = portCheck.matcher(lines[i]);
					if(m.matches() && port.isEmpty()){
					}
				}
			 }
			 if(getReceieved && hostName!=""){
					System.out.println("(Client Thread) Destination receieved: " + hostName);
					if(!destinationEqualityCheck(hostName)){
						System.out.println("(Client Thread) Setting destination to: " + hostName);
						setDestination((hostName + (port.isEmpty() ? "" : ":"+port)));
					}
				}
		}
		
		/*
		 * Below are thread safe ways that the clientConnectionHandler 
		 * uses to communicate with the serverConnectionHandler
		 */
		//Sets the destination variable to be used by the server thread t
		public synchronized void setDestination(String dest) {
			destination = dest;
		}
		
		//Check to see if the get received contains the same destination as currently set
		public synchronized boolean destinationEqualityCheck(String check){
			return check.equalsIgnoreCase(destination);
		}		

		//A thread-safe way to add messages to the messagesFromClient List<String>
		public synchronized void addMessageToList(String message) {
			messagesFromClient.add(message);
		}
	}

	//The proxy app starts up the main process that will spawn the ClientConnectionHandler and ServerConnectionHandler
	//It will also create and initialize the socket that will be used by the ClientConnectionHandler on a specific port
	//based on the command line argument passed in
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
		//Initialization for port value passed in
		int arg1=0;
		try {
			//If there is exactly one argument, assume that it's the port. 
			if (args.length == 1) {
				arg1 = Integer.parseInt(args[0]);
				//Ensure that the port # (passed in through the command line) is within a sane range
				//If it is not valid, use a default value defined in the constructor
				if (arg1 < 1 || arg1 > 65535) {
					System.out.println("Port specified is not valid, using default: " + defPort);
					arg1 = defPort;
				}
			//If no argument passed in, or too many arguments are passed in, print usage and continue using the default port
			}else if(args.length>1){
				System.out.println("Incorrect number of arguements. Please speficy the port that the proxy should wait for client connections on.");
				System.out.println("Specify it in the form of ProxyApp <port>");
				System.out.println("Since no port was provided, the default: " + defPort + " will be used");
				arg1=defPort;
			}else{
				System.out.println("No port specified, using default port: " + defPort);
				arg1=defPort;
			}
			//Instantiate the proxy on the specified port
			ProxyApp proxy = new ProxyApp(arg1);
			//Have the proxy process spawn the clientConnectionHandeler 
			ClientConnectionHandler clientHandeler = proxy.new ClientConnectionHandler();
			//move that clientHandler to a new thread spawned from the proxy process
			Thread clientThread = new Thread(clientHandeler);
			//Start the clientThread (which means that it runs the run methods associated with that thread)
			clientThread.start();
			//Do the same with the serverConnectionHandeler
			ServerConnectionHandler serverHandeler = proxy.new ServerConnectionHandler();
			Thread serverThread = new Thread(serverHandeler);
			serverThread.start();
		//catch any errors that might occur, print them out and terminate the server
		} catch (NumberFormatException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.out.println("Incorrect arguement. Please speficy the port that the proxy should wait for client connections on.");
			System.out.println("Specify it in the form of ProxyApp <port> where port is a number between 0 and 65535");
			System.exit(0);
		}
	}
}