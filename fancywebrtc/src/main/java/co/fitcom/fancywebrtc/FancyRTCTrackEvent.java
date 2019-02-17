package co.fitcom.fancywebrtc;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCTrackEvent {
    private FancyRTCRtpReceiver receiver;
    private List<FancyRTCMediaStream> streams;
    private FancyRTCMediaStreamTrack mediaTrack;
    private FancyRTCRtpTransceiver transceiver;

    FancyRTCTrackEvent(FancyRTCRtpReceiver receiver, List<FancyRTCMediaStream> streams, @Nullable FancyRTCMediaStreamTrack mediaTrack, @Nullable FancyRTCRtpTransceiver transceiver) {
        this.receiver = receiver;
        this.streams = streams;
        this.mediaTrack = mediaTrack;
        this.transceiver = transceiver;
    }

    public FancyRTCMediaStreamTrack getMediaTrack() {
        return mediaTrack;
    }

    public FancyRTCRtpReceiver getReceiver() {
        return receiver;
    }

    public FancyRTCRtpTransceiver getTransceiver() {
        return transceiver;
    }

    public List<FancyRTCMediaStream> getStreams() {
        return streams;
    }

}
