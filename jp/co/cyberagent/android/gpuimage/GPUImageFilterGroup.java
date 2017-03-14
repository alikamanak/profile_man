package jp.co.cyberagent.android.gpuimage;

import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

public class GPUImageFilterGroup extends GPUImageFilter {
    private final List<GPUImageFilter> mFilters;
    private int[] mFrameBufferTextures;
    private int[] mFrameBuffers;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private final FloatBuffer mGLTextureFlipBuffer;

    public GPUImageFilterGroup(List<GPUImageFilter> filters) {
        this.mFilters = filters;
        this.mGLCubeBuffer = ByteBuffer.allocateDirect(GPUImageRenderer.CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mGLCubeBuffer.put(GPUImageRenderer.CUBE).position(0);
        this.mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mGLTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);
        float[] flipTexture = TextureRotationUtil.getRotation(Rotation.NORMAL, false, true);
        this.mGLTextureFlipBuffer = ByteBuffer.allocateDirect(flipTexture.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mGLTextureFlipBuffer.put(flipTexture).position(0);
    }

    public void onInit() {
        super.onInit();
        for (GPUImageFilter filter : this.mFilters) {
            filter.init();
        }
    }

    public void onDestroy() {
        destroyFramebuffers();
        for (GPUImageFilter filter : this.mFilters) {
            filter.destroy();
        }
        super.onDestroy();
    }

    private void destroyFramebuffers() {
        if (this.mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(this.mFrameBufferTextures.length, this.mFrameBufferTextures, 0);
            this.mFrameBufferTextures = null;
        }
        if (this.mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(this.mFrameBuffers.length, this.mFrameBuffers, 0);
            this.mFrameBuffers = null;
        }
    }

    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        if (this.mFrameBuffers != null) {
            destroyFramebuffers();
        }
        this.mFrameBuffers = new int[(this.mFilters.size() - 1)];
        this.mFrameBufferTextures = new int[(this.mFilters.size() - 1)];
        for (int i = 0; i < this.mFilters.size() - 1; i++) {
            ((GPUImageFilter) this.mFilters.get(i)).onOutputSizeChanged(width, height);
            GLES20.glGenFramebuffers(1, this.mFrameBuffers, i);
            GLES20.glGenTextures(1, this.mFrameBufferTextures, i);
            GLES20.glBindTexture(3553, this.mFrameBufferTextures[i]);
            GLES20.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5121, null);
            GLES20.glTexParameterf(3553, 10240, 9729.0f);
            GLES20.glTexParameterf(3553, 10241, 9729.0f);
            GLES20.glTexParameterf(3553, 10242, 33071.0f);
            GLES20.glTexParameterf(3553, 10243, 33071.0f);
            GLES20.glBindFramebuffer(36160, this.mFrameBuffers[i]);
            GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.mFrameBufferTextures[i], 0);
            GLES20.glBindTexture(3553, 0);
            GLES20.glBindFramebuffer(36160, 0);
        }
        ((GPUImageFilter) this.mFilters.get(this.mFilters.size() - 1)).onOutputSizeChanged(width, height);
    }

    public void onDraw(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        runPendingOnDrawTasks();
        if (isInitialized() && this.mFrameBuffers != null && this.mFrameBufferTextures != null) {
            int previousTexture = textureId;
            int i = 0;
            while (i < this.mFilters.size() - 1) {
                GPUImageFilter filter = (GPUImageFilter) this.mFilters.get(i);
                GLES20.glBindFramebuffer(36160, this.mFrameBuffers[i]);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                FloatBuffer floatBuffer = this.mGLCubeBuffer;
                FloatBuffer floatBuffer2 = (i == 0 && this.mFilters.size() % 2 == 0) ? this.mGLTextureFlipBuffer : this.mGLTextureBuffer;
                filter.onDraw(previousTexture, floatBuffer, floatBuffer2);
                GLES20.glBindFramebuffer(36160, 0);
                previousTexture = this.mFrameBufferTextures[i];
                i++;
            }
            ((GPUImageFilter) this.mFilters.get(this.mFilters.size() - 1)).onDraw(previousTexture, cubeBuffer, textureBuffer);
        }
    }

    public List<GPUImageFilter> getFilters() {
        return this.mFilters;
    }
}
