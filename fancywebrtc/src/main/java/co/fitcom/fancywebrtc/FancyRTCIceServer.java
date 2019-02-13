package co.fitcom.fancywebrtc;

import android.support.annotation.Nullable;
import android.util.Log;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by triniwiz on 1/7/19
 */
public class FancyRTCIceServer {
    private String[] urls;
    private String credential;
    private FancyRTCIceCredentialType credentialType;
    private String username;
    PeerConnection.IceServer server;

    public FancyRTCIceServer(String url) {
        this.urls = new String[]{url};
    }

    public FancyRTCIceServer(String url, @Nullable String username, @Nullable String credential) {
        this.urls = new String[]{url};
        this.username = username;
        this.credential = credential;
    }

    public FancyRTCIceServer(String[] urls) {
        this.urls = urls;
    }

    public FancyRTCIceServer(String[] urls, String username, String credential) {
        this.urls = urls;
        this.username = username;
        this.credential = credential;
    }

    public PeerConnection.IceServer toWebRtc() {
        PeerConnection.IceServer.Builder builder = PeerConnection.IceServer.builder(Arrays.asList(urls));
        if (credential != null) {
            builder.setPassword(credential);
        }
        if (username != null) {
            builder.setUsername(username);
        }
        server = builder.createIceServer();
        return server;
    }

    public PeerConnection.IceServer getIceServer() {
        return toWebRtc();
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredentialType(FancyRTCIceCredentialType credentialType) {
        this.credentialType = credentialType;
    }

    public FancyRTCIceCredentialType getCredentialType() {
        return credentialType;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public String[] getUrls() {
        return urls;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
