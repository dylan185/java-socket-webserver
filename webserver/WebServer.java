/*
 * University of Texas at Arlington
 * CSE 4344 - Computer Networks Lab #1
 * Dylan Forsyth
 */
package webserver;

import java.util.*;
import java.io.*;
import java.net.*;
//import java.net.InetAddress;

/**
 *
 * @author Dylan
 */
public final class WebServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        // Set port
        int port = 6789;
        
        // Build listening socket
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Port Number is: " + serverSocket.getLocalPort());
        
        // Infinite loop to process requests
        while(true){
            // Listen
            Socket requestSocket = serverSocket.accept();
            
            // Create object to process message
            HttpRequest request = new HttpRequest(requestSocket);
            
            // Create and start new thread
            Thread thread = new Thread(request);
            thread.start();
        }
    }
    
}

// Build runnable class to pass the thread's constructor
final class HttpRequest implements Runnable{
    // strings for response later
    final static String CRLF = "\r\n";
    final static String HTML = "<HTML>";
//    final static String 
    Socket socket;
    
    // Socket Constructor
    public HttpRequest(Socket socket) throws Exception{
        this.socket = socket;
    }
    
    // Runs the incoming socket and catches if Exception occurs
    public void run(){
        try{
            processThread();
        } catch(Exception e){
            System.out.println(e);
        }
    }
    
    // Impliement Process
    private void processThread() throws Exception{
        // Declare Stuff File Play & Response Construction
        FileInputStream fins = null;
        boolean exists = true;
        boolean moved = false;
        String status, content, body = null; 
        
        // Get socket's input & output
        InputStream inStream = socket.getInputStream();
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
        
        // Build Buff Filter, get line from HTTP request
        BufferedReader buff = new BufferedReader(new InputStreamReader(inStream));
        String buffLine = buff.readLine();
        
        // Display
        System.out.printf("\nRequest is: "+ buffLine +"\n");
        System.out.printf("Requesting IP is: "+ socket.getInetAddress() +"\n");

        // Extract the wanted file and grab the name for checks, get rid of '/' at front
        StringTokenizer tok = new StringTokenizer(buffLine);
        tok.nextToken(); // get rid of 'GET'
        String requestedFile = tok.nextToken();
        String baseFileName = requestedFile.substring(1);//"." + baseFileName;
        System.out.println("Requested File is: " + requestedFile);
        
        // Get a relative path to file
        String filePath = new File("webserver/"+baseFileName).getAbsolutePath();
        
        // Try to open file and determine if it exists
        try{
            fins = new FileInputStream(filePath);
        }catch(FileNotFoundException e){
            exists = false; // nope!
        }
        
        // Check for index file call but with different path
        if(!exists && requestedFile.contains("index.html")){
            moved = true;
            filePath = new File("webserver/images/index.html").getAbsolutePath();
            fins = new FileInputStream(filePath);
        }
        // Create another simular check to fix kayne image if the 301 is triggered
        if(!exists && requestedFile.contains("webserver/images/zeeyee.png")){
            moved = true;
            filePath = new File("webserver/zeeyee.png").getAbsolutePath();
            fins = new FileInputStream(filePath);
        }
        
        /* 
         * Checks and then builds content message accordingly
         * 
         * If proper index.html is called, trigger 200 OK
         * 
         * If index.html is called but the wrong location, trigger 301 and
         * adjust the file call
         * 
         * If bad file call return 404
        */
        if(exists){
            status = "HTTP/1.1 200 OK";
            content = "Content-Type: " + getType(requestedFile) + CRLF;
        }
        else if(!exists && moved && requestedFile.contains("index.html")){
            status = "HTTP/1.1 301 Moved Permanently";
            requestedFile = "index.html";
            content = "Content-Type: " + getType(requestedFile) + CRLF;
            exists = true;
        } 
        else if(!exists && moved && requestedFile.contains("zeeyee.png")){
            status = "HTTP/1.1 301 Moved Permanently";
            requestedFile = "images/zeeyee.png";
            content = "Content-Type: " + getType(requestedFile) + CRLF;
            exists = true;
        } 
        else{
            status = "HTTP/1.1 404 Object Not Found";
            content = "Content-Type: " + getType(requestedFile) + CRLF;
            body = "<HTML><HEAD><TITLE>No Title Found</TITLE></HEAD><BODY>Not Body Found</BODY></HTML>";
        }
        
        // Send everything to page, "To Infinity and BEYOND!!"
        outStream.writeBytes(status);
        outStream.writeBytes(content);
        outStream.writeBytes(CRLF);
        if(exists){
            ship(fins, outStream);
            fins.close();
        } else{
            outStream.writeBytes(body);
        }
        
        // Close everything after finished
        outStream.close();
        socket.close();
        buff.close();
    }
    
    private static String getType(String name){
        String lowName = name.toLowerCase();
        if(lowName.endsWith(".htm") || lowName.endsWith(".html")){
            return "text/html";
        } else if(lowName.endsWith(".gif")){
            return "image/gif";
        } else if(lowName.endsWith(".jpeg")){
            return "image/jpeg";
        } else if(lowName.endsWith(".rtf")){
            return "text/enriched";
        } else if(lowName.endsWith(".png")){
            return "image/png";
        } else{
            return "text/plain";
        }
        
    }
    
    // Create buffer for bytes then copy them to outstream
    private static void ship(FileInputStream ins, OutputStream outs) throws Exception{
        byte[] byteBuffer = new byte[1024];
        int b = 0;
        while((b = ins.read(byteBuffer)) != -1){
            outs.write(byteBuffer, 0, b);
        }
    }
}
