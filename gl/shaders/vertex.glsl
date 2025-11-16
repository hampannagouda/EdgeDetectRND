// Path: EdgeDetectRND/gl/shaders/vertex.glsl

attribute vec4 aPosition;  // Vertex position
attribute vec2 aTexCoord;  // Texture coordinates
varying vec2 vTexCoord;    // Pass to fragment shader

void main() {
    gl_Position = aPosition;  // Set vertex position
    vTexCoord = aTexCoord;    // Pass texture coord
}