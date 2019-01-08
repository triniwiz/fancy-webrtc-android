package co.fitcom.fancywebrtc;

import com.google.gson.Gson;

import org.webrtc.IceCandidate;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCIceCandidate {
    private String candidate;
    private String sdpMid;
    private int sdpMLineIndex;
    private String usernameFragment;
    private String serverUrl;
    private IceCandidate iceCandidate;
    public FancyRTCIceCandidate() {
        candidate = "";
        sdpMid = "";
        sdpMLineIndex = 0;
        usernameFragment = "";
        serverUrl = "";
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public void setSdp(String sdp) {
        candidate = sdp;
    }

    public String getSdp(){
        return candidate;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public int getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(int sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }

    public void setUsernameFragment(String usernameFragment) {
        this.usernameFragment = usernameFragment;
    }

    public String getUsernameFragment() {
        return usernameFragment;
    }

    public String toJSON(){
        Gson gson = new Gson();
        return  gson.toJson(this);
    }

    public static FancyRTCIceCandidate fromJSON(String json){
        Gson gson = new Gson();
        return  gson.fromJson(json,FancyRTCIceCandidate.class);
    }

    IceCandidate getIceCandidate(){
        return new IceCandidate(getSdpMid(),getSdpMLineIndex(),getSdp());
    }
}
