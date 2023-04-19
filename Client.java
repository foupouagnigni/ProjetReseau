package sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	private String host;
	private int port;
	private String nickname;

	public static void main(String[] args) throws UnknownHostException, IOException {
		new Client("192.168.43.146", 12345).run();  // initialisation de la connection adresse ip serveur et nport serveur .  
	}

	public Client(String host, int port) {
		this.host = host; // ip serveur
		this.port = port; // port du serveur 
	}

	public void run() throws UnknownHostException, IOException {
		// connecttion au serveur
		Socket client = new Socket(host, port);
		System.out.println("connection etablie avec le serveur avec succes");

		// ce code démarrera un thread qui va écouter en permanence
		// les messages en entrée reçus depuis le client via le flux d'entrée
		// (input stream) et gérer ces messages en conséquence au fur et à mesure 
		//qu'ils sont reçus.
		// Cela permettra donc de recevoir des données du client en parallèle avec 
		//l'envoi de données vers le client (géré précédemment avec l'initialisation 
		//du flux d'impression).
		new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();

		// entrer votre nom 
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter votre nom: ");
		nickname = sc.nextLine();

		// on lis les messages entrees au clavier et on les envois directement au serveur . 
		System.out.println("Send messages: ");
		PrintStream output = new PrintStream(client.getOutputStream());
		while (sc.hasNextLine()) {
			output.println(nickname + ": " + sc.nextLine());
		}
		
		output.close();
		sc.close();
		client.close();
	}
}

class ReceivedMessagesHandler implements Runnable {

	private InputStream server;

	public ReceivedMessagesHandler(InputStream server) {
		this.server = server;
	}

	@Override
	public void run() {
		// on recoit les messages du serveur et on les affiches au fur et a mesure . 
		Scanner s = new Scanner(server);
		while (s.hasNextLine()) {
			System.out.println(s.nextLine());
		}
		s.close();
	}
}
