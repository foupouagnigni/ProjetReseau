
public class Client {
    private String host;							// adresse IP du serveur
    private int serverPort;		                    // port sur lequel le serveur écoute


    public Client(String host,int port) throws UnknownHostException {
        this.host = host;
        this.serverPort=port;
    }


    

       public void run() {

        
        // Connexion au serveur
        Socket socket = new Socket(host,serverPort);
        System.out.println("Connected to server " + this.host + ": " + serverPort);

        // Obtention des input/output
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintStream output = new PrintStream(socket.getOutputStream());
    
        ObjectOutputStream objectOutput=new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream  objectInput= new ObjectInputStream(socket.getInputStream());

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

         //-----------------------------TOUT LE HAUT EST CORRECT ET NE POSE PAS DE PROBLEME ----------------------------------------------------------------------------
        
        // Envoi de message
        while (true) {
            try {
                
            String message = scanner.nextLine();
            if (message.equalsIgnoreCase("quit")) {
                output.println(message);
                break;
            
                
        
            }else {

                output.println(message);

            } 
          
                      } catch (Exception e) {
                // TODO: handle exception
            }
        }

        scanner.close();
        socket.close();

    
      
    }
    
}



