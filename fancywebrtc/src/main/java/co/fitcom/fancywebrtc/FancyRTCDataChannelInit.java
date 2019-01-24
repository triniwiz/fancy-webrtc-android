package co.fitcom.fancywebrtc;

import com.google.gson.Gson;

import org.webrtc.DataChannel;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyRTCDataChannelInit {
    private boolean ordered = true;
    private int maxPacketLifeTime;
    private int maxRetransmits;
    private String protocol = "";
    private boolean negotiated = false;
    private int id;

    public FancyRTCDataChannelInit() {
    }

    public DataChannel.Init getInit() {
        DataChannel.Init init = new DataChannel.Init();
        init.id = id;
        init.ordered = ordered;
        init.maxRetransmits = maxRetransmits;
        init.maxRetransmitTimeMs = maxPacketLifeTime;
        init.protocol = protocol;
        return init;
    }

    public int getId() {
        return id;
    }

    public int getMaxPacketLifeTime() {
        return maxPacketLifeTime;
    }

    public int getMaxRetransmits() {
        return maxRetransmits;
    }

    public String getProtocol() {
        return protocol;
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
