package jp.co.cyberagent.android.gpuimage;

public class GPUImageSepiaFilter extends GPUImageColorMatrixFilter {
    public GPUImageSepiaFilter() {
        this(1.0f);
    }

    public GPUImageSepiaFilter(float intensity) {
        super(intensity, new float[]{0.3588f, 0.7044f, 0.1368f, 0.0f, 0.299f, 0.587f, 0.114f, 0.0f, 0.2392f, 0.4696f, 0.0912f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f});
    }
}
