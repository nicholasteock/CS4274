package mobile_psg.sensorMonitor;

import mobile_psg.mpsgStarter.MPSG;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.lang.Math;
import android.util.Log;

public class ContextUpdatingService extends IntentService implements SensorEventListener{
	// data
	private SensorManager mSensorManager;
	private Sensor gravitySensor;
	private Sensor lightSensor;
	private Sensor acceleroSensor;
	private Sensor magneticSensor;
	private String valueString = "";
	private boolean lowThreshold = false;
	private boolean isStill = false;
	private long start = 0;
	private long est = 0;
	
	// methods
	public ContextUpdatingService() {
		super("ContextUpdatingService");
		// TODO Auto-generated constructor stub
	}

	// methods
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		synchronized(this) {
			// get the value string
			//String valueString = "";
			/*
			float[] values = event.values;
			for(int i=0; i<values.length-1; i++) {
				valueString += (values[i] + "----");
			}
			valueString += values[values.length-1];*/
			
			// update corresponding context attribute
			int sensorType = event.sensor.getType();
			switch(sensorType) {
			case Sensor.TYPE_ACCELEROMETER:
				
				float vals[] = event.values;
			    //int sensor=arg0.sensor.getType();
			    double xx=event.values[0];
			    double yy=event.values[1];
			    double zz=event.values[2];
			    double accel = Math.round(Math.sqrt(Math.pow(xx, 2)
			                                    +Math.pow(yy, 2)
			                                    +Math.pow(zz, 2)));
			    /*
			    valueString += " ";
			    valueString += Double.toString(accel);
			    MPSG.DynamicContextData.put("person.acceleration", valueString);*/
			   
			    if(accel == 10.0) {
			    	isStill = true;
			    } else {
			    	isStill = false;
			    }
			    
			    if(accel <= 5.0) {
			    	lowThreshold = true;
			    	start = System.currentTimeMillis();
			    	
			
			    }
			    
			    valueString += " ";
			    valueString += Double.toString(accel);
			    
			    if(accel >= 27.0) {
			    	
			    	if(lowThreshold == true){
			    		est  = System.currentTimeMillis() - start;
			    		
			    		if(est < 1000) {
					    	valueString += " ";
					    	valueString += Long.toString(est);
					    	MPSG.DynamicContextData.put("person.acceleration", valueString);
					    	MPSG.updateContext();
					    	MPSG.sendQuery("");
					    	lowThreshold = false;
					    	valueString = "";
					    	Thread timerThread = new Thread() {
				        		public void run() {
				        			movementTimer();
				        		}
				        	};
				        	timerThread.start();
				        	lowThreshold = false;
					    	
			    		} else {
			    			lowThreshold = false;
			    		}
			    	}
			    	
			    	
			    	
			    	//MPSG.runFallDetectedSequence(); called when fall is detected
			    }
				break;
			case Sensor.TYPE_GRAVITY:
				
				/*
				float vals2[] = event.values;
			    //int sensor=arg0.sensor.getType();
			    double x=event.values[0];
			    double y=event.values[1];
			    double z=event.values[2];
			    valueString += "x";
			    valueString += Double.toString(x);
			    valueString += " ";
			    valueString += "y";
			    valueString += Double.toString(y);
			    valueString += " ";
			    valueString += "z";
			    valueString += Double.toString(z);
			    valueString += " ";*/
				
				MPSG.DynamicContextData.put("person.gravity", valueString);
				break;
			case Sensor.TYPE_LIGHT:
				MPSG.DynamicContextData.put("person.light", valueString);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:	
				MPSG.DynamicContextData.put("person.magnetism", valueString);
				break;
			}
		}
	}
	
	private void movementTimer() {
		long mStart = 0;
		long targetTime;
		mStart = System.currentTimeMillis();
		
		while(true) {
			if((System.currentTimeMillis() - mStart) > 2000)
				break;
		}
		
		while(isStill == true) {
			
			if((System.currentTimeMillis() - mStart) > 20000) {
				MPSG.sendQuery("");
				break;
			}
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		try {
			gravitySensor = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY).get(0);
			mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			lightSensor = mSensorManager.getSensorList(Sensor.TYPE_LIGHT).get(0);
			mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			acceleroSensor = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			mSensorManager.registerListener(this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			magneticSensor = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
			mSensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

