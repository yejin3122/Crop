package com.example.qisens_n_hyunki.cropexample1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CropView extends View implements View.OnTouchListener {
    private Paint paint;
    public static List<Point> points;
    boolean flgPathDraw = true;

    Point mfirstpoint = null;
    boolean bfirstpoint = false;  // first point가 있으면 true, 없으면 false

    Point mlastpoint = null;

    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test3);
    Context mContext;

    String TAG = "CropView";

    // canvas 크기 저장하는 변수 (ImageCropActivity에 intent로 넘겨줄꺼)
    int canvasWidth;
    int canvasHeight;

    // 지정한 범위의 x, y의 min, max값
    public static float minx=0, miny=0, maxx=0, maxy=0;

    // 현재 좌표에서 바로 전의 좌표를 저장하기 위한 변수
    public static List<Point> previousPoint;


    public CropView(Context c) {
        super(c);

        mContext = c;
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
//        paint.setPathEffect(new DashPathEffect(new float[] { 10, 20 }, 0));   // 선 모양이 곡선이 아니라 점선으로 나옴
        paint.setStrokeCap(Paint.Cap.ROUND);   // 선의 끝나는 지점의 장식 설정
        paint.setStrokeJoin(Paint.Join.ROUND);  // 선의 끝 모양을 설정
        paint.setStrokeWidth(100);
        paint.setColor(Color.WHITE);

        this.setOnTouchListener(this);
        points = new ArrayList<Point>();

        bfirstpoint = false;
    }

    public CropView(Context context, AttributeSet attrs) {

        super(context, attrs);
        Log.i(TAG,"a;ldksfj");
        mContext = context;
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);

        this.setOnTouchListener(this);
        points = new ArrayList<Point>();
        bfirstpoint = false;

    }

    public void onDraw(Canvas canvas) {
        // 비트맵 이미지를 캔버스 크기에 꽉채워 넣기위해 추가 --------------------------------------------
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvasWidth = width;
        canvasHeight = height;
        // -----------------------------------------------------------------------------------------

        Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        canvas.drawBitmap(resizeBitmap, 0, 0, null);

        Path path = new Path();
        boolean first = true;

        for (int i = 0; i < points.size(); i += 2) {
            Point point = points.get(i);
            if (first) {
                first = false;
                path.moveTo(point.x, point.y);
            } else if (i < points.size() - 1) {
                Point next = points.get(i + 1);
                path.quadTo(point.x, point.y, next.x, next.y);
            } else {
                mlastpoint = points.get(i);
                path.lineTo(point.x, point.y);
            }
        }
        canvas.drawPath(path, paint);
    }

    public boolean onTouch(View view, MotionEvent event) {

        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        if (flgPathDraw) {

            if (bfirstpoint) {

                if (comparePoint(mfirstpoint, point)) {
                    points.add(mfirstpoint);
                    flgPathDraw = false;
                    showCropDialog();
                } else {
                    points.add(point);
                }
            } else {
                points.add(point);
            }

            if (!(bfirstpoint)) {

                mfirstpoint = point;
                bfirstpoint = true;

                // 추가------------------------------------------------------------------------------
                minx = mfirstpoint.x;
                miny = mfirstpoint.y;
                maxx = mfirstpoint.x;
                maxy = mfirstpoint.y;

                // ---------------------------------------------------------------------------------
            }
        }

        invalidate();   // onDraw()가 호출되면서 이미지를 계속해서 새로 그림

        if (event.getAction() == MotionEvent.ACTION_UP) {  // ACTION_UP: 누른걸 땠을때

            mlastpoint = point;
            if (flgPathDraw) {
                if (points.size() > 12) {
                    if (!comparePoint(mfirstpoint, mlastpoint)) {
                        flgPathDraw = false;
                        points.add(mfirstpoint);
                        showCropDialog();
                    }
                }
            }
        }

        return true;
    }

    private boolean comparePoint(Point first, Point current) {
        int left_range_x = (int) (current.x - 3);
        int left_range_y = (int) (current.y - 3);

        int right_range_x = (int) (current.x + 3);
        int right_range_y = (int) (current.y + 3);

        // min, max값 비교하는거 추가 ----------------------------------------------------------------
        if (current.x < minx) minx = current.x;
        if (current.y < miny) miny = current.y;
        if (current.x > maxx) maxx = current.x;
        if (current.y > maxy) maxy = current.y;

        // point의 위치가 어느 방향으로 바꼈는지 비교


        // -----------------------------------------------------------------------------------------

        if ((left_range_x < first.x && first.x < right_range_x) && (left_range_y < first.y && first.y < right_range_y)) {  // first point하고 current point 차이가 작으면
            if (points.size() < 10) {  // 화면에 그려진게 별로 없으면(point가 10개 미만이면) crop 하지마셈
                return false;
            } else {   // 그려진게 많으면(point가 10개 이상이면) crop 하셈
                return true;
            }
        } else {
            return false;
        }

    }

    private void showCropDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:

                        intent = new Intent(mContext, ImageCropActivity.class);
                        intent.putExtra("crop", true);
                        intent.putExtra("width", canvasWidth);   // 추가
                        intent.putExtra("height", canvasHeight); // 추가
                        mContext.startActivity(intent);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        intent = new Intent(mContext, ImageCropActivity.class);
                        intent.putExtra("crop", false);
                        intent.putExtra("width", canvasWidth);   // 추가
                        intent.putExtra("height", canvasHeight); // 추가
                        mContext.startActivity(intent);

                        bfirstpoint = false;

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Do you Want to save Crop or Non-crop image?")
                .setPositiveButton("Crop", dialogClickListener)
                .setNegativeButton("Non-crop", dialogClickListener).show()
                .setCancelable(false);
    }
}