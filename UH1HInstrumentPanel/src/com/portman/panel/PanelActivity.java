package com.portman.panel;
 
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
 
public class PanelActivity extends Activity implements OnClickListener {
	DatagramSocket ss = null;
	Thread myCommsThread = null;
	protected static final int MSG_ID = 0x1335;
	public static final int SERVERPORT = 6000;
	private static String mClientMsg = "";
   
	// UI controls
	private static Airspeed mAirspeed;
	private static Altimeter mAltimeter;
	private static Torque mTorque;
	private static RPM 	   mRPM;
	private static TurnIndicator mTurnIndicator;
	private static ArtificialHorizon mArtificialHorizon;
	private static DirectionalGyro mDirectionalGyro;
	private static Variometer  mVariometer;
	private static FuelGauge mFuelGauge; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_panel);
	   
		Log.i("UH-1H PanelActivity", "onCreate");
	   
		// find controls	   
		mAirspeed = (Airspeed) findViewById(R.id.airspeed);
		mAltimeter = (Altimeter) findViewById(R.id.altimeter);
		mTorque = (Torque) findViewById(R.id.torque);
		mRPM		 = (RPM) findViewById(R.id.rpm);
		mTurnIndicator = (TurnIndicator) findViewById(R.id.turn_indicator);
		mArtificialHorizon = (ArtificialHorizon) findViewById(R.id.artificial_horizon);
		mDirectionalGyro = (DirectionalGyro) findViewById(R.id.directional_gyro);
		mVariometer = (Variometer) findViewById(R.id.variometer);
	   	mFuelGauge = (FuelGauge) findViewById(R.id.fuel_gauge);  
	   
	   	// set click handlers
	   	mRPM.setOnClickListener(this);
	   	mFuelGauge.setOnClickListener(this);   
	  	 
	   	this.myCommsThread = new Thread(new CommsThread());
	   	this.myCommsThread.start();
	}
   
	// Implement the OnClickListener callback
	public void onClick(View v) {
		if (mRPM.getVisibility() == View.VISIBLE) { // show engine gauges
			mRPM.setVisibility(View.GONE);
			mFuelGauge.setVisibility(View.VISIBLE);
		} 
		else if (mFuelGauge.getVisibility() == View.VISIBLE) {
			mFuelGauge.setVisibility(View.GONE);
			mRPM.setVisibility(View.VISIBLE);		   
		}
	}
 
	@Override
	protected void onStop() {
		super.onStop();
	   
		Log.i("UH1HPanelActivity", "OnStop");
	   
	   try {
		   // make sure you close the socket upon exiting
		   this.myCommsThread.interrupt();
		   ss.close();
	   } catch (Exception e) {
		   e.printStackTrace();
	   }
	}
 
	private static Handler myUpdateHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ID:			   
				try {
					Log.i("PanelActivity", "handleMessage: " + mClientMsg);
					// parse json
					JSONObject object = (JSONObject) new JSONTokener(mClientMsg).nextValue();
				   
					mAirspeed.setAirspeed((float)object.getDouble("AirspeedNeedle"));
					mAltimeter.setAltimeter((float)object.getDouble("Altimeter_10000_footPtr")/10000f, 
						   (float)object.getDouble("Altimeter_1000_footPtr")/1000f, 
						   (float)object.getDouble("Altimeter_100_footPtr")/100f);
					mTorque.setManifold((float)object.getDouble("Torque"));
					mRPM.setRPM((float)object.getDouble("Engine_RPM")/100f);
					mTurnIndicator.setTurnNeedlePosition((float)object.getDouble("TurnNeedle"));
					mTurnIndicator.setSlipballPosition((float)object.getDouble("Slipball"));
					mArtificialHorizon.setPitchAndBank((float)object.getDouble("AHorizon_Pitch"), (float)object.getDouble("AHorizon_Bank"));
				   	mDirectionalGyro.setGyroHeading((float)object.getDouble("GyroHeading"));
				   	mVariometer.setVariometer((float)object.getDouble("Variometer")/1000f);
				   	//mEngineGauge.setValues((float)object.getDouble("Oil_Temperature"), (float)object.getDouble("Oil_Pressure"), (float)object.getDouble("Fuel_Pressure"));
				   	mFuelGauge.setFuel((float)object.getDouble("Fuel_Tank"));				   
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
   
	class CommsThread implements Runnable {
		public void run() {
			byte[] receiveData = new byte[1024];
		   
			try {
				ss = new DatagramSocket(SERVERPORT );
			} catch (IOException e) {
				e.printStackTrace();
			}
        
			while (!Thread.currentThread().isInterrupted()) {
				Message m = new Message();
				m.what = MSG_ID;
				try {
					DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
					ss.receive(receivePacket);
					mClientMsg = new String(receivePacket.getData(), 0, receivePacket.getLength());
					myUpdateHandler.sendMessage(m);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
}