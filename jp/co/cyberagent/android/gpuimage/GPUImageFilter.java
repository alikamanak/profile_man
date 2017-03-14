package jp.co.cyberagent.android.gpuimage;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import com.facebook.ads.BuildConfig;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Scanner;

public class GPUImageFilter {
    public static final String NO_FILTER_FRAGMENT_SHADER = "varying highp vec2 textureCoordinate;\n \nuniform sampler2D inputImageTexture;\n \nvoid main()\n{\n     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n}";
    public static final String NO_FILTER_VERTEX_SHADER = "attribute vec4 position;\nattribute vec4 inputTextureCoordinate;\n \nvarying vec2 textureCoordinate;\n \nvoid main()\n{\n    gl_Position = position;\n    textureCoordinate = inputTextureCoordinate.xy;\n}";
    private final String mFragmentShader;
    protected int mGLAttribPosition;
    protected int mGLAttribTextureCoordinate;
    protected int mGLProgId;
    protected int mGLUniformTexture;
    private boolean mIsInitialized;
    private int mOutputHeight;
    private int mOutputWidth;
    private final LinkedList<Runnable> mRunOnDraw;
    private final String mVertexShader;

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageFilter.1 */
    class C05371 implements Runnable {
        private final /* synthetic */ int val$intValue;
        private final /* synthetic */ int val$location;

        C05371(int i, int i2) {
            this.val$location = i;
            this.val$intValue = i2;
        }

