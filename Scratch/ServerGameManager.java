import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerGameManager {

    public static Map<Integer, Socket> clientMap = new HashMap<Integer, Socket>();
    
    public static void addClient(int clientNum, Socket soc) {
        clientMap.put(clientNum, soc);
    }
    
    public static Set getClientSet() {
        return clientMap.entrySet();
    }

    /**
     * Application method to run the server runs in an infinite loop
     * listening on port 9898.  When a connection is requested, it
     * spawns a new handler thread to do the servicing and immediately 
     * returns to listening.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Server is running...");
        int clientNumber = 1;
        ServerSocket listener = new ServerSocket(9898);
        
        try {
            while (true) {
                new Handler(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A private thread to handle client requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private static class Handler extends Thread {
        private Socket socket;
        private int clientNumber;

        public Handler(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            
            try {
                socket.setKeepAlive(true);
            } catch (Exception e) {
                System.out.println("ERROR: Unable to keep socket alive for client #" + clientNumber);
                e.printStackTrace();
            }
            
            addClient(clientNumber, socket);

            log("New connection with client# " + clientNumber + " at " + socket);   
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading in
         */
        public void run() {
            try {
                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + clientNumber + ".");
                out.println("Enter a line with only a period to quit\n");

                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();
                    log("IN (" + clientNumber + "): " + input );
                    
                    if (input == null || input.equals(".")) {
                        break;
                    }

                    String output = input.toUpperCase();
                    
                    log("OUT (" + clientNumber + "): " + output);
                    out.println(output);
                }
            } catch (IOException e) {
                log("Error handling client #" + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client #" + clientNumber + " closed");
            }
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }
}
