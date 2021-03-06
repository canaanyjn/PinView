package com.canaanyjn.pinview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by canaan on 2015/9/23 0023.
 */
public class PinView extends View {
    private static final String TAG = PinView.class.getSimpleName();
    private Paint cellPaint;
    private Paint pathPaint;
    private Paint errorPaint;
    private CellManager mCellManager;
    private List<Integer> choosedCellNum;

    private ValueAnimator mCellChoosedAnimation;
    private ValueAnimator mErrorAnimation;

    private int width,height;
    private float rowDistance;
    private float colummDistance;
    private float lastX,lastY;
    private boolean isReleased = true;
    private boolean isError = false;

    private int cellRowNumber = 3;
    private int cellColummNumber = 3;
    private float cellRadius = 20;
    private int cellColor;
    //TODO:add th attr
    private int errorColor;
    private int mLastChoosedCell = -1,midChoosedCell = -1;
    private float choosedCellRadius;
    private float choosedCellX,choosedCellY;
    private String key;
    final int END = 102;


    private int pathColor;
    private float pathWidth;


    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case END:
                    solveError();
            }
        }
    };
    TimerTask task;
    Timer timer;

    public PinView(Context context) {
        super(context);
    }

    public PinView(Context context, AttributeSet attrs) {
        super(context,attrs);
        init(context, attrs);
    }

    public PinView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context,AttributeSet attrs) {
        initAttrs(context, attrs);
        initCellPaint();
        initPathPaint();
        initAnimation();
        choosedCellNum = new ArrayList();
        mCellManager = new CellManager(cellRowNumber,cellColummNumber,cellRadius);
    }

    private void initAttrs(Context context,AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PinView,0,0);
            cellRowNumber = typedArray.getInteger(R.styleable.PinView_rowNumber, 3);
            key = typedArray.getString(R.styleable.PinView_key);
            cellColummNumber = typedArray.getInteger(R.styleable.PinView_colummNumber, 3);
            cellColor = typedArray.getColor(R.styleable.PinView_cellColor, Color.BLACK);
            errorColor = typedArray.getColor(R.styleable.PinView_errorColor,Color.YELLOW);
            cellRadius = typedArray.getFloat(R.styleable.PinView_cellRadius, 20.0f);
            pathColor = typedArray.getColor(R.styleable.PinView_pathColor,Color.BLACK);
            pathWidth = typedArray.getFloat(R.styleable.PinView_pathWidth,10.0f);
            typedArray.recycle();
        }
    }

    private void initDistance() {
        rowDistance = (height - getPaddingTop() - getPaddingBottom() - (cellColummNumber+1)*cellRadius) / (cellColummNumber- 1);
        colummDistance = (width - getPaddingLeft() - getPaddingRight() - (cellRowNumber+1)*cellRadius) / (cellRowNumber - 1);
    }

    private void initCellPaint() {
        cellPaint = new Paint();
        cellPaint.setColor(cellColor);
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setAntiAlias(true);

        errorPaint = new Paint();
        errorPaint.setColor(errorColor);
        errorPaint.setStyle(Paint.Style.FILL);
        errorPaint.setAntiAlias(true);
    }

    private void initPathPaint() {
        pathPaint = new Paint();
        pathPaint.setColor(pathColor);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(pathWidth);
        pathPaint.setAntiAlias(true);

    }

    private void initCellManager() {
        mCellManager.setColummDistance(colummDistance);
        mCellManager.setRowDistance(rowDistance);
        mCellManager.setStartX(2 * cellRadius + getPaddingLeft());
        mCellManager.setStartY(2 * cellRadius + getPaddingTop());
        mCellManager.initCells();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(width, height);
        initDistance();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initCellManager();
        List<Cell> mCells = mCellManager.getCells();

        for (int i = 0;i<mCells.size();i++) {
            Cell cell = mCells.get(i);
            if (!isError && i == mLastChoosedCell) {
                canvas.drawCircle(cell.getX(), cell.getY(), choosedCellRadius, cellPaint);
            } else if (!isError && i == midChoosedCell) {
                canvas.drawCircle(cell.getX(), cell.getY(), choosedCellRadius, cellPaint);
            } else{
                canvas.drawCircle(cell.getX(), cell.getY(), cellRadius, cellPaint);
            }
        }

        //if error,draw the error ball
        if (isError){
            for (int i = 0;i<choosedCellNum.size();i++) {
                Cell cell = mCells.get(choosedCellNum.get(i));
                canvas.drawCircle(cell.getX(),cell.getY(),cellRadius,errorPaint);
            }
        }

        if (!isReleased&&choosedCellNum.size() > 0) {
            for (int j = 0;j<choosedCellNum.size()-1;j++) {
                Cell cellA = mCells.get(choosedCellNum.get(j));
                Cell cellB = mCells.get(choosedCellNum.get(j+1));
                canvas.drawLine(cellA.getX(),cellA.getY(),cellB.getX(),cellB.getY(),pathPaint);
            }
            if (!isError) {
                Cell cell = mCells.get(choosedCellNum.get(choosedCellNum.size()-1));
                canvas.drawLine(cell.getX(),cell.getY(),lastX,lastY,pathPaint);
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(TAG, "midCellNum---->" + midChoosedCell);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isError) {
                    solveError();
                }
                mLastChoosedCell = mCellManager.setDownPosition(x,y);
                if (mLastChoosedCell != -1) {
                    startChoosedAnimation();
                    choosedCellNum.add(mLastChoosedCell);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLastChoosedCell != -1) {
                    isReleased = false;
                    lastX = x;
                    lastY = y;
                    int choosedCell = mCellManager.setDownPosition(x, y);
                    if (choosedCell != -1 && choosedCell != mLastChoosedCell
                            && !choosedCellNum.contains(choosedCell)) {
                        int newMidChoosedCell = getMidCellNum(mLastChoosedCell,choosedCell);
                        mLastChoosedCell = choosedCell;
                        if (newMidChoosedCell != -1 && !choosedCellNum.contains(newMidChoosedCell)) {
                            midChoosedCell = newMidChoosedCell;
                            choosedCellNum.add(newMidChoosedCell);
                        }
                        Log.d(TAG, "midChoosedCell--->" + midChoosedCell);
                        choosedCellNum.add(mLastChoosedCell);
                        startChoosedAnimation();

                    } else {
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!getPinString().equals(key)) {
                    isError = true;
                    startErrorAnimation();
                } else {
                    isReleased = true;
                    choosedCellNum.clear();
                    invalidate();
                }
                break;
        }
        return true;
    }

    private int getMidCellNum(int lastChoosedCell,int choosedCell) {
        int xA = lastChoosedCell/3;
        int yA = lastChoosedCell%3;
        int xB = choosedCell/3;
        int yB = choosedCell%3;
        if (((xA == xB)&&(Math.abs(yA-yB)==2))||((yA == yB)&&(Math.abs(xA-xB)==2))
                ||(Math.abs(xA-xB)+Math.abs(yA-yB))==4) {
            int xC = (xA+xB)/2;
            int yC = (yA+yB)/2;
            return xC*3+yC;
        }
        return -1;
    }

    public String getPinString() {
        String result = "";
        for (int i = 0;i<choosedCellNum.size();i++) {
            result = result + " " + choosedCellNum.get(i);
        }
        Log.d(TAG, "result--->" + result);
        return result;
    }

    private void initAnimation() {
        mCellChoosedAnimation = ValueAnimator.ofFloat(1,1.5f);
        mCellChoosedAnimation.setDuration(170)
                .setInterpolator(new AccelerateDecelerateInterpolator());
        mCellChoosedAnimation.setRepeatMode(ValueAnimator.REVERSE);
        mCellChoosedAnimation.setRepeatCount(1);
        mCellChoosedAnimation.addUpdateListener(cellChoosedUpdateListener);
        mCellChoosedAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                midChoosedCell = -1;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void solveError() {
        isReleased = true;
        isError = false;
        pathPaint.setColor(pathColor);
        choosedCellNum.clear();
        invalidate();
        timer.cancel();
        task.cancel();
    }

    private void startErrorAnimation() {
        pathPaint.setColor(errorColor);

        //error last for one second
        timer = new Timer();
        task  = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = END;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task,1000);

    }

    private void startChoosedAnimation() {
        mCellChoosedAnimation.start();
    }

    ValueAnimator.AnimatorUpdateListener cellChoosedUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float progress =  ((Float)animation.getAnimatedValue()).floatValue();
            choosedCellRadius = cellRadius *progress;
            invalidate();
        }
    };

}
