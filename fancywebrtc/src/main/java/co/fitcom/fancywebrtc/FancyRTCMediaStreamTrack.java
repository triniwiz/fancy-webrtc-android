package co.fitcom.fancywebrtc;

import org.webrtc.MediaStreamTrack;

/**
 * Created by triniwiz on 1/17/19
 */
public class FancyRTCMediaStreamTrack {
    private MediaStreamTrack mediaStreamTrack;
    private FancyRTCMediaTrackSettings mediaTrackSettings;

    public FancyRTCMediaStreamTrack(MediaStreamTrack track) {
        mediaStreamTrack = track;
        mediaTrackSettings = new FancyRTCMediaTrackSettings(track.id(), track.kind());
    }

    public static interface FancyRTCMediaStreamTrackListener {
        public void onSuccess();

        public void onError(String error);
    }

    public FancyRTCMediaTrackSettings getSettings() {
        return mediaTrackSettings;
    }

    public String getId() {
        return mediaStreamTrack.id();
    }

    public boolean getEnabled() {
        return mediaStreamTrack.enabled();
    }

    public String getKind() {
        return mediaStreamTrack.kind();
    }

    public boolean getMute() {
        return mediaStreamTrack.enabled();
    }

    public void setEnabled(boolean enabled) {
        mediaStreamTrack.setEnabled(enabled);
    }

    public void dispose() {
        mediaStreamTrack.dispose();
    }

    public String getReadyState() {
        switch (mediaStreamTrack.state()) {
            case LIVE:
                return "live";
            default:
                return "ended";
        }
    }

    public MediaStreamTrack getMediaStreamTrack() {
        return mediaStreamTrack;
    }
}
