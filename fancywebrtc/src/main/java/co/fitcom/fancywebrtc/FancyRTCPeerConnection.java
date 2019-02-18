package co.fitcom.fancywebrtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;
import org.webrtc.audio.LegacyAudioDeviceModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;

/**
 * Created by triniwiz on 1/7/19
 */
public class FancyRTCPeerConnection {
    private FancyRTCConfiguration configuration;
    private PeerConnection connection;
    static PeerConnectionFactory factory;
    private Context context;
    static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FancyOnConnectionStateChangeListener onConnectionStateChangeListener;
    private FancyOnTrackListener onTrackListener;
    private FancyOnRemoveTrackListener onRemoveTrackListener;
    private FancyOnIceGatheringStateChangeListener onIceGatheringStateChangeListener;
    private FancyOnNegotiationNeededListener onNegotiationNeededListener;
    private FancyOnSignalingStateChangeListener onSignalingStateChangeListener;
    private FancyOnIceCandidateListener onIceCandidateListener;
    private FancyOnDataChannelListener onDataChannelListener;
    private FancyOnAddStreamListener onAddStreamListener;
    private FancyOnRemoveStreamListener onRemoveStreamListener;
    private FancyOnIceConnectionChangeListener onIceConnectionChangeListener;
    private Map<String, RtpReceiver> remoteReceivers = new HashMap<>();
    private Map<String, RtpTransceiver> remoteTransceivers = new HashMap<>();

    static {
        initFactory();
    }

    private static void initFactory() {
        executor.execute(() -> {
            PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder();
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            builder.setOptions(options);
            VideoEncoderFactory encoderFactory;
            VideoDecoderFactory decoderFactory;
            if (FancyWebRTCEglUtils.getRootEglBaseContext() != null) {
                encoderFactory = new DefaultVideoEncoderFactory(FancyWebRTCEglUtils.getRootEglBaseContext(), true, false);
                decoderFactory = new DefaultVideoDecoderFactory(FancyWebRTCEglUtils.getRootEglBaseContext());
            } else {
                encoderFactory = new SoftwareVideoEncoderFactory();
                decoderFactory = new SoftwareVideoDecoderFactory();
            }
            builder.setVideoDecoderFactory(decoderFactory);
            builder.setVideoEncoderFactory(encoderFactory);
            factory = builder.createPeerConnectionFactory();
        });
    }

