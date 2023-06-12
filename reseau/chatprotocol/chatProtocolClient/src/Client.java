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

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyBundle;

import static java.lang.Integer.parseInt;

public class Client {
    private String host;							// adresse IP du serveur
    private int serverPort;		                    // port sur lequel le serveur écoute

    // Création de la variable de contrôle
    protected boolean canRead = false;


    public Client(String host,int port) throws UnknownHostException {
        this.host = host;
        this.serverPort=port;
    }

    public void run() throws UnknownHostException, IOException {

        SessionService cryptoService = new SessionService();
        int bundleIndex = 0;

        // Connexion au serveur
        Socket socket = new Socket(host,serverPort);
        System.out.println("Connected to server " + this.host + ": " + serverPort);

        // Obtention des input/output
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintStream output = new PrintStream(socket.getOutputStream());
        ObjectInputStream  objectInput= new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream objectOutput=new ObjectOutputStream(socket.getOutputStream());


        // Lecture du message demandant l'id'
        String idMessage = input.readLine();
        System.out.println(idMessage);

        // Lecture du id saisi par l'utilisateur
        Scanner scanner = new Scanner(System.in);
        String deviceId = scanner.nextLine();

        // Envoi du pseudo au serveur
        output.println(deviceId);

        // Lecture du message demandant le nom
        String nameMessage = input.readLine();
        System.out.println(nameMessage);

        // Lecture du nom saisi par l'utilisateur
        String name = scanner.nextLine();

        // Envoi du pseudo au serveur
        output.println(name);

        cryptoService.logUser(name, parseInt(deviceId));

        List<PreKeyBundle> bundles = cryptoService.getBundles();

        objectOutput.writeObject(bundles);

        // Lecture du message de bienvenue
        String welcomeMessage = input.readLine();
        System.out.println(welcomeMessage);        


        // Lecture du message de bienvenue dans le chat
        String chatWelcomeMessage = input.readLine();
        System.out.println(chatWelcomeMessage);

        // Consignes pour l'envoi de message
        String instructionsMessage1 = input.readLine();
        String instructionsMessage2 = input.readLine();
        String instructionsMessage3 = input.readLine();
        String instructionsMessage4 = input.readLine();
        String instructionsMessage5 = input.readLine();
     
        
        // Affichage des instructions sur la console
        System.out.println(instructionsMessage1);
        System.out.println(instructionsMessage2);
        System.out.println(instructionsMessage3);
        System.out.println(instructionsMessage4);
        System.out.println(instructionsMessage5);

        System.out.println("DEBUT CONVERSATION");
        scanner.close();
         //-----------------------------TOUT LE HAUT EST CORRECT ET NE POSE PAS DE PROBLEME ----------------------------------------------------------------------------


         /*
          * 
            On va creer un thread qui sera responsable d el ecoute en permanence pour 
            l etablissement(recpetionnéles demandes) de la connexion et un autre pour la demande de connexion et l envoi des msg

        */

        // Création des threads pour gérer l'envoi et la réception de messages et Démarrage des threads
        new Thread(new SendThread(output,objectOutput, input,objectInput,cryptoService)).start();;
        new Thread(new ReceiveThread(output,objectOutput, input,objectInput,cryptoService)).start();
       
    }
    
}

class SendThread implements Runnable {

    private PrintStream output;
    private ObjectOutputStream objectOutput;
    private BufferedReader input;
    private ObjectInputStream objectInput;
    SessionService cryptoService;
   

