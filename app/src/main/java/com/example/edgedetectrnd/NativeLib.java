package com.example.edgedetectrnd;

public class NativeLib {
    static {
        System.loadLibrary("native-lib");
    }

    public static native void initOpenGL(int w, int h);
    public static native void processFrame(byte[] yuv, int w, int h, boolean edge);
    public static native void setFpsCallback(FpsCallback callback);
    public static native void cleanup();

    public interface FpsCallback {
        void onFps(float fps);
    }
}