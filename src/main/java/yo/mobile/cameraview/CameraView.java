package yo.mobile.cameraview;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

@SuppressWarnings("deprecation")
public class CameraView extends FrameLayout {

    private CameraViewImpl cameraViewImpl;

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
            cameraViewImpl = new CameraViewApi21(getContext());
        } else {
            cameraViewImpl = new CameraViewApi14(getContext());
        }
        addView(cameraViewImpl.getView());
    }
}
