package co.fitcom.fancywebrtc;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.webrtc.SessionDescription;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCSessionDescription {
    private FancyRTCSdpType type;
    private String description;

    public FancyRTCSessionDescription(FancyRTCSdpType type, String description) {
        this.type = type;
        this.description = description;
    }

    public FancyRTCSdpType getType() {
        return type;
    }

    public String getDescription() {
        return description;
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

    public SessionDescription getSessionDescription() {
        switch (this.type) {
            case PRANSWER:
                return new SessionDescription(SessionDescription.Type.PRANSWER, description);
            case ANSWER:
                return new SessionDescription(SessionDescription.Type.ANSWER, description);
            case OFFER:
                return new SessionDescription(SessionDescription.Type.OFFER, description);
            default:
                return null;
        }
    }

}
