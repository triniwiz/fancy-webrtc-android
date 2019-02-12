package co.fitcom.fancywebrtcdemo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.fitcom.fancywebrtc.FancyRTCAudioTrack;
import co.fitcom.fancywebrtc.FancyRTCDataChannel;

import co.fitcom.fancywebrtc.FancyRTCMediaConstraints;
import co.fitcom.fancywebrtc.FancyRTCMediaDevices;
import co.fitcom.fancywebrtc.FancyRTCMediaStream;
import co.fitcom.fancywebrtc.FancyRTCMediaStreamConstraints;
import co.fitcom.fancywebrtc.FancyRTCConfiguration;
import co.fitcom.fancywebrtc.FancyRTCDataChannelInit;
import co.fitcom.fancywebrtc.FancyRTCIceCandidate;
import co.fitcom.fancywebrtc.FancyRTCPeerConnection;
import co.fitcom.fancywebrtc.FancyRTCSdpType;
import co.fitcom.fancywebrtc.FancyRTCSessionDescription;

import co.fitcom.fancywebrtc.FancyRTCVideoTrack;
import co.fitcom.fancywebrtc.FancyWebRTC;
import co.fitcom.fancywebrtc.FancyWebRTCView;
import io.socket.client.IO;
import io.socket.client.Socket;

