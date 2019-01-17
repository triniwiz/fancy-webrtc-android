package co.fitcom.fancywebrtc;

import org.webrtc.RtpSender;

/**
 * Created by triniwiz on 1/17/19
 */
public class FancyRTCRtpSender {
    private RtpSender sender;

    public FancyRTCRtpSender(RtpSender sender) {
        this.sender = sender;
    }

    public RtpSender getSender() {
        return sender;
    }

    public FancyRTCDTMFSender getDtmf() {
        return new FancyRTCDTMFSender(sender.dtmf());
    }

    public String getId() {
        return sender.id();
    }

    public void dispose() {
        sender.dispose();
    }

    public FancyRTCMediaStreamTrack getTrack() {
        return new FancyRTCMediaStreamTrack(sender.track());
    }

    public FancyRTCRtpParameters getParameters() {
        return new FancyRTCRtpParameters(sender.getParameters());
    }

    public void replaceTrack(FancyRTCMediaStreamTrack track) {
        sender.setTrack(track.getMediaStreamTrack(), false);
    }
}
