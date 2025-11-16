cmake_minimum_required(VERSION 3.18.1)
project("edgedetectrnd")

# OpenCV path (modify to your folder)
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv/OpenCV-android-sdk/sdk/native/jni)

find_package(OpenCV REQUIRED core imgproc)

add_library(native-lib SHARED native-lib.cpp)

find_library(log-lib log)
find_library(GLESv2-lib GLESv2)

target_link_libraries(
        native-lib
        ${OpenCV_LIBS}
        ${GLESv2-lib}
        ${log-lib}
)
