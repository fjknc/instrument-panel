package com.portman.panel.uh1h;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class ArtificialHorizon extends View {

	private static final String TAG = ArtificialHorizon.class.getSimpleName();

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	
	private RectF faceRect;
	private Paint facePaint;
	
	private Paint scalePaint;
	private RectF scaleRect;
			
	private Paint needlePaint;
	
	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; // holds the cached static part
	
	// scale configuration	
	private static final float minPitchValue = (float) -Math.PI / 3.0f;
	private static final float maxPitchValue = (float) Math.PI / 3.0f;
	private static final float minBankValue = (float) -Math.PI;
	private static final float maxBankValue = (float) Math.PI;
	
	// hand dynamics
	private float pitch = 0f;
	private float bank = 0f;
	
	public ArtificialHorizon(Context context) {
		super(context);
		init();
	}

	public ArtificialHorizon(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ArtificialHorizon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
		
		pitch = bundle.getFloat("pitch");
		bank = bundle.getFloat("bank");		
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putFloat("pitch", pitch);
		state.putFloat("bank", bank);		
		return state;
	}

	private void init() {
		initDrawingTools();
	}

	private void initDrawingTools() {
		rimRect = new RectF(1f, 1f, 99f, 99f);

		rimPaint = new Paint();
		rimPaint.setAntiAlias(true);
		rimPaint.setColor(Color.LTGRAY);

		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.GRAY);
		rimCirclePaint.setStrokeWidth(1f);

		float rimSize = 2f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
			     rimRect.right - rimSize, rimRect.bottom - rimSize);
		
		facePaint = new Paint();
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setColor(Color.BLACK);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.STROKE);
		scalePaint.setColor(Color.WHITE);
		scalePaint.setStrokeWidth(2f);
		scalePaint.setAntiAlias(true);	
		
		float scalePosition = 3f;
		scaleRect = new RectF();
		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
					  faceRect.right - scalePosition, faceRect.bottom - scalePosition);

		needlePaint = new Paint();
		needlePaint.setAntiAlias(true);
		needlePaint.setColor(Color.WHITE);
		needlePaint.setStrokeWidth(2f);
		needlePaint.setStyle(Paint.Style.FILL_AND_STROKE);	
		
		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
		Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);
		
		int chosenDimension = Math.min(chosenWidth, chosenHeight);
		
		setMeasuredDimension(chosenDimension, chosenDimension);
	}
	
	private int chooseDimension(int mode, int size) {
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		} 
	}
	
	// in case there is no size specified
	private int getPreferredSize() {
		return 300;
	}

	private void drawRim(Canvas canvas) {
		// first, draw the metallic body
		canvas.drawOval(rimRect, rimPaint);
	}
	
	private void drawFace(Canvas canvas) {		
		canvas.drawOval(faceRect, facePaint);
		// draw the inner rim circle
		canvas.drawOval(faceRect, rimCirclePaint);
	}

	private void drawScale(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(-90, 50f, 50f);
		for (int i = 0; i < 7; ++i) {
			float y1 = scaleRect.top;
			float y2 = y1 + 3f;
			
			if (i % 3 == 0) // big tick
				canvas.drawLine(50f, y1, 50f, y2 + 5f, scalePaint);
			else  //small tick
				canvas.drawLine(50f, y1, 50f, y2, scalePaint);
			
			canvas.rotate(30, 50f, 50f);
		}
		canvas.restore();	
		
		// draw plane symbol
		canvas.drawLine(25f, 50f, 40f, 50f, scalePaint);
		canvas.drawLine(49f, 50f, 51f, 50f, scalePaint);
		canvas.drawLine(60f, 50f, 75f, 50f, scalePaint);
		canvas.drawLine(45f, 60f, 55f, 60f, scalePaint);
	}
		
	private void drawNeedle(Canvas canvas) {
		float bankAngle = (float) Math.toDegrees(bank);
		float pitchShift = -pitch * 100f / 2f;
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		
		// set clip
		Path path = new Path();
		path.addCircle(50f, 50f, 35f, Path.Direction.CW);
		canvas.clipPath(path);
		
		canvas.rotate(bankAngle, 50f, 50f);
		// draw bank needle
		canvas.drawLine(50f, 15f, 50f, 25f, needlePaint);
		
		// draw horizon
		canvas.translate(0.0f, pitchShift);
		canvas.drawLine(10f, 50f, 90f, 50f, needlePaint);
		canvas.restore();
	}

	private void drawBackground(Canvas canvas) {
		if (background == null) {
			Log.w(TAG, "Background not created");
		} else {
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		drawBackground(canvas);

		float scale = (float) getWidth();		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale / 100f, scale / 100f);

		drawNeedle(canvas);
		
		canvas.restore();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d(TAG, "Size changed to " + w + "x" + h);
		
		regenerateBackground();
	}
	
	private void regenerateBackground() {
		// free the old bitmap
		if (background != null) {
			background.recycle();
		}
		
		background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		float scale = (float) getWidth();		
		backgroundCanvas.scale(scale / 100f, scale / 100f);  // 0 - 100 coordinates
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);
		drawScale(backgroundCanvas);		
	}
		
	public void setPitchAndBank(float pitch, float bank) {
		if (pitch < minPitchValue) {
			pitch = minPitchValue;
		} else if (pitch > maxPitchValue) {
			pitch = maxPitchValue;
		}
		this.pitch = pitch;
		
		if (bank < minBankValue) {
			bank = minBankValue;
		} else if (bank > maxBankValue) {
			bank = maxBankValue;
		}
		this.bank = bank;
		invalidate();
	}
}
