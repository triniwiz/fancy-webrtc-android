package co.fitcom.fancywebrtc;

import org.webrtc.RtpTransceiver;

/**
 * Created by triniwiz on 1/17/19
 */
public class FancyRTCRtpTransceiver {
    private RtpTransceiver rtpTransceiver;

    public FancyRTCRtpTransceiver(RtpTransceiver rtpTransceiver) {
        this.rtpTransceiver = rtpTransceiver;
    }

    public RtpTransceiver getRtpTransceiver() {
        return rtpTransceiver;
    }

    public FancyRTCRtpTransceiverDirection getDirection() {
        switch (rtpTransceiver.getDirection()) {
            case RECV_ONLY:
                return FancyRTCRtpTransceiverDirection.RECV_ONLY;
            case SEND_ONLY:
                return FancyRTCRtpTransceiverDirection.SEND_ONLY;
            case SEND_RECV:
                return FancyRTCRtpTransceiverDirection.SEND_RECV;
            default:
                return FancyRTCRtpTransceiverDirection.INACTIVE;
        }
    }

    public void setDirection(FancyRTCRtpTransceiverDirection direction) {
        switch (direction) {
            case RECV_ONLY:
                rtpTransceiver.setDirection(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY);
                break;
            case SEND_ONLY:
                rtpTransceiver.setDirection(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY);
                break;
            case SEND_RECV:
                rtpTransceiver.setDirection(RtpTransceiver.RtpTransceiverDirection.SEND_RECV);
                break;
            default:
                rtpTransceiver.setDirection(RtpTransceiver.RtpTransceiverDirection.INACTIVE);
                break;
        }
    }


    public FancyRTCRtpTransceiverDirection getCurrentDirection() {
        switch (rtpTransceiver.getCurrentDirection()) {
            case RECV_ONLY:
                return FancyRTCRtpTransceiverDirection.RECV_ONLY;
            case SEND_ONLY:
                return FancyRTCRtpTransceiverDirection.SEND_ONLY;
            case SEND_RECV:
                return FancyRTCRtpTransceiverDirection.SEND_RECV;
            default:
                return FancyRTCRtpTransceiverDirection.INACTIVE;
        }
    }

    public String getMid() {
        return rtpTransceiver.getMid();
    }

    public FancyRTCRtpReceiver getReceiver() {
        return new FancyRTCRtpReceiver(rtpTransceiver.getReceiver());
    }

    public FancyRTCRtpSender getSender() {
        return new FancyRTCRtpSender(rtpTransceiver.getSender());
    }

    public boolean getStopped() {
        return rtpTransceiver.isStopped();
    }

    public void stop() {
        rtpTransceiver.stop();
    }

}
