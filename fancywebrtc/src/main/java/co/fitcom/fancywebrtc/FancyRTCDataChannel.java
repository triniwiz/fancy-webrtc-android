package co.fitcom.fancywebrtc;

import org.webrtc.DataChannel;

/**
 * Created by triniwiz on 1/16/19
 */
public class FancyRTCDataChannel {
    DataChannel dataChannel;
    FancyRTCDataChannel(DataChannel channel){
        dataChannel = channel;
    }

    public DataChannel getDataChannel() {
        return dataChannel;
    }
}
