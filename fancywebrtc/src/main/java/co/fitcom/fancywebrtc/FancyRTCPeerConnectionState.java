package co.fitcom.fancywebrtc;

/**
 * Created by triniwiz on 1/8/19
 */
public enum FancyRTCPeerConnectionState {
    NEW("new"),
    CONNECTING("connecting"),
    CONNECTED("connected"),
    DISCONNECTED("disconnected"),
    FAILED("failed"),
    CLOSED("closed");
    private String state;

    FancyRTCPeerConnectionState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }
}
