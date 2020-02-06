import java.net.*; 

// TCPServer.java
// =========
// This program will create a a TCPServer which listens to a specific port.
// The server has threads to handle the communication of incoming clients.


public class TCPServer
{
    private final int DEFAULT_PORT = 6701;
    private ServerSocket serverSocket = null;

    // Create a default server socket
    TCPServer()
    {
        try 
        {
            // Create the ServerSocket
            this.serverSocket = new ServerSocket(DEFAULT_PORT); 

            // Let the server listen on the port
            listen();
        } 
        catch (Exception e) 
        {
            System.out.println("Socket could not be established on port " + DEFAULT_PORT);
        }
    }

    // Create a server socket with a specified port
    TCPServer(int port)
    {
        try 
        {
            // Create the ServerSocket
            this.serverSocket = new ServerSocket(port); 
            listen();
        } 
        catch (Exception e) 
        {
            System.out.println("Socket could not be established on port " + port);
        }
    }

    // Start listening on the serverSocket
    public void listen() throws Exception
    {
        // Check to make sure socket is available
        if (this.serverSocket == null)
        {
            System.out.println("Socket has not been established. Exiting");
            return;
        }
        
        // Sit waiting for clients to talk to us
        while(true)
        {
            // When a client approaches
            Socket socket = this.serverSocket.accept();

            // Take care of their request
            new ServerThread(socket).start();
        }

    }

    public static void main (String[] args)
    {
        TCPServer server = new TCPServer();
    }
}