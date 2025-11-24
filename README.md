EdgeDetectRND — Real-Time Edge Detection Framework (Android · OpenCV · OpenGL ES · Web)

A high-performance real-time computer vision pipeline combining Android Camera2, C++/JNI, OpenCV, and OpenGL ES 2.0.
Includes a lightweight TypeScript web viewer to demonstrate multi-platform rendering of processed frames.

This project was built as part of an R&D Engineering Assignment, showcasing expertise in low-level rendering, native performance optimization, and cross-platform visualization.

✨ Why This Project Stands Out

✔ Zero-copy camera pipeline using SurfaceTexture
✔ Native NV21 → RGBA conversion with OpenCV
✔ Real-time Canny Edge Detection (OpenCV C++)
✔ GPU-accelerated rendering through OpenGL ES 2.0
✔ JNI bridging with clean architecture
✔ Raw ⇄ Edge mode toggle
✔ Native FPS tracking
✔ TypeScript web viewer that renders processed frames
✔ Production-grade structure, ready for future enhancements

🚀 Live Architecture Overview
Android Camera2 (YUV_420_888)
        ↓
ImageReader → Java (CameraRenderer)
        ↓ JNI Bridge (byte* → Mat)
C++ Native Layer (OpenCV)
        • NV21 → RGBA
        • Canny Edge Detection
        • FPS Calculation
        ↓
OpenGL ES 2.0 Renderer
        ↓
TextureView → Live Display


Web Viewer Flow:

static sample / base64 frame
        ↓
TypeScript (viewer.ts)
        ↓
Canvas / <img> rendering

📸 Preview

(Add your screenshot or GIF here to showcase the output)

Example placeholder:

screenshots/demo.png

🧩 Project Structure
EdgeDetectRND/
 ├── app/                     # Android Java (Camera, GL, UI)
 ├── jni/
 │    ├── native-lib.cpp      # C++ | OpenCV | OpenGL ES pipeline
 │    └── CMakeLists.txt
 ├── web/
 │    ├── src/                # TypeScript viewer
 │    ├── dist/
 │    └── package.json        # Web build scripts
 ├── screenshots/
 ├── README.md
 └── .gitignore

🛠️ Tech Stack
Android

Camera2 API

TextureView + SurfaceTexture

OpenGL ES 2.0

JNI + Native C++

OpenCV 4.x

Web

TypeScript

ES Modules

Lightweight static viewer

Canvas / DOM rendering

⚙️ Setup Instructions
1️⃣ Prerequisites

Android Studio Hedgehog / Iguana or newer

NDK r25c+

CMake 3.22+

OpenCV Android SDK 4.8.0+

Node.js LTS (for web viewer)

2️⃣ Install OpenCV

Download OpenCV Android SDK:
🔗 https://opencv.org/releases/

Extract and place:

jni/opencv/OpenCV-android-sdk/

3️⃣ Build & Run Android App

Open project in Android Studio

Sync Gradle

Connect a device (API 24+)

Run → Grant camera permission

You should now see a real-time processed camera feed with an Edge / Raw toggle and FPS counter.

4️⃣ Web Viewer Setup
cd web
npm install
npm run build
npm run start


Then open:

http://localhost:3000


This loads a dummy processed frame (static image), proving cross-platform renderability.

🧠 Deep-Dive: Native Processing Flow
1. Frame Acquisition

Camera frames arrive as YUV_420_888.
We convert to NV21 respecting row + pixel stride.

2. JNI Transfer

NV21 byte array → C++ via processFrame().

3. OpenCV Processing

cvtColor(NV21 → RGBA)

optional Canny(gray) edge mask

merge back to RGBA for GL

4. OpenGL ES Texture Update

Updated RGBA buffer is pushed via:

glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, rgbaMat.data);


Rendering handled in GLTextureRenderer.

5. FPS Callback

C++ computes FPS and invokes Java callback via JNI global reference.

🔮 Future Enhancements (Optional)

WebSocket live streaming to browser

PBO-based async texture upload

GPU-based Canny (compute shaders)

Flutter/WebAssembly viewer

AI-powered real-time segmentation


🤝 Contributions

PRs, suggestions, and improvements are welcome!

🏁 Final Note

This repository demonstrates mastery of:

Android low-level camera APIs

Native C++ development

OpenCV image processing

GPU rendering

Cross-platform architecture

Clean engineering practices

Use this as a strong showcase of your technical depth.

Thank You!.