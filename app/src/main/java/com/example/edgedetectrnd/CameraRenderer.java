package com.example.edgedetectrnd;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraRenderer implements ImageReader.OnImageAvailableListener {

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private GLTextureRenderer glRenderer;
    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private Context context;
    private int width, height;
    private boolean isEdgeMode = true;
    private FpsCallback fpsCallback;
    private long lastFpsTime = 0;
    private int frameCount = 0;

    public interface FpsCallback {
        void onFpsUpdate(int fps);
    }

    public CameraRenderer(Context context, SurfaceTexture surfaceTexture, int width, int height) {
        this.context = context;
        this.surfaceTexture = surfaceTexture;
        this.width = width;
        this.height = height;
        this.surfaceTexture.setDefaultBufferSize(width, height);
        this.surface = new Surface(surfaceTexture);
        this.glRenderer = new GLTextureRenderer(width, height);
    }

    public void startCamera() {
        startBackgroundThread();
        openCamera();
    }

    public void stopCamera() {
        closeCamera();
        stopBackgroundThread();
    }

    public void setEdgeMode(boolean edge) {
        this.isEdgeMode = edge;
    }

    public void setFpsCallback(FpsCallback callback) {
        this.fpsCallback = callback;
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            imageReader = ImageReader.newInstance(width, height, android.graphics.ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(this, backgroundHandler);

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    createCaptureSession();
                }

                @Override public void onDisconnected(CameraDevice c) {}
                @Override public void onError(CameraDevice c, int e) {}
            }, backgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCaptureSession() {
        try {
            CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(surface);
            requestBuilder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                requestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                                captureSession.setRepeatingRequest(requestBuilder.build(), null, backgroundHandler);
                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        @Override public void onConfigureFailed(CameraCaptureSession s) {}
                    }, backgroundHandler);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        if (image == null) return;

        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] data = new byte[ySize + uSize + vSize];
        yBuffer.get(data, 0, ySize);
        vBuffer.get(data, ySize, vSize);
        uBuffer.get(data, ySize + vSize, uSize);

        byte[] processed = NativeBridge.processFrame(data, width, height, isEdgeMode);
        glRenderer.updateTexture(processed);

        image.close();

        // FPS
        frameCount++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime > 1000) {
            int fps = frameCount;
            frameCount = 0;
            lastFpsTime = now;
            if (fpsCallback != null) fpsCallback.onFpsUpdate(fps);
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try { backgroundThread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private void closeCamera() {
        if (captureSession != null) { captureSession.close(); captureSession = null; }
        if (cameraDevice != null) { cameraDevice.close(); cameraDevice = null; }
        if (imageReader != null) { imageReader.close(); imageReader = null; }
    }
}