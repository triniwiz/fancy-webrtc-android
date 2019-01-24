package co.fitcom.fancywebrtc;

import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by triniwiz on 1/16/19
 */
public class FancyRTCMediaStream {
    private MediaStream stream;

    public FancyRTCMediaStream() {
        stream = new MediaStream(FancyUtils.getLongUUID());
    }

    public FancyRTCMediaStream(long trackId) {
        stream = new MediaStream(trackId);
    }

    FancyRTCMediaStream(MediaStream mediaStream) {
        stream = mediaStream;
    }

    public String getId() {
        return stream.getId();
    }

    public List<FancyRTCVideoTrack> getVideoTracks() {
        List<VideoTrack> tracks = stream.videoTracks;
        List<FancyRTCVideoTrack> fancyVideoTracks = new ArrayList<>();
        for (VideoTrack track : tracks) {
            fancyVideoTracks.add(new FancyRTCVideoTrack(track));
        }
        return fancyVideoTracks;
    }

    public List<FancyRTCAudioTrack> getAudioTracks() {
        List<AudioTrack> tracks = stream.audioTracks;
        List<FancyRTCAudioTrack> fancyAudioTracks = new ArrayList<>();
        for (AudioTrack track : tracks) {
            fancyAudioTracks.add(new FancyRTCAudioTrack(track));
        }
        return fancyAudioTracks;
    }


    public void addTrack(FancyRTCVideoTrack track) {
        stream.addTrack(track.videoTrack);
    }

    public void addTrack(FancyRTCAudioTrack track) {
        stream.addTrack(track.audioTrack);
    }


    public void removeTrack(FancyRTCVideoTrack track) {
        stream.removeTrack(track.videoTrack);
    }

    public void removeTrack(FancyRTCAudioTrack track) {
        stream.removeTrack(track.audioTrack);
    }

    public MediaStream getStream() {
        return stream;
    }
}
