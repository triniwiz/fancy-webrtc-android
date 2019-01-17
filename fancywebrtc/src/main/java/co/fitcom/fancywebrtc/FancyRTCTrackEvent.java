package co.fitcom.fancywebrtc;

import org.webrtc.MediaStreamTrack;

import java.util.List;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCTrackEvent {
    private FancyRTCRtpReceiver receiver;
    List<FancyRTCMediaStream> streams;
    FancyRTCMediaTrack mediaTrack;
    FancyRTCRtpTransceiver transceiver;

    FancyRTCTrackEvent() {

    }
}
