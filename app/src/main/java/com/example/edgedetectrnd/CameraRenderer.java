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

    private final Context context;

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private GLTextureRenderer glRenderer;

    private Surface surface;
    private int width, height;

    private boolean isEdgeMode = true;
    private FpsCallback fpsCallback;
    private long lastFpsTime = 0;
    private int frameCount = 0;

    public interface FpsCallback {
        void onFpsUpdate(int fps);
    }

    public CameraRenderer(Context context, SurfaceTexture texture, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;

        // ensure even dims (required for many YUV formats / OpenCV conversions)
        if ((width % 2) != 0 || (height % 2) != 0) {
            throw new IllegalArgumentException("width and height must be even. Got: " + width + "x" + height);
        }

        texture.setDefaultBufferSize(width, height);
        this.surface = new Surface(texture);

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
        CameraManager manager =
                (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = manager.getCameraIdList()[0];

            imageReader = ImageReader.newInstance(
                    width, height,
                    android.graphics.ImageFormat.YUV_420_888, 2);

            imageReader.setOnImageAvailableListener(this, backgroundHandler);

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    createSession();
                }

                @Override public void onDisconnected(CameraDevice c) {}
                @Override public void onError(CameraDevice c, int e) {}
            }, backgroundHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSession() {
        try {
            CaptureRequest.Builder builder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            builder.addTarget(surface);
            builder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(
                    Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession s) {
                            captureSession = s;
                            try {
                                builder.set(CaptureRequest.CONTROL_MODE,
                                        CameraMetadata.CONTROL_MODE_AUTO);
                                captureSession.setRepeatingRequest(
                                        builder.build(), null, backgroundHandler);
                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        @Override public void onConfigureFailed(CameraCaptureSession s) {}
                    },
                    backgroundHandler
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();
            if (image == null) return;

            // Convert to NV21 respecting strides/pixelStride
            byte[] nv21 = imageToNV21(image);
            if (nv21 == null) return;

            // send to native (expects NV21 bytes)
            NativeBridge.processFrame(nv21, width, height, isEdgeMode);

            // trigger GL draw (native updates GL texture)
            glRenderer.updateTexture(null);

            // FPS
            frameCount++;
            long now = System.currentTimeMillis();
            if (now - lastFpsTime > 1000) {
                if (fpsCallback != null)
                    fpsCallback.onFpsUpdate(frameCount);

                frameCount = 0;
                lastFpsTime = now;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (image != null) image.close();
        }
    }

    /**
     * Convert Image (YUV_420_888) to NV21 byte[] (Y + VU interleaved).
     * This handles rowStride and pixelStride.
     */
    private byte[] imageToNV21(Image image) {
        if (image == null) return null;

        int width = image.getWidth();
        int height = image.getHeight();
        Image.Plane[] planes = image.getPlanes();

        ByteBuffer yBuf = planes[0].getBuffer();
        ByteBuffer uBuf = planes[1].getBuffer();
        ByteBuffer vBuf = planes[2].getBuffer();

        int yRowStride = planes[0].getRowStride();
        int uRowStride = planes[1].getRowStride();
        int vRowStride = planes[2].getRowStride();
        int uPixelStride = planes[1].getPixelStride(); // could be 1 or 2

        byte[] nv21 = new byte[width * height * 3 / 2];

        // Copy Y plane
        byte[] rowData = new byte[yRowStride];
        int pos = 0;
        yBuf.position(0);
        for (int r = 0; r < height; r++) {
            yBuf.position(r * yRowStride);
            yBuf.get(rowData, 0, Math.min(yRowStride, yBuf.remaining()));
            System.arraycopy(rowData, 0, nv21, pos, width);
            pos += width;
        }

        // Copy interleaved VU (NV21 expects V then U)
        int chromaHeight = height / 2;
        int chromaWidth = width / 2;
        byte[] uRow = new byte[uRowStride];
        byte[] vRow = new byte[vRowStride];

        int uvPos = width * height;
        uBuf.position(0);
        vBuf.position(0);

        for (int r = 0; r < chromaHeight; r++) {
            int uRowStart = r * uRowStride;
            int vRowStart = r * vRowStride;

            // read rows (take care not to overflow)
            uBuf.position(uRowStart);
            int uRead = Math.min(uRowStride, uBuf.remaining());
            uBuf.get(uRow, 0, uRead);

            vBuf.position(vRowStart);
            int vRead = Math.min(vRowStride, vBuf.remaining());
            vBuf.get(vRow, 0, vRead);

            for (int c = 0; c < chromaWidth; c++) {
                int uvIndexInRow = c * uPixelStride;
                // defensive bounds check
                byte U = (uvIndexInRow < uRow.length) ? uRow[uvIndexInRow] : 0;
                byte V = (uvIndexInRow < vRow.length) ? vRow[uvIndexInRow] : 0;
                nv21[uvPos++] = V;
                nv21[uvPos++] = U;
            }
        }

        return nv21;
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try { backgroundThread.join(); } catch (Exception ignored) {}
        }
    }

    private void closeCamera() {
        if (captureSession != null) { captureSession.close(); captureSession = null; }
        if (cameraDevice != null) { cameraDevice.close(); cameraDevice = null; }
        if (imageReader != null) { imageReader.close(); imageReader = null; }
    }
}
