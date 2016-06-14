package yo.mobile.cameraview;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

@SuppressWarnings("deprecation")
public class CameraView extends FrameLayout {

    private final static String TAG = CameraView.class.getSimpleName();
    private CameraViewImpl cameraViewImpl;
    private boolean useFrontCamera;
    private OnCameraErrorListener onCameraErrorListener;
    private int frontCameraId;
    private int backCameraId;
    private boolean cameraExist;

    public interface OnCameraErrorListener {
        void onNoCamerasAvailable();

        void onCameraOpenFailed();
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
        if (Build.VERSION.SDK_INT >= 21) {
            cameraViewImpl = new CameraViewApi14(getContext(), this);
        } else {
            cameraViewImpl = new CameraViewApi14(getContext(), this);
        }
        cameraExist = cameraViewImpl.checkCameraExist();
        if (cameraExist) {
            addView(cameraViewImpl.getView());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        cameraViewImpl.openCamera();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cameraViewImpl.releaseCamera();
    }

    public void setUseFrontCamera(boolean use) {
        this.useFrontCamera = use;
    }

    public boolean isCameraExist() {
        return cameraExist;
    }

    public OnCameraErrorListener getOnCameraErrorListener() {
        return onCameraErrorListener != null ? onCameraErrorListener : new OnCameraErrorListener() {
            @Override
            public void onNoCamerasAvailable() {
                Log.e(TAG, "No available cameras");
            }

            @Override
            public void onCameraOpenFailed() {
                Log.e(TAG, "Open camera failed");
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

    int getCameraId() {
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
}
