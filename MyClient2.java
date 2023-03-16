import java.io.*;
import java.net.*;

public class MyClient2 {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 5001);
        OutputStream out = socket.getOutputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.write(("HELO\n").getBytes());
        out.flush();
        String response = br.readLine();
        System.out.println("Server response: " + response);

        out.write(("BYE\n").getBytes());
        out.flush();
        response = br.readLine();
        System.out.println("Server response: " + response);

        socket.close();
    }
}
