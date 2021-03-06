package jp.co.cyberagent.android.gpuimage;

import android.opengl.GLES20;

public class GPUImageLomoFilter extends GPUImageFilter {
    public static final String LOMO_FRAGMENT_SHADER = " precision mediump float;varying highp vec2 textureCoordinate;\n \n uniform sampler2D inputImageTexture;\n uniform sampler2D inputImageTexture2;\n uniform sampler2D inputImageTexture3;\n \n void main()\n {\n     \n     vec3 texel = texture2D(inputImageTexture, textureCoordinate).rgb;\n     \n     vec2 red = vec2(texel.r, 0.16666);\n     vec2 green = vec2(texel.g, 0.5);\n     vec2 blue = vec2(texel.b, 0.83333);\n     \n     texel.rgb = vec3(\n                      texture2D(inputImageTexture2, red).r,\n                      texture2D(inputImageTexture2, green).g,\n                      texture2D(inputImageTexture2, blue).b);\n     \n     vec2 tc = (2.0 * textureCoordinate) - 1.0;\n     float d = dot(tc, tc);\n     vec2 lookup = vec2(d, texel.r);\n     texel.r = texture2D(inputImageTexture3, lookup).r;\n     lookup.y = texel.g;\n     texel.g = texture2D(inputImageTexture3, lookup).g;\n     lookup.y = texel.b;\n     texel.b\t= texture2D(inputImageTexture3, lookup).b;\n     \n     gl_FragColor = vec4(texel,1.0);\n }";
    protected int mGLUniformTexture2;
    protected int mGLUniformTexture3;

    public GPUImageLomoFilter() {
        super(GPUImageFilter.NO_FILTER_VERTEX_SHADER, LOMO_FRAGMENT_SHADER);
    }

    public void onInit() {
        super.onInit();
        this.mGLUniformTexture2 = GLES20.glGetUniformLocation(this.mGLProgId, "inputImageTexture2");
        this.mGLUniformTexture3 = GLES20.glGetUniformLocation(this.mGLProgId, "inputImageTexture3");
    }

    protected void onDrawArraysPre() {
        GLES20.glUniform1i(this.mGLUniformTexture2, 3);
        GLES20.glUniform1i(this.mGLUniformTexture3, 3);
    }
}
