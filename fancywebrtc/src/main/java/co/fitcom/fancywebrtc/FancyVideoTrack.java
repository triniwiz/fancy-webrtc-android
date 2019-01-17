package co.fitcom.fancywebrtc;

import org.webrtc.VideoTrack;

/**
 * Created by triniwiz on 1/16/19
 */
public class FancyVideoTrack {
    VideoTrack videoTrack;

    FancyVideoTrack(VideoTrack track) {
        videoTrack = track;
    }

    public void setEnabled(boolean enabled) {
        videoTrack.setEnabled(enabled);
    }

    public VideoTrack getVideoTrack() {
        return videoTrack;
    }
}
