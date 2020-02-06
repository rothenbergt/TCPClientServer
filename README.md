TCPClient.java
=========
Client program which may perform the following:
 * Download a file
 * Download a partial file
 * Upload a file
 * Perform a GET request

TCPServer.java
=========
This program will create a a TCPServer which listens to a specific port.
The server has threads to handle the communication of incoming clients.

ServerThread.java
=========
A thread will be spun up whenever an incoming client
Needs to be served. This thread handles all requests
from clients. The requests this server can handle:
 * Send File
 * Send File (Specific amount of bytes)
 * Write File
 * GET Request
