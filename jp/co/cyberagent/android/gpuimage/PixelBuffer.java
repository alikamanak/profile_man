package jp.co.cyberagent.android.gpuimage;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import java.nio.IntBuffer;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class PixelBuffer {
    static final boolean LIST_CONFIGS = false;
    static final String TAG = "PixelBuffer";
    Bitmap mBitmap;
    EGL10 mEGL;
    EGLConfig mEGLConfig;
    EGLConfig[] mEGLConfigs;
    EGLContext mEGLContext;
    EGLDisplay mEGLDisplay;
    EGLSurface mEGLSurface;
    GL10 mGL;
    int mHeight;
    Renderer mRenderer;
    String mThreadOwner;
    int mWidth;

    public PixelBuffer(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        version = new int[2];
        int[] attribList = new int[]{12375, this.mWidth, 12374, this.mHeight, 12344};
        this.mEGL = (EGL10) EGLContext.getEGL();
        this.mEGLDisplay = this.mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        this.mEGL.eglInitialize(this.mEGLDisplay, version);
        this.mEGLConfig = chooseConfig();
        this.mEGLContext = this.mEGL.eglCreateContext(this.mEGLDisplay, this.mEGLConfig, EGL10.EGL_NO_CONTEXT, new int[]{12440, 2, 12344});
        this.mEGLSurface = this.mEGL.eglCreatePbufferSurface(this.mEGLDisplay, this.mEGLConfig, attribList);
        this.mEGL.eglMakeCurrent(this.mEGLDisplay, this.mEGLSurface, this.mEGLSurface, this.mEGLContext);
        this.mGL = (GL10) this.mEGLContext.getGL();
        this.mThreadOwner = Thread.currentThread().getName();
    }

    public void setRenderer(Renderer renderer) {
        this.mRenderer = renderer;
        if (Thread.currentThread().getName().equals(this.mThreadOwner)) {
            this.mRenderer.onSurfaceCreated(this.mGL, this.mEGLConfig);
            this.mRenderer.onSurfaceChanged(this.mGL, this.mWidth, this.mHeight);
            return;
        }
        Log.e(TAG, "setRenderer: This thread does not own the OpenGL context.");
    }

    public Bitmap getBitmap() {
        if (this.mRenderer == null) {
            Log.e(TAG, "getBitmap: Renderer was not set.");
            return null;
        } else if (Thread.currentThread().getName().equals(this.mThreadOwner)) {
            this.mRenderer.onDrawFrame(this.mGL);
            this.mRenderer.onDrawFrame(this.mGL);
            convertToBitmap();
            return this.mBitmap;
        } else {
            Log.e(TAG, "getBitmap: This thread does not own the OpenGL context.");
            return null;
        }
    }

    public void destroy() {
        this.mRenderer.onDrawFrame(this.mGL);
        this.mRenderer.onDrawFrame(this.mGL);
        this.mEGL.eglMakeCurrent(this.mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        this.mEGL.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
        this.mEGL.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
        this.mEGL.eglTerminate(this.mEGLDisplay);
    }

    private EGLConfig chooseConfig() {
        int[] attribList = new int[15];
        attribList[0] = 12325;
        attribList[2] = 12326;
        attribList[4] = 12324;
        attribList[5] = 8;
        attribList[6] = 12323;
        attribList[7] = 8;
        attribList[8] = 12322;
        attribList[9] = 8;
        attribList[10] = 12321;
        attribList[11] = 8;
        attribList[12] = 12352;
        attribList[13] = 4;
        attribList[14] = 12344;
        int[] numConfig = new int[1];
        this.mEGL.eglChooseConfig(this.mEGLDisplay, attribList, null, 0, numConfig);
        int configSize = numConfig[0];
        this.mEGLConfigs = new EGLConfig[configSize];
        this.mEGL.eglChooseConfig(this.mEGLDisplay, attribList, this.mEGLConfigs, configSize, numConfig);
        return this.mEGLConfigs[0];
    }

    private void listConfig() {
        Log.i(TAG, "Config List {");
        for (EGLConfig config : this.mEGLConfigs) {
            Log.i(TAG, "    <d,s,r,g,b,a> = <" + getConfigAttrib(config, 12325) + "," + getConfigAttrib(config, 12326) + "," + getConfigAttrib(config, 12324) + "," + getConfigAttrib(config, 12323) + "," + getConfigAttrib(config, 12322) + "," + getConfigAttrib(config, 12321) + ">");
        }
        Log.i(TAG, "}");
    }

    private int getConfigAttrib(EGLConfig config, int attribute) {
        int[] value = new int[1];
        if (this.mEGL.eglGetConfigAttrib(this.mEGLDisplay, config, attribute, value)) {
            return value[0];
        }
        return 0;
    }

    private void convertToBitmap() {
        IntBuffer ib = IntBuffer.allocate(this.mWidth * this.mHeight);
        IntBuffer ibt = IntBuffer.allocate(this.mWidth * this.mHeight);
        this.mGL.glReadPixels(0, 0, this.mWidth, this.mHeight, 6408, 5121, ib);
        for (int i = 0; i < this.mHeight; i++) {
            for (int j = 0; j < this.mWidth; j++) {
                ibt.put((((this.mHeight - i) - 1) * this.mWidth) + j, ib.get((this.mWidth * i) + j));
            }
        }
        this.mBitmap = Bitmap.createBitmap(this.mWidth, this.mHeight, Config.ARGB_8888);
        this.mBitmap.copyPixelsFromBuffer(ibt);
    }
}
