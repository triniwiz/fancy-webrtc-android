package co.fitcom.fancywebrtc;

import android.util.Log;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.VideoTrack;

/**
 * Created by triniwiz on 1/16/19
 */
public class FancyRTCVideoTrack extends FancyRTCMediaStreamTrack {
    VideoTrack videoTrack;
    FancyRTCMediaTrackSettings settings;
    public FancyRTCVideoTrack(VideoTrack track) {
        super(track);
        this.videoTrack = track;
        settings = new FancyRTCMediaTrackSettings(videoTrack.id(), "video");
    }

    public void stop() {
        videoTrack.setEnabled(false);
    }

    public FancyRTCMediaTrackSettings getSettings(){
        return settings;
    }

    public void setEnabled(boolean enabled) {
        videoTrack.setEnabled(enabled);
    }

    public VideoTrack getVideoTrack() {
        return videoTrack;
    }

    public void applyConstraints(FancyRTCMediaTrackConstraints constraints, FancyRTCMediaStreamTrackListener listener) {
        if (constraints.getFacingMode() != null) {
            String facingMode = constraints.getFacingMode();
            boolean useFrontCamera = facingMode == null || !facingMode.equals("environment");
            FancyRTCMediaDevices.FancyCapturer capturer = FancyRTCMediaDevices.videoTrackcapturerMap.get(videoTrack.id());
            if (capturer != null) {
                if (!capturer.getPosition().equals(constraints.getFacingMode())) {
                    if (capturer.getCapturer() != null) {
                        capturer.getCapturer().switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
                            @Override
                            public void onCameraSwitchDone(boolean b) {
                                capturer.setPosition(useFrontCamera ? "user" : "environment");
                                FancyRTCMediaDevices.videoTrackcapturerMap.put(videoTrack.id(), capturer);
                                listener.onSuccess();
                            }

                            @Override
                            public void onCameraSwitchError(String s) {
                                listener.onError(s);
                            }
                        });
                    }
                }

            }
        }
    }
}
