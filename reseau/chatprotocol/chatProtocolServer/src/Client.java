import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyBundle;

public class Client implements Runnable{

    private int deviceId;
    private String name;

    private Service server;
    private Socket socket;


    private Queue<PreKeyBundle> bundles = new LinkedList<PreKeyBundle>();
    
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

    public synchronized PreKeyBundle getBundle(){
        return bundles.remove();
    }

    public void addListOfBundles(List<PreKeyBundle> listOfBundles) {
        bundles.addAll(listOfBundles);
    }

    @Override
    public void run() {
        try {
            
            InputStream input = socket.getInputStream();
            PrintStream output = new PrintStream(socket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            ObjectInputStream  objectInput=new ObjectInputStream(input);
            ObjectOutputStream objectOutput=new ObjectOutputStream(socket.getOutputStream());
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

            List<PreKeyBundle> bundles = (List<PreKeyBundle>) objectInput.readObject();
            this.addListOfBundles(bundles);

            output.println("Welcome ! Your number's client is " + this.getDeviceId());


            // Ajouter le client à la liste de client
            String ipAddress = socket.getInetAddress().getHostAddress();
            server.addClient(this.getDeviceId(), ipAddress);

            //consigne pour l'envoie de message
            output.println("Welcome to the chat room, " + deviceId + "!");
            output.println("to choose a recipient, type '_deviceId' ");
            output.println("To send a private message, type '@deviceId' message");
            output.println("To send a message to multiple clients, type '#deviceId1,deviceId2 message' ");
            output.println("To quit, type 'quit'");

             //-----------------------------TOUT LE HAUT EST CORRECT ET NE POSE PAS DE PROBLEME ----------------------------------------------------------------------------


            while (true) {
                server.afficheClientConnecte();

                //Le serveur(le thread gerant la connexion avec le client) recupere le msg que veut envoyer le client
                String message = reader.readLine();

                if (message.equalsIgnoreCase("quit")) {
                    break;
                } else if (message.startsWith("_")){

                    String[] parts = message.split(" ", 2);
                    int recipientDeviceId = parseInt(parts[0].substring(1));

                    //*---------------- LE SERVEUR RECUPERE LE BUNDLE ET LE SIGNAL PROTOCOL ADDRESS DU DESTINATAIRE ET RENVOIS A L EXPEDITEUR */
                    Client recipientClient = server.getClientById(recipientDeviceId);
                    objectOutput.writeObject(recipientClient.getBundle());
                    objectOutput.writeObject(new SignalProtocolAddress(recipientClient.getName(), recipientDeviceId));

                    //recupere le handshake de l expediteur pour envoyer au destinataire
                    String handshake = (String)objectInput.readObject();

                    PrintStream outputx = new PrintStream(recipientClient.getSocket().getOutputStream());
                    ObjectInputStream  objectInputx=new ObjectInputStream(recipientClient.getSocket().getInputStream());
                    ObjectOutputStream objectOutputx=new ObjectOutputStream(recipientClient.getSocket().getOutputStream());

                    //valeur qui permettra de savoir à quel thread coté client c est destiné
                    boolean value=true;
                    outputx.println(value);

                    //envois du handshake au destinataire et de l'adresse du sender.
                    objectOutput.writeObject(new SignalProtocolAddress(this.getName(), this.getDeviceId()));
                    objectOutputx.writeObject(handshake);


                    //recuperation du handshake du destinataire
                    String handshakeOk = (String) objectInputx.readObject();

                    //transfert de ce handshake à l expediteur.
                    objectOutput.writeObject(handshakeOk);

                
                    /*
                     * 
                     * ECRIRE LE CODE desire ou modifie le.
                     * 
                     */


                    

                }else if (message.startsWith("@")) {

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
