package com.example.edgedetectrnd;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private TextureView textureView;
    private Button toggleButton;
    private TextView fpsText;
    private boolean isEdge = true;

    private CameraRenderer cameraRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        toggleButton = findViewById(R.id.toggleButton);
        fpsText = findViewById(R.id.fpsText);

        toggleButton.setOnClickListener(v -> {
            isEdge = !isEdge;
            toggleButton.setText(isEdge ? "Edge" : "Raw");
            if (cameraRenderer != null)
                cameraRenderer.setEdgeMode(isEdge);
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }

        initTextureView();
    }

    private void initTextureView() {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

                NativeBridge.initOpenGL(width, height);

                // FIXED: DO NOT USE LAMBDA HERE
                NativeBridge.setFpsCallback(new NativeBridge.FpsCallback() {
                    @Override
                    public void onFps(float fps) {
                        runOnUiThread(() ->
                                fpsText.setText(String.format("FPS: %.1f", fps))
                        );
                    }
                });

                cameraRenderer = new CameraRenderer(
                        MainActivity.this,
                        surface,
                        width,
                        height
                );

                cameraRenderer.setEdgeMode(isEdge);

                cameraRenderer.setFpsCallback(fps ->
                        runOnUiThread(() -> fpsText.setText("FPS: " + fps)));

                cameraRenderer.startCamera();
            }

            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int w, int h) {}

            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                NativeBridge.cleanup();
                if (cameraRenderer != null) cameraRenderer.stopCamera();
                return true;
            }

            @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 100 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
