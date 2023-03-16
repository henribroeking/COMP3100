import java.io.*;
import java.net.*;

public class MyServer2 {
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(5001);
        Socket s = ss.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        OutputStream out = s.getOutputStream();

        String message = in.readLine();
        if (message.equals("HELO")) {
            out.write(("G'DAY\n").getBytes());
        }

        message = in.readLine();
        if (message.equals("BYE")) {
            out.write(("BYE\n").getBytes());
        }

        ss.close();
    }
}
