package co.fitcom.fancywebrtc;

import java.util.Map;

/**
 * Created by triniwiz on 1/15/19
 */
public class FancyRTCMediaStreamConstraints {
    boolean hasVideoConstraints = false;
    boolean hasAudioConstraints = false;
    boolean isVideoEnabled = false;
    boolean isAudioEnabled = false;
    Map<String, Object> audioConstraints;
    Map<String, Object> videoConstraints;

    public FancyRTCMediaStreamConstraints(boolean audio, boolean video) {
        if (audio) {
            isAudioEnabled = true;
        }
        if (video) {
            isVideoEnabled = true;
        }
    }

    public FancyRTCMediaStreamConstraints(boolean audio, Map<String, Object> video) {
        hasVideoConstraints = true;
        if (audio) {
            isAudioEnabled = true;
        }
        isVideoEnabled = true;
        videoConstraints = video;
    }

    public FancyRTCMediaStreamConstraints(Map<String, Object> audio, boolean video) {
        hasAudioConstraints = true;
        isAudioEnabled = true;
        if (video) {
            isVideoEnabled = true;
        }
        audioConstraints = audio;
    }

    public FancyRTCMediaStreamConstraints(Map<String, Object> audio, Map<String, Object> video) {
        isAudioEnabled = true;
        isVideoEnabled = true;
        audioConstraints = audio;
        videoConstraints = video;
    }
}
