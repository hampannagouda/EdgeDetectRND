# EdgeDetectRND - Real-Time Canny Edge Detection

## Features
- Camera2 + TextureView
- JNI + OpenCV C++ Canny
- OpenGL ES 2.0 texture render
- Toggle: Raw / Edge
- FPS Counter
- TypeScript Web Viewer

## Setup
1. Extract OpenCV → `jni/opencv/OpenCV-android-sdk/`
2. Open in VS Code
3. `Ctrl+Shift+B` → **Build Android**
4. Run on device
5. Save frame:
   ```bash
   adb pull /sdcard/edge.png web/processed_sample.png