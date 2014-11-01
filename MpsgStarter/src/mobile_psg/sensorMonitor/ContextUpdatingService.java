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
	private boolean isFallDetected = false;
	private boolean isUpright = true;
	private long start = 0;
	private long est = 0;
	private long cStart;
	private boolean isPosChanged = false;
	
	private boolean isCalibrated = false;
	private boolean isCalibrating = false;
	private double cx = 0;
	private double cy = 0;
	private double cz = 0;
	private double x = 0;
	private double y = 0;
	private double z = 0;
	private double sx;
	private double sy;
	private double sz;
	
	private float gravity[];
	private float mfield[];
	private boolean isGrav = false;
	private boolean isField = false;
	
	float R[] = new float[9];
	float I[] = new float[9];
	float values[] = new float[3];
	
	int count = 0;
	
	// methods
	public ContextUpdatingService() {
		super("ContextUpdatingService");
		// TODO Auto-generated constructor stub
		/*Thread orienThread = new Thread() {
    		public void run() {
    			startOrientationSensor();
    		}
    	};
    	orienThread.start();*/
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
			   
			    //if(accel == 10.0) {
			    if(accel >= 9.0 || accel <= 11.0) {
			    	isStill = true;
			    } else {
			    	isStill = false;
			    }
			    
			    if(isFallDetected == false) {
				    if(accel <= 5.0) {
				    	lowThreshold = true;
				    	start = System.currentTimeMillis();
				    	
				
				    }
				    
				    valueString += " ";
				    valueString += Double.toString(accel);
				    //Log.d("accel", valueString);
				    
				    if(accel >= 20.0) {
				    	
				    	if(lowThreshold == true){
				    		est  = System.currentTimeMillis() - start;
				    		
				    		if(est < 1000) {
						    	valueString += " ";
						    	valueString += Long.toString(est);
						    	MPSG.DynamicContextData.put("person.acceleration", valueString);
						    	//MPSG.updateContext();
						    	//MPSG.sendQuery("");
						    	Log.d("FALL", valueString);
						    	lowThreshold = false;
						    	valueString = "";
						    	isFallDetected = true;
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
				    }
			    	
			    	
			    	
			    	//MPSG.runFallDetectedSequence(); called when fall is detected
			    }
				break;
			case Sensor.TYPE_GRAVITY:
				isGrav = true;
				gravity = event.values;
				//float vals2[] = event.values;
			    //int sensor=arg0.sensor.getType();
			   // double x=event.values[0];
			   // double y=event.values[1];
			   // double z=event.values[2];
			    /*
			    valueString += "x";
			    valueString += Double.toString(x);
			   valueString += " ";
			    valueString += "y";
			    valueString += Double.toString(y);
			    valueString += " ";
			    valueString += "z";
			    valueString += Double.toString(z);
			    valueString += " ";*/
			    
			    /*if(Math.abs(cx - x) >= 7.0 || Math.abs(cy - y) >= 7.0 || Math.abs(cz - z) >= 7.0) 
			    	isPosChanged = true;
			    else
			    	isPosChanged = false;*/
			  /*
			    if(isFallDetected == false) {
			    	
				    if(isCalibrated == false) {
				    	
				    	if(isCalibrating == true) {
				    		
				    		if(Math.abs(sx - x) <= 4.5 && Math.abs(sy - y) <= 4.5 && Math.abs(sz - z) <= 4.5) {
				    			if(System.currentTimeMillis() - cStart >= 2000) {
				    				isCalibrated = true;
				    				isCalibrating = false;
				    				cx = sx;
				    				cy = sy;
				    				cz = sz;
				    				Log.d("cx", Double.toString(cx));
				    				Log.d("cy", Double.toString(cy));
				    				Log.d("cz", Double.toString(cz));
				    			}
				    			
				    		} else {
				    			isCalibrating = false;
				    		}
				    			
				    		
				    		
				    	} else {
				    		cStart = System.currentTimeMillis();
				    		isCalibrating = true;
				    		sx = x;
				    		sy = y;
				    		sz = z;
				    		
				    	}
				    	
				    } else {
				    	if(Math.abs(cx - x) >= 3.0 || Math.abs(cy - y) >= 3.0 || Math.abs(cz - z) >= 3.0) {
				    		isCalibrated = false;
				    	}
				    		
				    }
			    }*/
			    /*
			    if( x >= 5.0)
			    	isUpright = true;
			    else
			    	isUpright = false;*/
			    
			    
			    
				
				MPSG.DynamicContextData.put("person.acceleration", valueString);
				break;
			case Sensor.TYPE_LIGHT:
				MPSG.DynamicContextData.put("person.light", valueString);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				isField = true;
				mfield = event.values;
				MPSG.DynamicContextData.put("person.magnetism", valueString);
				break;
			}
			//Log.d("x", Double.toString(Math.toDegrees(values[1])));
			//Log.d("y", Double.toString(Math.toDegrees(values[2])));
			//Log.d("z", Double.toString(Math.toDegrees(values[0])));
			//count++;
			
			if(sensorType == Sensor.TYPE_GRAVITY || sensorType == Sensor.TYPE_MAGNETIC_FIELD){
				if(isGrav && isField) {
					if(SensorManager.getRotationMatrix(R, I, gravity, mfield) == true) {
						SensorManager.getOrientation(R, values);
						x = Math.abs(Math.toDegrees(values[1]));
						y = Math.abs(Math.toDegrees(values[2]));
						z = Math.abs(Math.toDegrees(values[0]));
						
						if(Math.abs(cx - x) >= 45 || Math.abs(cy - y) >= 45) 
					    	isPosChanged = true;
					    else
					    	isPosChanged = false;
						
						if(isFallDetected == false) {
							if(isCalibrated == false) {
						    	
						    	if(isCalibrating == true) {
						    		
						    		if(Math.abs(sx - x) <= 25 && Math.abs(sy - y) <= 25){ 
						    			if(System.currentTimeMillis() - cStart >= 3000) {
						    				isCalibrated = true;
						    				isCalibrating = false;
						    				cx = x;
						    				cy = y;
						    				cz = z;
						    				Log.d("cx", Double.toString(cx));
						    				Log.d("cy", Double.toString(cy));
						    				Log.d("cz", Double.toString(cz));
						    			}
						    		
						    		
							    	}else {
						    			isCalibrating = false;
						    		}
						    	
						    	
							
								} else {
						    		cStart = System.currentTimeMillis();
						    		isCalibrating = true;
						    		sx = x;
						    		sy = y;
						    		sz = z;
						    		
						    	}
						
							} else {
								if(Math.abs(Math.abs(cx) - Math.abs(x)) >= 45 || Math.abs(Math.abs(cy) - Math.abs(y)) >= 45) 
							    	isCalibrated = false;  
							}
					    		
						}
					}
				}
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
		
		while(isStill == true && isPosChanged == true) {
			
			if((System.currentTimeMillis() - mStart) > 20000) {
				
				if(isPosChanged == true) {
					//MPSG.sendQuery("");
					Log.d("FALL", "1");
					valueString = "";
					isFallDetected = false;
					isCalibrated = false;
					isCalibrating = false;
					return;
				}
			}
		}
		
		Log.d("FALL", "0");
		Log.d("STILL", Boolean.toString(isStill));
		Log.d("POS", Boolean.toString(isPosChanged));
		Log.d("x", Double.toString(x));
		Log.d("y", Double.toString(y));
		
		valueString = "";
		isFallDetected = false;
		isCalibrated = false;
		isCalibrating = false;
		//MPSG.sendQuery("1");
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

