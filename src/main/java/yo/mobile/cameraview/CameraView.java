package yo.mobile.cameraview;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

@SuppressWarnings("deprecation")
public class CameraView extends TextureView implements TextureView.SurfaceTextureListener {

    private static CameraViewImpl IMPL;
    private final static String TAG = CameraView.class.getSimpleName();
    //    private CameraViewImpl cameraViewImpl;
    private boolean useFrontCamera = true;
    private OnCameraErrorListener onCameraErrorListener;
    private int frontCameraId;
    private int backCameraId;
    private boolean cameraExist;
    private int preferredHeight = 720;
    private float preferredAspect = 4f / 3f;
    private int mRatioWidth;
    private int mRatioHeight;

    public interface OnCameraErrorListener {
        void onNoCamerasAvailable();

        void onCameraOpenFailed(Exception e);
    }

    public CameraView(Context context) {
        super(context);
        init(null, 0);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("No camera permission - Use FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);");
        }
//        if (Build.VERSION.SDK_INT >= 21 && Camera2Helper.hasCamera2(getContext())) {
//            IMPL = new Camera1Api();
//        } else {
        IMPL = new Camera1Api();
//        }
        IMPL.initialize(this, getContext());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setSurfaceTextureListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        IMPL.releaseCamera();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        IMPL.openCamera(surface, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        IMPL.configureTransform(surface, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        IMPL.releaseCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
    }


    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    public void setUseFrontCamera(boolean use) {
        this.useFrontCamera = use;
    }

    public boolean isUseFrontCamera() {
        return useFrontCamera;
    }

    public boolean isCameraExist() {
        return cameraExist;
    }

    public boolean isFrontCameraExist() {
        return backCameraId >= 0;
    }

    public boolean isBackCameraExist() {
        return frontCameraId >= 0;
    }

    public OnCameraErrorListener getOnCameraErrorListener() {
        return onCameraErrorListener != null ? onCameraErrorListener : new OnCameraErrorListener() {
            @Override
            public void onNoCamerasAvailable() {
                Log.e(TAG, "No available cameras");
            }

            @Override
            public void onCameraOpenFailed(Exception e) {
                Log.e(TAG, "Open camera failed");
                e.printStackTrace();
            }
        };
    }

    public void setOnCameraErrorListener(OnCameraErrorListener onCameraErrorListener) {
        this.onCameraErrorListener = onCameraErrorListener;
    }

    void setFrontCameraId(int frontCameraId) {
        this.frontCameraId = frontCameraId;
    }

    void setBackCameraId(int backCameraId) {
        this.backCameraId = backCameraId;
    }

    int getCurrentCameraId() {
        int id = 0;
        if (useFrontCamera && frontCameraId >= 0) {
            id = frontCameraId;
        } else if (!useFrontCamera && backCameraId >= 0) {
            id = backCameraId;
        } else if (frontCameraId >= 0) {
            id = frontCameraId;
        } else if (backCameraId >= 0) {
            id = backCameraId;
        }
        return id;
    }

    public int getPreferredHeight() {
        return preferredHeight;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    public float getPreferredAspect() {
        return preferredAspect;
    }

    public void setPreferredAspect(float preferredAspect) {
        this.preferredAspect = preferredAspect;
    }
}
