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
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int w, int h) {
                NativeLib.initOpenGL(w, h);
                NativeLib.setFpsCallback(fps -> runOnUiThread(() -> fpsText.setText(String.format("FPS: %.1f", fps))));
                startProcessingLoop(w, h);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int w, int h) {}

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                NativeLib.cleanup();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        });
    }

    private void startProcessingLoop(int w, int h) {
        new Thread(() -> {
            while (textureView.isAvailable()) {
                byte[] yuv = new byte[w * h * 3 / 2];
                NativeLib.processFrame(yuv, w, h, isEdge);
                try { Thread.sleep(33); } catch (Exception ignored) {}
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Restart
            finish();
            startActivity(getIntent());
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}