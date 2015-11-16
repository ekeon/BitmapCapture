package com.ekeon.bitmaptest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

  SurfacePreviewTest surfacePreviewTest;
  FrameLayout frameLayout;
  Button btnSave;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    surfacePreviewTest = new SurfacePreviewTest(this);
    frameLayout = (FrameLayout) findViewById(R.id.main_framelayout);
    frameLayout.addView(surfacePreviewTest);

    btnSave = (Button) findViewById(R.id.button_save);
    btnSave.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View vv) {
        Bitmap bm = Bitmap.createBitmap(surfacePreviewTest.drawBitmap());

        if (bm != null) {
          try {
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fOut = null;
            File file = new File(path + "/", "screentest.jpg");
            fOut = new FileOutputStream(file);

            bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private class SurfacePreviewTest extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    SurfaceHolder surfaceHolder;
    Camera camera = null;

    public SurfacePreviewTest(Context context) {
      super(context);
      init(context);
    }

    public void init(Context context) {
      surfaceHolder = getHolder();
      surfaceHolder.addCallback(this);
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      camera = Camera.open();
      try {
        camera.setPreviewDisplay(holder);  //프리뷰를 홀더로
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

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
      Camera.Parameters params = camera.getParameters();
      int w = params.getPreviewSize().width;
      int h = params.getPreviewSize().height;
      int format = params.getPreviewFormat();
      YuvImage image = new YuvImage(data, format, w, h, null);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Rect area = new Rect(0, 0, w, h);
      image.compressToJpeg(area, 100, out);
      Bitmap bm = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());

      Matrix matrix = new Matrix();
      matrix.postRotate(90);
      Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);
    }

    public Bitmap drawBitmap() {
      Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);

      surfaceDestroyed(null);
      onDraw(canvas);
      surfaceCreated(null);

      return bitmap;
    }
  }
}
