
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
//Pass in the IP and port the proxy is running on....
public ClientApp(String server, int remote_port) {
	proxy=server;
	port=remote_port;
	try {
	InetAddress proxy_address = InetAddress.getByName(proxy);
	InetAddress local_address=InetAddress.getByName("127.0.0.1");
	SocketAddress proxy_socket_address = new InetSocketAddress(proxy_address,port);
	SocketAddress local_socket_address = new InetSocketAddress(local_address,3001);
	Proxy proxy = new Proxy(Proxy.Type.HTTP, proxy_socket_address);
	connection = new Socket(proxy);
	connection.bind(local_socket_address);
//	connection = new Socket(server_address.getHostAddress(), remote_port,local_address,local_port);	
	}
	catch (Exception e) {
		System.out.println("Could not connect to proxy: " + e.getMessage());
		e.printStackTrace();
		System.exit(0);
	}
	System.out.println("Connection to server established");
}
// GET http://www.w3.org/pub/WWW/TheProject.html HTTP/1.1
public void sendCommand(String text) { 
	try {
		OutputStreamWriter toServer = new OutputStreamWriter (connection.getOutputStream());
		toServer.write(text.length());
		toServer.write(text);
		toServer.flush();
	}catch (Exception e) {
		System.out.println("Could not send message to the server: " + e.getMessage());
		e.printStackTrace();
		return;
	}
}

public String waitForResponse() throws IOException {
		DataInputStream fromServer = new DataInputStream(connection.getInputStream());
		String message = "";	
		byte[] b = null;
		int temp = 0;
		System.out.println("Listening for commands");
		temp=fromServer.readByte();
		System.out.println(temp);
		byte[] bytes=new byte[temp];
		fromServer.readFully(bytes);
		message = new String(bytes);
		fromServer.close();
		return message;
}

public boolean connectToDestination(String address, int remote_port){
	try {
	InetSocketAddress remote_address = new InetSocketAddress(address, remote_port);
	System.out.println("Trying to connect to the server at address " + address + ":" + remote_port);
	connection.connect(remote_address);
	}catch (Exception e) {
		System.out.println("Could not establish connect to resource: " + e.getMessage());
		e.printStackTrace();
		return false;
//		System.exit(0);
	}
	System.out.println("Connection to resource established");
	return true;
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