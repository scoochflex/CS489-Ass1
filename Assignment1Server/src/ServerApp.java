import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.regex.*;


public class ServerApp {
	//Socket to manage connection with the currently connected client
	ServerSocket clientConnect;	
	
	// Default port to listen to for incoming clients
	int port = 80;

	public ServerApp(int n) {
		if (n < 1 && n > 65535) {
			System.out.println("Invalid port number. A valid port is between 1 and 65535. Server will be started on default port 2999.");
			n=2999;
		}
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
		System.out.println("Server app listening for incoming clients on port: " + n);
	}

	public static void main(String args[]) {
		int arg1 = 2999;		
		try {
			if (args.length == 1) {
				arg1 = Integer.parseInt(args[0]);
			}else{
				System.out.println("SimpleServer takes a port as an arguement in the form of ServerApp <portNum>. The default port will be used.");
			}
			ServerApp server = new ServerApp(arg1);
			System.out.println("SimpleServer running on port" + arg1);
			server.listen();
		} catch (IOException e){
			//System.out.println("Stream was closed before it could be read" + e.getMessage());
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		catch (Exception e) {
			//System.out.println("Invalid port number. A valid port is between 1 and 65535" + e.getMessage());
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}			
	}

	public void listen() throws IOException {
		Boolean session=false;
		Boolean waitForClients=true;
		while(waitForClients) {
			System.out.println("Waiting for incoming clients");
			Socket clientReq = clientConnect.accept();
			System.out.println("A client has connected");
			DataInputStream clientIn = new DataInputStream(clientReq.getInputStream());
			OutputStreamWriter clientOut = new OutputStreamWriter(clientReq.getOutputStream());
			byte[] b;
			String message = "";	
			session=true;
			//Session with client
			while (session) {
				System.out.println("Listening for commands");
				//Listen for input from client
				while (session) {
	//				System.out.println("Listening for commands");
					// Listen for input from server
					int byteEst = 0;
					byteEst = clientIn.available();
					while (byteEst != 0) {
						System.out.println("Bytes to be read: " + byteEst);
						b = new byte[byteEst];
						clientIn.readFully(b, 0, byteEst);
						for(int i =0; i<b.length;i++){
							String first_char = String.valueOf((char) b[i]);
							message += first_char;
						}
						byteEst = clientIn.available();
					}
					if(!message.isEmpty()){
						System.out.print("Command received: ");
						System.out.println(message);
						String resourcePath = checkIncomingMessageForResourcePath(message);
						//SEND types of response with specific data
						if(checkHaveResource(resourcePath)){
							
						}
						String response = "";
						try {
							clientOut.write(response.length());
							clientOut.write(response);
							clientOut.flush();
						} catch (Exception e) {
							System.out.println("Could not send message to the server: " + e.getMessage());
							e.printStackTrace();
							return;
						}
					}		
					message="";				
					}
				}
		}
	}
	
	public boolean checkHaveResource(String resourcePath){
		boolean found = false;
		
		return found;		
	}
	
	public String checkIncomingMessageForResourcePath(String message){
		String [] lines = message.split("\n");
		boolean getReceieved = false;
		String resourcePath = "";
		String hostName="";
		Matcher m;
		Pattern requestTypeP = Pattern.compile("(\\w+)\\s([^ ]*).*[\r\n]*");
		Pattern hostNameP = Pattern.compile("^[Hh]ost:\\s*(.*)[\r\n]*");
		int numLinesCheck = lines.length > 6 ? 6 : lines.length;
		for(int i=0; i<numLinesCheck;i++){
			System.out.println("i: " + i + " : " + lines[i]);
			m = requestTypeP.matcher(lines[i]); 
			if(m.matches() && m.group(1).equalsIgnoreCase("GET")){
				getReceieved=true;
				resourcePath=m.group(2);
			}
			if(getReceieved){
			m = hostNameP.matcher(lines[i]);
				if(m.matches()){
					hostName = m.group(1);
				}
			}
		}
		//Ensure that the message was meant for us...
		if(hostName.equalsIgnoreCase(clientConnect.getInetAddress().getHostAddress())){
			return resourcePath;
		}else{
			return "";
		}
	}
}