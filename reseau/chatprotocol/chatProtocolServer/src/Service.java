import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service {

    private static int nombreClient=0;
    private int port;
    private String ip;
    private Map<Integer, String> clientList;
    private List<Client> userList;//liste des clients

    public Service(int port,String ip) {
        this.port = port;
        this.ip=ip;
        this.clientList = new HashMap<>();
        this.userList=new ArrayList<Client>();
    }

    public static String recuperer() throws SocketException {

        String ip = "192.168.56.1";
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isUp() && iface.getName().equals("wlan0")) { 
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        ip = addr.getHostAddress();
                    }
                }
            }
        }
        return ip;
    }

    public void addClient(int deviceId, String ipAddress) {
        clientList.put(deviceId, ipAddress);
        System.out.println(deviceId + " has joined the chat");
    }

    public Client getClientById(int recipientDeviceId) throws Exception {
        for (Client client : userList) {
            if (client.getDeviceId() == recipientDeviceId) {
                return client;
            }else{
                throw new Exception("this Client doesn't exist");
            }
        }
        return null;
    }

    public void unicastMessage(int senderDeviceId, int recipientDeviceId, String privateMessage) {
        String recipientIpAddress = clientList.get(recipientDeviceId);
        if (recipientIpAddress != null) {
            try {
                Socket recipient = new Socket(recipientIpAddress, port);
                ObjectOutputStream output = new ObjectOutputStream(recipient.getOutputStream());
                output.writeObject(senderDeviceId + ": " + privateMessage);
                output.close();
                recipient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void multicastMessage(int senderDeviceId, int[] recipientDeviceId, String multicastMessage) {
        for (int recipientDevice : recipientDeviceId) {
            unicastMessage(senderDeviceId, recipientDevice, multicastMessage);
        }
    }

    public void broadcastMessage(int senderDeviceId, String message) {
        for (int recipientDeviceId : clientList.keySet()) {
            if (!Integer.valueOf(senderDeviceId).equals(Integer.valueOf(recipientDeviceId))) {
                unicastMessage(senderDeviceId, recipientDeviceId, message);
            }
        }
    }

    public void removeClient(int deviceId) {
        clientList.remove(deviceId);
        System.out.println(deviceId + " has left the chat");
    }

    public void deleteClient(Client client) {
        userList.remove(client);
    }

    public synchronized String[] getConnectedClients() {
        return clientList.keySet().toArray(new String[0]);
    }

    public synchronized String getIpAddress(int DeviceId) {
        return clientList.get(DeviceId);
    }


    public synchronized void afficheClientConnecte() throws IOException {
        for (Client client : this.userList) {
            {
                if (clientList.containsKey(client.getDeviceId())) {
                    PrintWriter pw = new PrintWriter(client.getSocket().getOutputStream(), true);
                    pw.println("Nom Client :" + client.getName() +" deviceId : " + String.valueOf(client.getDeviceId()) + "\n");
                }
            }
        }
    }

    public void run() throws IOException{

        InetAddress adresseServeur = InetAddress.getByName(ip); //  IP par celle de votre serveur
        ServerSocket server = new ServerSocket(port, 0, adresseServeur)

        { // le 0 c est la valeur du parametre backlog ,ici met en exergue le fait que il y'a pas de limite de connexion au serveur .
            protected void finalize() throws IOException {
                this.close();  // a chaque fois qu'il aura une erreur , on imposera la fermeture de notre connexion
            }
        };
        System.out.println("le Port "+port+" est maintenant ouvert.");


        while (true) {
            // on accepte un nouveau client
            System.out.println("Serveur en attente de connexions...");
            Socket socket = server.accept();
            ++nombreClient;

            Client client =new Client(this, socket);
            System.out.println(client.getDeviceId());
            userList.add(client);

            //on affiche l'adresse Ip du client
            System.out.println("connection etablie avec le client: " + socket.getInetAddress().getHostAddress());

            // on creait un nouveau thread pour gerer les connexions et il demarre automatiquement
            //clients au cas de multiples utilisateurs .
            new Thread(client).start();
            //new Thread(new ClientHandler(this, client,nombreClient)).start();

        }

    }
    
}
