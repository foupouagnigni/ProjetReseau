import java.util.List;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.PreKeyStore;
import org.whispersystems.libsignal.state.SessionStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyStore;
import org.whispersystems.libsignal.state.impl.InMemoryIdentityKeyStore;
import org.whispersystems.libsignal.state.impl.InMemoryPreKeyStore;
import org.whispersystems.libsignal.state.impl.InMemorySessionStore;
import org.whispersystems.libsignal.state.impl.InMemorySignedPreKeyStore;
import org.whispersystems.libsignal.util.KeyHelper;

public class User {

    private SignalProtocolAddress address;

    private IdentityKeyPair    identityKeyPair;
    private int                registrationId;
    private List<PreKeyRecord> preKeys;
    private SignedPreKeyRecord signedPreKey;

    private SessionStore      sessionStore;
    private PreKeyStore       preKeyStore;
    private SignedPreKeyStore signedPreKeyStore;
    private IdentityKeyStore  identityStore;

    private int preKeyCounter = 0;


    public User(SignalProtocolAddress address) {
        this.address = address;

        this.preKeyCounter = 1;

        identityKeyPair = KeyHelper.generateIdentityKeyPair();
        registrationId  = KeyHelper.generateRegistrationId(false);
        preKeys         = KeyHelper.generatePreKeys(preKeyCounter, 1);
        try {
            signedPreKey    = KeyHelper.generateSignedPreKey(identityKeyPair, preKeys.get(0).getId());
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sessionStore      = new InMemorySessionStore();
        preKeyStore       = new InMemoryPreKeyStore();
        signedPreKeyStore = new InMemorySignedPreKeyStore();
        identityStore     = new InMemoryIdentityKeyStore(identityKeyPair, registrationId);

        for (int i = 0; i < preKeys.size(); i++) {
            preKeyStore.storePreKey(preKeys.get(i).getId(), preKeys.get(i));
        }

        signedPreKeyStore.storeSignedPreKey(signedPreKey.getId(), signedPreKey);

    }


    public SignalProtocolAddress getAddress() {
        return address;
    }


    public void setAddress(SignalProtocolAddress address) {
        this.address = address;
    }


    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyPair;
    }


    public SignedPreKeyRecord getSignedPreKey() {
        return signedPreKey;
    }


    public void setSignedPreKey(SignedPreKeyRecord signedPreKey) {
        this.signedPreKey = signedPreKey;
    }


    public void setIdentityKeyPair(IdentityKeyPair identityKeyPair) {
        this.identityKeyPair = identityKeyPair;
    }


    public int getRegistrationId() {
        return registrationId;
    }


    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }


    public List<PreKeyRecord> getPreKeys() {
        return preKeys;
    }


    public void setPreKeys(List<PreKeyRecord> preKeys) {
        this.preKeys = preKeys;
    }


    public SessionStore getSessionStore() {
        return sessionStore;
    }


    public void setSessionStore(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }


    public PreKeyStore getPreKeyStore() {
        return preKeyStore;
    }


    public void setPreKeyStore(PreKeyStore preKeyStore) {
        this.preKeyStore = preKeyStore;
    }


    public SignedPreKeyStore getSignedPreKeyStore() {
        return signedPreKeyStore;
    }


    public void setSignedPreKeyStore(SignedPreKeyStore signedPreKeyStore) {
        this.signedPreKeyStore = signedPreKeyStore;
    }


    public IdentityKeyStore getIdentityStore() {
        return identityStore;
    }


    public void setIdentityStore(IdentityKeyStore identityStore) {
        this.identityStore = identityStore;
    }

    public void updateprekeys()  {
        preKeyCounter++;

        preKeys         = KeyHelper.generatePreKeys(preKeyCounter, 1);
        try {
            signedPreKey    = KeyHelper.generateSignedPreKey(identityKeyPair, preKeys.get(0).getId());
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int i = 0; i < preKeys.size(); i++) {
            preKeyStore.storePreKey(preKeys.get(i).getId(), preKeys.get(i));
        }

        signedPreKeyStore.storeSignedPreKey(signedPreKey.getId(), signedPreKey);
    }

}
