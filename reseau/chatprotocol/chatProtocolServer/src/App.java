import java.io.IOException;

public class App {
    public static void main(String[] args) throws Exception {
        
        try {
            // Création du socket serveur,on initialise notre serveur et on demarre son execution.
            Service service = new Service(12345,Service.recuperer());

            service.run();

        } catch (IOException e) {
            System.err.println("Erreur : impossible d'écouter sur le port 12345.");
            System.exit(-1);
        }

    }
}
