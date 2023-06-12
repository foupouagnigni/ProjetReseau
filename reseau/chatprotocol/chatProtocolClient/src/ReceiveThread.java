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