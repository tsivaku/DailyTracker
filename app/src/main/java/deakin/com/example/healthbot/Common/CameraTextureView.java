package deakin.com.example.healthbot.Common;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.view.TextureView;

import java.io.IOException;
import java.util.List;


public class CameraTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private Camera camera;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private SurfaceTexture surfaceTexture;
    private Handler uiHandler;

    public void setCamera(Camera camera) {
        this.camera = camera;
        if (camera == null) {
            return;
        }
        startCameraPreview();
    }

    public Camera getCamera() {
        return camera;
    }

    public CameraTextureView(Context context, Camera camera) {
        super(context);
        this.setSurfaceTextureListener(this);
        this.camera = camera;
        this.surfaceTexture = getSurfaceTexture();
        this.uiHandler = new Handler();

        mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        float ratio;
        if (mPreviewSize.height >= mPreviewSize.width)
            ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
        else
            ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

        setMeasuredDimension(width, height);
//        setMeasuredDimension(width, (int) (width * ratio));
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


    void startCameraPreview() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    if(camera !=null){
                    camera.setPreviewTexture(getSurfaceTexture()); }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Camera.Parameters parameters = camera.getParameters();

                // set preview width and height
//                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

//                parameters.setRotation(90);
//                camera.setParameters(parameters);
                List<String> focusModes = parameters.getSupportedFocusModes();
//                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//
//                }

//                if(parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
//                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//                }
//                camera.setParameters(parameters);
                camera.setDisplayOrientation(90);
                camera.startPreview();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startCameraPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}

