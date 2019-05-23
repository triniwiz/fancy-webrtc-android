# FancyWebRTC

## Usage


### Setup LocalStream

```xml
<co.fitcom.fancywebrtc.FancyWebRTCView
        android:id="@+id/localView"
        android:layout_width="380dp"
        android:layout_height="227dp"
        android:layout_marginBottom="8dp"
        android:layout_marginEnd="8dp"
        android:layout_marginStart="8dp"
        android:layout_marginTop="8dp" />
```


```java
  @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        localView = findViewById(R.id.localView);
        localView.setMirror(true);
        if (FancyWebRTC.hasPermissions(this)) {
            setUpUserMedia();
        } else {
            FancyWebRTC.requestPermissions(this);
        }
  }


 public void setUpUserMedia() {
        Map<String, Object> video = new HashMap<>();
        video.put("facingMode", "user"); // user :- front camera , environment :- back camera
        video.put("width", 960);
        video.put("height", 720);
        currentCameraPosition = "user";
        FancyRTCMediaStreamConstraints constraints = new FancyRTCMediaStreamConstraints(true, video);
        FancyRTCMediaDevices.getUserMedia(this, constraints, new FancyRTCMediaDevices.GetUserMediaListener() {
            @Override
            public void onSuccess(FancyRTCMediaStream mediaStream) {
                localStream = mediaStream;
                localView.setSrcObject(mediaStream);
            }

            @Override
            public void onError(String error) {

            }
        });
    }




@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        ...
        FancyRTCApplicationHelper.getInstance().handleResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ...
        if (requestCode == FancyWebRTC.WEBRTC_PERMISSIONS_REQUEST_CODE) {
            if (FancyWebRTC.hasPermissions(this)) {
                setUpUserMedia();
            }
        }
    }
```

### Toggle Camera

```java

    public void switchCamera(View view) {
        if (localStream != null) {
            for (FancyRTCVideoTrack track : localStream.getVideoTracks()) {
                FancyRTCMediaTrackConstraints constraints = new FancyRTCMediaTrackConstraints(null);
                String nextPosition = currentCameraPosition.equals("user") ? "environment" : "user";
                constraints.setFacingMode(nextPosition);
                track.applyConstraints(constraints, new FancyRTCMediaStreamTrack.FancyRTCMediaStreamTrackListener() {
                    @Override
                    public void onSuccess() {
                        if (nextPosition.equals("environment")) {
                            localView.setMirror(false);
                        } else {
                            localView.setMirror(true);
                        }
                        currentCameraPosition = nextPosition;
                    }

                    @Override
                    public void onError(String error) {
                        Log.d(TAG, "error " + error);
                    }
                });
            }
        }
    }
```


### ShareScreen
```java
    public void shareScreen(View view) {
        FancyRTCMediaDevices.getDisplayMedia(this, new FancyRTCMediaStreamConstraints(true, true), new FancyRTCMediaDevices.GetUserMediaListener() {
            @Override
            public void onSuccess(FancyRTCMediaStream mediaStream) {
                localStream = mediaStream;
                localView.setSrcObject(mediaStream);
                localView.setMirror(false);
            }

            @Override
            public void onError(String error) {

            }
        });
    }
```

## See Also

1. [V2 full call example](https://github.com/triniwiz/fancy-webrtc-android/blob/master/app/src/main/java/co/fitcom/fancywebrtcdemo/Advanced.java)
2. [V1 full call example](https://github.com/triniwiz/fancy-webrtc-android/blob/master/app/src/main/java/co/fitcom/fancywebrtcdemo/MainActivity.java)
3. [Demo Server :- Nodejs](https://github.com/triniwiz/nativescript-webrtc/tree/master/demo-server)