public class Advanced extends AppCompatActivity {
    FancyWebRTCView localView;
    FancyWebRTCView remoteView;
    FancyRTCPeerConnection connection;
    Socket socket;
    String me;
    FancyRTCMediaStream localStream;
    private final Map<String, FancyRTCDataChannel> dataChannels = new HashMap<>();
    private ArrayList<FancyRTCIceCandidate> remoteIceCandidates;
    static String TAG = FancyWebRTC.Tag;
    boolean inCall = false;
    boolean isInitiator = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);
        remoteIceCandidates = new ArrayList<>();
        me = UUID.randomUUID().toString();
        localView = findViewById(R.id.localView);
        localView.setMirror(true);
        remoteView = findViewById(R.id.remoteView);
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.secure = false;
            socket = IO.socket("http://192.168.0.10:3001", options);

            socket.on("call:incoming", args -> runOnUiThread(() -> {
                JSONObject object = (JSONObject) args[0];
                try {
                    String from = object.getString("from");
                    String session = object.getString("sdp");
                    String to = object.getString("to");
                    if (to.contains(me)) {
                        if (localStream != null) {
                            List<String> list = new ArrayList<>();
                            list.add(localStream.getId());
                            for (FancyRTCVideoTrack track : localStream.getVideoTracks()) {
                                connection.addTrack(track, list);
                            }

                            for (FancyRTCAudioTrack track : localStream.getAudioTracks()) {
                                connection.addTrack(track, list);
                            }
                        }
                        FancyRTCSessionDescription sdp = new FancyRTCSessionDescription(FancyRTCSdpType.OFFER, session);
                        createAnswerForOfferReceived(sdp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }));

            socket.on("call:answer", args -> runOnUiThread(() -> {
                JSONObject object = (JSONObject) args[0];
                try {
                    String from = object.getString("from");
                    String session = object.getString("sdp");
                    String to = object.getString("to");
                    if (to.contains(me)) {
                        FancyRTCSessionDescription sdp = new FancyRTCSessionDescription(FancyRTCSdpType.OFFER, session);
                        createAnswerForOfferReceived(sdp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }));

            socket.on("call:answered", args -> runOnUiThread(() -> {
                if (!inCall) {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        String from = object.getString("from");
                        String session = object.getString("sdp");
                        String to = object.getString("to");
                        if (to.contains(me)) {
                            FancyRTCSessionDescription sdp = new FancyRTCSessionDescription(FancyRTCSdpType.ANSWER, session);
                            handleAnswerReceived(sdp);
                            //dataChannelCreate("osei");
                            //dataChannelSend("osei", "Test", FancyWebRTC.DataChannelMessageType.TEXT);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }));

            socket.on("call:iceCandidate", args -> {
                JSONObject object = (JSONObject) args[0];

                try {
                    String from = object.getString("from");
                    String session = object.getString("sdp");
                    String to = object.getString("to");
                    String sdpMid = object.getString("sdpMid");
                    int sdpMLineIndex = object.getInt("sdpMLineIndex");
                    String serverUrl = object.getString("serverUrl");

                    if (to.contains(me)) {
                        FancyRTCIceCandidate candidate = new FancyRTCIceCandidate(session, sdpMid, sdpMLineIndex);
                        connection.addIceCandidate(candidate);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });

            socket.on(Socket.EVENT_CONNECT, args -> {
                JSONObject object = new JSONObject();
                try {
                    object.put("id", me);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("init", object);
            });

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        /*
        List<FancyRTCIceServer> servers = new ArrayList<>();
        FancyRTCIceServer turnServerOne = new FancyRTCIceServer("turn:192.158.29.39:3478?transport=udp", "JZEOEt2V3Qb0y27GRntt2u2PAYA=", "28224511:1379330808");
        FancyRTCIceServer turnServerTwo = new FancyRTCIceServer("turn:192.158.29.39:3478?transport=tcp", "JZEOEt2V3Qb0y27GRntt2u2PAYA=", "28224511:1379330808");
        FancyRTCIceServer turnServerThree = new FancyRTCIceServer("turn:numb.viagenie.ca", "muazkh", "webrtc@live.com");
        servers.add(turnServerOne);
        servers.add(turnServerTwo);
        servers.add(turnServerThree);
        */
        FancyRTCConfiguration configuration = new FancyRTCConfiguration();
        connection = new FancyRTCPeerConnection(this, configuration);
        connection.setOnTrackListener(event -> remoteView.setSrcObject(event.getStreams().get(0)));
        connection.setOnIceCandidateListener(candidate -> {
            JSONObject object = new JSONObject();
            try {
                object.put("from", me);
                object.put("sdp", candidate.getSdp());
                object.put("sdpMid", candidate.getSdpMid());
                object.put("sdpMLineIndex", candidate.getSdpMLineIndex());
                object.put("serverUrl", candidate.getServerUrl());
                socket.emit("iceCandidate", object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        if (FancyWebRTC.hasPermissions(this)) {
            setUpUserMedia();
        } else {
            FancyWebRTC.requestPermissions(this);
        }
    }

    public void setUpUserMedia() {
        Map<String, Object> video = new HashMap<>();
        video.put("facingMode", "user");
        video.put("width", 960);
        video.put("height", 720);
        FancyRTCMediaStreamConstraints constraints = new FancyRTCMediaStreamConstraints(true, video);
        FancyRTCMediaDevices.getUserMedia(this, constraints, new FancyRTCMediaDevices.GetUserMediaListener() {
            @Override
            public void onSuccess(FancyRTCMediaStream mediaStream) {
                localStream = mediaStream;
                localView.setSrcObject(mediaStream);
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public void makeCall(View view) {
        if (connection != null) {
            isInitiator = true;
            if (localStream != null) {
                List<String> list = new ArrayList<>();
                list.add(localStream.getId());
                for (FancyRTCVideoTrack track : localStream.getVideoTracks()) {
                    connection.addTrack(track, list);
                }
                for (FancyRTCAudioTrack track : localStream.getAudioTracks()) {
                    connection.addTrack(track, list);
                }
            }
            connection.createOffer(new FancyRTCMediaConstraints(), new FancyRTCPeerConnection.SdpCreateListener() {
                @Override
                public void onSuccess(FancyRTCSessionDescription description) {
                    setInitiatorLocalSdp(description);
                }

                @Override
                public void onError(String error) {
                    didReceiveError(error);
                }
            });
        }
    }

    public void answerCall(View view) {
    }

    public void endCall(View view) {
        connection.close();
        connection.dispose();
    }

    void handleRemoteDescriptionSet() {
        for (FancyRTCIceCandidate iceCandidate : remoteIceCandidates) {
            connection.addIceCandidate(iceCandidate);
        }
        remoteIceCandidates.clear();
    }

    void sendNonInitiatorSdp(FancyRTCSessionDescription sdp) {
        JSONObject object = new JSONObject();
        try {
            object.put("from", me);
            object.put("sdp", sdp.getDescription());
            /* handleAnswerReceived(sdp); */ // ???
            socket.emit("answered", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void sendInitiatorSdp(FancyRTCSessionDescription sdp) {
        JSONObject object = new JSONObject();
        try {
            object.put("from", me);
            object.put("sdp", sdp.getDescription());
            socket.emit("call", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void createAnswerForOfferReceived(final FancyRTCSessionDescription remoteSdp) {
        if (connection == null || remoteSdp == null) return;
       /* if (connection.getRemoteDescription() != null && (connection.getRemoteDescription().getType() == FancyRTCSdpType.ANSWER && remoteSdp.getType() == FancyRTCSdpType.ANSWER))
            return;
        */
        connection.setRemoteDescription(remoteSdp, new FancyRTCPeerConnection.SdpSetListener() {
            @Override
            public void onSuccess() {
                handleRemoteDescriptionSet();
                connection.createAnswer(new FancyRTCMediaConstraints(), new FancyRTCPeerConnection.SdpCreateListener() {
                    @Override
                    public void onSuccess(FancyRTCSessionDescription description) {
                        setNonInitiatorLocalSdp(description);
                    }

                    @Override
                    public void onError(String error) {
                        didReceiveError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                didReceiveError(error);
            }
        });
    }

    void handleAnswerReceived(final FancyRTCSessionDescription sdp) {
        if (connection == null || sdp == null || inCall) return;
        FancyRTCSessionDescription newSdp = new FancyRTCSessionDescription(FancyRTCSdpType.ANSWER, sdp.getDescription());
        connection.setRemoteDescription(newSdp, new FancyRTCPeerConnection.SdpSetListener() {
            @Override
            public void onSuccess() {
                inCall = true;
            }

            @Override
            public void onError(String error) {
                didReceiveError(error);
            }
        });
    }

    void setNonInitiatorLocalSdp(final FancyRTCSessionDescription sdp) {
        if (connection == null) return;
        if (connection.getLocalDescription() != null && (connection.getLocalDescription().getType() == FancyRTCSdpType.ANSWER && sdp.getType() == FancyRTCSdpType.ANSWER))
            return;
        connection.setLocalDescription(sdp, new FancyRTCPeerConnection.SdpSetListener() {
            @Override
            public void onSuccess() {
                sendNonInitiatorSdp(sdp);
            }

            @Override
            public void onError(String error) {
                didReceiveError(error);
            }
        });
    }

    void setInitiatorLocalSdp(final FancyRTCSessionDescription sdp) {
        if (connection == null) return;
        if (connection.getLocalDescription() != null && (connection.getLocalDescription().getType() == FancyRTCSdpType.ANSWER && sdp.getType() == FancyRTCSdpType.ANSWER))
            return;
        connection.setLocalDescription(sdp, new FancyRTCPeerConnection.SdpSetListener() {
            @Override
            public void onSuccess() {
                sendInitiatorSdp(sdp);
            }

            @Override
            public void onError(String error) {
                didReceiveError(error);
            }
        });
    }

    public void dataChannelCreate(final String name) {
        final FancyRTCDataChannelInit dataChannelInit = new FancyRTCDataChannelInit();
        FancyRTCDataChannel channel = connection.createDataChannel(name, dataChannelInit);
        dataChannels.put(name, channel);
        // registerDataChannelObserver(name);
    }

    void didReceiveError(String error) {
        Log.e(TAG, error);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FancyWebRTC.WEBRTC_PERMISSIONS_REQUEST_CODE) {
            if (FancyWebRTC.hasPermissions(this)) {
                setUpUserMedia();
            }
        }
    }
}
