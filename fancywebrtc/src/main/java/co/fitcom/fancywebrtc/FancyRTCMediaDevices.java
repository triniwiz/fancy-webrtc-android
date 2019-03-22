package co.fitcom.fancywebrtc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.annotation.NonNull;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.Size;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCMediaDevices {
    private static final int DEFAULT_HEIGHT = 480;
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_FPS = 15;
    public static final int WEBRTC_SCREEN_PERMISSIONS_REQUEST_CODE = 5179;
    static Map<String, CameraVideoCapturer> capturerMap = new HashMap<>();
    static Map<String, FancyCapturer> videoTrackcapturerMap = new HashMap<>();

    public static interface GetUserMediaListener {
        public void onSuccess(FancyRTCMediaStream mediaStream);

        public void onError(String error);
    }

    static class FancyCapturer {
        private CameraVideoCapturer capturer;
        private String position;

        FancyCapturer(CameraVideoCapturer capturer, String position) {
            this.capturer = capturer;
            this.position = position;
        }

        public CameraVideoCapturer getCapturer() {
            return capturer;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    public static void stopCapturers() {
        for (FancyCapturer entry : videoTrackcapturerMap.values()) {
            entry.getCapturer().dispose();
        }
        videoTrackcapturerMap.clear();
    }

    public static void getUserMedia(Context context, FancyRTCMediaStreamConstraints constraints, GetUserMediaListener listener) {
        FancyRTCPeerConnection.executor.execute(() -> {
            String streamId = FancyUtils.getUUID();
            PeerConnectionFactory factory = FancyRTCPeerConnection.factory;
            MediaStream localStream = FancyRTCPeerConnection.factory.createLocalMediaStream(streamId);

            VideoSource videoSource = null;
            String videoTrackId = FancyUtils.getUUID();
            if (constraints.isVideoEnabled) {
                videoSource = factory.createVideoSource(false);
                VideoTrack videoTrack = factory.createVideoTrack(videoTrackId, videoSource);
                localStream.addTrack(videoTrack);
            }

            if (constraints.isAudioEnabled) {
                MediaConstraints audioConstraints = new MediaConstraints();
                audioConstraints.optional.add(
                        new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
                audioConstraints.optional.add(
                        new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
                audioConstraints.optional.add(
                        new MediaConstraints.KeyValuePair("echoCancellation", "true"));
                audioConstraints.optional.add(
                        new MediaConstraints.KeyValuePair("googEchoCancellation2", "true"));
                audioConstraints.optional.add(
                        new MediaConstraints.KeyValuePair(
                                "googDAEchoCancellation", "true"));
                AudioSource audioSource = factory.createAudioSource(audioConstraints);
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

            CameraVideoCapturer capturer = enumerator.createCapturer(selectedDevice, new CameraVideoCapturer.CameraEventsHandler() {
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
            capturerMap.put(capturer.toString(), capturer);
            videoTrackcapturerMap.put(videoTrackId, new FancyCapturer(capturer, useFrontCamera ? "user" : "environment"));
            int w; // Final width
            int h; // Final height
            int f = frameRate; // Final Frames per second
            capturer.initialize(SurfaceTextureHelper.create(Thread.currentThread().getName(), FancyWebRTCEglUtils.getRootEglBaseContext()), context, videoSource.getCapturerObserver());
            List<CameraEnumerationAndroid.CaptureFormat> formatList = enumerator.getSupportedFormats(selectedDevice);
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


            /*
            CameraEnumerationAndroid.CaptureFormat firstFormat = enumerator.getSupportedFormats(selectedDevice).get(0); // Highest format
            if(h > firstFormat.height){
                h = firstFormat.height;
            }
            if(w > firstFormat.width){
                w = firstFormat.width;
            }
            if(f > firstFormat.framerate.max){
                f = firstFormat.framerate.max;
            }
            */

            List<Size> sizeList = new ArrayList<>();
            List<CameraEnumerationAndroid.CaptureFormat.FramerateRange> fpsRange = new ArrayList<>();
            for (CameraEnumerationAndroid.CaptureFormat format : formatList) {
                sizeList.add(new Size(format.width, format.height));
                fpsRange.add(new CameraEnumerationAndroid.CaptureFormat.FramerateRange(format.framerate.min, format.framerate.max));
            }
            Size closestSize = CameraEnumerationAndroid.getClosestSupportedSize(sizeList, w, h);
            CameraEnumerationAndroid.CaptureFormat.FramerateRange closestFrameRate = CameraEnumerationAndroid.getClosestSupportedFramerateRange(fpsRange, f);
            capturer.startCapture(closestSize.width, closestSize.height, closestFrameRate.max);
            //videoSource.adaptOutputFormat(w, h, f);
            FancyRTCMediaStream fancyMediaStream = new FancyRTCMediaStream(localStream);
            listener.onSuccess(fancyMediaStream);
        });
    }

    public static void getDisplayMedia(Context context, FancyRTCMediaStreamConstraints constraints, GetUserMediaListener listener) {
        FancyRTCPeerConnection.executor.execute(() -> {
            if (Build.VERSION.SDK_INT >= 21) {
                MediaProjectionManager mediaProjectionManager =
                        (MediaProjectionManager) context.getSystemService(
                                Context.MEDIA_PROJECTION_SERVICE);
                Intent intent = mediaProjectionManager.createScreenCaptureIntent();
                if (intent == null) {
                    listener.onError("Unknown error");
                } else {
                    FancyRTCApplicationHelper.getInstance().onResult(WEBRTC_SCREEN_PERMISSIONS_REQUEST_CODE, new FancyRTCApplicationHelper.Callback() {
                        @Override
                        public void onPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

                        }

                        @Override
                        public void onResult(int requestCode, int resultCode, Intent data) {
                            String streamId = FancyUtils.getUUID();
                            PeerConnectionFactory factory = FancyRTCPeerConnection.factory;
                            MediaStream localStream = FancyRTCPeerConnection.factory.createLocalMediaStream(streamId);

                            VideoSource videoSource = null;
                            String videoTrackId = FancyUtils.getUUID();
                            if (constraints.isVideoEnabled) {
                                videoSource = factory.createVideoSource(true);
                                VideoTrack videoTrack = factory.createVideoTrack(videoTrackId, videoSource);
                                localStream.addTrack(videoTrack);
                            }

                            if (constraints.isAudioEnabled) {
                                MediaConstraints audioConstraints = new MediaConstraints();
                                audioConstraints.optional.add(
                                        new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
                                audioConstraints.optional.add(
                                        new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
                                audioConstraints.optional.add(
                                        new MediaConstraints.KeyValuePair("echoCancellation", "true"));
                                audioConstraints.optional.add(
                                        new MediaConstraints.KeyValuePair("googEchoCancellation2", "true"));
                                audioConstraints.optional.add(
                                        new MediaConstraints.KeyValuePair(
                                                "googDAEchoCancellation", "true"));
                                AudioSource audioSource = factory.createAudioSource(audioConstraints);
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

                            ScreenCapturerAndroid screenCapturer = new ScreenCapturerAndroid(data, new MediaProjection.Callback() {
                                @Override
                                public void onStop() {
                                    super.onStop();
                                }
                            });

                            int w; // Final width
                            int h; // Final height
                            int f = frameRate; // Final Frames per second
                            screenCapturer.initialize(SurfaceTextureHelper.create(Thread.currentThread().getName(), FancyWebRTCEglUtils.getRootEglBaseContext()), context, videoSource.getCapturerObserver());
                            List<CameraEnumerationAndroid.CaptureFormat> formatList = enumerator.getSupportedFormats(selectedDevice);
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

                            List<Size> sizeList = new ArrayList<>();
                            List<CameraEnumerationAndroid.CaptureFormat.FramerateRange> fpsRange = new ArrayList<>();
                            for (CameraEnumerationAndroid.CaptureFormat format : formatList) {
                                sizeList.add(new Size(format.width, format.height));
                                fpsRange.add(new CameraEnumerationAndroid.CaptureFormat.FramerateRange(format.framerate.min, format.framerate.max));
                            }
                            Size closestSize = CameraEnumerationAndroid.getClosestSupportedSize(sizeList, w, h);
                            CameraEnumerationAndroid.CaptureFormat.FramerateRange closestFrameRate = CameraEnumerationAndroid.getClosestSupportedFramerateRange(fpsRange, f);
                            screenCapturer.startCapture(closestSize.width, closestSize.height, closestFrameRate.max);
                            //videoSource.adaptOutputFormat(w, h, f);
                            FancyRTCMediaStream fancyMediaStream = new FancyRTCMediaStream(localStream);
                            listener.onSuccess(fancyMediaStream);

                        }
                    });
                    ((Activity) context).startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), WEBRTC_SCREEN_PERMISSIONS_REQUEST_CODE);
                }
            } else {
                listener.onError("Device not supported");
            }
        });
    }
}
