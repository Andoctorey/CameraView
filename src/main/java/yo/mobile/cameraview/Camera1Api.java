package yo.mobile.cameraview;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.util.Log;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

import yo.mobile.cameraview.util.Camera1Helper;

import static android.content.ContentValues.TAG;

public class Camera1Api implements CameraViewImpl {

    private CameraView cameraView;
    private Context context;
    private Camera camera;
    private WindowManager windowManager;

    @Override
    public void initialize(CameraView cameraView, Context context) {
        this.cameraView = cameraView;
        this.context = context;
        camera = Camera1Helper.getDefaultCameraInstance();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        try {
            camera.setPreviewTexture(cameraView.getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
        }
    }

    @Override
    public boolean checkCameraExist() {
        return false;
    }

    @Override
    public void openCamera(SurfaceTexture surface, int width, int height) {
        try {
            releaseCamera();
            camera = Camera.open();
            // We need to make sure that our preview and recording video size are supported by the
            // camera. Query camera to find all the sizes and choose the optimal size given the
            // dimensions of our preview surface.
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
            Camera.Size optimalSize = Camera1Helper.getOptimalPreviewSize(mSupportedVideoSizes, height);

            // Use the same size for recording profile.
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            profile.videoFrameWidth = optimalSize.width;
            profile.videoFrameHeight = optimalSize.height;

            // likewise for the camera object itself.
            parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
            camera.setParameters(parameters);
            Camera1Helper.setCameraDisplayOrientation(windowManager, Camera1Helper.getDefaultCameraID(), camera);
            camera.startPreview();
            camera.setPreviewTexture(surface);
        } catch (Exception e) {
            cameraView.getOnCameraErrorListener().onCameraOpenFailed(e);
        }
    }

    @Override
    public void releaseCamera() {
        if (camera != null) {
            // release the camera for other applications
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void configureTransform(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }
}
