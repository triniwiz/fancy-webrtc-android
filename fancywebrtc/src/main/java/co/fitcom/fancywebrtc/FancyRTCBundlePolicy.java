package co.fitcom.fancywebrtc;

/**
 * Created by triniwiz on 1/7/19
 */
public enum FancyRTCBundlePolicy {
    BALANCED("balanced"),
    MAX_COMPAT("max-compat"),
    MAX_BUNDLE("max-bundle");

    private final String policy;


    FancyRTCBundlePolicy(final String policy){
        this.policy = policy;
    }

    @Override
    public String toString() {
        return policy;
    }
}