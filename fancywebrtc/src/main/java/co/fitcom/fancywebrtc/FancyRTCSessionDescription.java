package co.fitcom.fancywebrtc;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.webrtc.SessionDescription;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCSessionDescription {
    private SessionDescription sessionDescription;

    public FancyRTCSessionDescription(FancyRTCSdpType type, String description) {
        SessionDescription.Type sdpType;
        switch (type) {
            case OFFER:
                sdpType = SessionDescription.Type.OFFER;
                break;
            case ANSWER:
                sdpType = SessionDescription.Type.ANSWER;
                break;
            case PRANSWER:
                sdpType = SessionDescription.Type.PRANSWER;
                break;
            default:
                sdpType = SessionDescription.Type.OFFER;
        }
        sessionDescription = new SessionDescription(sdpType, description);
    }

    public FancyRTCSdpType getType() {
        switch (sessionDescription.type) {
            case OFFER:
                return FancyRTCSdpType.OFFER;
            case ANSWER:
                return FancyRTCSdpType.ANSWER;
            case PRANSWER:
                return FancyRTCSdpType.PRANSWER;
            default:
                return FancyRTCSdpType.OFFER;
        }
    }

    public String getDescription() {
        return sessionDescription.description;
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(sessionDescription);
    }

    public static FancyRTCSessionDescription fromJSON(String json) {
        Gson gson = new Gson();
        SessionDescription sessionDescription = gson.fromJson(json, SessionDescription.class);
        return fromRTCSessionDescription(sessionDescription);
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
        return sessionDescription;
    }

}
