cmake_minimum_required(VERSION 3.10.2)

project(native-lib)

# Path to OpenCV-android-sdk (jni folder contains OpenCVConfig.cmake)
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv/OpenCV-android-sdk/sdk/native/jni)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# DO NOT use static C++ linking with OpenCV Android
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS}")

find_package(OpenCV REQUIRED)

include_directories(${OpenCV_INCLUDE_DIRS})

add_library(
        native-lib
        SHARED
        native-lib.cpp
)

target_link_libraries(
        native-lib
        ${OpenCV_LIBS}
        log
)
