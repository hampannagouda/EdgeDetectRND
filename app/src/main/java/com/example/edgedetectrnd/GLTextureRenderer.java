package com.example.edgedetectrnd;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLTextureRenderer {

    private int width, height;
    private int program;
    private int positionHandle;
    private int texCoordHandle;
    private int textureHandle;

    private FloatBuffer vertexBuffer;
    private FloatBuffer texBuffer;

    private static final float[] VERTICES = {
            -1f, -1f,
            1f, -1f,
            -1f,  1f,
            1f,  1f
    };

    private static final float[] TEX_COORDS = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    public GLTextureRenderer(int width, int height) {
        this.width = width;
        this.height = height;

        // Convert arrays into FloatBuffers
        vertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(VERTICES);
        vertexBuffer.position(0);

        texBuffer = ByteBuffer.allocateDirect(TEX_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texBuffer.put(TEX_COORDS);
        texBuffer.position(0);

        initGL();
    }

    private void initGL() {
        String vShader =
                "attribute vec2 aPosition;" +
                        "attribute vec2 aTexCoord;" +
                        "varying vec2 vTexCoord;" +
                        "void main() {" +
                        "  gl_Position = vec4(aPosition, 0.0, 1.0);" +
                        "  vTexCoord = aTexCoord;" +
                        "}";

        String fShader =
                "precision mediump float;" +
                        "varying vec2 vTexCoord;" +
                        "uniform sampler2D uTexture;" +
                        "void main() {" +
                        "  gl_FragColor = texture2D(uTexture, vTexCoord);" +
                        "}";

        int vs = loadShader(GLES20.GL_VERTEX_SHADER, vShader);
        int fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fShader);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
        textureHandle  = GLES20.glGetUniformLocation(program, "uTexture");

        GLES20.glClearColor(0, 0, 0, 1);
    }

    private int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }

    /**
     * Draw OpenGL texture updated by native code.
     */
    public void updateTexture(byte[] unused) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);

        // Vertex buffer
        GLES20.glVertexAttribPointer(
                positionHandle, 2, GLES20.GL_FLOAT,
                false, 0, vertexBuffer
        );
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Texture buffer
        GLES20.glVertexAttribPointer(
                texCoordHandle, 2, GLES20.GL_FLOAT,
                false, 0, texBuffer
        );
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        // Bind texture from native code
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, NativeBridge.getNativeTextureId());

        GLES20.glUniform1i(textureHandle, 0);

        // Draw fullscreen quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
