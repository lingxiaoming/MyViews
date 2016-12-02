package com.chris.myviews.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.chris.myviews.R;

/**
 * 圆形八卦view
 * Created by lingxiaoming on 2016/11/22 0022.
 */

public class CircleView extends View {
    private int circleColor;//最外层颜色
    private int panelColor;//背景颜色
    private int pointColor;//圆点颜色
    private float pointRadiu;//圆点半径

    private Paint mainPain;

    Region rightRegion, leftRegion;
    Path rightPath, leftPath;
    Matrix mMapMatrix = null;

    private Toast toast;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
        initPaint();
    }

    private void init(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ClockView);
        circleColor = array.getColor(R.styleable.CircleView_circle_color, Color.BLACK);
        panelColor = array.getColor(R.styleable.CircleView_panel_color, Color.BLACK);
        pointColor = array.getColor(R.styleable.CircleView_point_color, Color.BLACK);
        pointRadiu = array.getDimension(R.styleable.CircleView_point_size, dpToPx(20));
    }

    private void initPaint() {
        mainPain = new Paint();
        mainPain.setAntiAlias(true);//抗锯齿
        mainPain.setDither(true);//抖动

        rightPath = new Path();
        rightRegion = new Region();
        leftPath = new Path();
        leftRegion = new Region();

        mMapMatrix = new Matrix();
        toast = Toast.makeText(getContext(), "right", Toast.LENGTH_SHORT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMapMatrix.reset();
        // ▼将剪裁边界设置为视图大小
        Region globalRegion = new Region(-w, -h, w, h);


        int pathRadiu = w / 4;
        RectF upCircle = new RectF(-pathRadiu, -pathRadiu * 2, pathRadiu, 0);
        RectF downCircle = new RectF(-pathRadiu, 0, pathRadiu, pathRadiu * 2);
        RectF fullCircle = new RectF(-2 * pathRadiu, -pathRadiu * 2, pathRadiu * 2, pathRadiu * 2);

        float wweepAngle = 180;

        rightPath.addArc(fullCircle, -90, wweepAngle);
        rightPath.arcTo(downCircle, 90, -wweepAngle, false);
        rightPath.arcTo(upCircle, 90, wweepAngle, false);
        rightPath.close();

        leftPath.addArc(fullCircle, 90, wweepAngle);
        leftPath.arcTo(upCircle, -90, -wweepAngle, false);
        leftPath.arcTo(downCircle, -90, wweepAngle, false);
        leftPath.close();

        rightRegion.setPath(rightPath, globalRegion);// ▼将 Path 添加到 Region 中
        leftRegion.setPath(leftPath, globalRegion);// ▼将 Path 添加到 Region 中

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int maxWidth = wm.getDefaultDisplay().getWidth();

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {//精确的
            maxWidth = Math.min(width, maxWidth);
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            maxWidth = Math.min(height, maxWidth);
        }
        setMeasuredDimension(maxWidth, maxWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getWidth() / 2, getHeight() / 2);

        // 获取测量矩阵(逆矩阵)
        if (mMapMatrix.isIdentity()) {
            canvas.getMatrix().invert(mMapMatrix);
        }

        paintCircle(canvas);//绘制外圆背景
        paintRightPath(canvas);//中间右弧线和圆饼
        paintLeftPath(canvas);//中间左弧线和圆饼

        // 绘制触摸区域颜色
        if (currentFlag == 0) {
            mainPain.setColor(Color.YELLOW);
            mainPain.setStyle(Paint.Style.FILL);
            paintRightPath(canvas);
        } else if (currentFlag == 1) {
            mainPain.setColor(Color.YELLOW);
            mainPain.setStyle(Paint.Style.FILL);
            paintLeftPath(canvas);
        }

        float x = down_x;
        float y = down_y;

        // ▼注意画布平移
//        canvas.translate(-getWidth()/2,- getHeight()/2);
        if (x == -1 && y == -1) return;          // 如果没有就返回
        mainPain.setColor(Color.GREEN);
        mainPain.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x-getWidth()/2,y- getHeight()/2,50,mainPain); // 在触摸位置绘制一个小圆
    }

    int downFlag = -1;//-1不在圆内，0右边，1左边
    int currentFlag = -1;

    float down_x = -1;
    float down_y = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float[] pts = new float[2];
        pts[0] = event.getX() - getWidth()/2;
        pts[1] = event.getY() - getHeight()/2;
//        mMapMatrix.mapPoints(pts);

        int x = (int) pts[0];
        int y = (int) pts[1];

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downFlag = getTouchedPath(x, y);
                currentFlag = downFlag;

                down_x = event.getX();
                down_y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                currentFlag = getTouchedPath(x, y);

                down_x = event.getX();
                down_y = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                currentFlag = getTouchedPath(x, y);
                // 如果手指按下区域和抬起区域相同且不为空，则判断点击事件
                if (currentFlag == downFlag && currentFlag != -1) {
                    if (currentFlag == 0) {
                        toast.cancel();
                        toast = Toast.makeText(getContext(), "right click", Toast.LENGTH_SHORT);
                        toast.show();
                    } else if (currentFlag == 1) {
                        toast.cancel();
                        toast = Toast.makeText(getContext(), "left click", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                downFlag = currentFlag = -1;

//                down_x = down_y = -1;
                break;
            case MotionEvent.ACTION_CANCEL:
                downFlag = currentFlag = -1;

//                down_x = down_y = -1;
                break;
        }
        invalidate();
        return true;
    }

    // 获取当前触摸点在哪个区域
    int getTouchedPath(int x, int y) {
        if (rightRegion.contains(x, y)) {
            return 0;
        } else if (leftRegion.contains(x, y)) {
            return 1;
        }
        return -1;
    }

    public void paintRightPath(Canvas canvas) {
        mainPain.setColor(Color.WHITE);
        mainPain.setStyle(Paint.Style.FILL);
        canvas.drawPath(rightPath, mainPain);

        mainPain.setColor(Color.BLACK);
        mainPain.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, -getHeight() / 4, pointRadiu, mainPain);
    }

    public void paintLeftPath(Canvas canvas) {
        mainPain.setColor(Color.BLACK);
        mainPain.setStyle(Paint.Style.FILL);
        canvas.drawPath(leftPath, mainPain);

        mainPain.setColor(Color.WHITE);
        mainPain.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, getHeight() / 4, pointRadiu, mainPain);
    }

    public void paintCircle(Canvas canvas) {
        mainPain.setColor(panelColor);
        mainPain.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(0, 0, getWidth() / 2, mainPain);
    }

    public int dpToPx(int dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dpValue * scale);
    }
}
