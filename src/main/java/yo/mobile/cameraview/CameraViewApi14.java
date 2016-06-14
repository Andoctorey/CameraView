package yo.mobile.cameraview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import yo.mobile.cameraview.util.CameraUtil;
import yo.mobile.cameraview.util.Degrees;

import static android.content.ContentValues.TAG;

@SuppressLint("ViewConstructor")
@SuppressWarnings("deprecation")
class CameraViewApi14 extends SurfaceView implements SurfaceHolder.Callback, CameraViewImpl {

    private CameraView cameraView;
    private SurfaceHolder mHolder;
    private Camera camera;
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private int width;
    private int height;

    CameraViewApi14(Context context, CameraView cameraView) {
        super(context);
        this.cameraView = cameraView;
        init(null, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
//            camera.setPreviewDisplay(holder);
//            camera.startPreview();
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
            camera.stopPreview();
        } catch (Exception ignored) {
        }
        try {
            width = w;
            height = h;
            setParameters();
            camera.setPreviewDisplay(mHolder);
            camera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean checkCameraExist() {
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras == 0) {
            cameraView.getOnCameraErrorListener().onNoCamerasAvailable();
            return false;
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
        return true;
    }

    @Override
    public void openCamera() {
        try {
            releaseCamera();
            camera = Camera.open(cameraView.getCurrentCameraId());
            if (camera == null) {
                cameraView.getOnCameraErrorListener().onCameraOpenFailed();
            }
        } catch (Exception e) {
            cameraView.getOnCameraErrorListener().onCameraOpenFailed();
            e.printStackTrace();
        }
    }

    private void setParameters() {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> videoSizes = parameters.getSupportedVideoSizes();
        if (videoSizes == null || videoSizes.size() == 0) {
            videoSizes = parameters.getSupportedPreviewSizes();
        }
        Camera.Size selectedSize = videoSizes.get(0);
        for (Camera.Size size : videoSizes) {
            if (size.height <= cameraView.getPreferredHeight()) {
                if (size.width == size.height * cameraView.getPreferredAspect()) {
                    selectedSize = size;
                }
                break;
            }
        }
        List<Camera.Size> bigEnough = new ArrayList<>();
        int w = selectedSize.width;
        int h = selectedSize.height;
        for (Camera.Size option : videoSizes) {
            if (option.height == width * h / w &&
                    option.width >= width && option.height >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            selectedSize = Collections.min(bigEnough, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size lhs, Camera.Size rhs) {
                    return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
                }
            });
        }

        parameters.setPreviewSize(selectedSize.width, selectedSize.height);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            parameters.setRecordingHint(true);
        }
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraView.getCurrentCameraId(), info);
        final int deviceOrientation = Degrees.getDisplayRotation(getContext());
        int displayOrientation = Degrees.getDisplayOrientation(
                info.orientation, deviceOrientation, info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        Log.d("CameraFragment", String.format("Orientations: Sensor = %d˚, Device = %d˚, Display = %d˚",
                info.orientation, deviceOrientation, displayOrientation));

        int previewOrientation;
        if (CameraUtil.isArcWelder()) {
            previewOrientation = 0;
        } else {
            previewOrientation = displayOrientation;
            if (Degrees.isPortrait(deviceOrientation) && cameraView.isUseFrontCamera()) {
                previewOrientation = Degrees.mirror(displayOrientation);
            }
        }
        parameters.setRotation(previewOrientation);
        camera.setDisplayOrientation(previewOrientation);
        camera.setParameters(parameters);
    }

    @Override
    public void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
