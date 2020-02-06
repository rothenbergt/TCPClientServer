# TCPClientServer
Java implementation of client/server TCP socket file server

TCPClient.java
=========
Client program which may perform the following:
 * Download a file
 * Download a partial file
 * Upload a file
 * Perform a GET request

 Command line syntax:
 --------------------
 * <hostname> [-w] <filename> <byteRange>

 * GET <filename> <HTTPVersion>
   HOST: <hostname>


Command line argument1: host domain name
Command line argument2: filename [-w] flag to write to server
Command line argument3: byte range
