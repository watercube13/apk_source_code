package com.android.cameratest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Camera2TestActivity extends Activity {
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private static final String[] REQUIRED_STORAGE_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String TAG = "Camera2Test";
    private CameraManager mCameraManager;
    private CameraManager mCameraManager1;
    private CameraDevice mCameraDevice;
    private CameraDevice mCameraDevice1;
    private CameraCaptureSession mCaptureSession;
    private CameraCaptureSession mCaptureSession1;
    private SurfaceView mSurfaceView;
    private Surface mRecord1Surface_360;
    private SurfaceView mSurfaceView1;
    private Surface mRecord1Surface_9950;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceHolder mSurfaceHolder1;
    private String mCameraId0 = "1";
    private String mCameraId1 = "0";
    private boolean isSupportOneCamera = true;
    private boolean isSupportTwoCamera = true;
    private MediaRecorder mediaRecorder_360;
    private MediaRecorder mediaRecorder_9950;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            createCaptureSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private CameraDevice.StateCallback mCameraStateCallback1 = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice1 = camera;
            createCaptureSession1();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice1 = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice1 = null;
        }
    };

    private CameraCaptureSession.StateCallback mCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCaptureSession = session;
            startPreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            // 配置失败处理
        }
    };

    private CameraCaptureSession.StateCallback mCaptureSessionStateCallback1 = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCaptureSession1 = session;
            startPreview1();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG,"onConfigureFailed");
            // 配置失败处理
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_test);
        mediaRecorder_360 = new MediaRecorder();
        mediaRecorder_9950 = new MediaRecorder();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,REQUIRED_STORAGE_PERMISSIONS,STORAGE_PERMISSIONS_REQUEST_CODE);
            // 检查相机权限
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        if(isSupportOneCamera){
            mSurfaceView = findViewById(R.id.surfaceView0);
            mSurfaceHolder = mSurfaceView.getHolder();
            /* 360 recorder */
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            mediaRecorder_360.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder_360.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder_360.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder_360.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder_360.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder_360.setVideoSize(1920, 1080);
            mediaRecorder_360.setVideoFrameRate(30);
            File file = new File(getExternalFilesDir(null), "camera_360_file.mp4");
            mediaRecorder_360.setOutputFile(file);
            try {
                mediaRecorder_360.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecord1Surface_360 = mediaRecorder_360.getSurface();
            mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    openCamera();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    // 处理 Surface 尺寸变化事件
                    // 可以在此处更新预览尺寸等参数
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    closeCamera();
                }
            });
        }

        if(isSupportTwoCamera){
            mCameraManager1 = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            mSurfaceView1 = findViewById(R.id.surfaceView1);
            mSurfaceHolder1 = mSurfaceView1.getHolder();
            /* 9950 recorder */
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            mediaRecorder_9950.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder_9950.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder_9950.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
            mediaRecorder_9950.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder_9950.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder_9950.setVideoSize(1920, 1080);
            mediaRecorder_9950.setVideoFrameRate(30);
            File file = new File(getExternalFilesDir(null), "camera_9950_file.ts");
            mediaRecorder_9950.setOutputFile(file);
            try {
                mediaRecorder_9950.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecord1Surface_9950 = mediaRecorder_9950.getSurface();
            mSurfaceHolder1.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    openCamera1();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    // 处理 Surface 尺寸变化事件
                    // 可以在此处更新预览尺寸等参数
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    closeCamera1();
                }
            });
        }

    }

    private void openCamera() {
        Log.d(TAG,"openCamera()");
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // 检查相机权限
                return;
            }

            mCameraManager.openCamera(mCameraId0, mCameraStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera1() {
        Log.d(TAG,"openCamera1()");
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // 检查相机权限
                return;
            }
            mCameraManager1.openCamera(mCameraId1, mCameraStateCallback1, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (mCaptureSession != null) {
            mediaRecorder_360.stop();
            mediaRecorder_360.reset();
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;

        }
    }

    private void closeCamera1() {
        if (mCaptureSession1 != null) {
            mediaRecorder_9950.stop();
            mediaRecorder_9950.reset();
            mCaptureSession1.close();
            mCaptureSession1 = null;
        }
        if (mCameraDevice1 != null) {
            mCameraDevice1.close();
            mCameraDevice1 = null;
        }
    }

    private void createCaptureSession() {
        try {
            List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(mSurfaceHolder.getSurface());
            outputSurfaces.add(mRecord1Surface_360);
            mCameraDevice.createCaptureSession(outputSurfaces, mCaptureSessionStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCaptureSession1() {
        try {
            List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(mSurfaceHolder1.getSurface());
            outputSurfaces.add(mRecord1Surface_9950);
            mCameraDevice1.createCaptureSession(outputSurfaces, mCaptureSessionStateCallback1, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            captureRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            captureRequestBuilder.addTarget(mRecord1Surface_360);
            mCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
            mediaRecorder_360.start();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview1() {
        try {
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice1.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(mSurfaceHolder1.getSurface());
            captureRequestBuilder.addTarget(mRecord1Surface_9950);
            mCaptureSession1.setRepeatingRequest(captureRequestBuilder.build(), null, null);
            mediaRecorder_9950.start();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}