package jp.co.cyberagent.android.gpuimage;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import jp.co.cyberagent.android.gpuimage.GPUImage.ScaleType;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

@TargetApi(11)
public class GPUImageRenderer implements Renderer, PreviewCallback {
    static final float[] CUBE;
    public static final int NO_IMAGE = -1;
    private int mAddedPadding;
    private GPUImageFilter mFilter;
    private boolean mFlipHorizontal;
    private boolean mFlipVertical;
    private final FloatBuffer mGLCubeBuffer;
    private IntBuffer mGLRgbBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private int mGLTextureId;
    private int mImageHeight;
    private int mImageWidth;
    private int mOutputHeight;
    private int mOutputWidth;
    private Rotation mRotation;
    private final Queue<Runnable> mRunOnDraw;
    private ScaleType mScaleType;
    public final Object mSurfaceChangedWaiter;
    private SurfaceTexture mSurfaceTexture;

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageRenderer.1 */
    class C05461 implements Runnable {
        private final /* synthetic */ Camera val$camera;
        private final /* synthetic */ byte[] val$data;
        private final /* synthetic */ Size val$previewSize;

        C05461(byte[] bArr, Size size, Camera camera) {
            this.val$data = bArr;
            this.val$previewSize = size;
            this.val$camera = camera;
        }

