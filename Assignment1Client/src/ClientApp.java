
import java.net.*;
//import java.nio.channels.SocketChannel;
import java.io.*;
import java.util.Arrays;
/*
 * This class is used to manage and establish the a connection 
 * to a remote resource through a proxy at a specific port and IP
 * It is instantiated in the client app interface class
 */
public class ClientApp {
	Socket destConnect;
	//Our data input stream to recieve messages forwarded by the proxy (from the server)
	BufferedReader fromServer;
	OutputStreamWriter toServer;
	String proxyAddress;
	int proxyPort;
	
/*
 * Pass in the IP and port the proxy is running on, after instantiation of the client app 
 * assigns given values to proxyAddress/Port which are used whe  attempting to set up the connection
 */
public ClientApp(String proxyAddress, int proxyPort) {
		this.proxyAddress = proxyAddress;
		this.proxyPort = proxyPort;
}

/*
 * This function is used by an instance of client app to receive data from the proxy.
 * Any exceptions thrown will be handled in the ClientAppInterface that called this method.
 * It will call getContentLength to obtain the content length from a header
 */
public void getResponse() throws Exception {
		//Open the data input stream from that resource
		fromServer = new BufferedReader(new InputStreamReader(destConnect.getInputStream()));
		//Initialize an empty message
		String message = "";
		//flag that indicates that the header has been stripped from the message
		boolean headerRead = false;
		boolean messageIsReady = false;
		//Initialize a byte array to hold the data before it is converted from UTF-8 format to a string
		char[] b = new char[10000];
		long totalBytesRead = 0;
		long contentLength = 0;
		//Initialize an integer to store the estimated number of bytes coming in through the data input stream
		int byteEst = 1;
		//Indicate to the user that we are waiting on a response from the server
		System.out.println("Waiting for a response from the server...");
		//This loop ensures that we block until a message is returned to us by the proxy
		while (!messageIsReady) {
			
			while(byteEst>0){
				byteEst = fromServer.read(b,0,10000);
				if(totalBytesRead==0){
					System.out.println("Message is:");
				}
				totalBytesRead+=byteEst;
				if(!headerRead){
					message+=String.valueOf(Arrays.copyOf(b,byteEst));
					System.out.println(message);
					if(message.equalsIgnoreCase("Could not connect to host")){
						messageIsReady = true;
						break;
					}
					int headerEnd = message.indexOf("\r\n\r\n");	
					if(headerEnd!=-1){
						String header = message.substring(0,headerEnd);
						message = message.substring(headerEnd+2,message.length());
						contentLength = getContentLength(header);
						headerRead=true;
					}
				}else{
					System.out.println(String.valueOf(b));
				}
				//Ensure that there is no more data to be read before returning
				if((contentLength<=totalBytesRead) && headerRead){
					messageIsReady = true;
					break;
				}else if(headerRead && contentLength==-1 && !message.isEmpty()){
					messageIsReady = true;
					break;
				}
			}

		}
		//Send a close connection message to the proxy and close the connection
		closeConnection();
}

/*
 * This function is used to parse a valid header and return the content length field if it exists
 * It is called in getReponse, if the length field was not found a value of -1 is returned
 */
public long getContentLength(String header){
	long length = -1;
	int contentPos=header.indexOf("Content-Length: ");
	if(contentPos!=-1){
		int ret = header.indexOf('\n',contentPos);
		String len = header.substring(contentPos+"Content-Length: ".length(),ret-1);
		System.out.println("Content-Length determined to be: " + len);
		length = Long.parseLong(len);
	}
	return length;
}
/*
 * This method is called in getResponse once a response is deemed to be complete and the connection is closed
 */
public void closeConnection() throws Exception{
	toServer.write("Message Receieved. Close Connection");
	toServer.flush();
	fromServer.close();
	fromServer = null;
	destConnect.close();	
}

/*
 * This function creates a connection to the proxy server. It is called in the client app interface class's main method
 * It returns a boolean indicating weather or not the attempt to connect to the proxy was successful
 * 
 */
public boolean connectToDestination(String address){
	try {
	URL destination=  new URL(address);
	//Create a new socket for this new connection based on the proxy address and port defined in the constructor
	destConnect = new Socket(proxyAddress, proxyPort);
	toServer = new OutputStreamWriter(destConnect.getOutputStream());
	String request = new HttpRequestHeader(address, destination.getHost().toString()).toString();
	toServer.write(request);
	toServer.flush();
	//Catch potential errors. Print messages for specific error types
	}catch (MalformedURLException e) {
		System.out.println("Could not establish a connection to address: " + address);
		System.out.println("A MalformedURLException occurred while trying to establish the connection to the resource.\nPlease ensure that address entered is in the form of a valid url e.g. http://java.sun.com/FAQ.html");
		return false;
	}
	catch (SocketTimeoutException e) {
		System.out.println("A SocketTimeoutException occurred while trying to establish the connection to the resource.\nPlease ensure that the proxy address is valid and reachable.");
		System.out.println("Could not establish a connection to resource: " + e.getMessage());
		return false;
	}catch (IOException e) {		
		System.out.println("An input/output error has occurred when trying to establish the connection to the resource.");
		System.out.println("Please ensure that the proxy is running and is located at: " + proxyAddress + ":" + proxyPort);
		System.out.println("Could not establish a connection to resource: " + e.getMessage());
		return false;
	}catch (SecurityException  e) {
		System.out.println("A SecurityException has occurred while trying to establish the connection to the resource.\nPlease check to see that you are not being blocked by a firewall.");
		System.out.println("Could not establish a connection to resource: " + e.getMessage());
		return false;
	}catch (IllegalArgumentException  e) {
		System.out.println("While trying to establish the connection to the resource an IllegalArgumentException occurred.\nPlease ensure that the proxy address is valid.");
		System.out.println("Could not establish a connection to resource: " + e.getMessage());
		return false;
	}catch(Exception e){
		System.out.println("Could not establish a connection to resource: " + e.getMessage());
		return false;
	}
	System.out.println("A connection to the proxy has been established.");
	return true;
}
	/*
	 * The HttpResponseHeader class is used to store and create response header as a string
	 * it formats these values into a correct response header when the toString method is called.
	 * It is used in listen to generate a response based on weather or not a value is found.
	 * 
	 */
	public class HttpRequestHeader{
		//Status line header data
		String destination;
		String httpVer = "1.0";
		//Entity header data
		String host;
		
		//Default constructor
		public HttpRequestHeader(){			
		}
		
		//Constructor that assigns values to destination and host
		public HttpRequestHeader(String destination, String host){
			this.destination = destination;
			this.host = host;
		}
		
		/* This toString function inserts the classes values into the correct places
		 * and generates required information for the http response header
		 * It returns the formatted response as a string
		 */
		public String toString(){
			String header="";
			//Status line in header
			header+="GET " + destination + " " + "HTTP/"+httpVer+"\r\n";
			//General and response header data automatically gathered
			header+="User-Agent: Java/1.8.0_101" + "\r\n";
			header+="Host: " + host + "\r\n";
			header+="Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\r\n";
			//Entity header lines
			header+= "Proxy-Connection: keep-alive\r\n\r\n";
			return header;
		}
	}
}