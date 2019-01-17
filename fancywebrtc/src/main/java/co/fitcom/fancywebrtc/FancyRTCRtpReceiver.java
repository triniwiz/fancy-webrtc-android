package co.fitcom.fancywebrtc;

import org.webrtc.RtpReceiver;

/**
 * Created by triniwiz on 1/17/19
 */
public class FancyRTCRtpReceiver {
    private RtpReceiver rtpReceiver;

    public FancyRTCRtpReceiver(RtpReceiver rtpReceiver) {
        this.rtpReceiver = rtpReceiver;
    }

    public RtpReceiver getRtpReceiver() {
        return rtpReceiver;
    }
}
