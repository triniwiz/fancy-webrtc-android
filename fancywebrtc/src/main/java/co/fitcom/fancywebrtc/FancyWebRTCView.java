package co.fitcom.fancywebrtc;

import android.content.Context;
import android.util.AttributeSet;

import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

/**
 * Created by Osei Fortune on 8/15/18
 */
public class FancyWebRTCView extends SurfaceViewRenderer {
    private MediaStreamTrack track;
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
            track.removeSink(this);
        }
        this.track = track;
        track.addSink(this);
    }

    public void setSrcObject(FancyRTCMediaStream stream) {
        mediaStream = stream.getStream();
        if (mediaStream.videoTracks.size() > 0) {
            VideoTrack track =  mediaStream.videoTracks.get(0);
            if (this.track != null) {
                ((VideoTrack) this.track).removeSink(this);
            }
            this.track = track;
            track.addSink(this);
        }
    }

    public void setSrcObject(MediaStream stream) {
        mediaStream = stream;
        if (mediaStream.videoTracks.size() > 0) {
            VideoTrack track = mediaStream.videoTracks.get(0);
            if (this.track != null) {
                ((VideoTrack) this.track).removeSink(this);
            }
            this.track = track;
            track.addSink(this);
        }
    }

    public void setSrcObject(FancyRTCMediaStreamTrack track) {
        MediaStreamTrack mediaStreamTrack = track.getMediaStreamTrack();
        if (mediaStreamTrack != null) {
            if (mediaStreamTrack instanceof org.webrtc.AudioTrack) return;
            if (this.track != null) {
                ((VideoTrack) this.track).removeSink(this);
            }
            try {
                this.track = mediaStreamTrack;
                VideoTrack videoTrack = (VideoTrack) mediaStreamTrack;
                videoTrack.addSink(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setSrcObject(MediaStreamTrack track) {
        if (track instanceof org.webrtc.AudioTrack) return;
        if (this.track != null) {
            ((VideoTrack) this.track).removeSink(this);
        }
        try {
            this.track = track;
            VideoTrack videoTrack = (VideoTrack) track;
            videoTrack.addSink(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
