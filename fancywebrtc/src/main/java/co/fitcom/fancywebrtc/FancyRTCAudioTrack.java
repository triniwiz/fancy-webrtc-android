package co.fitcom.fancywebrtc;

import org.webrtc.AudioTrack;

/**
 * Created by triniwiz on 1/16/19
 */
public class FancyRTCAudioTrack {
    AudioTrack audioTrack;

    FancyRTCAudioTrack(AudioTrack track) {
        audioTrack = track;
    }

    public void setEnabled(boolean enabled) {
        audioTrack.setEnabled(enabled);
    }

    public void setVolume(double volume) {
        audioTrack.setVolume(volume);
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }
}
