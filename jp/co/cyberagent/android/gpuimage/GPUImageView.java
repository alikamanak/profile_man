package jp.co.cyberagent.android.gpuimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import java.io.File;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;

public class GPUImageView extends GLSurfaceView {
    private GPUImageFilter mFilter;
    private GPUImage mGPUImage;
    private float mRatio;

    public GPUImageView(Context context) {
        super(context);
        this.mRatio = 0.0f;
        init();
    }

    public GPUImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRatio = 0.0f;
        init();
    }

    private void init() {
        this.mGPUImage = new GPUImage(getContext());
        this.mGPUImage.setGLSurfaceView(this);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mRatio == 0.0f) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int newWidth;
        int newHeight;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (((float) width) / this.mRatio < ((float) height)) {
            newWidth = width;
            newHeight = Math.round(((float) width) / this.mRatio);
        } else {
            newHeight = height;
            newWidth = Math.round(((float) height) * this.mRatio);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(newWidth, 1073741824), MeasureSpec.makeMeasureSpec(newHeight, 1073741824));
    }

    public void setRatio(float ratio) {
        this.mRatio = ratio;
        requestLayout();
        this.mGPUImage.deleteImage();
    }

    public void setFilter(GPUImageFilter filter) {
        this.mFilter = filter;
        this.mGPUImage.setFilter(filter);
        requestRender();
    }

    public GPUImageFilter getFilter() {
        return this.mFilter;
    }

    public void setImage(Bitmap bitmap) {
        this.mGPUImage.setImage(bitmap);
    }

    public void setImage(Uri uri) {
        this.mGPUImage.setImage(uri);
    }

    public void setImage(File file) {
        this.mGPUImage.setImage(file);
    }

    public void saveToPictures(String folderName, String fileName, OnPictureSavedListener listener) {
        this.mGPUImage.saveToPictures(folderName, fileName, listener);
    }
}
