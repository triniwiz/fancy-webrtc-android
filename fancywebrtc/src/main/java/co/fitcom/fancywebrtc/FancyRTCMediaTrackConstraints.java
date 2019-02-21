package co.fitcom.fancywebrtc;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by triniwiz on 1/9/19
 */
public class FancyRTCMediaTrackConstraints {
    private Map<String, Object> constraints;

    public FancyRTCMediaTrackConstraints(@Nullable Map<String, Object> constraints) {
        if (constraints != null) {
            this.constraints = constraints;
        } else {
            this.constraints = new HashMap<>();
        }
    }

    public void setFacingMode(String mode) {
        constraints.put("facingMode", mode);
    }

    public String getFacingMode() {
        return (String) constraints.get("facingMode");
    }
}
