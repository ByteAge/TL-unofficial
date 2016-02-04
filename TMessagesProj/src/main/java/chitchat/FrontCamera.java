package chitchat;

import android.content.Context;
import android.hardware.Camera;

/**
 * Created by RaminBT on 20/01/2016.
 */
public class FrontCamera {

    private Context context;
    private Camera camera;

    public FrontCamera(Context context) {
        this.context = context.getApplicationContext();

        camera=openFrontFacingCameraGingerbread();
    }

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    //Log.d("Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }
}
