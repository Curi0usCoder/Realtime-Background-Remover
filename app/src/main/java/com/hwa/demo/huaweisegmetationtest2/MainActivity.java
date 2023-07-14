package com.hwa.demo.huaweisegmetationtest2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationScene;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    SurfaceView mSurfaceView;
    Button StartCam,StopCam;
    ImageView SegmentedView;

    LensEngine lensEngine;
    MLImageSegmentationAnalyzer analyzer;
    MLAnalyzer sarath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.camera_view);
        StartCam = findViewById(R.id.start_cam);
        StopCam = findViewById(R.id.stop_cam);
        SegmentedView = findViewById(R.id.segmented_view);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    1);
        } else {
            // Permission has already been granted
            // Do camera-related work here
            MLImageSegmentationSetting setting = new MLImageSegmentationSetting
                    .Factory()
                    .setExact(false)
                    .setScene(MLImageSegmentationScene.FOREGROUND_ONLY)
                    .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG)
                    .create();
            analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting);

            analyzer.setTransactor(new ImageSegmentAnalyzerTransactor());

            Context context = this.getApplicationContext();
             lensEngine = new LensEngine.Creator(context,analyzer)
                    // Set the front or rear camera mode. LensEngine.BACK_LENS indicates the rear camera, and LensEngine.FRONT_LENS indicates the front camera.
                    .setLensType(LensEngine.FRONT_LENS)
                    .applyDisplayDimension(1280, 720)
                    .applyFps(20.0f)
                    .enableAutomaticFocus(true)
                    .create();
        }


        // Implement other logic of the SurfaceView control by yourself.

        StartCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    lensEngine.run(mSurfaceView.getHolder());

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                }

            }
        });

        StopCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (analyzer != null) {
                    try {
                        analyzer.stop();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                    }
                }
                if (lensEngine != null) {
                    lensEngine.release();
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==1){
            MLImageSegmentationSetting setting = new MLImageSegmentationSetting
                    .Factory()
                    .setExact(false)
                    .setScene(MLImageSegmentationScene.FOREGROUND_ONLY)
                    .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG)
                    .create();
            analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting);

            analyzer.setTransactor(new ImageSegmentAnalyzerTransactor());

            Context context = this.getApplicationContext();

           // analyzer.analyze(new MLFrame.Creator().setBitmap(bitmap).create(), null);
           /* for (Bitmap bitmap : bitmapList) {
                // Create an MLFrame object from the Bitmap
                MLFrame mlFrame = new MLFrame.Creator().setBitmap(bitmap).create();

                // Pass the MLFrame to your analyzer for analysis
                analyzer.(mlFrame);
            }*/

            lensEngine = new LensEngine.Creator(context,analyzer)
                    // Set the front or rear camera mode. LensEngine.BACK_LENS indicates the rear camera, and LensEngine.FRONT_LENS indicates the front camera.
                    .setLensType(LensEngine.FRONT_LENS)
                    .applyDisplayDimension(1280, 720)
                    .applyFps(20.0f)
                    .enableAutomaticFocus(true)
                    .create();
        }else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

    }

    public class ImageSegmentAnalyzerTransactor implements MLAnalyzer.MLTransactor<MLImageSegmentation> {
        @Override
        public void transactResult(MLAnalyzer.Result<MLImageSegmentation> results) {
            SparseArray<MLImageSegmentation> items = results.getAnalyseList();
            // Determine detection result processing as required. Note that only the detection results are processed.
            // Other detection-related APIs provided by ML Kit cannot be called.

//            SegmentedView.setImageBitmap(items.get(0).foreground);
//            SegmentedView.setImageBitmap(items.get(0).grayscale);
            SegmentedView.setImageBitmap(mirrorBitmap(items.get(0).foreground));


            Log.d("sarath",""+items.get(0));
        }
        @Override
        public void destroy() {
            // Callback method used to release resources when the detection ends.

            Toast.makeText(MainActivity.this, "destroy", Toast.LENGTH_SHORT).show();
        }
    }
    public Bitmap mirrorBitmap(Bitmap originalBitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f); // Flip horizontally
        Bitmap mirroredBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
        return mirroredBitmap;
    }

}