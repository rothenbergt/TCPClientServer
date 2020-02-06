import java.io.*; 
import java.net.*; 
import java.util.*;

// TCPClient.java
// =========
// Client program which may perform the following:
//  * Download a file
//  * Download a partial file
//  * Upload a file
//  * Perform a GET request

//  Command line syntax:
//  --------------------
//  * <hostname> [-w] <filename> <byteRange>
// 
//  * GET <filename> <HTTPVersion>
//    HOST: <hostname>


// Command line argument1: host domain name
// Command line argument2: filename [-w] flag to write to server
// Command line argument3: byte range

public class TCPClient
{ 
  private Socket clientSocket = null; 
  private DataOutputStream outToServer = null;
  private BufferedReader inFromServer = null;

  public TCPClient(String[] args)
  {
    // Check if the user has given command line arguments
    if (args.length < 1)
    {
      System.out.println("Proper Syntax: <hostname> [-w] <filename> <byteRange>");
      return;
    }

    try 
    {
      // If the first argument is GET, we have a get request. 
      // This is handled differently as the HOSTNAME is entered
      // as the second line of input
      if (args[0].equals("GET"))
      {
        HTTPGet(args);
        return;
      }

      // Otherwise, create a socket connection 
      getConnection(args[0]);

      // determine the request type
      determineRequest(args);
    } 
    catch (Exception e) 
    {
      //TODO: handle exception
    }
    
  }

  // Initalizes all connections necessary for TCP transfer
  public void getConnection(String ip) throws Exception
  {
    // Get the hostname given the IP address
    InetAddress inet = InetAddress.getByName(ip);
    String hostname = inet.getHostName();

    this.clientSocket = new Socket(hostname, 6701);
    this.outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
    this.inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
  }

  // Closes all TCP connections
  public void closeConnection() throws Exception
  {
    this.outToServer.close();
    this.inFromServer.close();
    this.clientSocket.close();
    this.clientSocket = null;
  }

  // Determines the type of request
  public void determineRequest(String[] arguments) throws Exception
  {
    // If the arguments are empty, or out of range
    if (arguments == null || arguments.length == 1 || arguments.length >= 4)
    {
      System.out.println("Proper Syntax: <hostname> [-w] <filename> <byteRange>");
      return;
    }

    // The user is downloading a file
    if (arguments.length == 2)
    {
      // If someone is trying to write a file, but they forgot the filename
      // Let them know how to fix the error.
      if (arguments[1].equals("-w"))
      {
        System.out.println("Must include filename when writing a file");
        System.out.println("Proper Syntax: <hostname> [-w] <filename> <byteRange>");

        // TODO: The server has an error here
        return;
      }

      // Get a file
      getFile(arguments[1], Integer.MAX_VALUE);
    }

    // The user is writing to a file or downloading a range of bytes
    if (arguments.length == 3)
    {
      // If the user is writing a file to the server
      if (arguments[1].equals("-w"))
        writeFile(arguments[2]);
      
      // Otherwise, they are getting a file from the server
      getFile(arguments[1], Integer.parseInt(arguments[2]));
    }
    
  } 

  // Attempt to recieve a complete file from the fileserver
  public void getFile(String filename, int range) throws Exception
  {    
      // Send the server the name and range
      outToServer.writeBytes(filename + " " + range + "\n");

      // Read the server response
      String check = inFromServer.readLine();
      
      // Check if the file was found or not
      if (check.equals("File Not Found"))
      {
        System.out.println("File not on server.");
        return;
      }
      
      // If the file was found, create a local stream for file download
      OutputStream download = new FileOutputStream(filename);

      int byteRead;
      int byteCount = 1;

      // Read from the input stream until we reach the end of the stream
      while ((byteRead = inFromServer.read()) != -1 && byteCount++ <= range)        
      {
        // Write these bytes into a file on the clients machine
        download.write(byteRead);
      }

      // Clean up connections
      download.close();
      closeConnection();

  }

  // Attempt to write a file to the file server
  public void writeFile(String filename) throws Exception
  {
    // Let the server know you plan on writing so they can get setup
    outToServer.writeBytes("hostname " + "-w " + filename + "\n");

    // Create a filestream 
    InputStream inputStream = new FileInputStream(filename);

    int byteRead;
    
    // Send file byte by byte through TCP until we've reached EOF
    while ((byteRead = inputStream.read()) != -1) 
      outToServer.write(byteRead);

    System.out.println("Finished sending");

    // Close the connection
    inputStream.close();
  }

  // GET request to the server
  public void HTTPGet(String[] args) throws Exception
  {
    // Get host GET request information from user
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    
    // Split the line up by spaces
    String[] host = inFromUser.readLine().split(" ");
    
    // Get a TCP socket connection with the given hostname
    getConnection(host[1]);

    // Send the server the filename we are interested in
    outToServer.writeBytes(args[0] + " " + args[1] + " " + args[2] +  '\n'); 

    String line;

    // Get the file line by line from the server and print it to the screen. 
    while ((line = inFromServer.readLine()) != null)
      System.out.println(line);

  }
  public static void main(String[] args) throws Exception 
  {
      TCPClient client = new TCPClient(args);
  } 
} 
