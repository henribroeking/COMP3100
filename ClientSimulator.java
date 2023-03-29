import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class Server {
    String type;
    int id;
    int cores;

    public Server(String type, int id, int cores) {
        this.type = type;
        this.id = id;
        this.cores = cores;
    }
}

public class ClientSimulator {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 50000;
    private static final String USERNAME = "username";

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
            String message;
            List<Server> largestServers = new ArrayList<>();
            int roundRobinIndex = 0;

            out.println("HELO");
            System.out.println("Sent: HELO");
            message = in.readLine();
            System.out.println("Received: " + message);

            out.println("AUTH " + USERNAME);
            System.out.println("Sent: AUTH " + USERNAME);
            message = in.readLine();
            System.out.println("Received: " + message);

            out.println("REDY");
            System.out.println("Sent: REDY");

            // Get the largest server type
            out.println("GETS All");
            System.out.println("Sent: GETS All");
            message = in.readLine();
            System.out.println("Received: " + message);

            String[] parts = message.split(" ");
            int nRecs = Integer.parseInt(parts[1]);

            out.println("OK");
            System.out.println("Sent: OK");

            int maxCores = 0;
            for (int i = 0; i < nRecs; i++) {
                message = in.readLine();
                System.out.println("Received: " + message);
                parts = message.split(" ");

                String type = parts[0];
                int id = Integer.parseInt(parts[1]);
                int cores = Integer.parseInt(parts[4]);

                if (cores > maxCores) {
                    maxCores = cores;
                    largestServers.clear();
                    largestServers.add(new Server(type, id, cores));
                } else if (cores == maxCores) {
                    largestServers.add(new Server(type, id, cores));
                }
            }

            out.println("OK");
            System.out.println("Sent: OK");
            message = in.readLine();
            System.out.println("Received: " + message);

            // Main scheduling loop
            while (true) {
                message = in.readLine();
                System.out.println("Received: " + message);

                if (message.startsWith("JOBN")) {
                    String[] jobDetails = message.split(" ");

                    // Schedule the job using LRR
                    Server server = largestServers.get(roundRobinIndex);
                    out.println("SCHD " + jobDetails[2] + " " + server.type + " " + server.id);
                    System.out.println("Sent: SCHD " + jobDetails[2] + " " + server.type + " " + server.id);

                    roundRobinIndex = (roundRobinIndex + 1) % largestServers.size();
                } else if (message.startsWith("NONE")) {
                    break;
                } else {
                    out.println("ERR");
                    System.out.println("Sent: ERR");
                }
            }
            out.println("QUIT");
            System.out.println("Sent: QUIT");
            message = in.readLine();
            System.out.println("Received: " + message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
