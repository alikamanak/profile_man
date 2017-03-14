package jp.co.cyberagent.android.gpuimage.util;

import com.google.android.gms.location.DetectedActivity;
import jp.co.cyberagent.android.gpuimage.Rotation;

public class TextureRotationUtil {
    private static /* synthetic */ int[] $SWITCH_TABLE$jp$co$cyberagent$android$gpuimage$Rotation;
    public static final float[] TEXTURE_NO_ROTATION;
    public static final float[] TEXTURE_ROTATED_180;
    public static final float[] TEXTURE_ROTATED_270;
    public static final float[] TEXTURE_ROTATED_90;

    static /* synthetic */ int[] $SWITCH_TABLE$jp$co$cyberagent$android$gpuimage$Rotation() {
        int[] iArr = $SWITCH_TABLE$jp$co$cyberagent$android$gpuimage$Rotation;
        if (iArr == null) {
            iArr = new int[Rotation.values().length];
            try {
                iArr[Rotation.NORMAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Rotation.ROTATION_180.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Rotation.ROTATION_270.ordinal()] = 4;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Rotation.ROTATION_90.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            $SWITCH_TABLE$jp$co$cyberagent$android$gpuimage$Rotation = iArr;
        }
        return iArr;
    }

    static {
        TEXTURE_NO_ROTATION = new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
        TEXTURE_ROTATED_90 = new float[]{1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f};
        TEXTURE_ROTATED_180 = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        TEXTURE_ROTATED_270 = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};
    }

    private TextureRotationUtil() {
    }

    public static float[] getRotation(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        float[] rotatedTex;
        switch ($SWITCH_TABLE$jp$co$cyberagent$android$gpuimage$Rotation()[rotation.ordinal()]) {
            case DetectedActivity.ON_FOOT /*2*/:
                rotatedTex = TEXTURE_ROTATED_90;
                break;
            case DetectedActivity.STILL /*3*/:
                rotatedTex = TEXTURE_ROTATED_180;
                break;
            case DetectedActivity.UNKNOWN /*4*/:
                rotatedTex = TEXTURE_ROTATED_270;
                break;
            default:
                rotatedTex = TEXTURE_NO_ROTATION;
                break;
        }
        if (flipHorizontal) {
            rotatedTex = new float[]{flip(rotatedTex[0]), rotatedTex[1], flip(rotatedTex[2]), rotatedTex[3], flip(rotatedTex[4]), rotatedTex[5], flip(rotatedTex[6]), rotatedTex[7]};
        }
        if (!flipVertical) {
            return rotatedTex;
        }
        return new float[]{rotatedTex[0], flip(rotatedTex[1]), rotatedTex[2], flip(rotatedTex[3]), rotatedTex[4], flip(rotatedTex[5]), rotatedTex[6], flip(rotatedTex[7])};
    }

    private static float flip(float i) {
        if (i == 0.0f) {
            return 1.0f;
        }
        return 0.0f;
    }
}
