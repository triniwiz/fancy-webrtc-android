package co.fitcom.fancywebrtc;

/**
 * Created by triniwiz on 1/7/19
 */
public enum FancyRTCRtcpMuxPolicy {
    NEGOTIATE("negotiate"),
    REQUIRE("require");

    private final String policy;

    FancyRTCRtcpMuxPolicy(final String policy) {
        this.policy = policy;
    }

    @Override
    public String toString() {
        return policy;
    }
}
