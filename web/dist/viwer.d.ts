export declare class FrameViewer {
    private imageElement;
    private statsElement;
    private base64Image;
    constructor(imageElementId: string, statsElementId: string, base64Image: string);
    private loadImage;
    updateStats(fps: number, resolution: string): void;
    updateFrame(newBase64: string, newFps: number, newRes: string): void;
}
