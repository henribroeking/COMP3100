import java.io.BufferedReader; // Import BufferedReader for reading data from server
import java.io.IOException; // Import IOException for handling exceptions during I/O operations
import java.io.InputStreamReader; // Import InputStreamReader for converting bytes to characters
import java.io.PrintWriter; // Import PrintWriter for sending data to the server
import java.net.Socket; // Import Socket for establishing a connection with the server
import java.util.ArrayList; // Import ArrayList for storing server data
import java.util.List; // Import List for general-purpose list operations

class Server {
    String type; // Server type
    int id; // Server ID
    int cores; // Number of cores in the server

    public Server(String type, int id, int cores) { // Constructor for Server class
        this.type = type;
        this.id = id;
        this.cores = cores;
    }
}

public class ClientSimulator {
    private static final String SERVER_ADDRESS = "localhost"; // Server address (hostname/IP)
    private static final int SERVER_PORT = 50000; // Server port number
    private static final String USERNAME = "username"; // Username for authentication

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // Create a socket and establish connection
                                                                         // with the server
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Create PrintWriter for sending
                                                                                   // data to the server
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Create
                                                                                                        // BufferedReader
                                                                                                        // for reading
                                                                                                        // data from the
                                                                                                        // server
        ) {
            String message; // Variable to store messages from the server
            List<Server> largestServers = new ArrayList<>(); // List to store the largest servers found
            int roundRobinIndex = 0; // Index to keep track of the current server to assign a job to in the
                                     // round-robin process

            out.println("HELO"); // Send HELO message to initiate the handshake
            System.out.println("Sent: HELO");
            message = in.readLine(); // Read server response
            System.out.println("Received: " + message);

            out.println("AUTH " + USERNAME); // Send AUTH message with the username for authentication
            System.out.println("Sent: AUTH " + USERNAME);
            message = in.readLine(); // Read server response
            System.out.println("Received: " + message);

            out.println("REDY"); // Send REDY message to indicate readiness for receiving server specs
            System.out.println("Sent: REDY");

            // Get the largest server type
            out.println("GETS All"); // Send GETS All message to request all server records
            System.out.println("Sent: GETS All");
            message = in.readLine(); // Read server response
            System.out.println("Received: " + message);

            String[] parts = message.split(" "); // Split the message into parts
            int nRecs = Integer.parseInt(parts[1]); // Get the number of server records

            out.println("OK"); // Send OK message to acknowledge receipt of record count
            System.out.println("Sent: OK");

            int maxCores = 0; // Variable to store the maximum number of cores found
            for (int i = 0; i < nRecs; i++) { // Iterate through server records
                message = in.readLine(); // Read server record
                System.out.println("Received: " + message);
                parts = message.split(" "); // Split the record into parts

                if (parts.length >= 5) { // Check if the record has enough parts
                    String type = parts[0]; // Get server type
                    int id = Integer.parseInt(parts[1]); // Get server ID
                    int cores = Integer.parseInt(parts[4]);

                    if (cores > maxCores) { // Check if the current server has more cores than the previous maximum
                        maxCores = cores; // Update the maximum number of cores
                        largestServers.clear(); // Clear the list of largest servers
                        largestServers.add(new Server(type, id, cores)); // Add the new largest server to the list
                    } else if (cores == maxCores) { // Check if the current server has the same number of cores as the
                                                    // maximum
                        largestServers.add(new Server(type, id, cores)); // Add the server to the list of largest
                                                                         // servers
                    }
                } else {
                    System.out.println("Unexpected message format: " + message); // Print error message for unexpected
                                                                                 // message format
                }
            }

            out.println("OK"); // Send OK message after processing all server records
            System.out.println("Sent: OK");
            message = in.readLine(); // Read server response
            System.out.println("Received: " + message);

            // Main scheduling loop
            while (true) { // Keep running the loop until a "NONE" message is received
                message = in.readLine(); // Read server message
                System.out.println("Received: " + message);

                if (message.startsWith("JOBN")) { // Check if the message starts with "JOBN"
                    String[] jobDetails = message.split(" "); // Split the job message into parts

                    // Schedule the job using LRR
                    Server server = largestServers.get(roundRobinIndex); // Get the server to assign the job to in the
                                                                         // round-robin process
                    out.println("SCHD " + jobDetails[2] + " " + server.type + " " + server.id); // Send SCHD message
                                                                                                // with job, server
                                                                                                // type, and server ID
                    System.out.println("Sent: SCHD " + jobDetails[2] + " " + server.type + " " + server.id);

                    roundRobinIndex = (roundRobinIndex + 1) % largestServers.size(); // Update the roundRobinIndex for
                                                                                     // the next iteration
                } else if (message.startsWith("NONE")) { // Check if the message starts with "NONE"
                    break; // Exit the loop
                } else {
                    out.println("ERR"); // Send ERR message for unexpected message format
                    System.out.println("Sent: ERR");
                }
            }
            out.println("QUIT"); // Send QUIT message to terminate the connection
            System.out.println("Sent: QUIT");
            message = in.readLine(); // Read server response
            System.out.println("Received: " + message);

        } catch (IOException e) { // Catch any IOExceptions that occur during the connection
            e.printStackTrace(); // Print the stack trace for the exception
        }
    }
}