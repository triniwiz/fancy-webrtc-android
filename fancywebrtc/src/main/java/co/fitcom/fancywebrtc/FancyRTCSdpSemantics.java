package co.fitcom.fancywebrtc;

/**
 * Created by triniwiz on 1/18/19
 */
public enum FancyRTCSdpSemantics {
    PLAN_B("plan-b"),
    UNIFIED_PLAN("unified-plan");
    private final String plan;

    FancyRTCSdpSemantics(String plan) {
        this.plan = plan;
    }

    @Override
    public String toString() {
        return this.plan;
    }
}