    private void init() {
        executor.execute(() -> connection = factory.createPeerConnection(configuration.getConfiguration(), new PeerConnection.Observer() {
            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                if (onConnectionStateChangeListener != null) {
                    onConnectionStateChangeListener.onChange();
                }
            }

            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                if (onSignalingStateChangeListener != null) {
                    onSignalingStateChangeListener.onSignalingStateChange();
                }
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                if (onIceConnectionChangeListener != null) {
                    onIceConnectionChangeListener.onChange();
                }
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                if (onIceGatheringStateChangeListener != null) {
                    onIceGatheringStateChangeListener.onIceGatheringStateChange();
                }
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                if (onIceCandidateListener != null) {
                    onIceCandidateListener.onIceCandidate(new FancyRTCIceCandidate(iceCandidate));
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                if (onAddStreamListener != null) {
                    onAddStreamListener.onAddStream(new FancyRTCMediaStream(mediaStream));
                }
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                if (onRemoveStreamListener != null) {
                    onRemoveStreamListener.onRemoveStream(new FancyRTCMediaStream(mediaStream));
                }
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                if (onDataChannelListener != null) {
                    onDataChannelListener.onDataChannel(new FancyRTCDataChannelEvent(new FancyRTCDataChannel(dataChannel)));
                }
            }

            @Override
            public void onRenegotiationNeeded() {
                if (onNegotiationNeededListener != null) {
                    onNegotiationNeededListener.onNegotiationNeeded();
                }
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                remoteReceivers.put(FancyUtils.getUUID(), rtpReceiver);
                if (onTrackListener != null) {
                    if (configuration.getSdpSemantics() == FancyRTCSdpSemantics.PLAN_B) {
                        List<FancyRTCMediaStream> list = new ArrayList<>();
                        for (MediaStream stream : mediaStreams) {
                            list.add(new FancyRTCMediaStream(stream));
                        }
                        FancyRTCTrackEvent event = new FancyRTCTrackEvent(new FancyRTCRtpReceiver(rtpReceiver), list, new FancyRTCMediaStreamTrack(rtpReceiver.track()), null);
                        onTrackListener.onTrack(event);
                    }
                }
            }

            @Override
            public void onTrack(RtpTransceiver rtpTransceiver) {
                remoteTransceivers.put(FancyUtils.getUUID(), rtpTransceiver);
                if (onTrackListener != null) {
                    FancyRTCTrackEvent event = new FancyRTCTrackEvent(new FancyRTCRtpReceiver(rtpTransceiver.getReceiver()), null, new FancyRTCMediaStreamTrack(rtpTransceiver.getReceiver().track()), new FancyRTCRtpTransceiver(rtpTransceiver));
                    onTrackListener.onTrack(event);
                }
            }
        }));
    }

    public FancyRTCPeerConnection(Context context) {
        this.context = context;
        configuration = new FancyRTCConfiguration();
        init();
    }

    public FancyRTCPeerConnection(Context context, FancyRTCConfiguration configuration) {
        this.context = context;
        this.configuration = configuration;
        init();
    }

    public FancyRTCSessionDescription getLocalDescription() {
        if (connection == null) return null;
        SessionDescription sdp = connection.getLocalDescription();
        if (sdp == null) return null;
        return FancyRTCSessionDescription.fromRTCSessionDescription(sdp);
    }

    public FancyRTCSessionDescription getRemoteDescription() {
        if (connection == null) return null;
        SessionDescription sdp = connection.getRemoteDescription();
        if (sdp == null) return null;
        return FancyRTCSessionDescription.fromRTCSessionDescription(sdp);
    }

    public FancyRTCPeerConnectionState getConnectionState() {
        if (connection != null) {
            switch (connection.iceConnectionState()) {
                case NEW:
                    return FancyRTCPeerConnectionState.NEW;
                case CLOSED:
                    return FancyRTCPeerConnectionState.CLOSED;
                case FAILED:
                    return FancyRTCPeerConnectionState.FAILED;
                case CHECKING:

                case COMPLETED:

                case CONNECTED:
                    return FancyRTCPeerConnectionState.CONNECTED;
                case DISCONNECTED:
                    return FancyRTCPeerConnectionState.DISCONNECTED;
            }
        }
        return FancyRTCPeerConnectionState.NEW;
    }

    public static interface FancyOnIceConnectionChangeListener {
        public void onChange();
    }

    public void setOnIceConnectionChange(FancyOnIceConnectionChangeListener listener) {
        onIceConnectionChangeListener = listener;
    }

    public static interface FancyOnConnectionStateChangeListener {
        public void onChange();
    }

    public void setOnConnectionStateChange(FancyOnConnectionStateChangeListener listener) {
        onConnectionStateChangeListener = listener;
    }

    public static interface FancyOnTrackListener {
        public void onTrack(FancyRTCTrackEvent event);
    }

    public void setOnTrackListener(FancyOnTrackListener listener) {
        onTrackListener = listener;
    }

    public static interface FancyOnRemoveTrackListener {
        public void onRemoveTrack();
    }

    public void setOnRemoveTrackListener(FancyOnRemoveTrackListener listener) {
        onRemoveTrackListener = listener;
    }

    public static interface FancyOnRemoveStreamListener {
        public void onRemoveStream(FancyRTCMediaStream stream);
    }

    public void setOnRemoveStreamListener(FancyOnRemoveStreamListener listener) {
        onRemoveStreamListener = listener;
    }

    public static interface FancyOnIceGatheringStateChangeListener {
        public void onIceGatheringStateChange();
    }

    public void setOnIceGatheringStateChangeListener(FancyOnIceGatheringStateChangeListener listener) {
        onIceGatheringStateChangeListener = listener;
    }

    public static interface FancyOnAddStreamListener {
        public void onAddStream(FancyRTCMediaStream stream);
    }

    public void setOnAddStreamListener(FancyOnAddStreamListener listener) {
        onAddStreamListener = listener;
    }

    public static interface FancyOnNegotiationNeededListener {
        public void onNegotiationNeeded();
    }

    public void setOnNegotiationNeededListener(FancyOnNegotiationNeededListener listener) {
        onNegotiationNeededListener = listener;
    }

    public static interface FancyOnSignalingStateChangeListener {
        public void onSignalingStateChange();
    }

    public void setOnSignalingStateChangeListener(FancyOnSignalingStateChangeListener listener) {
        onSignalingStateChangeListener = listener;
    }

    public static interface FancyOnIceCandidateListener {
        public void onIceCandidate(FancyRTCIceCandidate candidate);
    }

    public void setOnIceCandidateListener(FancyOnIceCandidateListener listener) {
        onIceCandidateListener = listener;
    }

    public static interface FancyOnDataChannelListener {
        public void onDataChannel(FancyRTCDataChannelEvent event);
    }

    public void setOnDataChannelListener(FancyOnDataChannelListener listener) {
        onDataChannelListener = listener;
    }

    public List<FancyRTCIceServer> getDefaultIceServers() {
        List<FancyRTCIceServer> list = new ArrayList<>();
        String[] defaultIceServers = new String[]{
                "stun:stun.l.google.com:19302",
                "stun:stun1.l.google.com:19302",
                "stun:stun2.l.google.com:19302",
                "stun:stun3.l.google.com:19302",
                "stun:stun4.l.google.com:19302"
        };
        for (String server : defaultIceServers) {
            list.add(new FancyRTCIceServer(server));
        }
        return list;
    }

    public void addIceCandidate(FancyRTCIceCandidate candidate) {
        executor.execute(() -> {
            if (connection != null) {
                connection.addIceCandidate(candidate.getIceCandidate());
            }
        });
    }

    public void addTrack(FancyRTCMediaStreamTrack track, List<String> streamIds) {
        executor.execute(() -> {
            if (connection != null) {
                connection.addTrack(track.getMediaStreamTrack(), streamIds);
            }
        });
    }

    public void close() {
        executor.execute(() -> {
            if (connection != null) {
                connection.close();
            }
        });
    }

    public static interface SdpCreateListener {
        public void onSuccess(FancyRTCSessionDescription description);

        public void onError(String error);
    }

    public static interface SdpSetListener {
        public void onSuccess();

        public void onError(String error);
    }

    public FancyRTCDataChannel createDataChannel(String label, FancyRTCDataChannelInit init) {
        if (connection != null) {
            DataChannel channel = connection.createDataChannel(label, init.getInit());
            return new FancyRTCDataChannel(channel);
        }
        return null;
    }

    public void dispose() {
        executor.execute(() -> {
            if (connection != null) {
                connection.dispose();
            }
        });
    }

    public void createAnswer(FancyRTCMediaConstraints mediaConstraints, SdpCreateListener listener) {
        executor.execute(() -> {
            if (connection != null) {

                if (!mediaConstraints.mandatory.contains(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveVideo", "true")) || !mediaConstraints.mandatory.contains(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveVideo", "false"))) {
                    mediaConstraints.mandatory.add(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveVideo", "true"));
                }

                if (!mediaConstraints.mandatory.contains(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveAudio", "true")) || !mediaConstraints.mandatory.contains(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveAudio", "false"))) {
                    mediaConstraints.mandatory.add(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveAudio", "true"));
                }

                connection.createAnswer(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        listener.onSuccess(FancyRTCSessionDescription.fromRTCSessionDescription(sessionDescription));
                    }

                    @Override
                    public void onSetSuccess() {

                    }

                    @Override
                    public void onCreateFailure(String s) {
                        listener.onError(s);
                    }

                    @Override
                    public void onSetFailure(String s) {

                    }
                }, mediaConstraints.getMediaConstraints());
            }
        });
    }

    public void createOffer(FancyRTCMediaConstraints mediaConstraints, SdpCreateListener listener) {
        executor.execute(() -> {
            if (connection != null) {

                if (!mediaConstraints.mandatory.contains(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveVideo", "true")) || !mediaConstraints.mandatory.contains(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveVideo", "false"))) {
                    mediaConstraints.mandatory.add(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveVideo", "true"));
                }

                if (!mediaConstraints.mandatory.contains(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveAudio", "true")) || !mediaConstraints.mandatory.contains(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveAudio", "false"))) {
                    mediaConstraints.mandatory.add(new FancyRTCMediaConstraints.KeyValue("OfferToReceiveAudio", "true"));
                }

                connection.createOffer(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        listener.onSuccess(FancyRTCSessionDescription.fromRTCSessionDescription(sessionDescription));
                    }

                    @Override
                    public void onSetSuccess() {

                    }

                    @Override
                    public void onCreateFailure(String s) {
                        listener.onError(s);
                    }

                    @Override
                    public void onSetFailure(String s) {
                    }
                }, mediaConstraints.getMediaConstraints());
            }
        });
    }

    public void setLocalDescription(FancyRTCSessionDescription sdp, SdpSetListener listener) {
        executor.execute(() -> {
            if (connection != null) {
                connection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {

                    }

                    @Override
                    public void onSetSuccess() {
                        listener.onSuccess();
                    }

                    @Override
                    public void onCreateFailure(String s) {

                    }

                    @Override
                    public void onSetFailure(String s) {
                        listener.onError(s);
                    }
                }, sdp.getSessionDescription());
            }
        });
    }

    public void setRemoteDescription(FancyRTCSessionDescription sdp, SdpSetListener listener) {
        executor.execute(() -> {
            if (connection != null) {
                connection.setRemoteDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {

                    }

                    @Override
                    public void onSetSuccess() {
                        listener.onSuccess();
                    }

                    @Override
                    public void onCreateFailure(String s) {

                    }

                    @Override
                    public void onSetFailure(String s) {
                        listener.onError(s);
                    }
                }, sdp.getSessionDescription());
            }
        });
    }

    public PeerConnection getConnection() {
        return connection;
    }


    AudioDeviceModule createLegacyAudioDevice() {
        return new LegacyAudioDeviceModule();
    }

    AudioDeviceModule createJavaAudioDevice() {
        return JavaAudioDeviceModule.builder(context)
                .setSamplesReadyCallback(new JavaAudioDeviceModule.SamplesReadyCallback() {
                    @Override
                    public void onWebRtcAudioRecordSamplesReady(JavaAudioDeviceModule.AudioSamples audioSamples) {

                    }
                })
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .setAudioRecordErrorCallback(new JavaAudioDeviceModule.AudioRecordErrorCallback() {
                    @Override
                    public void onWebRtcAudioRecordInitError(String s) {

                    }

                    @Override
                    public void onWebRtcAudioRecordStartError(JavaAudioDeviceModule.AudioRecordStartErrorCode audioRecordStartErrorCode, String s) {

                    }

                    @Override
                    public void onWebRtcAudioRecordError(String s) {

                    }
                })
                .setAudioTrackErrorCallback(new JavaAudioDeviceModule.AudioTrackErrorCallback() {
                    @Override
                    public void onWebRtcAudioTrackInitError(String s) {

                    }

                    @Override
                    public void onWebRtcAudioTrackStartError(JavaAudioDeviceModule.AudioTrackStartErrorCode audioTrackStartErrorCode, String s) {

                    }

                    @Override
                    public void onWebRtcAudioTrackError(String s) {

                    }
                })
                .createAudioDeviceModule();
    }

}
