//
//    Nhan Vu 1001193930
//


import java.util.*;   
import java.io.*;     //for exception and file transfer
import java.net.*;    //for sockets
import java.text.*;   //for date format

public class HttpServer implements Runnable {
   
   //universal client socket
   Socket clientSocket;

   //constructor to set ssocket to current using one
   //also possible by using port number to make a constructor
   HttpServer(Socket clientSocket) {
      this.clientSocket = clientSocket;
   }

   //main program creates threads when client connects
   public static void main(String args[]) throws Exception { 
      
      int port = 9000;  //declare port
      //server socket declaration
      ServerSocket serverSocket = new ServerSocket(port);
      
      //print out statement to declare program's start
      System.out.println("\nListening on port " + port + "\n");
      
      while (true) {
         Socket sock = serverSocket.accept();
         System.out.println("A client has connected\n");
         new Thread(new HttpServer(sock)).start();
      }
   }

   //when a thread is made it is ran here
   public void run() {

      try {
        //bring connection into stream
        BufferedReader request = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter response = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
         
        //prints line to terminal to seperate individual request 
        System.out.println("Request is is: \n -------------------------------------");
        
        //Get first line of Header
        String request_str = request.readLine(); 
        //get each word separated by space
        String[] tokens = request_str.split(" ");
        
        //301 or 200 in the try only, the catch does 404
        try{
          //file we are trying to retrieve
          String filename = (tokens[1].substring(1));  

          //if the file is in the previous directory
          if (new File(filename).isDirectory()) {
          filename=filename.replace('\\', '/');
          //compose header
          response.write("HTTP/1.0 301 Moved Permanently\r\n");
          //compose location
          response.write("Location: /"+filename+"/\r\n");
          //compose date
          Date now = new Date();
          response.write("Date:"+now+"\r\n"); 
          //compose server
          response.write("Server: Apache/1.3.0\r\n");
          //compose file last modified
          response.write("Last-modified: Tue Feb 27 21:44:07 CST 2018\r\n");
          //Formating page
          response.write("<TITLE>OOPS</TITLE>");
          response.write("<P>File has been moved Permanently.</P>");
          response.write("\r\n\r\n");

          response.close();
          request.close();
          clientSocket.close();
          return;
          }

          //try to retrieve file in directory 
          //failure results in file not found
          InputStream file = new FileInputStream(filename);

          //print to terminal the request  
          System.out.println(request_str);

          //print request to terminal now
          //loop through each line of the request
          while ((request_str = request.readLine()) != null) {   //check if end of request
            System.out.println(request_str);    //print request line to terminal
              if (request_str.isEmpty()) {      //stop at the end of request
                break;
              }
          }

          //200 response processing starts here
          //------------------------------------------------
          //compose response header
          response.write("HTTP/1.0 200 OK\r\n");
          //compose date
          Date now = new Date();
          response.write("Date:"+now+"\r\n"); 
          //compose server
          response.write("Server: Apache/1.3.0\r\n");
          //compose content
          //if html was requested
          if(filename.endsWith(".html")){ 
            response.write("Content-Type: text/html\r\n");
          }//if jpeg was requested
          else if(filename.endsWith(".jpeg")){ 
            response.write("Content-Type: image/jpeg\r\n");
          }//else any other type
          //more types of files could be added by using more else if
          else{
            response.write("Content-Type: text/plain\r\n");
          }

          //compose file last modified
          response.write("Last-modified: Tue Feb 27 21:44:07 CST 2018\r\n");
          response.write("\r\n");

          //Formating page
          response.write("<TITLE>OK</TITLE>");
          response.write("<P>File retrieved successfully.</P>");
    
          //sending of data
          //file is at most 1mb
          byte[] data =new byte[1000000];
          int n,i = 0;
          //until you're out of space, store file into byte array
          while ((n = file.read(data))>0){
            for (i = 0; i<= n; i++){
              //write data into response
              response.write(data[i]);
            }
          }
          //house keeping - close connections when done
          //note that you must close response before request for safety 
          //else you will get "java.net.SocketException: Socket closed" error
          response.close();
          request.close();
          clientSocket.close(); 
          }
          //if 404 exception is ever found
          catch(FileNotFoundException error){
            //compose response header
            response.write("HTTP/1.0 404 NOT FOUND\r\n");
            //compose date
            Date now = new Date();
            response.write("Date:"+now+"\r\n"); 
            //compose server
            response.write("Server: Apache/1.3.0\r\n");
            //compose content
            response.write("Content-Type: text/html\r\n");
            //compose file last modified
            response.write("Last-modified: Tue Feb 27 21:44:07 CST 2018\r\n");
            response.write("\r\n");

            //compose actual data
            response.write("<TITLE>OOPS!</TITLE>");
            String filename = (tokens[1].substring(1));
            response.write("<P>404 File "+filename+" Not Found</P>");
            //print to terminal that error has occured 
            System.out.println("404 NOT FOUND");
            //house keeping - close connections when done
            //note that you must close response before request for safety 
            //else you will get "java.net.SocketException: Socket closed" error
            response.close();
            request.close();
            clientSocket.close(); 
          }       
       } 
       //exception handling for the whole thing
       catch (IOException error) {
          System.err.println(error);
      }
   }
}