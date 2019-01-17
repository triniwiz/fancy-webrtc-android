package co.fitcom.fancywebrtc;

import android.support.annotation.Nullable;

import org.webrtc.DtmfSender;

/**
 * Created by triniwiz on 1/17/19
 */
public class FancyRTCDTMFSender {
    private DtmfSender sender;

    public FancyRTCDTMFSender(DtmfSender sender) {
        this.sender = sender;
    }

    public String getToneBuffer() {
        return sender.tones();
    }

    public DtmfSender getSender() {
        return sender;
    }

    public void dispose() {
        sender.dispose();
    }

    public void insertDTMF(final String tones, final @Nullable Integer duration, final @Nullable Integer interToneGap) {
        int d = duration != null ? duration : 100;
        int i = interToneGap != null ? interToneGap : 70;
        sender.insertDtmf(tones, d, i);
    }
}
