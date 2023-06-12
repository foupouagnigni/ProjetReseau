import java.io.IOException;


public class App {

    public static void main(String[] args) throws IOException {

		
		// String serverAddress=ServiceDiscoveryClient();
        // System.out.println("SORTIE SERVICE : "+serverAddress);
	
		new Client("192.168.56.1", 12345).run();
	}
}
