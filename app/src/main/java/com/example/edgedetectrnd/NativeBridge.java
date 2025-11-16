package com.example.edgedetectrnd;

public class NativeBridge {

    static {
        System.loadLibrary("native-lib");
    }

    public static native void initOpenGL(int w, int h);
    public static native byte[] processFrame(byte[] frame, int width, int height, boolean edge);
    public static native void cleanup();

    // JNI will call a method with signature (F)V → float parameter
    public static native void setFpsCallback(FpsCallback cb);

    // MUST MATCH native-lib.cpp → getNativeTextureId()
    public static native int getNativeTextureId();

    // MUST be float, not double
    public interface FpsCallback {
        void onFps(float fps);
    }
}
