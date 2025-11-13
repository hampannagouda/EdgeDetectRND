#include <jni.h>
#include <opencv2/opencv.hpp>
#include <GLES2/gl2.h>
#include <chrono>

using namespace cv;
using namespace std::chrono;

GLuint textureId = 0;
bool edgeMode = true;
float fps = 0.0f;
int frameCountCount = 0;
auto lastTime = steady_clock::now();

jobject fpsCallbackObj = nullptr;
jmethodID fpsMethodId = nullptr;

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectrnd_NativeLib_initOpenGL(JNIEnv* env, jclass, jint w, jint h) {
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectrnd_NativeLib_processFrame(JNIEnv* env, jclass, jbyteArray yuv, jint w, jint h, jboolean e) {
    edgeMode = e;
    jbyte* data = env->GetByteArrayElements(yuv, nullptr);
    Mat yuvMat(h + h/2, w, CV_8UC1, (uchar*)data);
    Mat rgba(h, w, CV_8UC4);
    cvtColor(yuvMat, rgba, COLOR_YUV2RGBA_NV21);

    if (edgeMode) {
        Mat gray, edges;
        cvtColor(rgba, gray, COLOR_RGBA2GRAY);
        Canny(gray, edges, 50, 150);
        cvtColor(edges, edges, COLOR_GRAY2RGBA);
        edges.copyTo(rgba);
    }

    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, rgba.data);
    env->ReleaseByteArrayElements(yuv, data, 0);

    // FPS
    frameCount++;
    auto now = steady_clock::now();
    auto ms = duration_cast<milliseconds>(now - lastTime).count();
    if (ms >= 1000) {
        fps = frameCount * 1000.0f / ms;
        frameCount = 0;
        lastTime = now;
        if (fpsCallbackObj && fpsMethodId) {
            env->CallVoidMethod(fpsCallbackObj, fpsMethodId, fps);
        }
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectrnd_NativeLib_setFpsCallback(JNIEnv* env, jclass, jobject callback) {
    if (fpsCallbackObj) env->DeleteGlobalRef(fpsCallbackObj);
    fpsCallbackObj = env->NewGlobalRef(callback);
    jclass cls = env->GetObjectClass(callback);
    fpsMethodId = env->GetMethodID(cls, "onFps", "(F)V");
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectrnd_NativeLib_cleanup(JNIEnv* env, jclass) {
    if (textureId) glDeleteTextures(1, &textureId);
    if (fpsCallbackObj) env->DeleteGlobalRef(fpsCallbackObj);
    textureId = 0;
    fpsCallbackObj = nullptr;
}