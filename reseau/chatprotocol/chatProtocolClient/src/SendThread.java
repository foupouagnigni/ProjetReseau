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
