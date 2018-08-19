package co.fitcom.fancywebrtc;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;

/**
 * Created by triniwiz on 8/15/18
 */
public class FancyWebRTCView extends SurfaceViewRenderer {
    private VideoTrack track;

    public FancyWebRTCView(Context context) {
        super(context);
    }

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
}
