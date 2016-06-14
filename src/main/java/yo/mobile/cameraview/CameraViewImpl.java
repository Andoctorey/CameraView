package yo.mobile.cameraview;


import android.view.View;

public interface CameraViewImpl {

    View getView();

    void openCamera();

    void releaseCamera();
}
