package com.example.edgedetectrnd;

public class NativeBridge {
    static {
        System.loadLibrary("native-lib");
    }

    public static native byte[] processFrame(byte[] yuv, int width, int height, boolean edgeMode);
}