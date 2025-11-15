package com.example.edgedetectrnd;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private TextureView textureView;
    private CameraRenderer cameraRenderer;
    private Button toggleButton;
    private TextView fpsText;
    private boolean isEdgeMode = true;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        toggleButton = findViewById(R.id.toggleButton);
        fpsText = findViewById(R.id.fpsText);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }

        setupCamera();
    }

    private void setupCamera() {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                cameraRenderer = new CameraRenderer(MainActivity.this, surface, width, height);
                cameraRenderer.startCamera();
                cameraRenderer.setFpsCallback(fps -> runOnUiThread(() -> fpsText.setText("FPS: " + fps)));
            }

            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture s, int w, int h) {}
            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture s) { return true; }
            @Override public void onSurfaceTextureUpdated(SurfaceTexture s) {}
        });

        toggleButton.setOnClickListener(v -> {
            isEdgeMode = !isEdgeMode;
            toggleButton.setText(isEdgeMode ? "Edge" : "Raw");
            if (cameraRenderer != null) {
                cameraRenderer.setEdgeMode(isEdgeMode);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 100 && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraRenderer != null) cameraRenderer.stopCamera();
    }
}