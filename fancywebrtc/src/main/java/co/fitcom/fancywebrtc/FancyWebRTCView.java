package co.fitcom.fancywebrtc;

import android.content.Context;
import android.util.AttributeSet;

import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

/**
 * Created by Osei Fortune on 8/15/18
 */
public class FancyWebRTCView extends SurfaceViewRenderer {
    private VideoTrack track;
    private FancyRTCMediaStream stream;
    private MediaStream mediaStream;

    public FancyWebRTCView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        init(FancyWebRTCEglUtils.getRootEglBaseContext(), null);
        setEnableHardwareScaler(true);
    }

    @Override
    public void setMirror(boolean mirror) {
        super.setMirror(mirror);
    }

    public void setVideoTrack(VideoTrack track) {
        if (this.track != null) {
            this.track.dispose();
        }
        this.track = track;
        track.addSink(this);
    }

    public void setSrcObject(FancyRTCMediaStream stream) {
        this.stream = stream;
        if (this.stream != null) {
            MediaStream mediaStream = this.stream.getStream();
            if (mediaStream.videoTracks.size() > 0) {
                VideoTrack track = mediaStream.videoTracks.get(0);
                if (this.track != null) {
                    this.track.dispose();
                }
                this.track = track;
                track.addSink(this);
            }
        }
    }

    public void setSrcObject(MediaStream stream) {
        if (this.stream != null) {
            mediaStream = stream;
            if (mediaStream.videoTracks.size() > 0) {
                VideoTrack track = mediaStream.videoTracks.get(0);
                if (this.track != null) {
                    this.track.dispose();
                }
                this.track = track;
                track.addSink(this);
            }
        }
    }
}
