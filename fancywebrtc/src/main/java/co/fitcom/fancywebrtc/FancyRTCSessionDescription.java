package co.fitcom.fancywebrtc;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.webrtc.SessionDescription;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCSessionDescription {
    private SessionDescription sessionDescription;

    private FancyRTCSessionDescription(SessionDescription sdp){
        sessionDescription = sdp;
    }
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
                sdpType = null;
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
                return null;
        }
    }

    public String getSDP() {
        return sessionDescription.description;
    }

    public String getDescription() {
        return sessionDescription.description;
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(sessionDescription);
    }

    static FancyRTCSessionDescription fromRTCSessionDescription(SessionDescription sdp) {
        return new FancyRTCSessionDescription(sdp);
    }

    public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

}
