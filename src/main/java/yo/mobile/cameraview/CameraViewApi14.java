package yo.mobile.cameraview;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import static android.R.attr.id;
import static android.content.ContentValues.TAG;

@SuppressWarnings("deprecation")
class CameraViewApi14 extends SurfaceView implements SurfaceHolder.Callback, CameraViewImpl {

    private CameraView cameraView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private int frontCameraId;

    CameraViewApi14(Context context) {
        super(context);
        init(null, 0);
    }

    CameraViewApi14(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    CameraViewApi14(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    CameraViewApi14(Context context, CameraView cameraView) {
        super(context);
        this.cameraView = cameraView;
        init(null, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        openCamera();
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Throwable e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null)
            return;
        try {
            mCamera.stopPreview();
        } catch (Exception ignored) {
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
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

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void openCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras == 0) {
            cameraView.getOnCameraErrorListener().onNoCamerasAvailable();
            return;
        }

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraView.setFrontCameraId(i);
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraView.setBackCameraId(i);
            }
        }

        try {
            releaseCamera();
            mCamera = Camera.open(id);
            if (mCamera == null) {
                cameraView.getOnCameraErrorListener().onCameraOpenFailed();
            }
        } catch (Exception e) {
            cameraView.getOnCameraErrorListener().onCameraOpenFailed();
            e.printStackTrace();
        }
    }

    @Override
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
