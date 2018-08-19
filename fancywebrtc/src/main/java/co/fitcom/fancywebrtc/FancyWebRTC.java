package co.fitcom.fancywebrtc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaSource;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by triniwiz on 8/15/18
 */

public class FancyWebRTC {

    private PeerConnectionFactory factory;
    private PeerConnection connection;
    private Map<String, MediaStream> localMediaStreams;
    private Map<String, MediaStream> remoteMediaStreams;
    private MediaConstraints defaultConstraints;
    private ArrayList<IceCandidate> remoteIceCandidates;
    private ArrayList<PeerConnection.IceServer> remoteIceServers;
    private FancyWebRTCListener listener;
    private WeakReference<FancyWebRTC> ref;
    private boolean videoEnabled = true;
    private boolean audioEnabled = true;
    public static String[] WEBRTC_PERMISSIONS = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO};
    public static int WEBRTC_PERMISSIONS_REQUEST_CODE = 12345;
    private PeerConnection.RTCConfiguration configuration;
    private String[] defaultIceServers = new String[]{
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302",
            "stun:stun3.l.google.com:19302",
            "stun:stun4.l.google.com:19302"
    };
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, MediaData> tracks = new HashMap<>();
    private final Map<String, DataChannel> dataChannels = new HashMap<>();
    private final Map<String, FancyWebRTCListener.GetUserMediaListener> getUserMediaListenerMap = new HashMap<>();
    private final Context appContext;
    public static String Tag = "co.fitcom.fancywebrtc";
    class MediaData {
        public final MediaSource mediaSource;
        public final MediaStreamTrack track;
        public final FancyWebRTCCapturer capturer;

        public MediaData(MediaSource mediaSource, MediaStreamTrack track, FancyWebRTCCapturer capturer) {
            this.mediaSource = mediaSource;
            this.track = track;
            this.capturer = capturer;
        }
    }

    public static void init(Context context) {
        FancyWebRTCEglUtils.getRootEglBase();
        PeerConnectionFactory.InitializationOptions.Builder builder = PeerConnectionFactory.InitializationOptions.builder(context);
        builder.setEnableInternalTracer(true);
        PeerConnectionFactory.initialize(builder.createInitializationOptions());

    }

    public FancyWebRTC(Context context, boolean videoEnabled, boolean audioEnabled) {
        initialize(videoEnabled, audioEnabled, null);
        this.appContext = context;
    }

    public FancyWebRTC(Context context, boolean videoEnabled, boolean audioEnabled, ArrayList<PeerConnection.IceServer> iceServers) {
        initialize(videoEnabled, audioEnabled, iceServers);
        this.appContext = context;
    }

    private void initialize(final boolean videoEnabled, final boolean audioEnabled, final @Nullable ArrayList<PeerConnection.IceServer> iceServers) {
        ref = new WeakReference<>(this);
        this.videoEnabled = videoEnabled;
        this.audioEnabled = audioEnabled;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder();
                PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
                builder.setOptions(options);

                factory = builder.createPeerConnectionFactory();

                remoteIceCandidates = new ArrayList<>();
                localMediaStreams = new HashMap<>();
                remoteMediaStreams = new HashMap<>();
                defaultConstraints = new MediaConstraints();

                defaultConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        "OfferToReceiveAudio",
                        "true"
                ));
                defaultConstraints.mandatory.add(
                        new MediaConstraints.KeyValuePair(
                                "OfferToReceiveVideo",
                                "true"
                        )
                );

                if (iceServers == null) {
                    ArrayList<PeerConnection.IceServer> defaultIceServersList = new ArrayList<>();
                    for (String url : defaultIceServers) {
                        defaultIceServersList.add(PeerConnection.IceServer.builder(url).createIceServer());
                    }
                    configuration = new PeerConnection.RTCConfiguration(defaultIceServersList);
                } else {
                    configuration = new PeerConnection.RTCConfiguration(iceServers);
                }

                VideoEncoderFactory encoderFactory;
                VideoDecoderFactory decoderFactory;
                if (FancyWebRTCEglUtils.getRootEglBaseContext() != null) {
                    encoderFactory = new DefaultVideoEncoderFactory(FancyWebRTCEglUtils.getRootEglBaseContext(), false, false);
                    decoderFactory = new DefaultVideoDecoderFactory(FancyWebRTCEglUtils.getRootEglBaseContext());
                } else {
                    encoderFactory = new SoftwareVideoEncoderFactory();
                    decoderFactory = new SoftwareVideoDecoderFactory();
                }

                builder.setVideoDecoderFactory(decoderFactory);
                builder.setVideoEncoderFactory(encoderFactory);


                configuration.enableDtlsSrtp = true;
                configuration.enableRtpDataChannel = true;

                configuration.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
                configuration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
                configuration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
                configuration.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
                configuration.keyType = PeerConnection.KeyType.ECDSA;
            }
        });

    }

    public enum DataChannelState {
        CONNECTING,
        OPEN,
        CLOSING,
        CLOSED
    }

    public void toggleSpeaker() {
        AudioManager manager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
            if (manager.isSpeakerphoneOn()) {
                manager.setSpeakerphoneOn(false);
            } else {
                manager.setSpeakerphoneOn(true);
            }
        }
    }

    public void toggleMic() {
        AudioManager manager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
            if (manager.isMicrophoneMute()) {
                manager.setMicrophoneMute(false
                );
            } else {
                manager.setMicrophoneMute(true);
            }
        }
    }

    public void speakerEnabled(boolean enabled) {
        AudioManager manager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
            manager.setSpeakerphoneOn(enabled);
        }
    }

    public void micEnabled(boolean enabled) {
        AudioManager manager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
            manager.setMicrophoneMute(enabled);
        }
    }

    public enum DataChannelMessageType {
        TEXT,
        BINARY
    }

    public void setListener(FancyWebRTCListener listener) {
        this.listener = listener;
    }

    public void connect() {
        if (connection != null) return;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                connection = factory.createPeerConnection(configuration, new PeerConnection.Observer() {
                    @Override
                    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                        if (listener != null) {
                            listener.webRTCClientOnSignalingChange(ref.get(), signalingState);
                        }
                    }

                    @Override
                    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                        if (listener != null) {
                            listener.webRTCClientOnIceConnectionChange(ref.get(), iceConnectionState);
                        }
                    }

                    @Override
                    public void onIceConnectionReceivingChange(boolean b) {
                        if (listener != null) {
                            listener.webRTCClientOnIceConnectionReceivingChange(ref.get(), b);
                        }

                    }

                    @Override
                    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                        if (listener != null) {
                            listener.webRTCClientOnIceGatheringChange(ref.get(), iceGatheringState);
                        }
                    }

                    @Override
                    public void onIceCandidate(IceCandidate iceCandidate) {
                        if (listener != null) {
                            listener.webRTCClientDidGenerateIceCandidate(ref.get(), iceCandidate);
                        }
                    }

                    @Override
                    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                        if (listener != null) {
                            listener.webRTCClientOnIceCandidatesRemoved(ref.get(), iceCandidates);
                        }
                    }

                    @Override
                    public void onAddStream(MediaStream mediaStream) {
                        if (listener != null) {
                            if (mediaStream.videoTracks.size() > 0) {
                                listener.webRTCClientDidReceiveRemoteVideoTrackStream(ref.get(), mediaStream.videoTracks.get(0), mediaStream);
                            }

                        }
                    }

                    @Override
                    public void onRemoveStream(MediaStream mediaStream) {
                        if (listener != null) {
                            listener.webRTCClientOnRemoveStream(ref.get(), mediaStream);
                        }
                    }

                    @Override
                    public void onDataChannel(DataChannel dataChannel) {
                        dataChannels.put(dataChannel.label(), dataChannel);
                        registerDataChannelObserver(dataChannel.label());
                    }

                    @Override
                    public void onRenegotiationNeeded() {
                        if (listener != null) {
                            listener.webRTCClientOnRenegotiationNeeded(ref.get());
                        }
                    }

                    @Override
                    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                    }
                });
            }
        });
    }

    private void registerDataChannelObserver(String name) {
        final DataChannel dataChannel = dataChannels.get(name);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (dataChannel != null) {
                    dataChannel.registerObserver(new DataChannel.Observer() {
                        @Override
                        public void onBufferedAmountChange(long l) {

                        }

                        @Override
                        public void onStateChange() {
                            if (listener != null) {
                                listener.webRTCClientDataChannelStateChanged(ref.get(), dataChannel.label(), dataChannel.state());
                            }
                        }

                        @Override
                        public void onMessage(DataChannel.Buffer buffer) {
                            byte[] bytes;
                            if (buffer.data.hasArray()) {
                                bytes = buffer.data.array();
                            } else {
                                bytes = new byte[buffer.data.remaining()];
                                buffer.data.get(bytes);
                            }

                            DataChannelMessageType type;
                            String data;
                            if (buffer.binary) {
                                type = DataChannelMessageType.BINARY;
                                data = Base64.encodeToString(bytes, Base64.NO_WRAP);
                            } else {
                                type = DataChannelMessageType.TEXT;
                                data = new String(bytes, Charset.forName("UTF-8"));
                            }

                            if (listener != null) {
                                listener.webRTCClientDataChannelMessageType(ref.get(), dataChannel.label(), data, type);
                            }
                        }
                    });
                }
            }
        });
    }

    public void enableTrack(String id, boolean enabled) {
        MediaData mediaData = tracks.get(id);
        if (mediaData != null) {
            if (mediaData.capturer != null) {
                if (enabled) {
                    mediaData.track.setEnabled(true);
                    mediaData.capturer.startVideo(appContext);
                } else {
                    mediaData.track.setEnabled(false);
                    mediaData.capturer.stopVideo();
                }
            } else {
                mediaData.track.setEnabled(enabled);
            }
        }
    }

    public void switchCamera(String id) {
        final MediaData mediaData = tracks.get(id);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (mediaData != null) {
                    if (mediaData.capturer != null) {
                        mediaData.capturer.toggleCamera();
                    }
                }
            }
        });
    }

    public FancyWebRTCListener getListener() {
        return listener;
    }

    public void disconnect() {
        if (connection == null) return;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                connection.close();
            }
        });
    }

    public void dataChannelSend(String name, String data, DataChannelMessageType type) {
        final DataChannel channel = dataChannels.get(name);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (channel != null) {
                    byte[] dataArray = null;
                    switch (type) {
                        case BINARY:
                            dataArray = Base64.decode(data, Base64.NO_WRAP);
                            break;
                        case TEXT:
                            try {
                                dataArray = data.getBytes("UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                return;
                            }
                            break;
                    }
                    ByteBuffer byteBuffer = ByteBuffer.wrap(dataArray);
                    DataChannel.Buffer buffer = new DataChannel.Buffer(byteBuffer, type == DataChannelMessageType.BINARY);
                    channel.send(buffer);
                }
            }
        });
    }

    public void dataChannelClose(String name) {
        final DataChannel channel = dataChannels.get(name);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (channel != null) {
                    channel.close();
                }
            }
        });
    }

    public void dataChannelCreate(String name) {
        final DataChannel.Init initData = new DataChannel.Init();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DataChannel channel = connection.createDataChannel(name, initData);
                dataChannels.put(name, channel);
                registerDataChannelObserver(name);
            }
        });
    }

    public void handleAnswerReceived(SessionDescription sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (connection == null || sdp == null) return;
                SessionDescription newSdp = new SessionDescription(SessionDescription.Type.ANSWER, sdp.description);
                connection.setRemoteDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {

                    }

                    @Override
                    public void onSetSuccess() {
                        Log.d("co.fitcom.fancywebrtc", "setRemoteDescription " + "onSetSuccess");
                    }

                    @Override
                    public void onCreateFailure(String s) {

                    }

                    @Override
                    public void onSetFailure(String s) {
                        if (listener != null) {
                            listener.webRTCClientDidReceiveError(ref.get(), s);
                        }
                    }
                }, newSdp);
            }
        });
    }

    public void createAnswerForOfferReceived(SessionDescription remoteSdp, MediaConstraints constraints) {
        Log.d(Tag,"createAnswerForOfferReceived " + "connection: " + connection + " remoteSdp: " +remoteSdp);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (connection == null || remoteSdp == null) return;
                if ( connection.getRemoteDescription() != null && (connection.getRemoteDescription().type == SessionDescription.Type.ANSWER && remoteSdp.type == SessionDescription.Type.ANSWER))
                    return;
                connection.setRemoteDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                    }

                    @Override
                    public void onSetSuccess() {
                        handleRemoteDescriptionSet();
                        connection.createAnswer(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                handleSdpGenerated(sessionDescription);
                                if (listener != null) {
                                    listener.webRTCClientStartCallWithSdp(ref.get(), sessionDescription);
                                }
                            }

                            @Override
                            public void onSetSuccess() {
                            }

                            @Override
                            public void onCreateFailure(String s) {

                            }

                            @Override
                            public void onSetFailure(String s) {
                                if (listener != null) {
                                    listener.webRTCClientDidReceiveError(ref.get(), s);
                                }
                            }
                        }, defaultConstraints);
                    }

                    @Override
                    public void onCreateFailure(String s) {

                    }

                    @Override
                    public void onSetFailure(String s) {
                        if (listener != null) {
                            listener.webRTCClientDidReceiveError(ref.get(), s);
                        }
                    }
                }, remoteSdp);
            }
        });

    }

    public void addIceCandidate(IceCandidate iceCandidate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (connection.getRemoteDescription() != null) {
                    connection.addIceCandidate(iceCandidate);
                } else {
                    remoteIceCandidates.add(iceCandidate);
                }
            }
        });
    }

    public MediaStream getLocalMediaStream(String id) {
        return localMediaStreams.get(id);
    }

    public MediaStream getRemoteMediaStream(String id) {
        return remoteMediaStreams.get(id);
    }

    public void addLocalStream(MediaStream stream) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                localMediaStreams.put(stream.getId(), stream);
                connection.addStream(stream);
            }
        });
    }

    public void addRemoteStream(MediaStream stream) {
        remoteMediaStreams.put(stream.getId(), stream);
    }

    public void makeOffer(MediaConstraints constraints) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (connection != null) {
                    connection.createOffer(new SdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {
                            handleSdpGenerated(sessionDescription);
                        }

                        @Override
                        public void onSetSuccess() {

                        }

                        @Override
                        public void onCreateFailure(String s) {
                            if (listener != null) {
                                listener.webRTCClientDidReceiveError(ref.get(), s);
                            }
                        }

                        @Override
                        public void onSetFailure(String s) {

                        }
                    }, defaultConstraints);
                }
            }
        });
    }

    public void getUserMedia(Quality quality, FancyWebRTCListener.GetUserMediaListener getUserMediaListener) {
        String streamId = randomId();
        if (!FancyWebRTC.hasPermissions(appContext)) {
            boolean videoPermission = ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            boolean audioPermission = ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            String error = "";
            if (!videoPermission && !audioPermission) {
                error = "Camera and Record Audio Permission needed.";
            } else if (!videoPermission) {
                error = "Camera Permission needed.";
            } else if (!audioPermission) {
                error = "Record Audio Permission needed.";
            }
            getUserMediaListener.webRTCClientOnGetUserMediaDidReceiveError(ref.get(), error);
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {

                MediaStream stream = factory.createLocalMediaStream(streamId);
                FancyWebRTCCapturer capturer = createCapturer(quality);
                VideoSource videoSource = factory.createVideoSource(false);
                String videoTrackId = randomId();
                VideoTrack videoTrack = factory.createVideoTrack(videoTrackId, videoSource);

                if (videoEnabled && ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    videoTrack.setEnabled(true);
                } else {
                    videoTrack.setEnabled(false);
                }

                capturer.setVideoSource(videoSource);

                capturer.startVideo(appContext);

                tracks.put(videoTrackId, new MediaData(videoSource, videoTrack, capturer));

                stream.addTrack(videoTrack);

                AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
                String audioTrackId = randomId();
                AudioTrack audioTrack = factory.createAudioTrack(audioTrackId, audioSource);
                if (audioEnabled && (ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                    audioTrack.setEnabled(true);
                } else {
                    audioTrack.setEnabled(false);
                }

                tracks.put(audioTrack.id(), new MediaData(audioSource, audioTrack, null));

                stream.addTrack(audioTrack);

                getUserMediaListener.webRTCClientOnGetUserMedia(ref.get(), stream);
            }
        });

    }

    private void handleRemoteDescriptionSet() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (IceCandidate iceCandidate : remoteIceCandidates) {
                    connection.addIceCandidate(iceCandidate);
                }
                remoteIceCandidates.clear();
            }
        });
    }

    private void handleSdpGenerated(SessionDescription sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (connection == null) return;
                if (connection.getLocalDescription() != null && (connection.getLocalDescription().type == SessionDescription.Type.ANSWER && sdp.type == SessionDescription.Type.ANSWER))
                    return;
                connection.setLocalDescription(
                        new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                Log.d("co.fitcom.fancywebrtc", "handleSdpGenerated " + "onCreateSuccess");
                            }

                            @Override
                            public void onSetSuccess() {
                                Log.d("co.fitcom.fancywebrtc", "handleSdpGenerated " + "onSetSuccess" + "type " + sdp.type);
                                if (listener != null) {
                                    listener.webRTCClientStartCallWithSdp(ref.get(), sdp);
                                }
                            }

                            @Override
                            public void onCreateFailure(String s) {

                            }

                            @Override
                            public void onSetFailure(String s) {
                                if (listener != null) {
                                    listener.webRTCClientDidReceiveError(ref.get(), s);
                                }

                            }
                        },
                        sdp
                );
            }
        });
    }

    public FancyWebRTCCapturer createCapturer(Quality quality) {
        CameraEnumerator enumerator;
        if (Camera2Enumerator.isSupported(appContext)) {
            enumerator = new Camera2Enumerator(appContext);
        } else {
            enumerator = new Camera1Enumerator(false);
        }

        String[] deviceNames = enumerator.getDeviceNames();
        FancyWebRTCCapturer videoCapturer = null;
        for (String deviceName : deviceNames) {
            List<CameraEnumerationAndroid.CaptureFormat> formatList = enumerator.getSupportedFormats(deviceName);
            CameraEnumerationAndroid.CaptureFormat selectedFormat = formatList.get(formatList.size() - 1); // Default to lowest
            for (CameraEnumerationAndroid.CaptureFormat format : enumerator.getSupportedFormats(deviceName)) {
                if (quality == Quality.LOWEST) {
                    selectedFormat = formatList.get(formatList.size() - 1);
                    break;
                } else if (quality == Quality.HIGHEST) {
                    selectedFormat = formatList.get(0);
                    break;
                } else if (quality == Quality.MAX_480P && format.height == 480) {
                    selectedFormat = format;
                    break;
                } else if (quality == Quality.MAX_720P && format.height == 720) {
                    selectedFormat = format;
                    break;
                } else if (quality == Quality.MAX_1080P && format.height == 1080) {
                    selectedFormat = format;
                    break;
                } else if (quality == Quality.MAX_2160P && format.height == 2160) {
                    selectedFormat = format;
                    break;
                }
            }

            if (enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) {
                    FancyWebRTCCapturer fancyWebRTCCapturer = new FancyWebRTCCapturer(ref.get(), capturer);
                    fancyWebRTCCapturer.setFormat(selectedFormat);
                    return fancyWebRTCCapturer;
                }
            }
        }

        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {

                List<CameraEnumerationAndroid.CaptureFormat> formatList = enumerator.getSupportedFormats(deviceName);
                CameraEnumerationAndroid.CaptureFormat selectedFormat = formatList.get(formatList.size() - 1); // Default to lowest
                for (CameraEnumerationAndroid.CaptureFormat format : enumerator.getSupportedFormats(deviceName)) {
                    if (quality == Quality.LOWEST) {
                        selectedFormat = formatList.get(formatList.size() - 1);
                        break;
                    } else if (quality == Quality.HIGHEST) {
                        selectedFormat = formatList.get(0);
                        break;
                    } else if (quality == Quality.MAX_480P && format.height == 480) {
                        selectedFormat = format;
                        break;
                    } else if (quality == Quality.MAX_720P && format.height == 720) {
                        selectedFormat = format;
                        break;
                    } else if (quality == Quality.MAX_1080P && format.height == 1080) {
                        selectedFormat = format;
                        break;
                    } else if (quality == Quality.MAX_2160P && format.height == 2160) {
                        selectedFormat = format;
                        break;
                    }
                }

                CameraVideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) {
                    FancyWebRTCCapturer fancyWebRTCCapturer = new FancyWebRTCCapturer(ref.get(), capturer);
                    fancyWebRTCCapturer.setFormat(selectedFormat);
                    return fancyWebRTCCapturer;
                }
            }
        }


        return videoCapturer;
    }

    private String randomId() {
        return UUID.randomUUID().toString();
    }

    public static boolean hasPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Context context) {
        android.support.v4.app.ActivityCompat.requestPermissions((Activity) context, WEBRTC_PERMISSIONS, WEBRTC_PERMISSIONS_REQUEST_CODE);
    }

    public enum Quality {
        MAX_480P,
        MAX_720P,
        MAX_1080P,
        MAX_2160P,
        HIGHEST,
        LOWEST
    }
}
