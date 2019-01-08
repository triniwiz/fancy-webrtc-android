package co.fitcom.fancywebrtc;

/**
 * Created by triniwiz on 1/7/19
 */
public enum FancyRTCIceCredentialType {
    PASSWORD("password"),
    TOKEN("token");

    private final String type;

    FancyRTCIceCredentialType(final String type){
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
