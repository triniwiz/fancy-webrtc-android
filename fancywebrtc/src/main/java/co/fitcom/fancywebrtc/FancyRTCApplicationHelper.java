package co.fitcom.fancywebrtc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.SparseArray;

/**
 * Created by triniwiz on 2/26/19
 */
public class FancyRTCApplicationHelper {
    private static SparseArray<Callback> callbackSparseArray = new SparseArray<>();
    private static FancyRTCApplicationHelper instance;

    static {
        instance = new FancyRTCApplicationHelper();
    }

    public static interface Callback {
        public void onPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

        public void onResult(int requestCode, int resultCode, @Nullable Intent data);
    }

    public void handlePermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Callback callback = callbackSparseArray.get(requestCode);
        if (callback != null) {
            callback.onPermissionResult(requestCode, permissions, grantResults);
            callbackSparseArray.remove(requestCode);
        }
    }

    public void handleResult(int requestCode, int resultCode, @Nullable Intent data) {
        Callback callback = callbackSparseArray.get(requestCode);
        if (callback != null) {
            callback.onResult(requestCode, resultCode, data);
            callbackSparseArray.remove(requestCode);
        }
    }

    public static FancyRTCApplicationHelper getInstance() {
        if (instance == null) {
            instance = new FancyRTCApplicationHelper();
        }
        return instance;
    }

    public void requestPermission(Context context, String permission, int requestCode, Callback callback) {
        callbackSparseArray.append(requestCode, callback);
        androidx.core.app.ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
    }

    public void requestPermissions(Context context, String[] permissions, int requestCode, Callback callback) {
        callbackSparseArray.append(requestCode, callback);
        androidx.core.app.ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);
    }

    public void onResult(int requestCode, Callback callback) {
        callbackSparseArray.append(requestCode, callback);
    }
}
