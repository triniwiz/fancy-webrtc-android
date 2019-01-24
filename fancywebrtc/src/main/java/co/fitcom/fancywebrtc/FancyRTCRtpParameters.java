package co.fitcom.fancywebrtc;

import org.webrtc.RtpParameters;

/**
 * Created by triniwiz on 1/17/19
 */
public class FancyRTCRtpParameters {
    RtpParameters parameters;

    public FancyRTCRtpParameters(RtpParameters parameters) {
        this.parameters = parameters;
    }

    public RtpParameters getParameters() {
        return parameters;
    }
}
