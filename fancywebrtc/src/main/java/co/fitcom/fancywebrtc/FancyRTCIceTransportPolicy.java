package co.fitcom.fancywebrtc;

/**
 * Created by triniwiz on 1/7/19
 */
public enum FancyRTCIceTransportPolicy {
    ALL("all"),
    PUBLIC("public"),
    RELAY("relay");
    private final String policy;
    FancyRTCIceTransportPolicy(final String policy){
        this.policy = policy;
    }

    @Override
    public String toString() {
        return policy;
    }
}
