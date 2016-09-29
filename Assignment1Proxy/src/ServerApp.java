import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.regex.*;

public class ServerApp {
	// Default port to listen to for incoming clients
	ServerSocket clientConnect;
	Socket destConnect;
	String destination;
	int port = 3000;
	//Max size of a message, 10 000 characters (bytes) long
	int MaxMsg = 10000;

	public ServerApp(int n){
		if (n < 1 && n > 65535) {
			System.out.println("Port specified is not valid, using default: " + port);
			n=port;
		}
		this.port=n;
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
		this.port = n;
	}

	public static void main(String args[]) {
		int arg1 = 3004;		
		try {
			if (args.length == 1) {
			arg1 = Integer.parseInt(args[0]);
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

	public void listen() throws IOException{
		Boolean session=false;
		Boolean waitForClients=true;
		while(waitForClients) {
		System.out.println("Waiting for incoming clients");
		Boolean destExpected=false;
		Socket clientReq = clientConnect.accept();
		Socket destReq;
		System.out.println("A client has connected");
		DataInputStream clientIn = new DataInputStream(clientReq.getInputStream());
		DataOutputStream clientOut = new DataOutputStream(clientReq.getOutputStream());
		DataInputStream destIn;
		DataOutputStream destOut;
		byte[] b;
		String message = "";	
		byte temp = 0;
		session=true;
		List<String> letters = new ArrayList<String>();
		//Session with client
		while (session) {
			System.out.println("Listening for commands");
			//Listen for input from client
				while(true){
					temp=clientIn.readByte();
					System.out.println(temp);		
					if(temp==10){
						temp=clientIn.readByte();
						if(temp==13){
							temp=clientIn.readByte();
							if(temp==10){
								break;
							}
						}
					}
					String first_char=String.valueOf((char)temp);
					System.out.println(first_char);
//					first_char.concat(message);
					letters.add(first_char);
				}
				System.out.println(letters);

				System.out.print("Command received: ");
				System.out.println(message);
				String [] components = message.split(("\\s+"));
				
				for(int i = 0; i<components.length;i++){
					System.out.println(components[i]);					
				}
				
				String request_type = components[0];
				switch(request_type){
					case("GET"):{
						b="ready".getBytes();
						temp=0;
						destExpected=true;
						for(int i=0; i<b.length;i++){
							temp=b[i];	
							clientOut.write(temp);
							clientOut.flush();
						}
						temp=4;
						clientOut.write(temp);
						clientOut.flush();
						message="";
					}
					break;
					case("CONNECT"):{
						System.out.println("Got to connect!");
					}
					break;
					default:
						continue;
				}
            }
		}
	}

	/*public void setDestination(DataInputStream in) throws IOException {
		String result = "";
		int temp = 0;
		this.setDestination(in);
		System.out.println("Waiting for destination address");
		while(temp!=4){
			temp=in.read();
			System.out.println(temp);
			result+=(char)temp;
		}
		System.out.println("Address received: ");
		System.out.println(result);
		this.destination=result;
	}*/

	public void getCommand(){
		
	}
}