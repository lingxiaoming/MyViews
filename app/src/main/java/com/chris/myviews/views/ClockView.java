package com.chris.myviews.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.chris.myviews.R;

import java.util.Calendar;

import static android.R.attr.radius;

/**
 * TODO 这里描述下用途吧
 * Created by lingxiaoming on 2016/10/19 0019.
 */

public class ClockView extends View {
    private float hourLength;//时针长度
    private float hourHeight;//时针宽度
    private int hourColor;//时针颜色

    private float minLength;//分针长度
    private float minHeight;//分针宽度
    private int minColor;//分针颜色

    private float secondLength;//秒针长度
    private float secondHeight;//秒针宽度
    private int secondColor;//秒针颜色

    private Paint clockBoardPaint;//表盘
    private int clockBoardRadiu;//表盘半径
    private int endLength;//指针尾长

    private int boradlineLongLenght = 60;//表盘长线条长度
    private int boradlineLongWidth = 8;//表盘长线条宽度
    private int boradlineShortLength = 20;//表盘短线条长度
    private int boradlineShortWidth = 4;//表盘短线条宽度


    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);//获取自定义的属性
        initPaint();//初始化画笔
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ClockView);

        hourLength = array.getDimension(R.styleable.ClockView_hour_length, dpToPx(120));
        hourHeight = array.getDimension(R.styleable.ClockView_hour_height, dpToPx(6));
        hourColor = array.getColor(R.styleable.ClockView_hour_color, Color.BLUE);

        minLength = array.getDimension(R.styleable.ClockView_min_length, dpToPx(150));
        minHeight = array.getDimension(R.styleable.ClockView_min_height, dpToPx(4));
        minColor = array.getColor(R.styleable.ClockView_min_color, Color.GREEN);

        secondLength = array.getDimension(R.styleable.ClockView_second_length, dpToPx(180));
        secondHeight = array.getDimension(R.styleable.ClockView_second_height, dpToPx(2));
        secondColor = array.getColor(R.styleable.ClockView_second_color, Color.RED);
    }

    private void initPaint() {
        clockBoardPaint = new Paint();
        clockBoardPaint.setAntiAlias(true);//抗锯齿
        clockBoardPaint.setDither(true);//抖动
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clockBoardRadiu = Math.min(w, h) / 2;
        endLength = clockBoardRadiu / 6; //尾部指针默认为半径的六分之一
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);

        paintCircle(canvas);//绘制外圆背景
        paintSurroundLine(canvas);//绘制围绕表盘的刻度
        drawTime(canvas);//绘制时间
        paintCenterPoint(canvas);//绘制中心圆点
        canvas.restore();

        postInvalidateDelayed(10);
    }


    public void drawTime(Canvas canvas) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); //时
        int minute = calendar.get(Calendar.MINUTE); //分
        int second = calendar.get(Calendar.SECOND); //秒
        int millisecond = calendar.get(Calendar.MILLISECOND); //毫秒

        float angleSecond = second * 360 / 60 + 6 * millisecond / 1000.0f; //秒针转过的角度
        float angleMinute = minute * 360 / 60 +  angleSecond / 60; //分针转过的角度
        float angleHour = (hour % 12) * 360 / 12 + angleMinute / 12; //时针转过的角度

        //绘制时针
        canvas.save();
        canvas.rotate(angleHour); //旋转到时针的角度
        RectF rectFHour = new RectF(-hourHeight / 2, -hourLength, hourHeight / 2, endLength);
        clockBoardPaint.setColor(hourColor); //设置指针颜色
        clockBoardPaint.setStyle(Paint.Style.STROKE);
        clockBoardPaint.setStrokeWidth(hourHeight); //设置边界宽度
        canvas.drawRoundRect(rectFHour, radius, radius, clockBoardPaint); //绘制时针
        canvas.restore();
        //绘制分针
        canvas.save();
        canvas.rotate(angleMinute);
        RectF rectFMinute = new RectF(-minHeight / 2, -minLength, minHeight / 2, endLength);
        clockBoardPaint.setColor(minColor);
        clockBoardPaint.setStrokeWidth(minHeight);
        canvas.drawRoundRect(rectFMinute, radius, radius, clockBoardPaint);
        canvas.restore();
        //绘制秒针
        canvas.save();
        canvas.rotate(angleSecond);
        RectF rectFSecond = new RectF(-secondHeight / 2, -secondLength, secondHeight / 2, endLength);
        clockBoardPaint.setColor(secondColor);
        clockBoardPaint.setStrokeWidth(secondHeight);
        canvas.drawRoundRect(rectFSecond, radius, radius, clockBoardPaint);
        canvas.restore();
        //绘制自己的view
        int radiu = 650;//半径
        canvas.save();
        canvas.rotate((hour % 12) * 360 / 12);
        clockBoardPaint.setColor(Color.CYAN);
        clockBoardPaint.setStyle(Paint.Style.STROKE);
        clockBoardPaint.setAlpha(150);
        clockBoardPaint.setStrokeWidth(secondHeight);
        RectF rect = new RectF(-radiu, -radiu, radiu, radiu);
        canvas.drawArc(rect, -90, 30, true, clockBoardPaint);
        canvas.restore();

        float radiu2 = radiu * (minute + second / 60.0f) / 60.0f;//分针半径 = 半径 * 分钟 / 60
        canvas.save();
        canvas.rotate((hour % 12) * 360 / 12);
        clockBoardPaint.setColor(Color.YELLOW);
        clockBoardPaint.setStyle(Paint.Style.FILL);
        clockBoardPaint.setAlpha(150);
        clockBoardPaint.setStrokeWidth(secondHeight);
        RectF rect2 = new RectF(-radiu2, -radiu2, radiu2, radiu2);
        canvas.drawArc(rect2, -90, 30, true, clockBoardPaint);
        canvas.restore();

        float radiu3 = radiu2 * second / 60.0f + radiu2 * (millisecond / 1000.0f / 60);//秒针半径 = 半径 * 秒钟 / 60second * 360 / 60 + 6 * millisecond / 1000.0f
        canvas.save();
        canvas.rotate((hour % 12) * 360 / 12);
        clockBoardPaint.setColor(Color.MAGENTA);
        clockBoardPaint.setStyle(Paint.Style.FILL);
        clockBoardPaint.setAlpha(150);
        clockBoardPaint.setStrokeWidth(secondHeight);
        RectF rect3 = new RectF(-radiu3, -radiu3, radiu3, radiu3);
        canvas.drawArc(rect3, -90, 30, true, clockBoardPaint);
        canvas.restore();

    }

    public void paintCircle(Canvas canvas) {
        clockBoardPaint.setColor(Color.WHITE);
        clockBoardPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, clockBoardRadiu, clockBoardPaint);
    }

    public void paintCenterPoint(Canvas canvas) {
        clockBoardPaint.setColor(Color.RED);
        clockBoardPaint.setStyle(Paint.Style.FILL);
//        clockBoardPaint.setAlpha(150);
        canvas.drawCircle(0, 0, clockBoardRadiu / 40, clockBoardPaint);
    }

    public void paintSurroundLine(Canvas canvas) {
        for (int i = 0; i < 60; i++) {

            if (i % 5 == 0) {//长线条
                clockBoardPaint.setColor(Color.BLACK);
                clockBoardPaint.setStyle(Paint.Style.FILL);
                clockBoardPaint.setStrokeWidth(boradlineLongWidth);
                canvas.drawLine(clockBoardRadiu - boradlineLongLenght, 0, clockBoardRadiu, 0, clockBoardPaint);
                clockBoardPaint.setTextSize(64);
                String textContent = ((i / 5) == 0 ? 12 : (i / 5)) + "";
                Rect textBound = new Rect();
                clockBoardPaint.getTextBounds(textContent, 0, textContent.length(), textBound);
                int w = textBound.width();
                int h = textBound.height();
                canvas.save();
                canvas.rotate(-90);
                canvas.drawText(textContent, clockBoardRadiu - boradlineLongLenght - w - 10, h / 2, clockBoardPaint);
                canvas.restore();
            } else {//短线条
                clockBoardPaint.setColor(Color.BLACK);
                clockBoardPaint.setStyle(Paint.Style.FILL);
                clockBoardPaint.setStrokeWidth(boradlineShortWidth);
                canvas.drawLine(clockBoardRadiu - boradlineShortLength, 0, clockBoardRadiu, 0, clockBoardPaint);
            }

            canvas.rotate(6);
        }
    }


    public int dpToPx(int dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dpValue * scale);
    }
}
