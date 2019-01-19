package co.fitcom.fancywebrtcdemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.UUID;

import co.fitcom.fancywebrtc.FancyWebRTC;
import co.fitcom.fancywebrtc.FancyWebRTCCapturer;
import co.fitcom.fancywebrtc.FancyWebRTCListener;
import co.fitcom.fancywebrtc.FancyWebRTCView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    FancyWebRTCView localView;
    FancyWebRTCView remoteView;
    FancyWebRTC webRTC;
    FancyWebRTCCapturer capturer;
    Socket socket;
    String me;
    VideoTrack remoteTrack;
    MediaStream localStream;
    boolean mirror = true;
    private static String TAG = "co.fitcom.fancywebrtc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FancyWebRTC.init(this);
        me = UUID.randomUUID().toString();
        localView = findViewById(R.id.localView);
        localView.setMirror(true);
        remoteView = findViewById(R.id.remoteView);
/*
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.secure = false;
            socket = IO.socket("http://192.168.0.10:3001", options);

            socket.on("call:incoming", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject object = (JSONObject) args[0];
                            try {
                                String from = object.getString("from");
                                String session = object.getString("sdp");
                                String to = object.getString("to");
                                Log.d(TAG, "call:incoming" + " to: " + to + " from: " + from);
                                if (to.contains(me)) {
                                    webRTC.addLocalStream(localStream);
                                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER, session);
                                    webRTC.createAnswerForOfferReceived(sdp, new MediaConstraints());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            socket.on("call:answer", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject object = (JSONObject) args[0];
                            try {
                                String from = object.getString("from");
                                String session = object.getString("sdp");
                                String to = object.getString("to");
                                Log.d(TAG, "call:answer");
                                Log.d(TAG, "me : " + me + " from: " + from + " to: " + to);
                                if (to.contains(me)) {
                                    Log.d(TAG, me);
                                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER, session);
                                    webRTC.createAnswerForOfferReceived(sdp, new MediaConstraints());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            socket.on("call:answered", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject object = (JSONObject) args[0];
                            try {
                                String from = object.getString("from");
                                String session = object.getString("sdp");
                                String to = object.getString("to");
                                if (to.contains(me)) {
                                    Log.d(TAG, "call:answered");
                                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, session);
                                    webRTC.handleAnswerReceived(sdp);
                                    webRTC.dataChannelCreate("osei");
                                    webRTC.dataChannelSend("osei", "Test", FancyWebRTC.DataChannelMessageType.TEXT);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            socket.on("call:iceCandidate", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
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
                            IceCandidate candidate = new IceCandidate(sdpMid, sdpMLineIndex, session);
                            webRTC.addIceCandidate(candidate);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("id", me);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("init", object);
                }
            });

            //socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        webRTC = new FancyWebRTC(this, true, true);
        webRTC.setListener(new FancyWebRTCListener() {
            @Override
            public void webRTCClientDidReceiveError(FancyWebRTC client, String error) {
                Log.e(TAG, error);
            }

            @Override
            public void webRTCClientStartCallWithSdp(FancyWebRTC client, SessionDescription sdp) {
                Log.d(TAG, "webRTCClientStartCallWithSdp" + " type: " + sdp.type);
                if (sdp.type == SessionDescription.Type.ANSWER) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("from", me);
                        object.put("sdp", sdp.description);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("answered", object);
                    webRTC.handleAnswerReceived(sdp);
                } else if (sdp.type == SessionDescription.Type.OFFER) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("from", me);
                        object.put("sdp", sdp.description);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("call", object);
                }
            }

            @Override
            public void webRTCClientDataChannelStateChanged(FancyWebRTC client, String name, DataChannel.State state) {
                Log.d(TAG, "webRTCClientDataChannelStateChanged " + "Name " + name + " State :" + state);
            }

            @Override
            public void webRTCClientDataChannelMessageType(FancyWebRTC client, String name, String message, FancyWebRTC.DataChannelMessageType type) {
                Log.d(TAG, "webRTCClientDataChannelMessageType " + "Name " + name + " Message : " + message + " Type: " + type);
            }

            @Override
            public void webRTCClientOnRemoveStream(FancyWebRTC client, MediaStream stream) {

            }

            @Override
            public void webRTCClientDidReceiveRemoteVideoTrackStream(FancyWebRTC client, final VideoTrack track, final MediaStream stream) {
                Log.d(TAG, "webRTCClientDidReceiveRemoteVideoTrackStream " + track + " first : " + stream.videoTracks.get(0).enabled());
                remoteTrack = track;
                remoteView.setVideoTrack(track);
            }

            @Override
            public void webRTCClientDidGenerateIceCandidate(FancyWebRTC client, IceCandidate candidate) {
                JSONObject object = new JSONObject();
                try {
                    object.put("from", me);
                    object.put("sdp", candidate.sdp);
                    object.put("sdpMid", candidate.sdpMid);
                    object.put("sdpMLineIndex", candidate.sdpMLineIndex);
                    object.put("serverUrl", candidate.serverUrl);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("iceCandidate", object);
            }

            @Override
            public void webRTCClientOnRenegotiationNeeded(FancyWebRTC client) {

            }

            @Override
            public void webRTCClientOnIceCandidatesRemoved(FancyWebRTC client, IceCandidate[] iceCandidates) {

            }

            @Override
            public void webRTCClientOnIceConnectionChange(FancyWebRTC client, PeerConnection.IceConnectionState iceConnectionState) {

            }

            @Override
            public void webRTCClientOnIceConnectionReceivingChange(FancyWebRTC client, boolean change) {

            }

            @Override
            public void webRTCClientOnIceGatheringChange(FancyWebRTC client, PeerConnection.IceGatheringState iceGatheringState) {

            }

            @Override
            public void webRTCClientOnSignalingChange(FancyWebRTC client, PeerConnection.SignalingState signalingState) {

            }

            @Override
            public void webRTCClientOnCameraSwitchDone(FancyWebRTC client, boolean done) {
                if (done) {
                    mirror = !mirror;
                    localView.setMirror(mirror);
                }
            }

            @Override
            public void webRTCClientOnCameraSwitchError(FancyWebRTC client, String error) {

            }
        });
        webRTC.getUserMedia(FancyWebRTC.Quality.HIGHEST, new FancyWebRTCListener.GetUserMediaListener() {
            @Override
            public void webRTCClientOnGetUserMedia(FancyWebRTC client, MediaStream stream) {
                localStream = stream;
                if (localStream != null) {
                    localStream.videoTracks.get(0).addSink(localView);
                }
            }

            @Override
            public void webRTCClientOnGetUserMediaDidReceiveError(FancyWebRTC client, String error) {
                FancyWebRTC.requestPermissions(MainActivity.this);
            }
        });

        webRTC.connect();
        */
    }

    public void goToAdvanced(View view) {
        Intent intent = new Intent(this, Advanced.class);
        if(webRTC != null){
            webRTC.disconnect();
        }
        startActivity(intent);
    }

    public void requestPermissions() {

    }

    public void makeCall(View view) {
        webRTC.addLocalStream(localStream);
        webRTC.makeOffer(new MediaConstraints());
    }


    public void endCall(View view) {
        webRTC.disconnect();
    }

    public void answerCall(View view) {

    }

    public void switchCamera(View view) {
        if (localStream != null) {
            webRTC.switchCamera(localStream.videoTracks.get(0).id());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FancyWebRTC.WEBRTC_PERMISSIONS_REQUEST_CODE) {
            if (FancyWebRTC.hasPermissions(this)) {
                webRTC.getUserMedia(FancyWebRTC.Quality.HIGHEST, new FancyWebRTCListener.GetUserMediaListener() {
                    @Override
                    public void webRTCClientOnGetUserMedia(FancyWebRTC client, MediaStream stream) {
                        localStream = stream;
                        if (localStream != null) {
                            localStream.videoTracks.get(0).addSink(localView);
                        }
                    }

                    @Override
                    public void webRTCClientOnGetUserMediaDidReceiveError(FancyWebRTC client, String error) {

                    }
                });
            }
        }
    }
}
