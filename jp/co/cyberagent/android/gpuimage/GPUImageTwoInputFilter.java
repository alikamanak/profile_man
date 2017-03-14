package jp.co.cyberagent.android.gpuimage;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

public class GPUImageTwoInputFilter extends GPUImageFilter {
    private static final String VERTEX_SHADER = "attribute vec4 position;\nattribute vec4 inputTextureCoordinate;\nattribute vec4 inputTextureCoordinate2;\n \nvarying vec2 textureCoordinate;\nvarying vec2 textureCoordinate2;\n \nvoid main()\n{\n    gl_Position = position;\n    textureCoordinate = inputTextureCoordinate.xy;\n    textureCoordinate2 = inputTextureCoordinate2.xy;\n}";
    public int filterInputTextureUniform2;
    public int filterSecondTextureCoordinateAttribute;
    public int filterSourceTexture2;
    private Bitmap mBitmap;
    private ByteBuffer mTexture2CoordinatesBuffer;

    /* renamed from: jp.co.cyberagent.android.gpuimage.GPUImageTwoInputFilter.1 */
    class C05531 implements Runnable {
        private final /* synthetic */ Bitmap val$bitmap;

        C05531(Bitmap bitmap) {
            this.val$bitmap = bitmap;
        }

        public void run() {
            if (GPUImageTwoInputFilter.this.filterSourceTexture2 == -1) {
                GLES20.glActiveTexture(33987);
                GPUImageTwoInputFilter.this.filterSourceTexture2 = OpenGlUtils.loadTexture(this.val$bitmap, -1, false);
            }
        }
    }

    public GPUImageTwoInputFilter(String fragmentShader) {
        this(VERTEX_SHADER, fragmentShader);
    }

    public GPUImageTwoInputFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        this.filterSourceTexture2 = -1;
        setRotation(Rotation.NORMAL, false, false);
    }

    public void onInit() {
        super.onInit();
        this.filterSecondTextureCoordinateAttribute = GLES20.glGetAttribLocation(getProgram(), "inputTextureCoordinate2");
        this.filterInputTextureUniform2 = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture2");
        GLES20.glEnableVertexAttribArray(this.filterSecondTextureCoordinateAttribute);
        if (this.mBitmap != null) {
            setBitmap(this.mBitmap);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        runOnDraw(new C05531(bitmap));
    }

    public void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(1, new int[]{this.filterSourceTexture2}, 0);
        this.filterSourceTexture2 = -1;
    }

    protected void onDrawArraysPre() {
        GLES20.glEnableVertexAttribArray(this.filterSecondTextureCoordinateAttribute);
        GLES20.glActiveTexture(33987);
        GLES20.glBindTexture(3553, this.filterSourceTexture2);
        GLES20.glUniform1i(this.filterInputTextureUniform2, 3);
        this.mTexture2CoordinatesBuffer.position(0);
        GLES20.glVertexAttribPointer(this.filterSecondTextureCoordinateAttribute, 2, 5126, false, 0, this.mTexture2CoordinatesBuffer);
    }

    public void setRotation(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        float[] buffer = TextureRotationUtil.getRotation(rotation, flipHorizontal, flipVertical);
        ByteBuffer bBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder());
        FloatBuffer fBuffer = bBuffer.asFloatBuffer();
        fBuffer.put(buffer);
        fBuffer.flip();
        this.mTexture2CoordinatesBuffer = bBuffer;
    }
}
