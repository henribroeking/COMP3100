import java.net.*;
import java.io.*;

public class MyServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Server started, listening on port 12345");

        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        // Receive message from client
        String message = in.readLine();
        System.out.println("Client says: " + message);

        // Send response to client
        out.println("G'day from the server!");

        // Receive another message from client
        message = in.readLine();
        System.out.println("Client says: " + message);

        // Send final response to client
        out.println("Bye");

        clientSocket.close();
        serverSocket.close();
    }
}
