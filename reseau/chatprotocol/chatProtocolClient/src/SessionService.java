import java.util.ArrayList;
import java.util.List;

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyBundle;

public class SessionService {

    private final int NUMBER_OF_BUNDLES = 10;

    private SessionController sessionController;

    public SessionService() {
    }

    public byte[] startSession(SignalProtocolAddress address, PreKeyBundle bundle){
        return sessionController.initiateSession(address, bundle);
    }

    public byte[] joinSession(SignalProtocolAddress address, byte[] message){
        return sessionController.ConnectSession(address, message);
    }

    public void logUser(String name, int deviceId){
        sessionController = new SessionController(new UserController(
                                                                     new User(
                                                                              new SignalProtocolAddress(name, deviceId)
                                                                              )
                                                                     )                                                               
                                                  );
    }

    public List<PreKeyBundle> getBundles() {
        List<PreKeyBundle> bundles = new ArrayList<PreKeyBundle>();
        
        for (int i = 0; i < NUMBER_OF_BUNDLES; i++) {
            bundles.add(sessionController.getBundle());
        }

        return bundles;
    }
    
    public byte[] encrypt(String message) {
        return sessionController.encrypt(message);
    }

    public byte[] decrypt(byte[] message) {
        return sessionController.decrypt(message);
    }

}
