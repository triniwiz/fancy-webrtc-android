package co.fitcom.fancywebrtc;

import android.net.sip.SipSession;

import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.IceCandidate;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by triniwiz on 1/7/19
 */
public class FancyRTCPeerConnection {
    private FancyRTCConfiguration configuration;
    private PeerConnection connection;
    private PeerConnectionFactory factory;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FancyOnConnectionStateChangeListener onConnectionStateChangeListener;
    private FancyOnTrackListener onTrackListener;
    private FancyOnRemoveTrackListener onRemoveTrackListener;
    private FancyOnIceGatheringStateChangeListener onIceGatheringStateChangeListener;
    private FancyOnNegotiationNeededListener onNegotiationNeededListener;
    private FancyOnSignalingStateChangeListener onSignalingStateChangeListener;

    public FancyRTCPeerConnection() {
        configuration = new FancyRTCConfiguration();
        executor.execute(() -> {
            PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder();
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            builder.setOptions(options);
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

            factory = builder.createPeerConnectionFactory();
            connection = factory.createPeerConnection(configuration.getConfiguration(), new PeerConnection.Observer() {
                @Override
                public void onSignalingChange(PeerConnection.SignalingState signalingState) {

                }

                @Override
                public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                    if (onConnectionStateChangeListener != null) {
                        onConnectionStateChangeListener.onChange();
                    }
                }

                @Override
                public void onIceConnectionReceivingChange(boolean b) {

                }

                @Override
                public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

                }

                @Override
                public void onIceCandidate(IceCandidate iceCandidate) {
                }

                @Override
                public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

                }

                @Override
                public void onAddStream(MediaStream mediaStream) {

                }

                @Override
                public void onRemoveStream(MediaStream mediaStream) {

                }

                @Override
                public void onDataChannel(DataChannel dataChannel) {

                }

                @Override
                public void onRenegotiationNeeded() {
                    if (onNegotiationNeededListener != null) {
                        onNegotiationNeededListener.onNegotiationNeeded();
                    }
                }

                @Override
                public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

                }

                @Override
                public void onTrack(RtpTransceiver rtpTransceiver) {

                }
            });
        });
    }

    public FancyRTCPeerConnection(FancyRTCConfiguration configuration) {
        this.configuration = configuration;
    }

    public FancyRTCSessionDescription geLocalDescription() {
        if (connection != null) {
            return FancyRTCSessionDescription.fromRTCSessionDescription(connection.getLocalDescription());
        }
        return null;
    }

    public FancyRTCSessionDescription getRemoteDescription() {
        if (connection != null) {
            return FancyRTCSessionDescription.fromRTCSessionDescription(connection.getRemoteDescription());
        }
        return null;
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

    public static interface FancyOnConnectionStateChangeListener {
        public void onChange();
    }

    public void setOnConnectionStateChange(FancyOnConnectionStateChangeListener listener) {
        onConnectionStateChangeListener = listener;
    }

    public static interface FancyOnTrackListener {
        public void onTrack();
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

    public static interface FancyOnIceGatheringStateChangeListener {
        public void onIceGatheringStateChange();
    }

    public void setOnIceGatheringStateChangeListener(FancyOnIceGatheringStateChangeListener listener) {
        onIceGatheringStateChangeListener = listener;
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
        if (connection != null) {
            executor.execute(() -> connection.addIceCandidate(candidate.getIceCandidate()));
        }
    }

    public void addTrack() {
        if (connection != null) {
            // TODO
            // executor.execute(() -> connection.addTrack(null, null));

        }
    }

    public void close() {
        if (connection != null) {
            executor.execute(() -> connection.close());
        }
    }

    public static interface SdpCreateListener {
        public void onSuccess(FancyRTCSessionDescription description);

        public void onError(String error);
    }

    public static interface SdpSetListener {
        public void onSuccess();

        public void onError(String error);
    }

    public void createDataChannel(String label, FancyRTCDataChannelInit init) {
        if (connection != null) {
            executor.execute(() -> connection.createDataChannel(label, init.getInit()));
        }
    }

    public void createAnswer(FancyMediaConstraints mediaConstraints, SdpCreateListener listener) {
        if (connection != null) {
            executor.execute(() -> connection.createAnswer(new SdpObserver() {
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
            }, mediaConstraints.getMediaConstraints()));
        }
    }

    public void createOffer(FancyMediaConstraints mediaConstraints, SdpCreateListener listener) {
        if (connection != null) {
            executor.execute(() -> connection.createOffer(new SdpObserver() {
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
            }, mediaConstraints.getMediaConstraints()));
        }
    }

    public void setLocalDescription(FancyRTCSessionDescription sdp, SdpSetListener listener) {
        if (connection != null) {
            executor.execute(() -> connection.setLocalDescription(new SdpObserver() {
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
            }, sdp.getSessionDescription()));
        }
    }

    public void setRemoteDescription(FancyRTCSessionDescription sdp, SdpSetListener listener) {
        if (connection != null) {
            executor.execute(() -> connection.setRemoteDescription(new SdpObserver() {
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
            }, sdp.getSessionDescription()));
        }
    }
}
