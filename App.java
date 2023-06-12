import java.io.IOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import static java.lang.Integer.parseInt;


public class ClientE {

    public static void main(String[] args) throws IOException {

		
		// String serverAddress=ServiceDiscoveryClient();
        // System.out.println("SORTIE SERVICE : "+serverAddress);
	
		new Client("192.168.43.126", 12345).run();
	}
}





