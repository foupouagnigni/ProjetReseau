package sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.InetAddress;

public class Server {  // par defaut la methode d'envoi est le tcp 

	private int port;
	private String ip;
	private List<PrintStream> clients;
	private ServerSocket server;

	public static void main(String[] args) throws IOException {
		new Server(12345,"192.168.43.146").run();  // on initialise notre serveur . 
	}

	public Server(int port,String ip) {
		this.port = port;
		this.ip=ip;
		this.clients = new ArrayList<PrintStream>();  // printstream nous permet d'ecrire des informations sous differents formats dans un fichier par exemple . 
	}

	public void run() throws IOException {
		
		InetAddress adresseServeur = InetAddress.getByName(ip); //  IP par celle de votre serveur
		 server = new ServerSocket(port, 0, adresseServeur)
		{ // le 0 ici met en exergue le fait que il y'a pas de limite de connexion au serveur . 
			protected void finalize() throws IOException {
				this.close();  // a chaque fois qu'il aura une erreur , on imposera la fermeture de notre connexion 
			}
		};
		System.out.println("le Port"+port+"est maintenant ouvert.");

		while (true) {
			// on accepteun nouveau client
			Socket client = server.accept();
			System.out.println("connection etablie avec le client: " + client.getInetAddress().getHostAddress());
			
			// on recupere les informations du client dans une liste CLients . celle ci servira plus tard a envoye des messages au client . 
			this.clients.add(new PrintStream(client.getOutputStream()));
			
			// on creait un nouveau thread pour gerer les connexions 
			//clients au cas de multiples utilisateurs . 
			new Thread(new ClientHandler(this, client.getInputStream())).start();
		}
	}

	void broadcastMessages(String msg) {
		for (PrintStream client : this.clients) {
			client.println(msg);  // ici on envoie le message aux differents clients grace a la liste Client  . ici client est de type socket
		}
	}
}

class ClientHandler implements Runnable {

	private Server server;
	private InputStream client;

	public ClientHandler(Server server, InputStream client) {
		this.server = server;
		this.client = client;
	}

	@Override
	public void run() {
		String message;
		
		// lorsque il y'a un nouveau message , on l'envoie a tous les clients    
		Scanner sc = new Scanner(this.client);
		while (sc.hasNextLine()) {
			message = sc.nextLine();
			server.broadcastMessages(message); // broadcast est une methode de 
		}
		sc.close();
	}
}
