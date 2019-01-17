package co.fitcom.fancywebrtc;

/**
 * Created by triniwiz on 1/17/19
 */
public enum FancyRTCRtpTransceiverDirection {
    INACTIVE("inactive"),
    RECV_ONLY("recvonly"),
    SEND_ONLY("sendonly"),
    SEND_RECV("sendrecv");
    private final String direction;

    FancyRTCRtpTransceiverDirection(String direction) {
        this.direction = direction;
    }
}
