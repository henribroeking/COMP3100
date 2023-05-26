// Importing the necessary classes from the Java standard library
import java.io.*;  
import java.net.*;

// Main class definition. The class is called "ClientSimulator"
public class ClientSimulator {

    // The entry point of the program
    public static void main(String[] args) {  
        try {   
            // Creating a new socket and connecting it to localhost on port 50000
            Socket skt = new Socket("localhost",50000);  

            // Creating a new DataOutputStream which can write data to the socket
            DataOutputStream dataOutput = new DataOutputStream(skt.getOutputStream());

            // Creating a new BufferedReader which can read data from the socket
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(skt.getInputStream()));

            // Define a variable to store server responses
            String serverResponse = "";

            // Sending a HELO message to the server
            dataOutput.write(("HELO\n").getBytes());

            // Reading the server's response
            serverResponse = bufferedReader.readLine();

            // Retrieving the user's name
            String user = System.getProperty("user.name");

            // Sending an AUTH message to the server with the user's name
            dataOutput.write(("AUTH " + user + "\n").getBytes());

            // Reading the server's response
            serverResponse = bufferedReader.readLine();

            // Sending a REDY message to the server
            dataOutput.write(("REDY\n").getBytes());

            // Reading the server's response
            serverResponse = bufferedReader.readLine();

            // Convert the server's response to a string and split it into an array
            String jobStr = serverResponse.toString();
            String[] jobDetails = jobStr.split(" ");

            // Checking if the first item in the array is "JOBN", which indicates a job
            if (jobDetails[0].equals("JOBN")) {

                // Extracting the number of cores required for the job
                int coresNeeded = extractCoresRequired(jobStr);

                // Sending a GETS Capable message to the server with the cores, RAM, and memory required
                dataOutput.write(("GETS Capable " + coresNeeded + " " + extractRamRequired(jobStr) + " " + extractMemoryRequired(jobStr) + "\n").getBytes());

                // Reading the server's response
                serverResponse = bufferedReader.readLine();

                // Convert the server's response to a string and split it into an array
                String serverDetailsStr = serverResponse.toString();
                String[] serverDetails = serverDetailsStr.split(" ");

                // Extract the number of servers from the server's response
                int serverCount = Integer.valueOf(serverDetails[1]);

                // Sending an OK message to the server
                dataOutput.write(("OK\n").getBytes());

                // Reading the server's response
                String serverStr = bufferedReader.readLine();
                boolean hasDoubleCapacity = false;

                // Loop over the server's responses to find a server with at least double capacity
                for(int i = 1; i < serverCount; i++) {
                    serverResponse = bufferedReader.readLine();

                    if(!hasDoubleCapacity && extractServerSize(serverResponse) >= coresNeeded * 2) {
                        hasDoubleCapacity = true;
                        serverStr = serverResponse;
                    }
                }
                
                // Sending an OK message to the server
                dataOutput.write(("OK\n").getBytes());

                // Preparing the scheduling message
                String msgToSend  = "SCHD " + extractJobID(jobStr) + " " + extractServerType(serverStr) + " " + extractServerID(serverStr) + "\n";

                // Sending the scheduling message to the server
                dataOutput.write(msgToSend.getBytes());

                // Reading the server's responses until it responds with OK
                while(!(serverResponse = bufferedReader.readLine()).equals("OK")) {}
            }
            
            // Sending a REDY message to the server
            dataOutput.write(("REDY\n").getBytes());

            // Reading the server's responses until it responds with NONE
            while(!(serverResponse = bufferedReader.readLine()).equals("NONE")) {
                jobStr = serverResponse.toString();
                jobDetails = jobStr.split(" ");

                // Checking if the first item in the array is "JOBN", which indicates a job
                if(jobDetails[0].equals("JOBN")) {
                    int coresNeeded = extractCoresRequired(jobStr);
                    dataOutput.write(("GETS Capable " + coresNeeded + " " + extractRamRequired(jobStr) + " " + extractMemoryRequired(jobStr) + "\n").getBytes());
                    serverResponse = bufferedReader.readLine();
                    String serverDetailsStr = serverResponse.toString();
                    String[] serverDetails = serverDetailsStr.split(" ");
                    int serverCount = Integer.valueOf(serverDetails[1]);
                    dataOutput.write(("OK\n").getBytes());
                    
                    String serverStr = bufferedReader.readLine();
                    serverStr = getFirstCapableServer(serverStr, serverCount, coresNeeded, bufferedReader, dataOutput);
                    
                    String msgToSend  = "SCHD " + extractJobID(jobStr) + " " + extractServerType(serverStr) + " " + extractServerID(serverStr) + "\n";
                    dataOutput.write(msgToSend.getBytes());
                    while(!(serverResponse = bufferedReader.readLine()).equals("OK")) {}
                }

                // Sending a REDY message to the server
                dataOutput.write(("REDY\n").getBytes());
            }

            // Sending a QUIT message to the server
            dataOutput.write(("QUIT\n").getBytes());

            // Flushing the output stream and closing it and the input stream
            dataOutput.flush();
            dataOutput.close();
            bufferedReader.close();

            // Closing the socket
            skt.close();
        } catch(Exception e) {
            // If there is an exception, we catch it and print the stack trace to the console for debugging.
            System.out.println(e);
        }
    }

    // Helper method to extract the number of cores from the server string
    static int extractServerSize(String serverStr) {
        String[] serverDetails = serverStr.split(" ");
        return Integer.valueOf(serverDetails[4]);
    }

    // Helper method to extract the server type from the server string
    static String extractServerType(String serverStr) {
        String[] serverDetails = serverStr.split(" ");
        return serverDetails[0];
    }

    // Helper method to extract the server ID from the server string
    static String extractServerID(String serverStr) {
        String[] serverDetails = serverStr.split(" ");
        return serverDetails[1];
    }

    // Helper method to extract the server status from the server string
    static String extractServerStatus(String serverStr) {
        String[] serverDetails = serverStr.split(" ");
        return serverDetails[2];
    }

    // Helper method to extract the waiting job count from the server string
    static int extractWaitingJobCount(String serverStr) {
        String[] serverDetails = serverStr.split(" ");
        return Integer.valueOf(serverDetails[7]);
    }

    // Helper method to extract the number of cores required from the job string
    static int extractCoresRequired(String jobStr) {
        String[] jobDetails = jobStr.split(" ");
        return Integer.valueOf(jobDetails[4]);
    }

    // Helper method to extract the amount of RAM required from the job string
    static int extractRamRequired(String jobStr) {
        String[] jobDetails = jobStr.split(" ");
        return Integer.valueOf(jobDetails[5]);
    }

    // Helper method to extract the amount of memory required from the job string
    static int extractMemoryRequired(String jobStr) {
        String[] jobDetails = jobStr.split(" ");
        return Integer.valueOf(jobDetails[6]);
    }

    // Helper method to extract the job ID from the job string
    static String extractJobID(String jobStr) {
        String[] jobDetails = jobStr.split(" ");
        return jobDetails[2];
    }
    
    // Helper method to find the first capable server
    static String getFirstCapableServer(String serverStr, int serverCount, int coresNeeded, BufferedReader bufferedReader, DataOutputStream dataOutput) throws IOException {
        boolean hasDoubleCapacity = false;
        String serverResponse = "";
        
        for(int i = 1; i < serverCount; i++) {
            serverResponse = bufferedReader.readLine();

            if(!hasDoubleCapacity && extractServerSize(serverResponse) >= coresNeeded * 2) {
                hasDoubleCapacity = true;
                serverStr = serverResponse;
            }
        }
        dataOutput.write(("OK\n").getBytes());
        return serverStr;
    }
}
