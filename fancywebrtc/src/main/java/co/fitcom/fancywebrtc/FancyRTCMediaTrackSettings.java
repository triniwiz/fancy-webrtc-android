package co.fitcom.fancywebrtc;

/**
 * Created by triniwiz on 2019-05-23
 */
public class FancyRTCMediaTrackSettings {
    private String id;
    private String type;

    FancyRTCMediaTrackSettings(String id, String type) {
        this.id = id;
        this.type = type;
    }

    private FancyRTCMediaDevices.FancyCapturer getCapturer() {
        return FancyRTCMediaDevices.videoTrackcapturerMap.get(id);
    }

    private boolean isVideo() {
        return type.equals("video");
    }

    public int getWidth() {
        if (isVideo()) {
            if (getCapturer() != null) {
                return getCapturer().getWidth();
            }
            return 0;
        }
        return 0;
    }

    public int getHeight() {
        if (isVideo()) {
            if (getCapturer() != null) {
                return getCapturer().getHeight();
            }
            return 0;
        }
        return 0;
    }

    public int getFrameRate() {
        if (isVideo()) {
            if (getCapturer() != null) {
                return getCapturer().getFrameRate();
            }
            return 0;
        }
        return 0;
    }

    public int getAspectRatio() {
        if (isVideo()) {
            if (getCapturer() != null) {
                return getCapturer().getAspectRatio();
            }
            return 0;
        }
        return 0;
    }

    public String facingMode() {
        if (isVideo()) {
            if (getCapturer() != null) {
                return getCapturer().getPosition();
            }
            return "";
        }
        return "";
    }
}
