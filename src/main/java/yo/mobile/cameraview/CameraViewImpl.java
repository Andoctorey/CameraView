package yo.mobile.cameraview;


import android.content.Context;
import android.graphics.SurfaceTexture;

public interface CameraViewImpl {

    void initialize(CameraView cameraView, Context context);

    boolean checkCameraExist();

    void openCamera(SurfaceTexture surface, int width, int height);

    void releaseCamera();
}
