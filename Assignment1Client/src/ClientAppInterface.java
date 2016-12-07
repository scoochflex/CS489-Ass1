import java.util.Scanner;
/*
 *	This class was created to take in command line arguments
 *	and give feedback to the user about the status of the connection
 *	to the specified resource
 */
public class ClientAppInterface {
	
	public static void main(String args[]) throws Exception {
		//Variable for address of proxy
		String host = null;
		//Variable for port of proxy
		int port = 0;
		//arguments are provided using a space as a delimiter	
		//ensure that there are only 2 arguments before attempting to establish a connection to the specified server and port
		if (args.length < 2 ) {
			System.out.println("No address for the proxy server or port were provided.");
			System.out.println("You can specify a proxy server address by supplying\nit as the first argument and a port by supplying it as the second arguemnet delimited by a space.");
			System.exit(0);
		}else if(args.length>2){
			System.out.println("Too many arguments passed in! Please supply only the address and port number of the proxy server.");
			System.exit(0);
		}else{
			//First argument passed in is the host
			host=args[0];
			//Try to parse the second argument as an integer
			try {
				//Second argument passed in is (should be) the port number
				int val = Integer.parseInt(args[1]);
					if (val < 1 || val > 65535) {
						System.out.println("Port specified is not valid. Port should be greater than 0 and less than 65535");
						System.out.println("Client exiting...");
						System.exit(0);
						}else{
						port = val;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
		}
		System.out.println("Connections will be made through the proxy at the address " + host + ":" +port);
		ClientApp client = new ClientApp(host, port);
		String request="";
		boolean res;
		Scanner cmd = new Scanner(System.in);
		System.out.println("Please enter the address of the server you would like to communicate with, type exit to close the application: ");
		//Loop until we decide to break and shut down the client
		while(true){
			request=cmd.nextLine();
			if(request.equalsIgnoreCase("Exit")){
				cmd.close();
				System.out.println("Client shutting down.");
				break;
			}
			
			res = client.connectToDestination(request);			

			if(res){
				client.getResponse();
			}
			System.out.println("Please enter the address of the server you would like to communicate with, type exit to close the application: ");
		}
	}
}
