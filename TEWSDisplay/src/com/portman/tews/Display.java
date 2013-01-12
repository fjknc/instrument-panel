package com.portman.tews;

import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class Display extends View {

	private static final String TAG = Display.class.getSimpleName();

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	
	private RectF faceRect;
	private Paint facePaint;
	
	private Paint scalePaint;
	private RectF scaleRect;
	
	private Paint symbolTextPaint;	
	private Paint symbolPaint;	

	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; // holds the cached static part
	
	// scale configuration
	private static final int totalNicks = 12;
	private static final float degreesPerNick = 360f / totalNicks;
	
	private JSONObject jsonThreats = null;
	
	private HashMap<String, String> emiterNames;
	private HashSet<String> airborneTypes;
	
	private int cycle = 0;
	
	public Display(Context context) {
		super(context);
		init();
	}

	public Display(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Display(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		return state;
	}

	private void init() {
		initDrawingTools();
		
		// init emiter names
		// air
		emiterNames = new HashMap<String, String>(50);
		emiterNames.put("mig-23", 	"23");
		emiterNames.put("mig-29c", 	"29");
		emiterNames.put("su-27", 	"29");
		emiterNames.put("su-33", 	"29");
		emiterNames.put("mig-31", 	"31");
		emiterNames.put("su-30", 	"30");
		emiterNames.put("F-4E", 	"F4");
		emiterNames.put("f-14A", 	"14");
		emiterNames.put("F-15C", 	"15");
		emiterNames.put("F-16c", 	"16");
		emiterNames.put("f-18a", 	"18");
		emiterNames.put("f-18c", 	"18");
		emiterNames.put("a-50", 	"50");
		emiterNames.put("e-2c hawkeye", "E2");
		emiterNames.put("e-3a", 	"E3");
		
		//ship
		emiterNames.put("Albatros ",		"HP");
		emiterNames.put("TAKR Kuznetsov",	"SW");
		emiterNames.put("Rezky ",		"TP");
		emiterNames.put("Moscow", 		"T2");
		emiterNames.put("Neustrashimy", "TP");
		emiterNames.put("CVN-70 Vinson", 	"SP");
		emiterNames.put("FFG-7 Oliver H. Perry class", "SM");
		emiterNames.put("SG-47 Ticonderoga class", "SM");
		
		// ground
		emiterNames.put("s-300ps 40b6m tr", "10");
		emiterNames.put("s-300ps 40b6md sr","CS");
		emiterNames.put("s-300ps 64h6e sr", "BB");
		emiterNames.put("buk 9s18m1 sr", 	"SD");
		emiterNames.put("buk 9a310m1 ln", 	"11");
		emiterNames.put("kub 1s91 str",		"6");
		emiterNames.put("osa 9a33 ln", 		"08");
		emiterNames.put("strela-10 9a35", 	"13");
		emiterNames.put("Dog Ear Radar", 	"DE");
		emiterNames.put("tor 9a331", 		"15");
		emiterNames.put("tunguska 2c6m", 	"S6");
		emiterNames.put("shilka zsu-23-4", 	"23");
		emiterNames.put("roland ads", 		"RO");
		emiterNames.put("patriot cp",	 	"P");
		emiterNames.put("patriot str",	 	"P");
		emiterNames.put("gepard", 			"GP");
		emiterNames.put("hawk ln", 			"HA");
		emiterNames.put("hawk tr", 			"H");
		emiterNames.put("M163 Vulcan", 		"VU");
		emiterNames.put("55g6 ewr station", "S6");
		
		// init air emiter types
		airborneTypes = new HashSet<String>(20);
		airborneTypes.add("mig-23");
		airborneTypes.add("mig-29c");
		airborneTypes.add("su-27");
		airborneTypes.add("su-33");
		airborneTypes.add("mig-31");
		airborneTypes.add("su-30");
		airborneTypes.add("F-4E");
		airborneTypes.add("f-14A");
		airborneTypes.add("F-15C");
		airborneTypes.add("F-16C");
		airborneTypes.add("f-18a");
		airborneTypes.add("f-18c");
		airborneTypes.add("a-50");
		airborneTypes.add("e-2c hawkeye");
		airborneTypes.add("e-3a");
		
		// init test threats
		try {
			jsonThreats = (JSONObject) new JSONTokener("{ 'Mode':1, 'Emiters':[{'ID':'1', 'Power':0.4, 'Azimuth':0.6, 'Priority':200, 'SignalType':'scan', 'Type':'mig-29'}, {'ID':'2', 'Power':0.8, 'Azimuth':-1.2, 'Priority':210, 'SignalType':'missile_radio_guided', 'Type':'su-30'}]}").nextValue();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // test threats
	}

	private void initDrawingTools() {
		rimRect = new RectF(10f, 10f, 990f, 990f);

		rimPaint = new Paint();
		rimPaint.setAntiAlias(true);
		rimPaint.setColor(Color.GRAY);

		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.DKGRAY);
		rimCirclePaint.setStrokeWidth(10f);

		float rimSize = 20f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
			     rimRect.right - rimSize, rimRect.bottom - rimSize);
		
		facePaint = new Paint();
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setColor(Color.BLACK);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		scalePaint.setColor(Color.GREEN);
		scalePaint.setStrokeWidth(10f);
		scalePaint.setAntiAlias(true);
		
		float scalePosition = 30f;
		scaleRect = new RectF();
		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
					  faceRect.right - scalePosition, faceRect.bottom - scalePosition);

		symbolTextPaint = new Paint();
		symbolTextPaint.setColor(Color.GREEN);
		symbolTextPaint.setAntiAlias(true);
		symbolTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		symbolTextPaint.setTextAlign(Paint.Align.CENTER);
		symbolTextPaint.setTextSize(40f);

		symbolPaint = new Paint();
		symbolPaint.setAntiAlias(true);
		symbolPaint.setColor(Color.GREEN);
		symbolPaint.setStrokeWidth(5f);
		symbolPaint.setStyle(Paint.Style.STROKE);	
		
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
		
		// draw center mark
		canvas.drawLine(460f, 500f, 540f, 500f, scalePaint);
		canvas.drawLine(500f, 460f, 500f, 540f, scalePaint);
		
		for (int i = 0; i < totalNicks; ++i) {			
			canvas.drawCircle(500f, scaleRect.top, 5, scalePaint);
	
			canvas.rotate(degreesPerNick, 500f, 500f);
		}
		canvas.restore();		
	}
	
	private void drawThreats(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);

		if (cycle++ > 10) // flickering cycle
			cycle = 0;
		
		// iterate all emiters
		JSONArray emiters;
		float maxPriority = 0;
		
		try {
			emiters = jsonThreats.getJSONArray("Emiters");
			
			// find max priority
			for (int i = 0; i < emiters.length(); i++) {
			    JSONObject emiter = emiters.getJSONObject(i);
			    float priority = (float) (emiter.getDouble("Priority"));
			    if (priority > maxPriority)
			    	maxPriority = priority;
			}

			for (int i = 0; i < emiters.length(); i++) {
			    JSONObject emiter = emiters.getJSONObject(i);
			    float azimuth = (float) (emiter.getDouble("Azimuth") * 180f/ Math.PI);
			    float priority = (float) (emiter.getDouble("Priority")); // 160, 180, 360
			    float power = (float) (emiter.getDouble("Power")); // 0 - 1 (> 0,5 inner circle)
			    String signalType = emiter.getString("SignalType"); // scan, lock, missile_radio_guided, track_while_scan
			    String type = emiter.getString("Type"); // mig-29c, osa, etc...
			    			    
			    canvas.save(Canvas.MATRIX_SAVE_FLAG);
			    
				canvas.rotate(azimuth, 500f, 500f);
				
				// draw emiter ID
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				float x = 500f;
				float y = 500f;
				canvas.translate(0f, -100 - (1f - power) * 300f);
				canvas.rotate(-azimuth, x, y + 15f);
				
				if (!emiterNames.containsKey(type))
					canvas.drawText(type, x, y + 15f, symbolTextPaint);
				else
					canvas.drawText(emiterNames.get(type), x, y + 15f, symbolTextPaint);
				
				if (signalType.contentEquals("missile_radio_guided") && cycle < 5) { // draw circle - missile launch
					canvas.drawCircle(x, y, 50f, symbolPaint);
				}
				
				// check airborne
				if (airborneTypes.contains(type) && !signalType.contentEquals("missile_radio_guided")) {
					canvas.drawLine(490f, 480f, 500f, 470f, symbolPaint);
					canvas.drawLine(500f, 470f, 510f, 480f, symbolPaint);					
				}
				
				//if (signalType.contentEquals("missile_radio_guided")) { // draw circle - missile in the air
				//	if (cycle < 5)
				//		canvas.drawCircle(x, y, 50f, symbolPaint);
				//	canvas.drawText("M", x, y + 15f, symbolTextPaint);
				//}
				
				if (priority == maxPriority) { // draw diamond - high priority
					canvas.save(Canvas.MATRIX_SAVE_FLAG);
					canvas.rotate(45, x, y);
					canvas.drawRect(470f, 470f, 530f, 530f, symbolPaint);
					canvas.restore();
				}
				
				canvas.restore(); // emiter symbol 
				
				canvas.restore(); // emiter azimuth 
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		canvas.scale(scale/1000f, scale/1000f);

		drawThreats(canvas);
		
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
		backgroundCanvas.scale(scale / 1000f, scale / 1000f);
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);
		drawScale(backgroundCanvas);		
	}
	
	public void showThreats(JSONObject object) {
		jsonThreats = object;
		invalidate();
	}
}
