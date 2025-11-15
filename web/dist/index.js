"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
// Main entry point: Initialize the viewer with dummy data
const viewer_1 = require("./viewer");
// Dummy base64 for a static processed frame (simple grayscale placeholder; replace with actual edge-detected base64 from Android)
const dummyBase64Image = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=='; // Tiny 1x1 grayscale pixel for demo
document.addEventListener('DOMContentLoaded', () => {
    const viewer = new viewer_1.FrameViewer('processedFrame', 'stats', dummyBase64Image);
    console.log('Edge Detection Web Viewer initialized.');
});
