package yo.mobile.cameraview;


import android.view.View;

public interface CameraViewImpl {

    View getView();

    boolean checkCameraExist();

    void openCamera();

    void releaseCamera();
}
