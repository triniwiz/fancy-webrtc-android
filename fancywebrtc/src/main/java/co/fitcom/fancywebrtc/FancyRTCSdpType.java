package co.fitcom.fancywebrtc;

/**
 * Created by triniwiz on 1/8/19
 */
public enum FancyRTCSdpType {
    ANSWER("answer"),
    OFFER("offer"),
    PRANSWER("pranswer"),
    ROLLBACK("rollback");

    private String type;

    FancyRTCSdpType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
