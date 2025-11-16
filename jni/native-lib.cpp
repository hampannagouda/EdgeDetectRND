#include <jni.h>
#include <opencv2/opencv.hpp>
#include <GLES2/gl2.h>
#include <android/log.h>
#include <chrono>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "NativeBridge", __VA_ARGS__)

using namespace cv;
using namespace std::chrono;

// ===============================
// GLOBAL STATE
// ===============================
GLuint textureId = 0;

jobject fpsCallbackObj = nullptr;
jmethodID fpsMethodId = nullptr;

int frameCount = 0;
auto lastTime = std::chrono::steady_clock::now();

// ===============================
// HELPER – Fix NV21/YUV crashes
// ===============================
static void fixYuvSize(int &width, int &height) {
    if (width % 2 != 0) width--;
    if (height % 2 != 0) height--;
}

// ===============================
// initOpenGL()
// ===============================
extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectrnd_NativeBridge_initOpenGL
        (JNIEnv* env, jclass clazz, jint width, jint height) {

    fixYuvSize(width, height);

    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
                 width, height,
                 0, GL_RGBA,
                 GL_UNSIGNED_BYTE, nullptr);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    LOGD("OpenGL texture created %dx%d (fixed YUV-safe size)", width, height);
}

// ===============================
// processFrame()
// ===============================
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetectrnd_NativeBridge_processFrame
        (JNIEnv* env, jclass clazz, jbyteArray yuvData,
         jint width, jint height, jboolean edgeEnabled) {

    fixYuvSize(width, height);

    jbyte* yuvBytes = env->GetByteArrayElements(yuvData, nullptr);

    // NV21 image size
    int yuvSize = width * height * 3 / 2;

    // Wrap YUV (NV21)
    Mat yuvMat(height + height / 2, width, CV_8UC1, (uchar*)yuvBytes);
    Mat rgbaMat(height, width, CV_8UC4);

    // Convert YUV → RGBA
    cvtColor(yuvMat, rgbaMat, COLOR_YUV2RGBA_NV21);

    if (edgeEnabled) {
        Mat gray, edges;
        cvtColor(rgbaMat, gray, COLOR_RGBA2GRAY);
        Canny(gray, edges, 60, 140);
        cvtColor(edges, rgbaMat, COLOR_GRAY2RGBA);
    }

    // Upload to OpenGL texture
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexSubImage2D(GL_TEXTURE_2D,
                    0, 0, 0,
                    width, height,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    rgbaMat.data);

    env->ReleaseByteArrayElements(yuvData, yuvBytes, 0);

    // FPS
    frameCount++;
    auto now = steady_clock::now();
    auto elapsed = duration_cast<milliseconds>(now - lastTime).count();

    if (elapsed >= 1000) {
        float fps = frameCount * 1000.0f / elapsed;
        frameCount = 0;
        lastTime = now;

        if (fpsCallbackObj && fpsMethodId) {
            env->CallVoidMethod(fpsCallbackObj, fpsMethodId, fps);
        }
        LOGD("FPS = %.1f", fps);
    }

    return nullptr;
}

// ===============================
// setFpsCallback()
// ===============================
extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectrnd_NativeBridge_setFpsCallback
        (JNIEnv* env, jclass clazz, jobject callback) {

    if (fpsCallbackObj) {
        env->DeleteGlobalRef(fpsCallbackObj);
    }

    fpsCallbackObj = env->NewGlobalRef(callback);

    jclass cbClass = env->GetObjectClass(callback);
    fpsMethodId = env->GetMethodID(cbClass, "onFps", "(F)V");

    LOGD("FPS callback registered");
}

// ===============================
// cleanup()
// ===============================
extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectrnd_NativeBridge_cleanup
        (JNIEnv* env, jclass clazz) {

    if (textureId) {
        glDeleteTextures(1, &textureId);
        textureId = 0;
    }

    if (fpsCallbackObj) {
        env->DeleteGlobalRef(fpsCallbackObj);
        fpsCallbackObj = nullptr;
    }

    LOGD("Native cleanup complete");
}

// ===============================
// getNativeTextureId()
// ===============================
extern "C" JNIEXPORT jint JNICALL
Java_com_example_edgedetectrnd_NativeBridge_getNativeTextureId
        (JNIEnv*, jclass) {
    return textureId;
}
