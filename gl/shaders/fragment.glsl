// Path: EdgeDetectRND/gl/shaders/fragment.glsl

precision mediump float;   // Medium precision for mobile
varying vec2 vTexCoord;   // From vertex shader
uniform sampler2D sTexture; // Input texture (from OpenCV RGBA)

void main() {
    gl_FragColor = texture2D(sTexture, vTexCoord);  // Sample and output color
}