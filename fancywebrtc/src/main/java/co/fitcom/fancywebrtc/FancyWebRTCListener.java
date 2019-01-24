package co.fitcom.fancywebrtc;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

/**
 * Created by Osei Fortune on 8/15/18
 */
public interface FancyWebRTCListener {
    void webRTCClientDidReceiveError(FancyWebRTC client, String error);

    void webRTCClientStartCallWithSdp(FancyWebRTC client, SessionDescription sdp);

    void webRTCClientDataChannelStateChanged(FancyWebRTC client, String name, DataChannel.State state);

    void webRTCClientDataChannelMessageType(FancyWebRTC client, String name, String message, FancyWebRTC.DataChannelMessageType type);

    void webRTCClientOnRemoveStream(FancyWebRTC client, MediaStream stream);

    void webRTCClientDidReceiveStream(FancyWebRTC client, MediaStream stream);

    void webRTCClientDidGenerateIceCandidate(FancyWebRTC client, IceCandidate candidate);

    void webRTCClientOnRenegotiationNeeded(FancyWebRTC client);

    void webRTCClientOnIceCandidatesRemoved(FancyWebRTC client, IceCandidate[] iceCandidates);

    void webRTCClientOnIceConnectionChange(FancyWebRTC client, PeerConnection.IceConnectionState iceConnectionState);

    void webRTCClientOnIceConnectionReceivingChange(FancyWebRTC client, boolean change);

    void webRTCClientOnIceGatheringChange(FancyWebRTC client, PeerConnection.IceGatheringState iceGatheringState);

    void webRTCClientOnSignalingChange(FancyWebRTC client, PeerConnection.SignalingState signalingState);

    void webRTCClientOnCameraSwitchDone(FancyWebRTC client, boolean done);

    void webRTCClientOnCameraSwitchError(FancyWebRTC client, String error);

    public interface GetUserMediaListener{
        void webRTCClientOnGetUserMedia(FancyWebRTC client, MediaStream stream);
        void webRTCClientOnGetUserMediaDidReceiveError(FancyWebRTC client, String error);
    }
}
