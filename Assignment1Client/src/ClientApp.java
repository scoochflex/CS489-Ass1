
import java.net.*;
import java.nio.channels.SocketChannel;
import java.io.*;
import java.util.Scanner;
/*
 * This class is used to manage and establish the a connection 
 * to a remote resource through a proxy at a specific port and IP
 */
public class ClientApp {
	// Our socket connection to the server, establishes connection through the proxy
	URLConnection serverConnection;
	//Our data input stream to recieve messages forwarded by the proxy (from the server)
	DataInputStream fromServer;
	//A reference to our proxy
	Proxy proxy;
	
//Pass in the IP and port the proxy is running on, after instantiation of the client app it can be use to connect to a remote resource through the proxy and attain a response
public ClientApp(String server, int remote_port) {
	try {
	//This is a useful intermediate data type as determines the ip address if a host if given the name. If given the IP it will take it as is
	InetAddress proxy_inet_address = InetAddress.getByName(server);
	//Create a socket address with the specified port and created inetaddress
	SocketAddress proxy_socket_address = new InetSocketAddress(proxy_inet_address,remote_port);
	//The type of the proxy is a HTTP proxy
	proxy = new Proxy(Proxy.Type.HTTP, proxy_socket_address);
	}
	catch (Exception e) {
		System.out.println("Could not connect to proxy: " + e.getMessage());
		e.printStackTrace();
		System.exit(0);
	}
	//Give feedback to the user letting them know that the connection to the proxy was setup (not created) OK
	System.out.println("Connection to proxy defined");
}

//This function is used by an instance of client app to receive data from the proxy. It may throw an IOException which will be handled in the ClientAppInterface 
public String waitForResponse() throws IOException {
		//Open the data input stream from that resource
		fromServer = new DataInputStream(serverConnection.getInputStream());
		//Initialize an empty message
		String message = "";
		//Initialize a byte array to hold the data before it is converted from UTF-8 format to a string
		byte[] b;
		//Initialize an integer to store the estimated number of bytes coming in through the data input stream
		int byteEst = 0;
		//Indicate to the user that we are waiting on a response from the server
		System.out.println("Waiting for a response from the server...");
		//This loop ensures that we block until a message is returned to us by the proxy
		while (message.isEmpty()) {
			//Gets an estimate of the number of bytes available for reading from the underlying data stream
			byteEst = fromServer.available();
			while(byteEst!=0){
				//Debug message
				//System.out.println("Bytes to be read: " + byteEst);
				//Initialize the byte array to store all the bytes available for reading
				b = new byte[byteEst];
				//Read all available bytes from the data input stream and store them in b
				fromServer.readFully(b, 0, byteEst);
				//Convert the data character by character to a string
				for(int i =0; i<b.length;i++){
					String temp_char = String.valueOf((char) b[i]);
					message += temp_char;
				}
				//Ensure that there is no more data to be read before returning
				byteEst = fromServer.available();
			}
		}
		//Print out the message after it is received
		System.out.println("Message is: \n" + message);
		return message;
}

//This function is used to establish a connection to a destination specified by the user
public boolean connectToDestination(String address){
	try {
	//First we create a URL based on the string given to us by the user, if it is of an invalid format, an error will be thrown when trying to establish the connection
	//This intermediate data type is used because it easily generated the correct header data when trying to establish a connection to the resource
	URL destination = new URL(address);
	//Get appropriate URLConnection object from the URL we created above
	//It creates the connection by communicating with the proxy specified by proxy
	//Proxy is defined above in the ClientApp constructor
	serverConnection = destination.openConnection(proxy);
	//Connect to the resource specified by the address argument
	serverConnection.connect();
	//Catch potential errors. Print messages for specific error types
	}catch (SocketTimeoutException e) {
		System.out.println("Could not establish connect to resource: " + e.getMessage());
		e.printStackTrace();
		System.out.println("A SocketTimeoutException occured while trying to establish the connection to the resource. Please ensure that the proxy address is valid and reachable.");
		return false;
	}catch (IOException e) {
		System.out.println("Could not establish connect to resource: " + e.getMessage());
		e.printStackTrace();
		System.out.println("An input/output error has occured when trying to establish the connection to the resource");
		return false;
	}catch (SecurityException  e) {
		System.out.println("Could not establish connect to resource: " + e.getMessage());
		e.printStackTrace();
		System.out.println("A SecurityException has occured while trying to establish the connection to the resource. Please check to see that you are not being blocked by a firewall.");
		return false;
	}catch (IllegalArgumentException  e) {
		System.out.println("Could not establish connect to resource: " + e.getMessage());
		e.printStackTrace();
		System.out.println("While trying to establish the connection to the resource an IllegalArgumentException occured. Please ensure that the proxy address is valid.");
		return false;
	}catch(Exception e){
		System.out.println("Could not establish connect to resource: " + e.getMessage());
		e.printStackTrace();
		return false;
	}
	System.out.println("Content at destination address retrieved successfully.");
	return true;
}

}