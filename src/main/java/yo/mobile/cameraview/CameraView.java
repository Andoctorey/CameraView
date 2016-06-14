package yo.mobile.cameraview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;
import java.util.List;

import yo.mobile.cameraview.util.CameraHelper;

@SuppressWarnings("deprecation")
public class CameraView extends TextureView implements TextureView.SurfaceTextureListener {

    private final static String TAG = CameraView.class.getSimpleName();
    //    private CameraViewImpl cameraViewImpl;
    private boolean useFrontCamera = true;
    private OnCameraErrorListener onCameraErrorListener;
    private int frontCameraId;
    private int backCameraId;
    private boolean cameraExist;
    private int preferredHeight = 720;
    private float preferredAspect = 4f / 3f;
    private Camera mCamera;

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
//        if (Build.VERSION.SDK_INT >= 21) {
//            cameraViewImpl = new CameraViewApi14(getContext(), this);
//        } else {
//            cameraViewImpl = new CameraViewApi14(getContext(), this);
//        }
//        cameraExist = cameraViewImpl.checkCameraExist();
//        if (cameraExist) {
//
//        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setSurfaceTextureListener(this);
        mCamera = CameraHelper.getDefaultCameraInstance();
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
        }
//        cameraViewImpl.openCamera();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        try {
            releaseCamera();
            mCamera = Camera.open();
            // We need to make sure that our preview and recording video size are supported by the
            // camera. Query camera to find all the sizes and choose the optimal size given the
            // dimensions of our preview surface.
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
            Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                    mSupportedPreviewSizes, width, height);

            // Use the same size for recording profile.
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            profile.videoFrameWidth = optimalSize.width;
            profile.videoFrameHeight = optimalSize.height;

            // likewise for the camera object itself.
            parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            mCamera.setPreviewTexture(surface);
        } catch (Exception e) {
            getOnCameraErrorListener().onCameraOpenFailed(e);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
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
