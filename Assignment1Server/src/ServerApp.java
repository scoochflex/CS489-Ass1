import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * The class ServerApp will be used to store the ServerSocket object and will be created to listen for connections
 * on a port passed in. It will verify the validity of the port number, if it is invalid a free port will be 
 * selected to start listening for connections on 
 */

public class ServerApp {
	//Socket to manage connection with the currently connected client
	ServerSocket clientConnect;	

	File rootDir = new File("").getAbsoluteFile();
	
	// Port to listen to for incoming clients
	int port;

	public ServerApp(int n) {
		if (n < 1 && n > 65535) {
			System.out.println("Invalid port number. A valid port is between 1 and 65535. Server will be started on a free port.");
			port = 0;
		}else{
			port = n;
		}
		try {
			clientConnect = new ServerSocket(port);
			System.out.println("Server app listening for incoming clients on port: " + clientConnect.getLocalPort());
		}catch (IllegalArgumentException e) {
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void main(String args[]) {
		int arg1 = 2999;		
		try {
			if (args.length == 1) {
				arg1 = Integer.parseInt(args[0]);
			}else{
				System.out.println("SimpleServer takes a port as an arguement in the form of ServerApp <portNum>. The default port will be used.");
			}
			ServerApp server = new ServerApp(arg1);
			System.out.println("SimpleServer running on port" + arg1);
			server.listen();
		}
		catch (IOException e){
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
	/*
	 * The listen function is used cause a particular instance of the ServerApp to wait for requests
	 * to come in. It will parse the requests to determine what response should be send (404, 403, 200 etc...)
	 * It will also respond to requests using the same http version that was sent with the request. 
	 */
	public void listen() throws IOException {
		Boolean session=false;
		Boolean waitForClients=true;
		while(waitForClients) {
			System.out.println("Waiting for incoming clients");
			Socket clientReq = clientConnect.accept();
			System.out.println("A client has connected");
			DataInputStream clientIn = new DataInputStream(clientReq.getInputStream());
			OutputStreamWriter clientOut = new OutputStreamWriter(clientReq.getOutputStream());			
			byte[] b;
			String message = "";	
			session=true;
			//Session with client
			while (session) {
				boolean messageRead = false;
				//Listen for input from client
				//System.out.println("Listening for commands");
				// Listen for input from client
				int byteEst = 0;
				byteEst = clientIn.available();
				while (byteEst != 0) {
					System.out.println("Bytes to be read: " + byteEst);
					b = new byte[byteEst];
					clientIn.readFully(b, 0, byteEst);
					for(int i =0; i<b.length;i++){
						String first_char = String.valueOf((char) b[i]);
						message += first_char;
					}
					byteEst = clientIn.available();
				}
				
				if(!message.isEmpty()){		
					messageRead=true;
					System.out.print("Command received: ");
					System.out.println(message);
					
//					char [] charsToSend = new char[MAX_CHUNK_SIZE];
					
					HttpResponseHeader responseHeader = new HttpResponseHeader();

					//First parsed value is the resource path requested
					//Second parsed value is the http version that the request was sent in
					//Second parsed value is the hostname
					String[] parsedVals = parseIncomingMessage(message);
					String fileName = parsedVals[0];
				    File filePath = null ;
				    System.out.println("File Name: " + fileName + ", file path: " + filePath);
				    if(!fileName.isEmpty()){
				    	filePath = checkHaveResource(fileName);					    	
				    }
				    //If the filePath is empty we haven't found the file
				    if(!(filePath==null)){
				    	System.out.println("Requested filepath determined to be: " + filePath.toString());
				    	if(filePath.canRead()){
				    		BufferedReader fileReader = new BufferedReader(new FileReader(filePath.toString()));
//				    		int contentLength = 0;
//				    		boolean headerWritten = false;
//				    		boolean chunked = false;
				    		int character = fileReader.read();
				    		responseHeader = new HttpResponseHeader(parsedVals[1],200, "OK", Files.probeContentType(filePath.toPath()),filePath.length(), Files.getLastModifiedTime(filePath.toPath()).toString());
							clientOut.write(responseHeader.toString());
				    		//Read file into 
				    		while(character!=-1){				    			
				    			character=fileReader.read();
				    			clientOut.write(String.valueOf(((char)character)));
				    			//File is >10MB so we will write the header
//				    			if(contentLength>=MAX_CHUNK_SIZE || chunked){
//				    				chunked=true;
//				    				System.out.println("File is bigger than 10KB... Size of file is: " + contentLength);
//				    				if(!headerWritten){
//							    		responseHeader = new HttpResponseHeader(parsedVals[1],200, "OK", Files.probeContentType(filePath.toPath()),filePath.length(), Files.getLastModifiedTime(filePath.toPath()).toString());
//							    		System.out.println(responseHeader.toString());
//										clientOut.write(responseHeader.toString());
//										clientOut.flush();
//										headerWritten=true;
//				    				}
//				    				clientOut.write(Integer.toHexString(contentLength) + "\r\n");
//									clientOut.write(charsToSend);
//					    			clientOut.write("\r\n");
//									clientOut.flush();
//				    			}else{
//				    				System.out.println("File is less than 10KB, sending OK... Size of file is: " + contentLength);
//						    		responseHeader = new HttpResponseHeader(parsedVals[1],200, "OK", Files.probeContentType(filePath.toPath()), contentLength, Files.getLastModifiedTime(filePath.toPath()).toString());
//									clientOut.write(responseHeader.toString());									
//									clientOut.write(charsToSend);				    				
//									clientOut.flush();
//				    				break;
//				    			}
				    		}
				    		clientOut.flush();
//				    		if(chunked){
//			    				System.out.println("File chunking ending");
//				    			clientOut.write("0\r\n\r\n");
//				    			clientOut.flush();
//				    		}
				    		fileReader.close();
				    	}else{
		    				System.out.println("Sending error response 405... Size of file is: ");
				    		responseHeader = new HttpResponseHeader(parsedVals[1],405, "Method Not Allowed");
							clientOut.write(responseHeader.toString());
							clientOut.flush();
				    	}
				    }else{
	    				System.out.println("Sending error response 404... Size of file is: ");
				    	responseHeader = new HttpResponseHeader(parsedVals[1],404, "File Not Found");
						clientOut.write(responseHeader.toString());
						clientOut.flush();
				    }
				    
				}
				
				message="";
				if(messageRead){
					System.out.println("Session ending...");
					session = false;
					clientReq.close();					
				}
				//While session end
				}
		}
	}
	
	public File checkHaveResource(String filePathWithName){
		//Strip away file name and append path to the files directory in the server folder if we can
		int fileNameStartIndex = filePathWithName.lastIndexOf('/');
	    String fileName = fileNameStartIndex!=-1 ? filePathWithName.substring(fileNameStartIndex+1,filePathWithName.length()) : "" ;
	    String filePathNoName = fileNameStartIndex!=-1 ? filePathWithName.substring(0,filePathWithName.length()-fileName.length()) : "" ;
	    System.out.println("fileName: " + fileName + " filePathNoName: " + filePathNoName);
		File filePath = null;
		String filesDirPath = rootDir.getPath()+"/files" + filePathNoName;
	    File [] files = new File(filesDirPath).listFiles();
		for(File path:files){								
			if(path.isFile()){
				String pathTemp = path.getName();
				if(pathTemp.equalsIgnoreCase(fileName)){
					filePath = path;
					break;
				}
			}
		}		
		return filePath;		
	}
	
	public String [] parseIncomingMessage(String message){
		String [] lines = message.split("\n");		
		boolean getReceieved = false;
		String [] parsedVals = {"","",""};
		Matcher m;
		Pattern discardServerName = Pattern.compile("http://\\d+\\.\\d+\\.\\d+\\.\\d+:?\\d*(.*)[\r\n]*");
		Pattern requestTypeP = Pattern.compile("(\\w+)\\s([^ ]*)\\s[A-Za-z]+/(\\d\\.\\d)[\r\n]*");
		Pattern hostNameP = Pattern.compile("^[Hh]ost:\\s*(.*)[\r\n]*");
		int numLinesCheck = lines.length > 6 ? 6 : lines.length;
		for(int i=0; i<numLinesCheck;i++){
			m = requestTypeP.matcher(lines[i]); 
			if(m.matches() && m.group(1).equalsIgnoreCase("GET")){
				getReceieved=true;
				//First parsed value is the resource path requested (2nd match group)
				parsedVals[0]=m.group(2);
				//Second parsed value is the http version that the request was sent in
				parsedVals[1]=m.group(3);
				//Remove our address and just get the resource path
				m = discardServerName.matcher(m.group(2));
				if(m.matches()){
					parsedVals[0] = m.group(1);
				}		
			}
			if(getReceieved){
			m = hostNameP.matcher(lines[i]);
				if(m.matches()){
					//Third parsed value was the hostname
					parsedVals[2] = m.group(1);
				}
			}
		}
//		System.out.println("Host name receieved: " + parsedVals[2] + " Actual Name: " + clientConnect.getInetAddress().getHostAddress());
		//Ensure that the message was meant for us...
//		if(parsedVals[2].equalsIgnoreCase(clientConnect.getInetAddress().getHostAddress())){
			return parsedVals;
//		}
	}
	//Class to store and create response header as a string
	public class HttpResponseHeader{
		//Status line header data
		String method;
		int code;
		String httpVer;
		//Entity header data
		String contentType;
		long contentLength;
		boolean transferEncodeing;
		String lastModified;
		
		public HttpResponseHeader(){			
		}
		
		public HttpResponseHeader(String HttpVer, int Code, String Method,String ContentType,long ContentLength, String LastModified){
			httpVer=HttpVer;
			code = Code;
			method = Method;
			contentType =ContentType;
			if(ContentLength==0){
				transferEncodeing=true;
			}else{
				transferEncodeing=false;
			}
			contentLength = ContentLength;
			lastModified = LastModified;
		}
		
		public HttpResponseHeader(String HttpVer, int Code, String Method){
			httpVer=HttpVer;
			code = Code;
			method = Method;
			contentType ="";
			contentLength = 0;	
			lastModified = "";
		}
		
		public String toString(){
			String header="";
			//Status line in header
			header+="HTTP/"+httpVer + " " + code + " " + method + "\n";
			//General and response header data automatically gathered
			Date date = new Date();			
			header+="Date: " + DateFormat.getDateInstance().format(date)+ "\n";
			header+="Server: " +  System .getProperty("os.name")+ "\n";
//			header+="Accept-Ranges: bytes\n";
			//Entity header lines
			header+=contentType.isEmpty() ? "" : "Content Type: " + contentType + "\n";
			header+= transferEncodeing ? "Transfer-Encoding: chunked\n": "";
			header+=contentLength==0 ? "" : "Content Length: " + contentLength + "\n";
			header+=lastModified=="" ? "" : "Last Modified: " + lastModified + "\n";
			return header;
		}
	}
}