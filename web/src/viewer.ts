// Modular class for handling image display and stats overlay
export class FrameViewer {
    private imageElement: HTMLImageElement;
    private statsElement: HTMLElement;
    private base64Image: string;

    constructor(imageElementId: string, statsElementId: string, base64Image: string) {
        this.imageElement = document.getElementById(imageElementId) as HTMLImageElement;
        this.statsElement = document.getElementById(statsElementId) as HTMLElement;
        this.base64Image = base64Image;
        this.loadImage();
        this.updateStats(15, '640x480'); // Hardcoded demo values (FPS, resolution)
    }

    private loadImage(): void {
        this.imageElement.src = this.base64Image;
        this.imageElement.onload = () => {
            console.log('Processed frame loaded successfully.');
        };
    }

    updateStats(fps: number, resolution: string): void {
        const statsText = `FPS: ${fps} | Resolution: ${resolution}`;
        this.statsElement.textContent = statsText;
        console.log(`Updated stats: ${statsText}`);
    }

    // Optional: Method to update with new frame (for future WebSocket integration)
    updateFrame(newBase64: string, newFps: number, newRes: string): void {
        this.base64Image = newBase64;
        this.imageElement.src = newBase64;
        this.updateStats(newFps, newRes);
    }
}