        public void run() {
            GPUImageNativeLibrary.YUVtoRBGA(this.val$data, this.val$previewSize.width, this.val$previewSize.height, GPUImageRenderer.this.mGLRgbBuffer.array());
            GPUImageRenderer.this.mGLTextureId = OpenGlUtils.loadTexture(GPUImageRenderer.this.mGLRgbBuffer, this.val$previewSize, GPUImageRenderer.this.mGLTextureId);
            this.val$camera.addCallbackBuffer(this.val$data);
            if (GPUImageRenderer.this.mImageWidth != this.val$previewSize.width) {
                GPUImageRenderer.this.mImageWidth = this.val$previewSize.width;
                GPUImageRenderer.this.mImageHeight = this.val$previewSize.height;
                GPUImageRenderer.this.adjustImageScaling();
            }
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageRenderer.2 */
    class C05472 implements Runnable {
        private final /* synthetic */ Camera val$camera;

        C05472(Camera camera) {
            this.val$camera = camera;
        }

        public void run() {
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            GPUImageRenderer.this.mSurfaceTexture = new SurfaceTexture(textures[0]);
            try {
                this.val$camera.setPreviewTexture(GPUImageRenderer.this.mSurfaceTexture);
                this.val$camera.setPreviewCallback(GPUImageRenderer.this);
                this.val$camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageRenderer.3 */
    class C05483 implements Runnable {
        private final /* synthetic */ GPUImageFilter val$filter;

        C05483(GPUImageFilter gPUImageFilter) {
            this.val$filter = gPUImageFilter;
        }

        public void run() {
            GPUImageFilter oldFilter = GPUImageRenderer.this.mFilter;
            GPUImageRenderer.this.mFilter = this.val$filter;
            if (oldFilter != null) {
                oldFilter.destroy();
            }
            GPUImageRenderer.this.mFilter.init();
            GLES20.glUseProgram(GPUImageRenderer.this.mFilter.getProgram());
            GPUImageRenderer.this.mFilter.onOutputSizeChanged(GPUImageRenderer.this.mOutputWidth, GPUImageRenderer.this.mOutputHeight);
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageRenderer.4 */
    class C05494 implements Runnable {
        C05494() {
        }

        public void run() {
            GLES20.glDeleteTextures(1, new int[]{GPUImageRenderer.this.mGLTextureId}, 0);
            GPUImageRenderer.this.mGLTextureId = GPUImageRenderer.NO_IMAGE;
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageRenderer.5 */
    class C05505 implements Runnable {
        private final /* synthetic */ Bitmap val$bitmap;
        private final /* synthetic */ boolean val$recycle;

        C05505(Bitmap bitmap, boolean z) {
            this.val$bitmap = bitmap;
            this.val$recycle = z;
        }

        public void run() {
            Bitmap resizedBitmap = null;
            if (this.val$bitmap.getWidth() % 2 == 1) {
                resizedBitmap = Bitmap.createBitmap(this.val$bitmap.getWidth() + 1, this.val$bitmap.getHeight(), Config.ARGB_8888);
                Canvas can = new Canvas(resizedBitmap);
                can.drawARGB(0, 0, 0, 0);
                can.drawBitmap(this.val$bitmap, 0.0f, 0.0f, null);
                GPUImageRenderer.this.mAddedPadding = 1;
            } else {
                GPUImageRenderer.this.mAddedPadding = 0;
            }
            GPUImageRenderer.this.mGLTextureId = OpenGlUtils.loadTexture(resizedBitmap != null ? resizedBitmap : this.val$bitmap, GPUImageRenderer.this.mGLTextureId, this.val$recycle);
            if (resizedBitmap != null) {
                resizedBitmap.recycle();
            }
            GPUImageRenderer.this.mImageWidth = this.val$bitmap.getWidth();
            GPUImageRenderer.this.mImageHeight = this.val$bitmap.getHeight();
            GPUImageRenderer.this.adjustImageScaling();
        }
    }

    static {
        CUBE = new float[]{GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, 1.0f, GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, 1.0f, 1.0f, 1.0f};
    }

    public GPUImageRenderer(GPUImageFilter filter) {
        this.mSurfaceChangedWaiter = new Object();
        this.mGLTextureId = NO_IMAGE;
        this.mSurfaceTexture = null;
        this.mScaleType = ScaleType.CENTER_CROP;
        this.mFilter = filter;
        this.mRunOnDraw = new LinkedList();
        this.mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mGLCubeBuffer.put(CUBE).position(0);
        this.mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        setRotation(Rotation.NORMAL, false, false);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDisable(2929);
        this.mFilter.init();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.mOutputWidth = width;
        this.mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(this.mFilter.getProgram());
        this.mFilter.onOutputSizeChanged(width, height);
        synchronized (this.mSurfaceChangedWaiter) {
            this.mSurfaceChangedWaiter.notifyAll();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 r5) {
        /*
        r4 = this;
        r0 = 16640; // 0x4100 float:2.3318E-41 double:8.2213E-320;
        android.opengl.GLES20.glClear(r0);
        r1 = r4.mRunOnDraw;
        monitor-enter(r1);
    L_0x0008:
        r0 = r4.mRunOnDraw;	 Catch:{ all -> 0x0032 }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x0032 }
        if (r0 == 0) goto L_0x0026;
    L_0x0010:
        monitor-exit(r1);	 Catch:{ all -> 0x0032 }
        r0 = r4.mFilter;
        r1 = r4.mGLTextureId;
        r2 = r4.mGLCubeBuffer;
        r3 = r4.mGLTextureBuffer;
        r0.onDraw(r1, r2, r3);
        r0 = r4.mSurfaceTexture;
        if (r0 == 0) goto L_0x0025;
    L_0x0020:
        r0 = r4.mSurfaceTexture;
        r0.updateTexImage();
    L_0x0025:
        return;
    L_0x0026:
        r0 = r4.mRunOnDraw;	 Catch:{ all -> 0x0032 }
        r0 = r0.poll();	 Catch:{ all -> 0x0032 }
        r0 = (java.lang.Runnable) r0;	 Catch:{ all -> 0x0032 }
        r0.run();	 Catch:{ all -> 0x0032 }
        goto L_0x0008;
    L_0x0032:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0032 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: jp.co.cyberagent.android.gpuimage.GPUImageRenderer.onDrawFrame(javax.microedition.khronos.opengles.GL10):void");
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        Size previewSize = camera.getParameters().getPreviewSize();
        if (this.mGLRgbBuffer == null) {
            this.mGLRgbBuffer = IntBuffer.allocate(previewSize.width * previewSize.height);
        }
        if (this.mRunOnDraw.isEmpty()) {
            runOnDraw(new C05461(data, previewSize, camera));
        }
    }

    public void setUpSurfaceTexture(Camera camera) {
        runOnDraw(new C05472(camera));
    }

    public void setFilter(GPUImageFilter filter) {
        runOnDraw(new C05483(filter));
    }

    public void deleteImage() {
        runOnDraw(new C05494());
    }

    public void setImageBitmap(Bitmap bitmap) {
        setImageBitmap(bitmap, true);
    }

    public void setImageBitmap(Bitmap bitmap, boolean recycle) {
        if (bitmap != null) {
            runOnDraw(new C05505(bitmap, recycle));
        }
    }

    public void setScaleType(ScaleType scaleType) {
        this.mScaleType = scaleType;
    }

    protected int getFrameWidth() {
        return this.mOutputWidth;
    }

    protected int getFrameHeight() {
        return this.mOutputHeight;
    }

    private void adjustImageScaling() {
        float outputWidth = (float) this.mOutputWidth;
        float outputHeight = (float) this.mOutputHeight;
        if (this.mRotation == Rotation.ROTATION_270 || this.mRotation == Rotation.ROTATION_90) {
            outputWidth = (float) this.mOutputHeight;
            outputHeight = (float) this.mOutputWidth;
        }
        float ratioMin = Math.min(outputWidth / ((float) this.mImageWidth), outputHeight / ((float) this.mImageHeight));
        this.mImageWidth = Math.round(((float) this.mImageWidth) * ratioMin);
        this.mImageHeight = Math.round(((float) this.mImageHeight) * ratioMin);
        float ratioWidth = 1.0f;
        float ratioHeight = 1.0f;
        if (((float) this.mImageWidth) != outputWidth) {
            ratioWidth = ((float) this.mImageWidth) / outputWidth;
        } else if (((float) this.mImageHeight) != outputHeight) {
            ratioHeight = ((float) this.mImageHeight) / outputHeight;
        }
        float[] cube = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(this.mRotation, this.mFlipHorizontal, this.mFlipVertical);
        if (this.mScaleType == ScaleType.CENTER_CROP) {
            float distHorizontal = ((1.0f / ratioWidth) - 1.0f) / 2.0f;
            float distVertical = ((1.0f / ratioHeight) - 1.0f) / 2.0f;
            textureCords = new float[]{addDistance(textureCords[0], distVertical), addDistance(textureCords[1], distHorizontal), addDistance(textureCords[2], distVertical), addDistance(textureCords[3], distHorizontal), addDistance(textureCords[4], distVertical), addDistance(textureCords[5], distHorizontal), addDistance(textureCords[6], distVertical), addDistance(textureCords[7], distHorizontal)};
        } else {
            cube = new float[]{CUBE[0] * ratioWidth, CUBE[1] * ratioHeight, CUBE[2] * ratioWidth, CUBE[3] * ratioHeight, CUBE[4] * ratioWidth, CUBE[5] * ratioHeight, CUBE[6] * ratioWidth, CUBE[7] * ratioHeight};
        }
        this.mGLCubeBuffer.clear();
        this.mGLCubeBuffer.put(cube).position(0);
        this.mGLTextureBuffer.clear();
        this.mGLTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1.0f - distance;
    }

    public void setRotationCamera(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        setRotation(rotation, flipVertical, flipHorizontal);
    }

    public void setRotation(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        this.mRotation = rotation;
        this.mFlipHorizontal = flipHorizontal;
        this.mFlipVertical = flipVertical;
        adjustImageScaling();
    }

    public Rotation getRotation() {
        return this.mRotation;
    }

    public boolean isFlippedHorizontally() {
        return this.mFlipHorizontal;
    }

    public boolean isFlippedVertically() {
        return this.mFlipVertical;
    }

    protected void runOnDraw(Runnable runnable) {
        synchronized (this.mRunOnDraw) {
            this.mRunOnDraw.add(runnable);
        }
    }
}
