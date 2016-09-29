import java.io.IOException;
import java.util.Scanner;

public class ClientAppInterface {

	public ClientAppInterface() {
		
	}
	
	public static void main(String args[]) throws IOException {
		//Default value for host
		String host = "127.0.0.1";
		//Default value for port
		int port = 8118;
		//arguments are provided using a space as a delimiter	
		//ensure that there are only 2 arguments before attempting to establish a connection to the specified server and port
		if (args.length < 2 ) {
			System.out.println("No address for the proxy server and a port were provided, default values will be used.");
			System.out.println("You can specify a proxy server address by supplying it as the first argument, you can also supply the desired port if you know iot by passing it in afterwords.");
		}else{
			//First argument passed in is the host
			host=args[0];
			//Try to parse the second argument as an integer
			try {
				//Second argument passed in is (should be) the port number
				port = Integer.parseInt(args[1]);
				}
				catch (Exception e) {
					System.out.println("Port specified is not valid: " + e.getMessage());
					e.printStackTrace();
					System.out.println("Using default value of : " + port + " instead");
				}
		}		
//		String destination = "https://www.google.ca/";
//		String destination = "http://www.w3.org/pub/WWW/";
		
		ClientApp client = new ClientApp(host, port);
		String request="";
		String response="";
		Scanner cmd = new Scanner(System.in);
		System.out.println("Please enter a command, type help for a list of valid commands.");
		while(true){
			System.out.println("Client> ");
			request=cmd.nextLine();
			switch (request){
			case "Send": //Send one request, and wait for one response
			case "send":{
				client.sendCommand("GET http://www.w3.org/pub/WWW/TheProject.html HTTP/1.1");
				request="";
				}break;
			case "Session": //Start a session with a 
			case "session":{
				client.sendCommand(request);
				request="";
				}break;
			case "Destination": //Start a session with a destination server or address
			case "destination":{
				System.out.println("Please enter the address of the server you would like to communicate with: ");
				request=cmd.nextLine();
				client.connectToDestination(request,3000);
				response=client.waitForResponse();
				System.out.println(response);
				if(response.equals("ready")){
					client.sendCommand(request);
				}else{
					System.out.println("Could not set destination address. Please try again.");
				}
				System.out.println("Destination address changed.");
				request="";
				}break;
			case "Get":
			case "get":{
				System.out.println("Please enter what you would like to send to the specified server");
				request=cmd.nextLine();
				}break;
			case "Help":
			case "help":{
				System.out.println("Send - sends plaintext to the address specified. Packets are routed through the proxy");
			}break;
			case "exit":
			case "Exit":{
				//Tell the proxy that the session is over 
				client.sendCommand("End Transmission");
				cmd.close();
				client.endSession();
				}break;
			default:{
				System.out.println("Command not understood.\nPlease type help for a list of valid commands with breif descriptions");
				break;
				}
			}
		}
	}

}
