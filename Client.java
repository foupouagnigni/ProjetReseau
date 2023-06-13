

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import static java.lang.Integer.parseInt;

//Clienthandler c est coe ca qu un client est vu du côté du serveur//



public class Client implements Runnable{

    private int deviceId;
    private String name;

    private Service server;
    private Socket socket;

    
    public Client(Service server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }


    @Override
    public void run() {
        try {
           
             
            InputStream input = socket.getInputStream();
            PrintStream output = new PrintStream(socket.getOutputStream());
    
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            ObjectOutputStream objectOutput=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream  objectInput=new ObjectInputStream(socket.getInputStream());
          
        
            /*
             * lorsqu on utilise objet printstream output
             * output.println(data) n ecris pas directement sur la sortie
             * il faut faire un object.readline(data) , object etant un bufferedreader
             *  output.println(name) écrit une chaîne de caractères name suivie d'un caractère de retour à la ligne (\n) dans le flux de sortie output.
             * pour lire name dans ce flux à l entree un readline est necessaire si onutilise un bufferedreader sinon declarer un scanner avec comme entree input
             * 
             */

             // Scanner scanner = new Scanner(input); //j ai opté pour un bufferedreader par souci d homogeneité avec le code client
           
            output.println("Entez votre identifiant");
            this.setDeviceId(parseInt(reader.readLine()));
           
            output.println("Enter your nickname:");
            this.setName(reader.readLine());
           
        
            output.println("Welcome ! Your number's client is " + this.getDeviceId());


            // Ajouter le client à la liste de client
            String ipAddress = socket.getInetAddress().getHostAddress();
            server.addClient(this.getDeviceId(), ipAddress);

            //consigne pour l'envoie de message
            output.println("Welcome to the chat room, " + deviceId + "!");
            output.println("To send a private message, type '@deviceId' message");
            output.println("To send a message to multiple clients, type '#deviceId1,deviceId2 message' ");
            output.println("To send a message to all others, type directly the 'message' ");
            output.println("To quit, type 'quit'");

             //-----------------------------TOUT LE HAUT EST CORRECT ET NE POSE PAS DE PROBLEME ----------------------------------------------------------------------------


            while (true) {
                server.afficheClientConnecte();

               
                String message = reader.readLine();

                if (message.equalsIgnoreCase("quit")) {
                    break;
                } 

                
                    /*
                     * 
                     * ECRIRE LE CODE desire ou modifie le.
                     * 
                     */


                
                else if (message.startsWith("@")) {

                    String[] parts = message.split(" ", 2);
                    int recipientDeviceId = parseInt(parts[0].substring(1));
                    String privateMessage = parts[1];
                    server.unicastMessage(deviceId, recipientDeviceId, privateMessage);

                } else if (message.startsWith("#")) {

                    String[] parts = message.split(" ", 2);
                    String[] recipientIds = parts[0].substring(1).split(",");
                    int[] recipientDeviceId = new int[recipientIds.length];
                    for (int i = 0; i < recipientDeviceId.length; i++) {
                        recipientDeviceId[i] = parseInt(recipientIds[i].trim());
                    }

                    String multicastMessage = parts[1];
                    server.multicastMessage(deviceId, recipientDeviceId, multicastMessage);

                } else {
                    server.broadcastMessage(deviceId, message);
                }
            }

          //  scanner.close();

            // retire le client de la liste de clients connectés
            server.removeClient(deviceId);
            server.deleteClient(this);

            // fermeture de la socket client
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    
}
