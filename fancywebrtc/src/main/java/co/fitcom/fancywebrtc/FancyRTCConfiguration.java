package co.fitcom.fancywebrtc;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by triniwiz on 1/7/19
 */
public class FancyRTCConfiguration {
    private FancyRTCBundlePolicy bundlePolicy;
    private int iceCandidatePoolSize = 0;
    private List<FancyRTCIceServer> iceServers;
    private FancyRTCIceTransportPolicy iceTransportPolicy;
    private String peerIdentity;
    private FancyRTCRtcpMuxPolicy rtcpMuxPolicy;
    private PeerConnection.RTCConfiguration configuration;

    public FancyRTCConfiguration() {
        iceServers = new ArrayList<>();
        String[] defaultIceServers = new String[]{
                "stun:stun.l.google.com:19302",
                "stun:stun1.l.google.com:19302",
                "stun:stun2.l.google.com:19302",
                "stun:stun3.l.google.com:19302",
                "stun:stun4.l.google.com:19302"
        };
        for (String server : defaultIceServers) {
            iceServers.add(new FancyRTCIceServer(server));
        }
        List<PeerConnection.IceServer> list = new ArrayList<>();
        for (FancyRTCIceServer server : iceServers) {
            list.add(server.toWebRtc());
        }
        configuration = new PeerConnection.RTCConfiguration(list);
    }

    private void initProperties() {
        switch (configuration.bundlePolicy) {
            case BALANCED:
                bundlePolicy = FancyRTCBundlePolicy.BALANCED;
                break;
            case MAXBUNDLE:
                bundlePolicy = FancyRTCBundlePolicy.MAX_BUNDLE;
                break;
            case MAXCOMPAT:
                bundlePolicy = FancyRTCBundlePolicy.MAX_COMPAT;
                break;
        }

        iceCandidatePoolSize = configuration.iceCandidatePoolSize;
        switch (configuration.iceTransportsType) {
            case ALL:
                iceTransportPolicy = FancyRTCIceTransportPolicy.ALL;
                break;
            case NONE:

                break;
            case NOHOST:

                break;
            case RELAY:
                iceTransportPolicy = FancyRTCIceTransportPolicy.RELAY;
                break;
        }

        switch (configuration.rtcpMuxPolicy) {
            case REQUIRE:
                rtcpMuxPolicy = FancyRTCRtcpMuxPolicy.REQUIRE;
                break;
            case NEGOTIATE:
                rtcpMuxPolicy = FancyRTCRtcpMuxPolicy.NEGOTIATE;
                break;
        }
    }

    public FancyRTCConfiguration(List<FancyRTCIceServer> iceServers) {
        this.iceServers = iceServers;
        List<PeerConnection.IceServer> list = new ArrayList<>();
        for (FancyRTCIceServer server : iceServers) {
            list.add(server.toWebRtc());
        }
        configuration = new PeerConnection.RTCConfiguration(list);
    }

    public void setBundlePolicy(FancyRTCBundlePolicy bundlePolicy) {
        this.bundlePolicy = bundlePolicy;
        switch (bundlePolicy) {
            case BALANCED:
                configuration.bundlePolicy = PeerConnection.BundlePolicy.BALANCED;
                break;
            case MAX_BUNDLE:
                configuration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
                break;
            case MAX_COMPAT:
                configuration.bundlePolicy = PeerConnection.BundlePolicy.MAXCOMPAT;
                break;
        }
    }

    public FancyRTCBundlePolicy getBundlePolicy() {
        return bundlePolicy;
    }

    public void setIceCandidatePoolSize(int iceCandidatePoolSize) {
        this.iceCandidatePoolSize = iceCandidatePoolSize;
        configuration.iceCandidatePoolSize = iceCandidatePoolSize;
    }

    public int getIceCandidatePoolSize() {
        return iceCandidatePoolSize;
    }

    public FancyRTCIceTransportPolicy getIceTransportPolicy() {
        return iceTransportPolicy;
    }

    public void setIceTransportPolicy(FancyRTCIceTransportPolicy iceTransportPolicy) {
        this.iceTransportPolicy = iceTransportPolicy;
        switch (iceTransportPolicy) {
            case RELAY:
                configuration.iceTransportsType = PeerConnection.IceTransportsType.RELAY;
                break;
            case ALL:
                configuration.iceTransportsType = PeerConnection.IceTransportsType.ALL;
                break;
            default:
                break;
        }
    }

    public FancyRTCRtcpMuxPolicy getRtcpMuxPolicy() {
        return rtcpMuxPolicy;
    }

    public void setRtcpMuxPolicy(FancyRTCRtcpMuxPolicy rtcpMuxPolicy) {
        this.rtcpMuxPolicy = rtcpMuxPolicy;
        switch (rtcpMuxPolicy){
            case NEGOTIATE:
                configuration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.NEGOTIATE;
                break;
            case REQUIRE:
                configuration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
                break;
        }
    }

    public void setIceServers(List<FancyRTCIceServer> iceServers) {
        this.iceServers = iceServers;
        List<PeerConnection.IceServer> list = new ArrayList<>();
        for (FancyRTCIceServer server : iceServers) {
            list.add(server.toWebRtc());
        }
        configuration.iceServers = list;
    }

    public List<FancyRTCIceServer> getIceServers() {
        return iceServers;
    }

    public void setPeerIdentity(String peerIdentity) {
        this.peerIdentity = peerIdentity;
    }

    public String getPeerIdentity() {
        return peerIdentity;
    }

    PeerConnection.RTCConfiguration getConfiguration() {
        return configuration;
    }
}
