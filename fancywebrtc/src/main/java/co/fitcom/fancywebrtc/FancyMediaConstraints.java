package co.fitcom.fancywebrtc;

import com.google.gson.Gson;

import org.webrtc.MediaConstraints;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by triniwiz on 1/8/19
 */
public class FancyMediaConstraints {

    public static class KeyValue {
        private String key;
        private String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public final List<KeyValue> mandatory = new ArrayList<>();
    public final List<KeyValue> optional = new ArrayList<>();

    public FancyMediaConstraints() { }

    public String toJSON(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public MediaConstraints getMediaConstraints() {
        MediaConstraints constraints = new MediaConstraints();
        for (KeyValue keyValue: mandatory){
            constraints.mandatory.add(new MediaConstraints.KeyValuePair(keyValue.key,keyValue.value));
        }
        for (KeyValue keyValue: optional){
            constraints.optional.add(new MediaConstraints.KeyValuePair(keyValue.key,keyValue.value));
        }
        return constraints;
    }
}
