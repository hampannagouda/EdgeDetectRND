// Main entry point: Initialize the viewer with dummy data
import { FrameViewer } from './viewer';

// Dummy base64 for a static processed frame (simple grayscale placeholder; replace with actual edge-detected base64 from Android)
const dummyBase64Image: string = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=='; // Tiny 1x1 grayscale pixel for demo

document.addEventListener('DOMContentLoaded', () => {
    const viewer = new FrameViewer('processedFrame', 'stats', dummyBase64Image);
    console.log('Edge Detection Web Viewer initialized.');
});