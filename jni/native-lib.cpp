#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOG_TAG "NativeLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

using namespace cv;

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetectrnd_NativeBridge_processFrame(
        JNIEnv* env, jclass, jbyteArray yuv, jint width, jint height, jboolean edgeMode) {

    if (yuv == nullptr) return nullptr;
    jbyte* _yuv = env->GetByteArrayElements(yuv, NULL);
    jsize yuv_len = env->GetArrayLength(yuv);
    if (_yuv == nullptr) return nullptr;

    // Create Mat from NV21 buffer (height + height/2 rows, width cols)
    Mat myuv(height + height / 2, width, CV_8UC1, (unsigned char*)_yuv);
    Mat mrgba;
    try {
        cvtColor(myuv, mrgba, COLOR_YUV2RGBA_NV21);
        if (edgeMode) {
            Mat gray, edges;
            cvtColor(mrgba, gray, COLOR_RGBA2GRAY);
            Canny(gray, edges, 50, 150);
            cvtColor(edges, mrgba, COLOR_GRAY2RGBA);
        }
    } catch (cv::Exception &e) {
        LOGD("OpenCV exception: %s", e.what());
        env->ReleaseByteArrayElements(yuv, _yuv, JNI_ABORT);
        return nullptr;
    }

    int outBytes = (int)(mrgba.total() * mrgba.elemSize());
    jbyteArray result = env->NewByteArray(outBytes);
    if (result != NULL) {
        env->SetByteArrayRegion(result, 0, outBytes, (jbyte*)mrgba.data);
    }

    // release original buffer (no copy back)
    env->ReleaseByteArrayElements(yuv, _yuv, JNI_ABORT);
    return result;
}
