package com.example.asif2.barcode_reader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView surfaceView;
    private TextView scan_text;

    private Camera camera = null;

    final static int requestId = 1001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
        scan_text = findViewById(R.id.scan_text);

        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE | Barcode.DATA_MATRIX |Barcode.EAN_13)
                .build();

        if (!barcodeDetector.isOperational()) {

            Toast.makeText(this, "Unable to init setup", Toast.LENGTH_SHORT).show();

        } else {


            cameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                    .setRequestedPreviewSize(640, 480)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();


            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA}, requestId);

                            return;
                        }

                        cameraSource.start(surfaceView.getHolder());
                        flashOnButton();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }

               @Override
               public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                   flashOnButton();
               }

               @Override
               public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                    cameraSource.stop();
                    barcodeDetector.release();
               }
           });

            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {

                    final SparseArray<Barcode> barcode = detections.getDetectedItems();

                    if(barcode.size() != 0){

                        scan_text.post(new Runnable() {
                            @Override
                            public void run() {
                                scan_text.setText(barcode.valueAt(0).displayValue);
                            }
                        });
                    }
                    else {

                        Toast.makeText(MainActivity.this, "Unable to detect", Toast.LENGTH_SHORT).show();

                    }

                }
            });

        }
    }

    private void flashOnButton() {
        camera=getCamera(cameraSource);
        if (camera != null) {
            try {
                Camera.Parameters param = camera.getParameters();
                int zoom = param.getMaxZoom();
                param.setZoom(zoom);
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO );


                    camera.setParameters(param);

            } catch (Exception e) {
                Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private static Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }


}