        public void run() {
            GLES20.glUniform1i(this.val$location, this.val$intValue);
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageFilter.2 */
    class C05382 implements Runnable {
        private final /* synthetic */ float val$floatValue;
        private final /* synthetic */ int val$location;

        C05382(int i, float f) {
            this.val$location = i;
            this.val$floatValue = f;
        }

        public void run() {
            GLES20.glUniform1f(this.val$location, this.val$floatValue);
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageFilter.3 */
    class C05393 implements Runnable {
        private final /* synthetic */ float[] val$arrayValue;
        private final /* synthetic */ int val$location;

        C05393(int i, float[] fArr) {
            this.val$location = i;
            this.val$arrayValue = fArr;
        }

        public void run() {
            GLES20.glUniform2fv(this.val$location, 1, FloatBuffer.wrap(this.val$arrayValue));
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageFilter.4 */
    class C05404 implements Runnable {
        private final /* synthetic */ float[] val$arrayValue;
        private final /* synthetic */ int val$location;

        C05404(int i, float[] fArr) {
            this.val$location = i;
            this.val$arrayValue = fArr;
        }

        public void run() {
            GLES20.glUniform3fv(this.val$location, 1, FloatBuffer.wrap(this.val$arrayValue));
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageFilter.5 */
    class C05415 implements Runnable {
        private final /* synthetic */ float[] val$arrayValue;
        private final /* synthetic */ int val$location;

        C05415(int i, float[] fArr) {
            this.val$location = i;
            this.val$arrayValue = fArr;
        }

        public void run() {
            GLES20.glUniform4fv(this.val$location, 1, FloatBuffer.wrap(this.val$arrayValue));
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageFilter.6 */
    class C05426 implements Runnable {
        private final /* synthetic */ float[] val$arrayValue;
        private final /* synthetic */ int val$location;

        C05426(int i, float[] fArr) {
            this.val$location = i;
            this.val$arrayValue = fArr;
        }

        public void run() {
            GLES20.glUniform1fv(this.val$location, this.val$arrayValue.length, FloatBuffer.wrap(this.val$arrayValue));
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageFilter.7 */
    class C05437 implements Runnable {
        private final /* synthetic */ int val$location;
        private final /* synthetic */ PointF val$point;

        C05437(PointF pointF, int i) {
            this.val$point = pointF;
            this.val$location = i;
        }

        public void run() {
            GLES20.glUniform2fv(this.val$location, 1, new float[]{this.val$point.x, this.val$point.y}, 0);
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageFilter.8 */
    class C05448 implements Runnable {
        private final /* synthetic */ int val$location;
        private final /* synthetic */ float[] val$matrix;

        C05448(int i, float[] fArr) {
            this.val$location = i;
            this.val$matrix = fArr;
        }

        public void run() {
            GLES20.glUniformMatrix3fv(this.val$location, 1, false, this.val$matrix, 0);
        }
    }

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageFilter.9 */
    class C05459 implements Runnable {
        private final /* synthetic */ int val$location;
        private final /* synthetic */ float[] val$matrix;

        C05459(int i, float[] fArr) {
            this.val$location = i;
            this.val$matrix = fArr;
        }

        public void run() {
            GLES20.glUniformMatrix4fv(this.val$location, 1, false, this.val$matrix, 0);
        }
    }

    public GPUImageFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public GPUImageFilter(String vertexShader, String fragmentShader) {
        this.mRunOnDraw = new LinkedList();
        this.mVertexShader = vertexShader;
        this.mFragmentShader = fragmentShader;
    }

    public final void init() {
        onInit();
        this.mIsInitialized = true;
        onInitialized();
    }

    public void onInit() {
        this.mGLProgId = OpenGlUtils.loadProgram(this.mVertexShader, this.mFragmentShader);
        this.mGLAttribPosition = GLES20.glGetAttribLocation(this.mGLProgId, "position");
        this.mGLUniformTexture = GLES20.glGetUniformLocation(this.mGLProgId, "inputImageTexture");
        this.mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(this.mGLProgId, "inputTextureCoordinate");
        this.mIsInitialized = true;
    }

    public void onInitialized() {
    }

    public final void destroy() {
        this.mIsInitialized = false;
        GLES20.glDeleteProgram(this.mGLProgId);
        onDestroy();
    }

    public void onDestroy() {
    }

    public void onOutputSizeChanged(int width, int height) {
        this.mOutputWidth = width;
        this.mOutputHeight = height;
    }

    public void onDraw(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        GLES20.glUseProgram(this.mGLProgId);
        runPendingOnDrawTasks();
        if (this.mIsInitialized) {
            cubeBuffer.position(0);
            GLES20.glVertexAttribPointer(this.mGLAttribPosition, 2, 5126, false, 0, cubeBuffer);
            GLES20.glEnableVertexAttribArray(this.mGLAttribPosition);
            textureBuffer.position(0);
            GLES20.glVertexAttribPointer(this.mGLAttribTextureCoordinate, 2, 5126, false, 0, textureBuffer);
            GLES20.glEnableVertexAttribArray(this.mGLAttribTextureCoordinate);
            if (textureId != -1) {
                GLES20.glActiveTexture(33984);
                GLES20.glBindTexture(3553, textureId);
                GLES20.glUniform1i(this.mGLUniformTexture, 0);
            }
            onDrawArraysPre();
            GLES20.glDrawArrays(5, 0, 4);
            GLES20.glDisableVertexAttribArray(this.mGLAttribPosition);
            GLES20.glDisableVertexAttribArray(this.mGLAttribTextureCoordinate);
            GLES20.glBindTexture(3553, 0);
        }
    }

    protected void onDrawArraysPre() {
    }

    protected void runPendingOnDrawTasks() {
        while (!this.mRunOnDraw.isEmpty()) {
            ((Runnable) this.mRunOnDraw.removeFirst()).run();
        }
    }

    public boolean isInitialized() {
        return this.mIsInitialized;
    }

    public int getOutputWidth() {
        return this.mOutputWidth;
    }

    public int getOutputHeight() {
        return this.mOutputHeight;
    }

    public int getProgram() {
        return this.mGLProgId;
    }

    public int getAttribPosition() {
        return this.mGLAttribPosition;
    }

    public int getAttribTextureCoordinate() {
        return this.mGLAttribTextureCoordinate;
    }

    public int getUniformTexture() {
        return this.mGLUniformTexture;
    }

    protected void setInteger(int location, int intValue) {
        runOnDraw(new C05371(location, intValue));
    }

    protected void setFloat(int location, float floatValue) {
        runOnDraw(new C05382(location, floatValue));
    }

    protected void setFloatVec2(int location, float[] arrayValue) {
        runOnDraw(new C05393(location, arrayValue));
    }

    protected void setFloatVec3(int location, float[] arrayValue) {
        runOnDraw(new C05404(location, arrayValue));
    }

    protected void setFloatVec4(int location, float[] arrayValue) {
        runOnDraw(new C05415(location, arrayValue));
    }

    protected void setFloatArray(int location, float[] arrayValue) {
        runOnDraw(new C05426(location, arrayValue));
    }

    protected void setPoint(int location, PointF point) {
        runOnDraw(new C05437(point, location));
    }

    protected void setUniformMatrix3f(int location, float[] matrix) {
        runOnDraw(new C05448(location, matrix));
    }

    protected void setUniformMatrix4f(int location, float[] matrix) {
        runOnDraw(new C05459(location, matrix));
    }

    protected void runOnDraw(Runnable runnable) {
        synchronized (this.mRunOnDraw) {
            this.mRunOnDraw.addLast(runnable);
        }
    }

    public static String loadShader(String file, Context context) {
        try {
            InputStream ims = context.getAssets().open(file);
            String re = convertStreamToString(ims);
            ims.close();
            return re;
        } catch (Exception e) {
            e.printStackTrace();
            return BuildConfig.FLAVOR;
        }
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : BuildConfig.FLAVOR;
    }
}
