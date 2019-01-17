package co.fitcom.fancywebrtc;

import org.webrtc.AudioTrack;

/**
 * Created by triniwiz on 1/16/19
 */
public class FancyRTCAudioTrack extends FancyRTCMediaStreamTrack {
    AudioTrack audioTrack;

    FancyRTCAudioTrack(AudioTrack track) {
        super(track);
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