    public SendThread(PrintStream output, ObjectOutputStream objectOutput,BufferedReader input, ObjectInputStream objectInput, SessionService cryptoService) {
        this.output = output;
        this.objectOutput = objectOutput;
        this.input = input;
        this.objectInput = objectInput;
        this.cryptoService = cryptoService;
    }
    public void run() {

        Scanner scanner = new Scanner(System.in);
        // Envoi de message
        while (true) {
            try {
                
            String message = scanner.nextLine();
            if (message.equalsIgnoreCase("quit")) {
                output.println(message);
                break;
            } else if (message.startsWith("_")){
              //  Boolean destine=false;
                /*LE CLIENT  (l expediteur) recupere le bundle et le signal protocol address du destinataire*/
                PreKeyBundle bundle = (PreKeyBundle) objectInput.readObject();
                SignalProtocolAddress address = (SignalProtocolAddress) objectInput.readObject();

                /*processus d etablissement de la connexion avec le destinataire */
                byte[] handshake = cryptoService.startSession(address,bundle);

                objectOutput.writeObject(handshake.toString());//doit etre envoyé au destinataire,ta methode toString peut poser probleme 
               // destine=Boolean.valueOf(input.readLine());
                // if(destine){
                //     canRead=true;    
                // }

                String handshakeOk = (String) objectInput.readObject();//objet recu du destinataire
                /*je propose
                * 
                * byte[] handshakeOk = (byte[]) objectInput.readObject(); pour remplacer la ligne ci dessus 
                 */
                

                cryptoService.decrypt(handshakeOk.getBytes("UTF-8"));
                //revois ceci , cryptoservice semble prendre un tableau de byte mais getByte fais vraiment laconversion?

            }else if (message.startsWith("@")) {

                String[] parts = message.split(" ", 2);
                int recipientDeviceId = parseInt(parts[0].substring(1));
                String privateMessage = parts[1];
                output.println("@"+ recipientDeviceId + " " + cryptoService.encrypt(privateMessage));
                //server.unicastMessage(deviceId, recipientDeviceId, privateMessage);

            } else if (message.startsWith("#")) {

                String[] parts = message.split(" ", 2);
                String recipientIds = parts[0].substring(1);
                String multicastMessage = parts[1];
                output.println("#"+ recipientIds + " " + cryptoService.encrypt(multicastMessage));
               // server.multicastMessage(deviceId, recipientDeviceId, multicastMessage);

            } else {
                output.println(cryptoService.encrypt(message));
            }
          //  output.println(message);
                      } catch (Exception e) {
                // TODO: handle exception
            }
        }

        scanner.close();
        // Fermeture de la connexion
        //socket.close();

    }
}

class ReceiveThread implements Runnable {
   
    private PrintStream output;
    private ObjectOutputStream objectOutput;
    private BufferedReader input;
    private ObjectInputStream objectInput;
   SessionService cryptoService;

    public ReceiveThread(PrintStream output, ObjectOutputStream objectOutput,BufferedReader input, ObjectInputStream objectInput,SessionService cryptoService) {
        this.output = output;
        this.objectOutput = objectOutput;
        this.input = input;
        this.objectInput = objectInput;
        this.cryptoService = cryptoService;
    }
    
    public void run() {

        while (true) {
            try {
                
            if(Boolean.valueOf(input.readLine())){

                /*handshakeOKAY faut gerer ca */
                SignalProtocolAddress address = (SignalProtocolAddress) objectInput.readObject();

                String handshake = (String)  objectInput.readObject();



                  /*processus d etablissement de la connexion avec le destinataire */
                byte[] handshakeOk = cryptoService.joinSession(address,handshake.getBytes("UTF-8"));

                objectOutput.writeObject(handshakeOk.toString());

               // cryptoService.decrypt(handshakeOKAY.getBytes("UTF-8"));
                //revois ceci , cryptoservice semble prendre un tableau de byte mais getByte fais vraiment laconversion?               
               
               // canRead=false;

            }

            String message = input.readLine();

            String[] parts = message.split(": ", 2);
            int recipientDeviceId = parseInt(parts[0].substring(1));
            String privateMessage = parts[1];

            String clearMessage = cryptoService.decrypt(privateMessage.getBytes("UTF-8")).toString();

            System.out.println(recipientDeviceId + ": " + clearMessage);

                        } catch (Exception e) {
 
            }


    }
}
}