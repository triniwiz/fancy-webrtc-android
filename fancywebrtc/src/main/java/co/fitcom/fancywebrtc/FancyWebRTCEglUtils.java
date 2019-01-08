package co.fitcom.fancywebrtc;

import org.webrtc.*;

/**
 * Created by Osei Fortune on 8/15/18
 */
public class FancyWebRTCEglUtils {
   private static EglBase rootEglBase;

    public static EglBase getRootEglBase() {
        if (rootEglBase == null) {
            rootEglBase = EglBase.create();
        }
        return rootEglBase;
    }

    public static EglBase.Context getRootEglBaseContext() {
        return getRootEglBase() != null ? getRootEglBase().getEglBaseContext() : null;
    }
}
