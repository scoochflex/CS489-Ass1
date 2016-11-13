
import java.net.*;
import java.nio.channels.SocketChannel;
import java.io.*;
import java.util.Scanner;

public class ClientApp {
	// Our socket connection to the server
	protected static Socket connection;
	URLConnection serverConnection;
	DataInputStream fromServer;
	Proxy proxy;
	// The input command stream from the server
	String proxy_address;
	int port;
//Pass in the IP and port the proxy is running on....
public ClientApp(String server, int remote_port) {
	proxy_address=server;
	port=remote_port;
	try {
	InetAddress proxy_inet_address = InetAddress.getByName(proxy_address);
	InetAddress local_address=InetAddress.getByName("127.0.0.1");
	SocketAddress proxy_socket_address = new InetSocketAddress(proxy_inet_address,port);
	SocketAddress local_socket_address = new InetSocketAddress(local_address,3000);
	proxy = new Proxy(Proxy.Type.HTTP, proxy_socket_address);
//	connection = new Socket(proxy);
//	connection.bind(local_socket_address);
//	connection = new Socket(proxy_address, port, local_address, 3000);

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
//public void sendCommand(String text) { 
//	try {
//		serverConnection.setDoOutput(true);
//		OutputStreamWriter toServer = new OutputStreamWriter (serverConnection.getOutputStream());
//		toServer.write(text);
//		toServer.flush();
//		toServer.close();
//	}catch (Exception e) {
//		System.out.println("Could not send message to the server: " + e.getMessage());
//		e.printStackTrace();
//		return;
//	}
//}

public String waitForResponse() throws IOException {
		String message = "";	
		byte[] b;
		int byteEst = 0;
		System.out.println("Listening for commands");
		while (message.isEmpty()) {
			byteEst = fromServer.available();
			while(byteEst!=0){
				System.out.println("Bytes to be read: " + byteEst);
				b = new byte[byteEst];
				fromServer.readFully(b, 0, byteEst);
				for(int i =0; i<b.length;i++){
					String first_char = String.valueOf((char) b[i]);
					message += first_char;
				}
				byteEst = fromServer.available();
			}
		}
		System.out.println(message.length() + "Message is: " + message);
		return message;
}

public boolean connectToDestination(String address){
	try {
//	InetSocketAddress remote_address = new InetSocketAddress(address, remote_port);
//	System.out.println("Trying to connect to the server at address " + address + ":" + remote_port);
//	connection.connect(remote_address);
//	InetSocketAddress remote_address = new InetSocketAddress(address, remote_port);
	URL destination = new URL(address);
	serverConnection = destination.openConnection(proxy);
	serverConnection.connect();
	fromServer = new DataInputStream(serverConnection.getInputStream());
//	sendCommand("CONNECT "+ remote_address.toString()+ " HTTP/1.1\nUser-Agent: Java/1.8.0_101\nHost: "+remote_address.toString()+"\nAccept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\nProxy-Connection: keep-alive\n");
	}catch (Exception e) {
		System.out.println("Could not establish connect to resource: " + e.getMessage());
		e.printStackTrace();
		return false;
	}
	System.out.println("Content at destination address retrieved successfully.");
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