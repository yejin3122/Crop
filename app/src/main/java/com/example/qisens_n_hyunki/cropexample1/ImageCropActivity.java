package com.example.qisens_n_hyunki.cropexample1;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

public class ImageCropActivity extends Activity {
    ImageView compositeImageView;
    boolean crop;

    // CropView에서 받아온 intent
    int canvasWidth;
    int canvasHeight;
    String TAG = "ImageCropActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_crop_activity);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            crop = extras.getBoolean("crop");
            canvasWidth = extras.getInt("width");
            canvasHeight = extras.getInt("height");
        }

        int widthOfscreen = 0;
        int heightOfScreen = 0;
        DisplayMetrics dm = new DisplayMetrics();
        try {
            getWindowManager().getDefaultDisplay().getMetrics(dm);
        } catch (Exception ex) {
        }
        widthOfscreen = dm.widthPixels;
        heightOfScreen = dm.heightPixels;

        compositeImageView = (ImageView)findViewById(R.id.iv);


        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.test3);

        Bitmap resultingImage = Bitmap.createBitmap(widthOfscreen, heightOfScreen, bitmap2.getConfig());

        Canvas canvas = new Canvas(resultingImage);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // crop version1 - 그린 모양대로 crop --------------------------------------------------------
//        Path path = new Path();
//
//        for (int i = 0; i < CropView.points.size(); i++) {
//            path.lineTo(CropView.points.get(i).x, CropView.points.get(i).y);   // 기준점에서 (x, y)까지 line 그리기
//        }
//
//        canvas.drawPath(path, paint);   // 설정한 path를 화면에 print
        // -----------------------------------------------------------------------------------------

        // crop version2 - 지정한 범위에 외접하는 사각형만큼 crop
        canvas.drawRect(CropView.minx-50, CropView.miny-50, CropView.maxx+50, CropView.maxy+50, paint);

        if (crop) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        } else {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        }

        Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap2, canvasWidth, canvasHeight, true);  // 화면에 이미지 채워넣기위해 추가
        canvas.drawBitmap(resizeBitmap, 0, 0, paint);
        compositeImageView.setImageBitmap(resultingImage);
    }
}