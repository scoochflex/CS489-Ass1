import java.io.IOException;
import java.util.Scanner;
/*
 *	This class was created to take in command line arguments
 *	and give feedback to the user about the status of the connection
 *	to the specified resource
 */
public class ClientAppInterface {
	
	public static void main(String args[]) throws IOException {
		//Default value for host
		String host = "127.0.0.1";
		//Default value for port
		int port = 3000;
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
		//Feedback for the user so they know that the client starts up on port 300
		System.out.println("Attempting to set up connection to proxy on port: " + port);
		ClientApp client = new ClientApp(host, port);
		String request="";
		String response="";
		boolean res;
		Scanner cmd = new Scanner(System.in);
		System.out.println("Please enter the address of the server you would like to communicate with, type exit to close the application: ");
		while(true){
			request=cmd.nextLine();
			
			res = client.connectToDestination(request);
			if(res){
				response = client.waitForResponse();
			}
			
			System.out.println("Please enter the address of the server you would like to communicate with, type exit to close the application: ");
		}
	}
}
