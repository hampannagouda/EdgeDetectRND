# EdgeDetectRND – Real-Time Canny Edge Detection Viewer

Android app using **Camera2 API → JNI → OpenCV C++ → OpenGL ES 2.0** with a **TypeScript web viewer**.

---

## Features Implemented

| Feature | Status |
|-------|--------|
| Camera2 + SurfaceTexture | Done |
| JNI Bridge to C++ | Done |
| OpenCV C++ Canny Edge Detection | Done |
| OpenGL ES 2.0 Texture Rendering | Done |
| Toggle Raw / Edge View | Done |
| FPS Counter | Done |
| TypeScript Web Viewer | Done |
| Modular Architecture | Done |
| Clean Git History | Done |

---

## Screenshots / Demo

![Demo](screenshots/demo.gif)

---

## Architecture Overview
Camera2 API
↓ (ImageReader → YUV_420_888)
Java (CameraRenderer)
↓ (JNI: byte[] → native)
C++ (native-lib.cpp)
↓ (OpenCV: cv::Mat → Canny)
C++ → returns processed RGBA buffer
↓ (JNI → Java byte[])
GLRenderer (OpenGL ES 2.0)
↓ (PBO or direct texture update)
Screen (Real-time 12–18 FPS)


Web viewer loads `processed_sample.png` (saved from app) + displays FPS/resolution.

---

## Setup Instructions

### 1. Prerequisites

- **Android Studio** (2023.1.1+)
- **NDK** (r25c or later)
- **CMake** (3.22+)
- **OpenCV Android SDK** (4.8.0+)
- **Node.js** (for web)

---

### 2. Install OpenCV

1. Download: https://opencv.org/releases/
2. Extract `opencv-4.8.0-android-sdk.zip`
3. Copy `OpenCV-android-sdk` → `jni/opencv/`

---

### 3. Configure NDK & CMake

```gradle
android {
    ndkVersion "25.2.9519653"
    externalNativeBuild {
        cmake {
            path "jni/CMakeLists.txt"
        }
    }
}

4. Build & Run

Open in Android Studio
Sync Gradle
Build → Run on device (API 24+)
Grant camera permission

5. Web Viewer
bashcd web
npm install
npx tsc
npx live-server dist

Bonus Features

Toggle button: Raw ↔ Edge
Real-time FPS counter
OpenGL shader support (grayscale fallback)
Sample processed image saved to web/processed_sample.png