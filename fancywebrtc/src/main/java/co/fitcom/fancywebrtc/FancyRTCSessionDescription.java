package co.fitcom.fancywebrtc;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.webrtc.SessionDescription;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCSessionDescription {
    private FancyRTCSdpType type;
    private String sdp;

    public FancyRTCSessionDescription(FancyRTCSdpType type, String sdp) {
        this.type = type;
        this.sdp = sdp;
    }

    public FancyRTCSdpType getType() {
        return type;
    }

    public String getSdp() {
        return sdp;
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static FancyRTCSessionDescription fromJSON(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, FancyRTCSessionDescription.class);
    }

    static FancyRTCSessionDescription fromRTCSessionDescription(SessionDescription sdp) {
        FancyRTCSdpType type;
        switch (sdp.type) {
            case OFFER:
                type = FancyRTCSdpType.OFFER;
                break;
            case ANSWER:
                type = FancyRTCSdpType.ANSWER;
                break;
            case PRANSWER:
                type = FancyRTCSdpType.PRANSWER;
                break;
            default:
                type = FancyRTCSdpType.ROLLBACK;
        }
        return new FancyRTCSessionDescription(type, sdp.description);
    }
}
