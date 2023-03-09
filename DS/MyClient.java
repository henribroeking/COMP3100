import java.net.*;
import java.io.*;

public class MyClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        System.out.println("Connected to server: " + socket.getInetAddress().getHostAddress());

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Send message to server
        out.println("Hello from the client!");

        // Receive response from server
        String response = in.readLine();
        System.out.println("Server says: " + response);

        // Send another message to server
        out.println("Bye");

        // Receive final response from server
        response = in.readLine();
        System.out.println("Server says: " + response);

        socket.close();
    }
}
