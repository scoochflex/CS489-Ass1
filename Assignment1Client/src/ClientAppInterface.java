import java.io.IOException;
import java.util.Scanner;

public class ClientAppInterface {

	public ClientAppInterface() {
		
	}
	
	public static void main(String args[]) throws IOException {
		//arguments are provided using a space as a delimiter	
		//ensure that there are only 2 arguments before attempting to establish a connection to the specified server and port
		if (args.length != 2) {
			System.out.println("Please provide an address for the server and a port, make sure the proxy is running on the same port!");
			return;
		}
		//Default value for host
		String host = "127.0.0.1";
//		String destination = "https://www.google.ca/";
//		String destination = "http://www.w3.org/pub/WWW/";
		//First argument passed in is the host
		host=args[0];
		//Default value for port
		int port = 3000;
		try {
			//Second argument passed in is (should be) the port number
			port = Integer.parseInt(args[1]);
			}
			catch (Exception e) {
				System.out.println("Port specified is not valid: " + e.getMessage());
				e.printStackTrace();
				return;
			}
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
					client.sendCommand(request);
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
					client.sendCommand("Change Destination");
					response=client.waitForResponse();
					System.out.println(response);
					if(response.equals("ready")){
						client.sendCommand(request);
					}else{
						System.out.println("Could not set destination address. Please try again.");
					}
					System.out.println("Destination address changed.");
					request="";
				}
				break;
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
