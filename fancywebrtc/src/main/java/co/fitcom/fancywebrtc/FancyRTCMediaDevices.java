package co.fitcom.fancywebrtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.Map;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCMediaDevices {
    static PeerConnectionFactory factory;
    private static final int DEFAULT_HEIGHT = 480;
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_FPS = 25;

    public static interface GetUserMediaListener {
        public void onSuccess(FancyRTCMediaStream mediaStream);

        public void onError(String error);
    }

    public static void getUserMedia(Context context, FancyRTCMediaStreamConstraints constraints, GetUserMediaListener listener) {
        FancyRTCPeerConnection.executor.execute(() -> {
            String streamId = FancyUtils.getUUID();
            Log.d("co.test", " getUserMedia" + FancyRTCPeerConnection.factory);
            MediaStream localStream = factory.createLocalMediaStream(streamId);

            VideoSource videoSource = null;
            if (constraints.isVideoEnabled) {
                videoSource = factory.createVideoSource(false);
                String videoTrackId = FancyUtils.getUUID();
                VideoTrack videoTrack = factory.createVideoTrack(videoTrackId, videoSource);
                localStream.addTrack(videoTrack);
            }

            if (constraints.isAudioEnabled) {
                AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
                String audioTrackId = FancyUtils.getUUID();
                AudioTrack audioTrack = factory.createAudioTrack(audioTrackId, audioSource);
                if (constraints.audioConstraints != null && constraints.audioConstraints.containsKey("volume")) {
                    double volume = (double) constraints.audioConstraints.get("volume");
                    audioTrack.setVolume(volume);
                }
                localStream.addTrack(audioTrack);
            }

            CameraEnumerator enumerator;
            if (Camera2Enumerator.isSupported(context)) {
                enumerator = new Camera2Enumerator(context);
            } else {
                enumerator = new Camera1Enumerator(false);
            }

            String[] deviceNames = enumerator.getDeviceNames();
            String selectedDevice = null;
            boolean useFrontCamera = true;
            if (constraints.videoConstraints != null && constraints.videoConstraints.containsKey("facingMode")) {
                String facingMode = (String) constraints.videoConstraints.get("facingMode");
                useFrontCamera = facingMode == null || !facingMode.equals("environment");
            }
            Object width = null;
            Object height = null;
            Integer minWidth = -1;
            Integer minHeight = -1;
            Integer idealWidth = -1;
            Integer idealHeight = -1;
            Integer maxWidth = -1;
            Integer maxHeight = -1;
            Integer frameRate = DEFAULT_FPS;
            if (constraints.videoConstraints != null && constraints.videoConstraints.containsKey("width") && constraints.videoConstraints.containsKey("height")) {
                width = constraints.videoConstraints.get("width");
                height = constraints.videoConstraints.get("height");
                Object rate = constraints.videoConstraints.get("frameRate");
                frameRate = rate != null ? (Integer) rate : DEFAULT_FPS;
                if (width != null && width.getClass() == Map.class) {
                    Map<?, ?> widthMap = (Map<?, ?>) width;
                    if (widthMap.containsKey("min")) {
                        minWidth = (Integer) widthMap.get("min");
                    }
                    if (widthMap.containsKey("ideal")) {
                        idealWidth = (Integer) widthMap.get("ideal");
                    }
                    if (widthMap.containsKey("max")) {
                        maxWidth = (Integer) widthMap.get("max");
                    }
                }
                if (height != null && height.getClass() == Map.class) {
                    Map<?, ?> heightMap = (Map<?, ?>) height;
                    if (heightMap.containsKey("min")) {
                        minHeight = (Integer) heightMap.get("min");
                    }
                    if (heightMap.containsKey("ideal")) {
                        idealHeight = (Integer) heightMap.get("ideal");
                    }
                    if (heightMap.containsKey("max")) {
                        maxHeight = (Integer) heightMap.get("max");
                    }
                }
            }

            if (useFrontCamera) {
                for (int i = 0; i < deviceNames.length; i++) {
                    String deviceName = deviceNames[i];
                    if (enumerator.isFrontFacing(deviceName)) {
                        selectedDevice = deviceName;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < deviceNames.length; i++) {
                    String deviceName = deviceNames[i];
                    if (enumerator.isBackFacing(deviceName)) {
                        selectedDevice = deviceName;
                        break;
                    }
                }
            }

            VideoCapturer capturer = enumerator.createCapturer(selectedDevice, new CameraVideoCapturer.CameraEventsHandler() {
                @Override
                public void onCameraError(String s) {

                }

                @Override
                public void onCameraDisconnected() {

                }

                @Override
                public void onCameraFreezed(String s) {

                }

                @Override
                public void onCameraOpening(String s) {

                }

                @Override
                public void onFirstFrameAvailable() {

                }

                @Override
                public void onCameraClosed() {

                }
            });

            int w; // Final width
            int h; // Final height
            int f = frameRate; // Final Frames per second

            capturer.initialize(SurfaceTextureHelper.create("FancyWebRTCGetMedia", FancyWebRTCEglUtils.getRootEglBaseContext()), context, videoSource.getCapturerObserver());

            if (width != null && width.getClass() == Integer.class) {
                w = (int) width;
            } else if (maxWidth > -1) {
                w = maxWidth;
            } else if (idealWidth > -1) {
                w = idealWidth;
            } else if (minWidth > -1) {
                w = minWidth;
            } else {
                w = DEFAULT_WIDTH;
            }

            if (height != null && height.getClass() == Integer.class) {
                h = (int) height;
            } else if (maxHeight > -1) {
                h = maxHeight;
            } else if (idealHeight > -1) {
                h = idealHeight;
            } else if (minHeight > -1) {
                h = minHeight;
            } else {
                h = DEFAULT_HEIGHT;
            }

            capturer.startCapture(w, h, f);

            FancyRTCMediaStream fancyMediaStream = new FancyRTCMediaStream(localStream);
            listener.onSuccess(fancyMediaStream);
        });
    }
}
