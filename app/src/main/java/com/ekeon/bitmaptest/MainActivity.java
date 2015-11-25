package com.ekeon.bitmaptest;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

  private SurfacePreviewTest surfacePreviewTest;
  private Camera camera;

  private ImageView capturedImageHolder;
  private FrameLayout cameraFrameLayout;
  private Button btnSave;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    cameraFrameLayout = (FrameLayout) findViewById(R.id.main_framelayout);
    capturedImageHolder = (ImageView)findViewById(R.id.iv_capture);
    camera = checkDeviceCamera();

    surfacePreviewTest = new SurfacePreviewTest(this, camera);

    cameraFrameLayout.addView(surfacePreviewTest);


    btnSave = (Button) findViewById(R.id.button_save);
    btnSave.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d("TAG", "button click");
        camera.takePicture(null, null, takePicture);
      }
    });
  }

  private void makeDir() {
    String str = Environment.getExternalStorageState();

    if (TextUtils.equals(str, Environment.MEDIA_MOUNTED)) {
      String dirPath = "/sdcard/StudyBitmap";
      File file = new File(dirPath);

      if (!file.exists()) {
        file.mkdirs();
      }
    } else {
      Log.d("TAG", "fail");
    }
  }

  private Camera.PictureCallback takePicture = new Camera.PictureCallback() {
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
      // TODO Auto-generated method stub
      FileOutputStream fos;

      if(data != null) {
        makeDir();
        Bitmap bitmap1 = BitmapFactory.decodeByteArray(data, 0, data.length);
        capturedImageHolder.setImageBitmap(bitmap1);
        try {
          long now = System.currentTimeMillis();
          fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/StudyBitmap/" + now + "capture.jpg");
          bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
        camera.startPreview();
      }

    }
  };

  private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight){
    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    return resizedBitmap;
  }

  private Camera checkDeviceCamera(){
    Camera mCamera = null;
    try {
      mCamera = Camera.open();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return mCamera;
  }

  private class SurfacePreviewTest extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder surfaceHolder;
    Camera camera = null;

    public SurfacePreviewTest(Context context, Camera camera) {
      super(context);
      this.camera = camera;
      this.surfaceHolder = getHolder();
      this.surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      try {
        this.camera.setPreviewDisplay(holder);  //프리뷰를 홀더로
        this.camera.startPreview();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      camera.setDisplayOrientation(90);
      camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      camera.stopPreview();
      camera.release();
      camera = null;
    }

  }
}
