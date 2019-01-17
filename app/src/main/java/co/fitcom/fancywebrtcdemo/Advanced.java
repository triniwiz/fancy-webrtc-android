package co.fitcom.fancywebrtcdemo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.PeerConnectionFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import co.fitcom.fancywebrtc.FancyVideoTrack;
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
    private static String TAG = "co.fitcom.fancywebrtc.advanced";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);
        PeerConnectionFactory.InitializationOptions.Builder builder = PeerConnectionFactory.InitializationOptions.builder(this);
        builder.setEnableInternalTracer(true);
        PeerConnectionFactory.initialize(builder.createInitializationOptions());
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
                    Log.d(TAG, "call:incoming" + " to: " + to + " from: " + from);
                    if (to.contains(me)) {
                        if (localStream != null) {
                            for (FancyVideoTrack track : localStream.getVideoTracks()) {
                                connection.addTrack(track);
                            }
                            for (FancyRTCAudioTrack track : localStream.getAudioTracks()) {
                                connection.addTrack(track);
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
                    Log.d(TAG, "call:answer");
                    Log.d(TAG, "me : " + me + " from: " + from + " to: " + to);
                    if (to.contains(me)) {
                        Log.d(TAG, me);
                        FancyRTCSessionDescription sdp = new FancyRTCSessionDescription(FancyRTCSdpType.OFFER, session);
                        createAnswerForOfferReceived(sdp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }));

            socket.on("call:answered", args -> runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        String from = object.getString("from");
                        String session = object.getString("sdp");
                        String to = object.getString("to");
                        if (to.contains(me)) {
                            Log.d(TAG, "call:answered");
                            FancyRTCSessionDescription sdp = new FancyRTCSessionDescription(FancyRTCSdpType.ANSWER, session);
                            handleAnswerReceived(sdp);
                            dataChannelCreate("osei");
                            //dataChannelSend("osei", "Test", FancyWebRTC.DataChannelMessageType.TEXT);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }));

            socket.on("call:iceCandidate", args -> {
                Log.d(TAG, "call:iceCandidate");
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

        FancyRTCConfiguration configuration = new FancyRTCConfiguration();
        connection = new FancyRTCPeerConnection(configuration);
        connection.setOnTrackListener(event -> {
            connection.addTrack(event.getMediaTrack());

        });
        connection.setOnIceCandidateListener(candidate -> {
            JSONObject object = new JSONObject();
            try {
                object.put("from", me);
                object.put("sdp", candidate.getSdp());
                object.put("sdpMid", candidate.getSdpMid());
                object.put("sdpMLineIndex", candidate.getSdpMLineIndex());
                object.put("serverUrl", candidate.getServerUrl());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("iceCandidate", object);
        });
        if (FancyWebRTC.hasPermissions(this)) {
            setUpUserMedia();
        } else {
            FancyWebRTC.requestPermissions(this);
        }
    }

    public void setUpUserMedia() {
        FancyRTCMediaStreamConstraints constraints = new FancyRTCMediaStreamConstraints(true, true);
        FancyRTCMediaDevices.getUserMedia(this, constraints, new FancyRTCMediaDevices.GetUserMediaListener() {
            @Override
            public void onSuccess(FancyRTCMediaStream mediaStream) {
                localView.setSrcObject(mediaStream);
                localStream = mediaStream;
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public void makeCall(View view) {
        Log.d(TAG, "makeCall " + connection);
        if (connection != null) {
            if (localStream != null) {
                for (FancyVideoTrack track : localStream.getVideoTracks()) {
                    connection.addTrack(track);
                }
                for (FancyRTCAudioTrack track : localStream.getAudioTracks()) {
                    connection.addTrack(track);
                }
            }
            connection.createOffer(new FancyRTCMediaConstraints(), new FancyRTCPeerConnection.SdpCreateListener() {
                @Override
                public void onSuccess(FancyRTCSessionDescription description) {
                    handleSdpGenerated(description);
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

    void startCall(FancyRTCSessionDescription sdp) {
        Log.d(TAG, "startCall" + " type: " + sdp.getType());
        if (sdp.getType() == FancyRTCSdpType.ANSWER) {
            JSONObject object = new JSONObject();
            try {
                object.put("from", me);
                object.put("sdp", sdp.getDescription());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("answered", object);
            handleAnswerReceived(sdp);
        } else if (sdp.getType() == FancyRTCSdpType.OFFER) {
            JSONObject object = new JSONObject();
            try {
                object.put("from", me);
                object.put("sdp", sdp.getDescription());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("call", object);
        }
    }

    void createAnswerForOfferReceived(final FancyRTCSessionDescription remoteSdp) {
        if (connection == null || remoteSdp == null) return;
        if (connection.getRemoteDescription() != null && (connection.getRemoteDescription().getType() == FancyRTCSdpType.ANSWER && remoteSdp.getType() == FancyRTCSdpType.ANSWER))
            return;
        connection.setRemoteDescription(remoteSdp, new FancyRTCPeerConnection.SdpSetListener() {
            @Override
            public void onSuccess() {
                handleRemoteDescriptionSet();
                connection.createAnswer(new FancyRTCMediaConstraints(), new FancyRTCPeerConnection.SdpCreateListener() {
                    @Override
                    public void onSuccess(FancyRTCSessionDescription description) {
                        handleSdpGenerated(description);
                        startCall(description);
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    void handleAnswerReceived(final FancyRTCSessionDescription sdp) {
        if (connection == null || sdp == null) return;
        FancyRTCSessionDescription newSdp = new FancyRTCSessionDescription(FancyRTCSdpType.ANSWER, sdp.getDescription());
        connection.setRemoteDescription(newSdp, new FancyRTCPeerConnection.SdpSetListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(String error) {
                didReceiveError(error);
            }
        });
    }

    void handleSdpGenerated(final FancyRTCSessionDescription sdp) {
        if (connection == null) return;
        if (connection.getLocalDescription() != null && (connection.getLocalDescription().getType() == FancyRTCSdpType.ANSWER && sdp.getType() == FancyRTCSdpType.ANSWER))
            return;
        connection.setLocalDescription(sdp, new FancyRTCPeerConnection.SdpSetListener() {
            @Override
            public void onSuccess() {
                startCall(sdp);
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
