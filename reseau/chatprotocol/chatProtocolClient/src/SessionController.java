import java.io.UnsupportedEncodingException;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;

public class SessionController {
    private UserController userController;

    private SessionBuilder sessionBuilder;
    private SessionCipher sessionCipher;

    public SessionController(UserController userController) {
        this.userController = userController;
    }

    public byte[] initiateSession(SignalProtocolAddress address, PreKeyBundle bundle) {
        
        sessionBuilder = new SessionBuilder(userController.getUser().getSessionStore(), 
                                            userController.getUser().getPreKeyStore(), 
                                            userController.getUser().getSignedPreKeyStore(), 
                                            userController.getUser().getIdentityStore(), 
                                            address);

        try {
            sessionBuilder.process(bundle);
        } catch (InvalidKeyException | UntrustedIdentityException e) {
            e.printStackTrace();
        }

        sessionCipher = new SessionCipher(userController.getUser().getSessionStore(), 
                                          userController.getUser().getPreKeyStore(), 
                                          userController.getUser().getSignedPreKeyStore(), 
                                          userController.getUser().getIdentityStore(), 
                                          address);                              

        try {
            return sessionCipher.encrypt("Handshake".getBytes("UTF-8")).serialize();
        } catch (UnsupportedEncodingException | UntrustedIdentityException e) {
            e.printStackTrace();
        }
        return null;                                  
    }

    public byte[] ConnectSession(SignalProtocolAddress address, byte[] message) {
        sessionCipher = new SessionCipher(userController.getUser().getSessionStore(), 
                                          userController.getUser().getPreKeyStore(), 
                                          userController.getUser().getSignedPreKeyStore(), 
                                          userController.getUser().getIdentityStore(), 
                                          address);
        
        try {
            sessionCipher.decrypt(new PreKeySignalMessage(message));

            return sessionCipher.encrypt("Ok Handshake".getBytes("UTF-8")).serialize();

        } catch (DuplicateMessageException | LegacyMessageException | InvalidMessageException | InvalidKeyIdException
                | InvalidKeyException | UntrustedIdentityException | InvalidVersionException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] encrypt(String message) {
        try {
            return sessionCipher.encrypt(message.getBytes("UTF-8")).serialize();
        } catch (UnsupportedEncodingException | UntrustedIdentityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] decrypt(byte[] message) {


        try {
                return sessionCipher.decrypt(new SignalMessage(message));
        } catch (InvalidMessageException | DuplicateMessageException | LegacyMessageException
            | NoSessionException | UntrustedIdentityException e) {
            e.printStackTrace();
        }
        return null;

    }

    public PreKeyBundle getBundle(){
        return userController.generateBundle();
    }
    
}