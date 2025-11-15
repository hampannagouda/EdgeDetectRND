"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.FrameViewer = void 0;
// Modular class for handling image display and stats overlay
class FrameViewer {
    constructor(imageElementId, statsElementId, base64Image) {
        this.imageElement = document.getElementById(imageElementId);
        this.statsElement = document.getElementById(statsElementId);
        this.base64Image = base64Image;
        this.loadImage();
        this.updateStats(15, '640x480'); // Hardcoded demo values (FPS, resolution)
    }
    loadImage() {
        this.imageElement.src = this.base64Image;
        this.imageElement.onload = () => {
            console.log('Processed frame loaded successfully.');
        };
    }
    updateStats(fps, resolution) {
        const statsText = `FPS: ${fps} | Resolution: ${resolution}`;
        this.statsElement.textContent = statsText;
        console.log(`Updated stats: ${statsText}`);
    }
    // Optional: Method to update with new frame (for future WebSocket integration)
    updateFrame(newBase64, newFps, newRes) {
        this.base64Image = newBase64;
        this.imageElement.src = newBase64;
        this.updateStats(newFps, newRes);
    }
}
exports.FrameViewer = FrameViewer;
