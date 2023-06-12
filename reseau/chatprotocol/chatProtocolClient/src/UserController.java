import org.whispersystems.libsignal.state.PreKeyBundle;

public class UserController {
    private User user;

    public UserController(User user) {
        this.user = user;
    }

    public PreKeyBundle generateBundle() {
        PreKeyBundle bundle = new PreKeyBundle(user.getRegistrationId(), 
                                user.getAddress().getDeviceId(),
                                user.getPreKeys().get(0).getId(),
                                user.getPreKeys().get(0).getKeyPair().getPublicKey(),
                                user.getSignedPreKey().getId(),
                                user.getSignedPreKey().getKeyPair().getPublicKey(),
                                user.getSignedPreKey().getSignature(),
                                user.getIdentityKeyPair().getPublicKey()
                                );

        user.updateprekeys();

        return bundle;
    }

    public User getUser() {
        return user;
    }    

}
