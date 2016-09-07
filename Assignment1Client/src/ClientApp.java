
import java.net.*;
import java.nio.channels.SocketChannel;
import java.io.*;
import java.util.Scanner;

public class ClientApp {
	// Our socket connection to the server
	protected static Socket connection;
	// The input command stream from the server
	String proxy;
	int port;

public ClientApp(String server, int remote_port) {
	try {
	InetAddress server_address = InetAddress.getByName(server);
	InetAddress local_address=InetAddress.getByName("127.0.0.1");
	int local_port=3001;
	System.out.println("Trying to connect to the server at address " + server_address.getHostAddress() + ":" + remote_port);
//	connection = new Socket(proxy, port);
	connection = new Socket(server_address.getHostAddress(), remote_port,local_address,local_port);	
	}
	catch (Exception e) {
		System.out.println("Could not reach server: " + e.getMessage());
		e.printStackTrace();
		System.exit(0);
	}
	System.out.println("Connected to server established");
}
// GET http://www.w3.org/pub/WWW/TheProject.html HTTP/1.1
public void sendCommand(String text) {
	try {
		OutputStreamWriter toServer = new OutputStreamWriter (connection.getOutputStream());
		byte[] b = text.getBytes();
		int c=0;
		for(int i=0; i<b.length;i++){
			c=b[i];	
			toServer.write(text, 0, text.length());
			toServer.flush();
		}
//		c=4;
//		toServer.write(c);
//		toServer.flush();
	}catch (Exception e) {
		System.out.println("Could not send a request to the server: " + e.getMessage());
		e.printStackTrace();
		return;
	}
}

public String waitForResponse() throws IOException {
		BufferedReader fromServer = new BufferedReader (new InputStreamReader (connection.getInputStream()));
		String result = "";	
		byte[] b = null;
		int temp = 0;
		//this.setDestination(in);
		System.out.println("Listening for commands");
		while(true){
			temp=fromServer.read();
			if(temp==4){
				break;
			}
			System.out.println(temp);
			result+=(char)temp;
		}
		fromServer.close();
		return result;
}
public synchronized void endSession() {
	System.out.println("Closing session with server at address " + proxy);
	try { 
		connection.close();
		}
catch (IOException e) {
	System.out.println("Client app failed to close the conection with the server. Exiting anyways: " + e);
	System.exit(0);
	}
	}
}