import java.io.*;
import java.net.*;
import java.time.LocalDate;

// ServerThread.java
// =========
// A thread will be spun up whenever an incoming client
// Needs to be served. This thread handles all requests
// from clients. The requests this server can handle:
//  * Send File
//  * Send File (Specific amount of bytes)
//  * Write File
//  * GET Request

public class ServerThread extends Thread 
{
    // Turn debug on (true) or off (false)
    private boolean debug = true;

    // Connection members
    private Socket connectionSocket = null;
    private BufferedReader inFromClient = null;
    private DataOutputStream outToClient = null;

    // Creates a ServerThread 
    public ServerThread(Socket socket)
    {
        this.connectionSocket = socket;
        getConnections();
    }

    // Method which handles GET requests on the server
    public void handleGET(String[] clientRequest) 
    {
        try 
        {
            // Create a filestream 
            InputStream fileInputStream;

            // Determine the file being requested
            if (clientRequest[1].equals("/"))
            {
                // The client is requesting the base of the server.
                // The default base of the server is index.html
                fileInputStream = new FileInputStream("index.html");
            }
            else
            {   
                // The client is requesting a specific file
                // Create a filestream if the file is available
                fileInputStream = new FileInputStream(clientRequest[1]);
            }

            // Create a Buffered Reader on the inputstream to more easily read the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            
            // Create the HTTP response header
            outToClient.writeBytes("HTTP/1.1 200 OK\r\n"); // Version & status code

            // TODO: Fix the date here
            outToClient.writeBytes("Date: " + LocalDate.now() + " \n");
            outToClient.writeBytes("Content-Type: text/html\r\n"); // The type of data
            outToClient.writeBytes("Connection: close\r\n"); // Will close stream
            outToClient.writeBytes("\r\n"); // End of headers
            
            String line;
            
            // Start reading the file. Send the file line by line to the client
            while ((line = reader.readLine()) != null)
                outToClient.writeBytes(line + "\n");

            // Close the reader 
            reader.close();

            return;
        } 
        catch (Exception e) 
        {
            // The file was not located on the server or we had an error
            // When we were sending the bytes to the client
            System.out.println("File not found");
        }
        
        try 
        {
            // If the file could not be found, create the HTTP response header
            outToClient.writeBytes("HTTP/1.1 404 Not Found \r\n"); 

            // TODO: Add in date to the response header

            outToClient.writeBytes("Content-Type: text/html\r\n"); 
            outToClient.writeBytes("Connection: close\r\n"); 
            outToClient.writeBytes("\r\n"); 
        } 
        catch (Exception e) 
        {
            // We had an error writing to the client    
        }
    }

    // Determines the client request and routes to the appropriate method
    public void determineRequest() throws Exception
    {
        System.out.println("Attempting to read from client..");
       
        String[] clientRequest = inFromClient.readLine().split(" ");

        if(clientRequest[0].equals("GET"))
        {
            handleGET(clientRequest);
            return;
        }

        if (clientRequest[1].equals("-w"))
        {
            System.out.println("Writing to file");
            writeFile(clientRequest[2]);
        }

        if (clientRequest.length < 2)
            sendFile(clientRequest[0], Integer.MAX_VALUE);
        else if (clientRequest.length < 3)
            sendFile(clientRequest[0], Integer.parseInt(clientRequest[1]));

    }

    public void writeFile(String filename) throws Exception
    {
        System.out.println("We are on the server. Lets write the file");

        File fileCheck = new File(filename);

        if (fileCheck.exists())
        {
            System.out.println("File exists on server");
            return;
        }

        // Create a local stream for file download
        OutputStream download = new FileOutputStream(filename);

        int byteRead;

        // Read from the input stream until we reach the end of the stream
        while ((byteRead = inFromClient.read()) != -1)        
        {
            // Write these bytes into a file on the clients machine
            download.write(byteRead);
        }

        // We have finished with downloading the file
        download.close();

    }

    // Sends a file if the file is on the server. 
    public void sendFile(String filename, int amount) throws Exception
    {
        System.out.println("Sending <" + filename + "> to <"+ connectionSocket.getInetAddress() + ">");
        
        try 
        {
            // Create a filestream 
            InputStream inputStream = new FileInputStream(filename);

            // If we got here, the file was found
            outToClient.writeBytes("File Found\n");

            // Get the file size
            double fileLengthBytes = new File(filename).length();

            // Variables 
            int byteRead;
            double byteCount = 0;
            int currPercent = 10;
            
            // Read the file byte by byte
            while ((byteRead = inputStream.read()) != -1 && byteCount <= amount) 
            {
                // Send bytes over TCP socket to the client
                outToClient.write(byteRead);

                // Print debug messages if requested
                if (debug)
                {
                    if ((byteCount++ / fileLengthBytes) * 100 >= currPercent)
                    {
                        System.out.println("Sent " + (int)(currPercent) + "% of <" + filename + ">");
                        currPercent += 10;

                        if (currPercent == 100)
                            System.out.println("Sent 100% of <" + filename + ">");
                    }
                }
            }

            System.out.println("Finished sending <" + filename + "> to <"+ connectionSocket.getInetAddress() + ">");

            // Close the connection
            inputStream.close();
        } 
        catch (Exception e) 
        {
            // In the event of an error,
            // let the client know that the file wasn't located on the server. 
            outToClient.writeBytes("File Not Found");
        }
    }

    // Crate the input and output connections
    public void getConnections()
    {
        try 
        {
            this.inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
            this.outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
        } 
        catch (Exception e) 
        {
            System.out.print("input / output connections could not be made on thread" + Thread.currentThread().getName());
        }
    }

    // Close all connections once the thread is finished
    public void closeConnections() throws Exception
    {
        this.inFromClient.close();
        this.outToClient.close();
        this.connectionSocket.close();
    }

    // Start the thread
    public void run()
    {
        try 
        {   
            // Read client request and route to appropriate method
            determineRequest();

            // End the session
            closeConnections();

            System.out.println("Client has disconnected");

        } 
        catch (Exception e) 
        {
            System.out.println("General run error in thread " + Thread.currentThread().getName());
        }

    }
}