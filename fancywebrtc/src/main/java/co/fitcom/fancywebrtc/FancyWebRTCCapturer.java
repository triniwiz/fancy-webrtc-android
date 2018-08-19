package co.fitcom.fancywebrtc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CapturerObserver;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.lang.ref.WeakReference;

/**
 * Created by triniwiz on 8/15/18
 */
public class FancyWebRTCCapturer {
    private CameraVideoCapturer videoCapturer;
    private VideoSource videoSource;
    private CameraEnumerationAndroid.CaptureFormat format;
    private FancyWebRTC webRTC;

    public boolean hasPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }


    public FancyWebRTCCapturer(FancyWebRTC webRTC, CameraVideoCapturer videoCapturer) {
        this.videoCapturer = videoCapturer;
        this.webRTC = webRTC;
    }

    public void startVideo(Context context) {

        videoCapturer.initialize(SurfaceTextureHelper.create("fancyWebRTCVideoCapturer", FancyWebRTCEglUtils.getRootEglBaseContext()), context, videoSource.getCapturerObserver());
        videoCapturer.startCapture(format.width, format.height, format.framerate.max);
    }

    public void setVideoSource(VideoSource videoSource) {
        this.videoSource = videoSource;
    }

    public void setFormat(CameraEnumerationAndroid.CaptureFormat format) {
        this.format = format;
    }

    public void stopVideo() {
        try {
            videoCapturer.stopCapture();
        } catch (InterruptedException e) {

        }
    }

    public void toggleCamera() {
        videoCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(boolean b) {
                if (webRTC != null && webRTC.getListener() != null) {
                    webRTC.getListener().webRTCClientOnCameraSwitchDone(webRTC, b);
                }
            }

            @Override
            public void onCameraSwitchError(String s) {
                if (webRTC != null && webRTC.getListener() != null) {
                    webRTC.getListener().webRTCClientOnCameraSwitchError(webRTC, s);
                }
            }
        });
    }

}
