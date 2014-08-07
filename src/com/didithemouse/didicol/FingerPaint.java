package com.didithemouse.didicol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class FingerPaint extends SurfaceView implements SurfaceHolder.Callback {

	final static int canvasHeight = CreateActivity.canvasHeight;
	final static int canvasWidth = CreateActivity.canvasWidth;
	
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    
    private Paint mPaint;
    
    private SurfaceHolder sh;
    
    private boolean canDrag = true;

    int color = 0xFF000000;
    int clearColor = 0x00FFFFFF;
    
    public FingerPaint(Context c) {
        super(c);
        sh = getHolder();
        sh.addCallback(this);
        this.setZOrderOnTop(true);
        //this.setZOrderOnTop(false);
        sh.setFormat(PixelFormat.TRANSPARENT);

        //setCleaner();
       
        //Par�metros de la brocha
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//true);
        mPaint.setDither(true);//true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(8);
                
        isPainting = true;
        
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mCanvas = new Canvas();
    }
    
    public void setBitmap(Bitmap bm)
    {
    	if (bm == null) return;
    	mBitmap=bm;
    	mCanvas.setBitmap(bm);
    	surfaceCreated(sh);
    }
    
    boolean isPainting = true;
    public void setDraw()
    {
        mPaint.setColor(color);
    	mPaint.setXfermode(null);
        isPainting = true;
        canDrag=false;
        setSize(8);
    }

    public void setErase()
    {
    	mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
    	mPaint.setColor(clearColor);
    	setSize(31);
        isPainting = false;
        canDrag=false;
    }
    
    public void setSize(int size)
    {
        mPaint.setStrokeWidth(size);
        canDrag=false;
    }
    
    public void setColor(int _color){
    	color = _color;
    }
    
    protected void drawx(Canvas canvas){
        if(canvas != null)
        {
    	canvas.drawColor(clearColor, Mode.MULTIPLY);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
       // cleanButton(canvas);
        sh.unlockCanvasAndPost(canvas);
        }
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    MochilaContents mc = MochilaContents.getInstance();
    private void touch_start(float x, float y) {
    	mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mPaint.setStyle(Style.FILL);
        mCanvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        mCanvas.drawCircle(x, y, mPaint.getStrokeWidth()/2, mPaint);
        Canvas c = sh.lockCanvas();
        if (c != null)
        {
        	c.drawColor(clearColor, Mode.MULTIPLY);
            c.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            c.drawCircle(x, y, mPaint.getStrokeWidth()/2, mPaint);
            //cleanButton(c);
            sh.unlockCanvasAndPost(c);
        }
        mPaint.setStyle(Style.STROKE);
    }
    private void touch_move(float x, float y) {
    	mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
        mX = x;
        mY = y;
    }
    private void touch_up() {
    	mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    
    //Avoid infinite recursion (?)
    MotionEvent me = null;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
    	//EVITAR Multitouch
    	if(event.getPointerCount() > 1) return true;
    	    	
    	float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                drawx(sh.lockCanvas());
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = (x - mX) >= 0? x - mX : mX - x;
                float dy = (y - mY) >= 0? y - mY : mY - y;
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                touch_move(x, y);
                drawx(sh.lockCanvas());
                }
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                drawx(sh.lockCanvas());
                break;
        }
        return true;
    }
    

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
    	Canvas c = sh.lockCanvas();
    	if (c != null)
    	{
    		c.drawColor(clearColor, Mode.MULTIPLY);
            c.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    		sh.unlockCanvasAndPost(c);
    	}
	}     

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
    
}
