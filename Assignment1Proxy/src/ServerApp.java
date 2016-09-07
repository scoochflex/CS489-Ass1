import java.net.*;
import java.io.*;
import java.lang.*;

class InvalidPortNumberException extends Exception{
	public InvalidPortNumberException (String message){
		super(message);
	}
}

public class ServerApp {
	// Default port to listen to for incoming clients
	ServerSocket clientConnect;
	Socket destConnect;
	String destination;
	int port = 3000;

	public ServerApp(int n) throws InvalidPortNumberException{
		if (n < 1 && n > 65535) {
			throw new InvalidPortNumberException("Invalid port number. A valid port is between 1 and 65535");
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
		int arg1 = 3000;
		if (args.length == 1) {
			try {
				arg1 = Integer.parseInt(args[0]);
				ServerApp server = new ServerApp(arg1);
				System.out.println("SimpleServer running on port" + arg1);
				server.listen();
			} catch (InvalidPortNumberException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(0);
			}catch (IOException e){
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
		
	}

	public void listen() throws IOException{
		Boolean session=false;
		Boolean waitForClients=true;
		while(waitForClients){
		System.out.println("Waiting for incoming clients");
		Boolean destExpected=false;
		Socket clientReq = clientConnect.accept();
		Socket destReq;
		System.out.println("A client has connected");
		DataInputStream clientIn = new DataInputStream(clientReq.getInputStream());
		DataOutputStream clientOut = new DataOutputStream(clientReq.getOutputStream());
		DataInputStream destIn;
		DataOutputStream destOut;
		String result = "";	
		byte[] b = null;
		int temp = 0;
		session=true;
		//Session with client
		while (session) {
			System.out.println("Listening for commands");
			//Listen for input from client
				while(true){
					temp=clientIn.read();
					if(temp==4){
						break;
					}
					//debug
					//System.out.println(temp);
					result+=(char)temp;
				}
				System.out.println("Command received: ");
				System.out.println(result);
				if(destExpected){
					this.destination=result;
					destExpected=false;
				}
				switch(result){
					case("change destination"):
					case("Change Destination"):{
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
						result="";
					}break;
					case("end transmission"):
					case("End Transmission"):{
			        clientConnect.close();
					clientIn.close();
					clientOut.close();
					session=false;
					result="";
					}break;
					case("shutdown"):
					case("Shutdown"):{
			        clientConnect.close();
					clientIn.close();
					clientOut.close();
					session=false;
					waitForClients=false;
					result="";
					}break;
					case("session"):
					case("Session"):{
						System.out.println("Got this far...");
						URL destURL = new URL(destination);
				        URLConnection yc = destURL.openConnection();
						System.out.println("Got a little further...");
				        destIn = new DataInputStream(yc.getInputStream());
				        String inputLine;
				        while ((inputLine = destIn.readLine()) != null) 
				            System.out.println(inputLine);
						System.out.println("Finished!");
						result="";
					}break;
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