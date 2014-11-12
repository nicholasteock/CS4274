package mobile_psg.mpsgStarter;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;
import mobile_psg.sensorMonitor.ContextUpdatingService;
import mobile_psg.tcpsession.TCP_Session_Handler;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.example.mobile_psg.R;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class MpsgStarter extends Activity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{

	public static MPSG mpsg = null;
    
	private ProgressBar mProgress;
	
	private CheckBox isFamilyMember;
	
	private static LocationClient mLocationClient;

	private Button elderlyOption;
    private Button caretakerOption;
    private Button optionBack;
    private Button registerPerson;
    private Button leaveSend;
    private Button query;
    private Button viewfall;
    private Button ignorefall;
    private Button realfall;
    private Button falsefall;
    private Button bluetoothDcButton;
   
    private EditText name;
    private EditText userPhone;
    private EditText nokPhone;
    private EditText familyMemberPhone;
    private EditText queryInput;
    
    private static TextView welcomeText;
    private static TextView personalText;
    private static TextView nokText;
    private static TextView elderlyText;
    private static TextView errorText;
    private static TextView connectedText;
    private static TextView fallalertText;
    private static TextView verifyfallText;
    private static TextView bluetoothDcText;
    
    // Variables used for bluetooth
    private static CharSequence bluetoothToastText = "";
	private static int toastDuration = Toast.LENGTH_SHORT;
	private static int REQUEST_ENABLE_BT;
	private static Toast bluetoothToast = null;
	private ArrayAdapter<String> newDeviceListArrayAdapter;
	private static TextView bluetoothText;
	private ListView newDevicesList;
	private BluetoothSocket btsocket;
	private BluetoothDevice connect_device = null;
	private static BluetoothAdapter bluetooth = null;
	private static Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	private static Ringtone r = null;
	private static boolean keepTagConnected = false;
    
    private int timeout = 10; //10 seconds timeout for connecting
    private static final int SERVERPORT = 5000;
    
    private Context myContext = this;
    public static Handler mHandler;
    private static String resultStr = "";
    private static String connStatus = "Start MPSG";
    private static String resultString = "";
    private static String queryStatus = "invisible";
    private static String helpStatus = "";
    
    private static Intent intent;
    
    private static String userChoice = "";
  //  private ServerThread server;
    HashMap<String, String> registerParams = new HashMap<String, String>();
    
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

    }
    
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
    	
       Log.d("MPSG", "Connection Failed");
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpsg_starter);
        
        mLocationClient = new LocationClient(this, this, this);
        
        loadFirstScreen();
        
        mHandler = new Handler(){
  		  @Override
  		  public void handleMessage(Message msg) {
  			  showNotification();
  			  loadFallalertScreen();
  		     }
  		 };
//        mHandler.post(updateText);

        if (savedInstanceState != null) {
        	Log.d("MPSG", "Saved state not null");
	        String myResultString = savedInstanceState.getString("resultString");
	        Log.d("MPSG", "resultstring="+myResultString);
	        if (!myResultString.contentEquals("")) {
	        	errorText.setText(myResultString);
	        }

	        String myConnectStatus = savedInstanceState.getString("connStatus");
	        Log.d("MPSG", "connstatus="+myConnectStatus);
	        if (myConnectStatus.contentEquals("invisible")) {
	        	loadConnectedScreen();
	        }
	  
	        String myQueryStatus = savedInstanceState.getString("queryStatus");
	        Log.d("MPSG", "querystatus="+myQueryStatus);
	        if (myQueryStatus.contentEquals("visible")) {
	        	loadFirstScreen();
	        }
	        
        }
    }
    
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }
 
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	Log.d("MPSG", "Saving state");
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putString("connStatus", connStatus);
		savedInstanceState.putString("queryStatus", queryStatus);
		savedInstanceState.putString("resultString", resultString);
    };
    
    private void loadFirstScreen() {
    	 mProgress 			= (ProgressBar) findViewById(R.id.progressBar1);
         isFamilyMember 		= (CheckBox) findViewById(R.id.familyMember);
         elderlyOption 		= (Button) findViewById(R.id.elderlyOption);
         caretakerOption 	= (Button) findViewById(R.id.caretakerOption);
         optionBack 			= (Button) findViewById(R.id.optionBack);
         registerPerson 		= (Button) findViewById(R.id.registerPerson);
   //      query 				= (Button) findViewById(R.id.query);
        
         welcomeText 		= (TextView) findViewById(R.id.welcomeText);
         personalText 		= (TextView) findViewById(R.id.personalText);
         nokText 			= (TextView) findViewById(R.id.nokText);
         elderlyText 		= (TextView) findViewById(R.id.elderlyText);
         errorText 			= (TextView) findViewById(R.id.errorText);
         name 				= (EditText) findViewById(R.id.name);
         userPhone 			= (EditText) findViewById(R.id.userPhone);
         nokPhone 			= (EditText) findViewById(R.id.nokPhone);
         familyMemberPhone 	= (EditText) findViewById(R.id.familyMemberPhone);
   //      queryInput			= (EditText) findViewById(R.id.queryInput);
         isFamilyMember.setOnClickListener(isFamilyMemberListener);
         elderlyOption.setOnClickListener(elderlyOptionListener);
         caretakerOption.setOnClickListener(caretakerOptionListener);
         optionBack.setOnClickListener(optionBackListener);
         registerPerson.setOnClickListener(registerPersonListener);
         
    	welcomeText.setVisibility(View.VISIBLE);
        elderlyOption.setVisibility(View.VISIBLE);
        caretakerOption.setVisibility(View.VISIBLE);
        optionBack.setVisibility(View.INVISIBLE);
        registerPerson.setVisibility(View.INVISIBLE);
        personalText.setVisibility(View.INVISIBLE);
        nokText.setVisibility(View.INVISIBLE);
        elderlyText.setVisibility(View.INVISIBLE);
        name.setVisibility(View.INVISIBLE);
        userPhone.setVisibility(View.INVISIBLE);
        nokPhone.setVisibility(View.INVISIBLE);
        familyMemberPhone.setVisibility(View.INVISIBLE);
        mProgress.setVisibility(View.INVISIBLE);
        isFamilyMember.setVisibility(View.INVISIBLE);
     //   query.setVisibility(View.INVISIBLE);
     //   queryInput.setVisibility(View.INVISIBLE);
        errorText.setText("");
        
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
    };
    
    private void loadElderlyRegisterScreen() {
    	welcomeText.setVisibility(View.INVISIBLE);
        elderlyOption.setVisibility(View.INVISIBLE);
        caretakerOption.setVisibility(View.INVISIBLE);
        optionBack.setVisibility(View.VISIBLE);
        registerPerson.setVisibility(View.VISIBLE);
        personalText.setVisibility(View.VISIBLE);
        nokText.setVisibility(View.VISIBLE);
        elderlyText.setVisibility(View.INVISIBLE);
        name.setVisibility(View.VISIBLE);
        userPhone.setVisibility(View.VISIBLE);
        nokPhone.setVisibility(View.VISIBLE);
        familyMemberPhone.setVisibility(View.INVISIBLE);
        mProgress.setVisibility(View.INVISIBLE);
        isFamilyMember.setVisibility(View.INVISIBLE);
   //     query.setVisibility(View.INVISIBLE);
   //     queryInput.setVisibility(View.INVISIBLE);
        errorText.setText("");
    };
    
    private void loadCaretakerRegisterScreen() {
    	welcomeText.setVisibility(View.INVISIBLE);
        elderlyOption.setVisibility(View.INVISIBLE);
        caretakerOption.setVisibility(View.INVISIBLE);
        optionBack.setVisibility(View.VISIBLE);
        registerPerson.setVisibility(View.VISIBLE);
        personalText.setVisibility(View.VISIBLE);
        nokText.setVisibility(View.INVISIBLE);
        elderlyText.setVisibility(View.INVISIBLE);
        name.setVisibility(View.VISIBLE);
        userPhone.setVisibility(View.VISIBLE);
        nokPhone.setVisibility(View.INVISIBLE);
        familyMemberPhone.setVisibility(View.INVISIBLE);
        mProgress.setVisibility(View.INVISIBLE);
        isFamilyMember.setVisibility(View.VISIBLE);
     //   query.setVisibility(View.INVISIBLE);
     //   queryInput.setVisibility(View.INVISIBLE);

        
        errorText.setText("");
    };
    
    private void loadConnectedScreen() {
    	setContentView(R.layout.connected_screen);
        connectedText       = (TextView) findViewById(R.id.connectedText);
        fallalertText       = (TextView) findViewById(R.id.fallalertText);
        verifyfallText      = (TextView) findViewById(R.id.verifyfallText);
        bluetoothText 		= (TextView) findViewById(R.id.bluetoothText);
        bluetoothDcText 	= (TextView) findViewById(R.id.bluetoothDcText);
        leaveSend           = (Button) findViewById(R.id.leaveSend);
        viewfall            = (Button) findViewById(R.id.viewfall);
        ignorefall          = (Button) findViewById(R.id.ignorefall);
        realfall            = (Button) findViewById(R.id.realfall);
        falsefall           = (Button) findViewById(R.id.falsefall);
        bluetoothDcButton 	= (Button) findViewById(R.id.bluetoothDcButton);
        
        query 				= (Button) findViewById(R.id.query);
        queryInput			= (EditText) findViewById(R.id.queryInput);
        
		newDevicesList 		= (ListView) findViewById(R.id.new_bluetooth_devices);
        
        leaveSend.setOnClickListener(leaveSendListener);
        viewfall.setOnClickListener(viewfallListener);
        ignorefall.setOnClickListener(ignorefallListener);
        realfall.setOnClickListener(realfallListener);
        falsefall.setOnClickListener(falsefallListener);
        bluetoothDcButton.setOnClickListener(dcButtonListener);
        
        query.setOnClickListener(querySendListener);
        
        if(userChoice == "caretaker") {
            loadCaretakerConnectedScreen();   
        }
        else {
            loadElderlyConnectedScreen();
        }

    };
    
    private void loadRegisteringScreen() {
    	welcomeText.setVisibility(View.INVISIBLE);
        elderlyOption.setVisibility(View.INVISIBLE);
        caretakerOption.setVisibility(View.INVISIBLE);
        optionBack.setVisibility(View.INVISIBLE);
        registerPerson.setVisibility(View.INVISIBLE);
        personalText.setVisibility(View.INVISIBLE);
        nokText.setVisibility(View.INVISIBLE);
        elderlyText.setVisibility(View.INVISIBLE);
        name.setVisibility(View.INVISIBLE);
        userPhone.setVisibility(View.INVISIBLE);
        nokPhone.setVisibility(View.INVISIBLE);
        familyMemberPhone.setVisibility(View.INVISIBLE);
        mProgress.setVisibility(View.INVISIBLE);
        isFamilyMember.setVisibility(View.INVISIBLE);
    	mProgress.setVisibility(View.VISIBLE);
    	errorText.setText(registerParams.toString());
    }

    private void loadCaretakerConnectedScreen() {
        connectedText.setText("Connected As Caretaker");
        connectedText.setVisibility(View.VISIBLE);
        leaveSend.setVisibility(View.VISIBLE);
        bluetoothText.setVisibility(View.INVISIBLE);
        bluetoothDcText.setVisibility(View.INVISIBLE);
        bluetoothDcButton.setVisibility(View.INVISIBLE);
        newDevicesList.setVisibility(View.INVISIBLE);
        fallalertText.setVisibility(View.INVISIBLE);
        ignorefall.setVisibility(View.INVISIBLE);
        viewfall.setVisibility(View.INVISIBLE);
        verifyfallText.setVisibility(View.INVISIBLE);
        falsefall.setVisibility(View.INVISIBLE);
        realfall.setVisibility(View.INVISIBLE);
        
        
        query.setVisibility(View.INVISIBLE);
        queryInput.setVisibility(View.INVISIBLE);
        
    };
    
    private void loadElderlyConnectedScreen() {
        connectedText.setText("Connected As Elderly");
        connectedText.setVisibility(View.VISIBLE);
        leaveSend.setVisibility(View.VISIBLE);
        bluetoothText.setVisibility(View.VISIBLE);
        bluetoothDcText.setVisibility(View.INVISIBLE);
        bluetoothDcButton.setVisibility(View.INVISIBLE);
        newDevicesList.setVisibility(View.VISIBLE);
        fallalertText.setVisibility(View.INVISIBLE);
        ignorefall.setVisibility(View.INVISIBLE);
        viewfall.setVisibility(View.INVISIBLE);
        verifyfallText.setVisibility(View.INVISIBLE);
        falsefall.setVisibility(View.INVISIBLE);
        realfall.setVisibility(View.INVISIBLE);
        
        query.setVisibility(View.INVISIBLE);
        queryInput.setVisibility(View.INVISIBLE);
        
//        enableBluetooth();
    };

    private void loadFallalertScreen() {
        connectedText.setVisibility(View.INVISIBLE);
        leaveSend.setVisibility(View.INVISIBLE);
        fallalertText.setVisibility(View.VISIBLE);
        ignorefall.setVisibility(View.VISIBLE);
        viewfall.setVisibility(View.VISIBLE);
        verifyfallText.setVisibility(View.INVISIBLE);
        falsefall.setVisibility(View.INVISIBLE);
        realfall.setVisibility(View.INVISIBLE);
    };
    
    private void loadVerifyfallScreen() {
        connectedText.setVisibility(View.INVISIBLE);
        leaveSend.setVisibility(View.INVISIBLE);
        fallalertText.setVisibility(View.INVISIBLE);
        ignorefall.setVisibility(View.INVISIBLE);
        viewfall.setVisibility(View.INVISIBLE);
        verifyfallText.setVisibility(View.VISIBLE);
        falsefall.setVisibility(View.VISIBLE);
        realfall.setVisibility(View.VISIBLE);
    };
    
    private void loadElderlyLocationScreen() {
        connectedText.setText("Elderly Name: "+ MPSG.elderlyname +"\n Elderly Location: " + MPSG.elderlylocation);
        connectedText.setVisibility(View.VISIBLE);
        leaveSend.setVisibility(View.VISIBLE);
        bluetoothText.setVisibility(View.INVISIBLE);
        newDevicesList.setVisibility(View.INVISIBLE);
        fallalertText.setVisibility(View.INVISIBLE);
        ignorefall.setVisibility(View.INVISIBLE);
        viewfall.setVisibility(View.INVISIBLE);
        verifyfallText.setVisibility(View.INVISIBLE);
        falsefall.setVisibility(View.INVISIBLE);
        realfall.setVisibility(View.INVISIBLE);
        
        query.setVisibility(View.INVISIBLE);
        queryInput.setVisibility(View.INVISIBLE);
        
    };
    
    // Checks user inputs before submitting for registration.
    private boolean validateUserInputs() {
    	
		if( userChoice == "elderly" ) {
			String inputName 		= name.getText().toString();
			String inputPhone 		= userPhone.getText().toString();
			String inputNokPhone 	= nokPhone.getText().toString();
			
			if( inputName.length() == 0 ||
				inputPhone.length() == 0 ||
				inputNokPhone.length() == 0 )
			{
    			errorText.setText("*All fields are required.");
    			return false;
			}
			
			else if(!isValidPhone( inputPhone ) || 
					!isValidPhone( inputNokPhone ) )
			{
    			errorText.setText("*Phone number invalid.");
    			return false;
			}
			
			else {
				errorText.setText("Valid");
			}
		}
		
		if( userChoice == "caretaker" ) {
			String inputName 		= name.getText().toString();
			String inputPhone 		= userPhone.getText().toString();
			String inputFamilyPhone = familyMemberPhone.getText().toString();    			

			if( inputName.length() == 0 ||
    			inputPhone.length() == 0 )
			{
    			errorText.setText("*All fields are required.");
    			return false;
			}
			
			if( !isValidPhone( inputPhone ) ) {
    			errorText.setText("*Phone number invalid.");
    			return false;
			}
			
			if( isFamilyMember.isChecked() ) {
				if( inputFamilyPhone.length() == 0 )
				{
        			errorText.setText("*All fields are required.");
        			return false;
    			}
				
				if( !isValidPhone( inputFamilyPhone ) ) {
        			errorText.setText("*Phone number invalid.");
        			return false;
    			}
			}
		}
		
		return true;
    };

    // Forms user hashmap for registration
    private HashMap<String, String> formRegisterHashMap() {
    	HashMap<String, String> registerParams = new HashMap<String, String>();
    	
    	if( userChoice == "elderly" ) {
			String inputName 		= name.getText().toString();
			String inputPhone 		= userPhone.getText().toString();
			String inputNokPhone 	= nokPhone.getText().toString();
		
			registerParams.put("identity", "elderly");
			
			registerParams.put("username", inputName);
			registerParams.put("userPhone", inputPhone);
			registerParams.put("nokPhone", inputNokPhone);
		}
		
		if( userChoice == "caretaker" ) {
			String inputName 		= name.getText().toString();
			String inputPhone 		= userPhone.getText().toString();
			String inputFamilyPhone = familyMemberPhone.getText().toString();    			
			
			registerParams.put("identity", "caretaker");
			registerParams.put("username", inputName);
			registerParams.put("userPhone", inputPhone);
			
			if( isFamilyMember.isChecked() ) {
				registerParams.put("isFamily", "true");
				registerParams.put("familyMemberPhone", inputFamilyPhone);
			}
			else {
				registerParams.put("isFamily", "false");
			}
		}
		
		return registerParams;
    };
    
    private void registerUser( HashMap<String, String> registerParams ) {
    	loadRegisteringScreen();
    	
    	mProgress.setVisibility(View.VISIBLE);
    	mpsg = new MPSG(getBaseContext(), SERVERPORT, registerParams);
    	
    	long registerStartTime = System.currentTimeMillis();
    	long registerEndTime = 0;
    	
    	// Search for a proxy and connect to the best proxy
    	MPSG.searchProxy(); // Commented temporarily to test the MPSG-proxy-coalition flow
    	
    	/*// Connect to the selected proxy
    	Thread mpsgconnect = new Thread() {
    		public void run() {
    			mpsg.connect();
    		}
    	};
    	mpsgconnect.start();*/
    	
    	int i = 0;
    	// Wait for a result in registering through proxy
    	while (MPSG.statusString.contentEquals("Connecting")) {
    		if (i > timeout) {
    			errorText.setText("Error in waiting for result in registration with proxy");
    			return;
    		}
    		try {
    			Thread.sleep(500);
    		} catch (Exception e) {
	        	errorText.setText("Error in waiting for result in registration with proxy");
    		}
    	}
    	
    	if (MPSG.statusString.contentEquals("Connected")) {
    		registerEndTime = System.currentTimeMillis();
        	// Start the Service which updates the context information for the MPSG
        	intent = new Intent(myContext, ContextUpdatingService.class);
        	if(registerParams.get("identity")=="elderly")
        	{
        		Log.d("contextservice","entered if");
        		startService(intent);
        	}
        		
        	
        	connStatus = "invisible";
        	loadConnectedScreen();
        	
    	} else {
    		registerEndTime = System.currentTimeMillis();
    		errorText.setText("FAILED: "+ MPSG.statusString);
    		// TODO: Code for starting MPSG old directly without proxy
    	}
    	Log.d("EXPERIMENTAL_RESULTS", "Total response time for registration: " + Math.abs(registerEndTime - registerStartTime));
    };
    
    private OnClickListener isFamilyMemberListener = new OnClickListener() {
    	public void onClick(View v) {
    		if( isFamilyMember.isChecked() ) {
    			elderlyText.setVisibility(View.VISIBLE);
    			familyMemberPhone.setVisibility(View.VISIBLE);
    		}
    		else {
    			elderlyText.setVisibility(View.INVISIBLE);
    			familyMemberPhone.setVisibility(View.INVISIBLE);
    		}
    	}
    };

    private OnClickListener elderlyOptionListener = new OnClickListener() {
    	public void onClick(View v) {
    		userChoice = "elderly";
    		registerParams.clear();
    		loadElderlyRegisterScreen();
        }
    };

    private OnClickListener caretakerOptionListener = new OnClickListener() {
    	public void onClick(View v) {
    		userChoice = "caretaker";
    		registerParams.clear();
    		loadCaretakerRegisterScreen();
        }
    };
    
    private OnClickListener optionBackListener = new OnClickListener() {
    	public void onClick(View v) {
    		userChoice = "";
    		registerParams.clear();
    		loadFirstScreen();
        }
    };
    
    private OnClickListener registerPersonListener = new OnClickListener() {
    	public void onClick(View v) {
    		if( validateUserInputs() ) {
    			registerParams = formRegisterHashMap();		
    			registerUser( registerParams );
    		}
    	}
    };
    
    private OnClickListener updateSendListener = new OnClickListener() { 
        @Override
        public void onClick(View v) {
        	// Start a new thread to send out the query
        	Thread updateThread = new Thread() {
        		public void run() {
        			mpsg.updateContext();
        		}
        	};
        	updateThread.start();
        }      
    };
   
    private OnClickListener querySendListener = new OnClickListener() { 
        @Override
        public void onClick(View v) {
        	
        	EditText editText = (EditText) findViewById(R.id.queryInput);
            final String message = editText.getText().toString();
            
            System.out.println(message);
        	// Start a new thread to send out the query
        	Thread queryThread = new Thread() {
        		public void run() {
        			//mpsg.sendQuery(message);
        			mpsg.runFallDetectedSequence();
        		}
        	};
        	queryThread.start();
        }      
    };
    
    private OnClickListener dcButtonListener = new OnClickListener() { 
        @Override
        public void onClick(View v) {
        	Log.d("BLUETOOTH", "DC button clicked");
        	bluetoothDcText.setVisibility(View.INVISIBLE);
            bluetoothDcButton.setVisibility(View.INVISIBLE);
        	r.stop();
        }
    };
    
    public static void setQueryResult (String result) {
       	Log.d("MPSG", "Setting query result to " + result);
       	resultStr = result + "\n";
        Log.d("EXPERIMENTAL_RESULTS", "Time for getting query response:" + Math.abs(System.currentTimeMillis() - MPSG.queryStart));
    }
    
    private Runnable updateText = new Runnable() {
    	public void run() {
    		resultString = errorText.getText() + resultStr;
    		errorText.setText(errorText.getText() + resultStr);
    		resultStr = "";
    		mHandler.postDelayed(this, 1000);
    	}
    };
    
    private OnClickListener leaveSendListener = new OnClickListener() { 
        @Override
        public void onClick(View v) {
        	// Start a new thread to send out the query
        	Thread leaveThread = new Thread() {
        		public void run() {
        			mpsg.disconnect();
        		}
        	};
        	keepTagConnected = false;
        	leaveThread.start();
        	//Intent intent=new Intent(myContext, ContextUpdatingService.class);
        	boolean check=stopService(intent);
        	Log.d("servicestop",Boolean.toString(check));
        	int i = 0;
        	// Wait for a result in registering through proxy
        	while (MPSG.leaveStatusString.contentEquals("Disconnecting")) {
        		if (i > timeout) {
        			errorText.setText("Error in waiting for result in de-registration with coalition");
        			return;
        		}
        		try {
        			Thread.sleep(500);
        		} catch (Exception e) {
    	        	errorText.setText("Error in waiting for result in de-registration with coalition");
        		}
        	}
        	if (MPSG.leaveStatusString.contentEquals("Disconnected")) {
	        	connStatus = "visible";
	        	setContentView(R.layout.activity_mpsg_starter);
	        	loadFirstScreen();
	        	errorText.setText("");
	        	resultString = "";
	        	       	
	        	// Do cleanup of data structures
	        	MPSG.conn = null;
	        	MPSG.datain = null;
	        	MPSG.dnsSearchStatus = false;
	        	MPSG.ongoingSession = false;
	        	MPSG.sessionStatusFlag = false;
	        	MPSG.DynamicContextData = null;
	        	MPSG.iplist = null;
	        	MPSG.proxyIp = null;
	        	MPSG.prevProxyIP = null; 
	        	MPSG.subnetSearchStatus = false;
        	}
        }      
    };
    
    private OnClickListener viewfallListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("FALLFLOW", "In viewfalllistener");
            setHelpstatus("");
            loadVerifyfallScreen();
            camera("Someelderly");
        }
    };
    
    private OnClickListener ignorefallListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("FALLFLOW", "In ignorefallListener");
            setHelpstatus("ignorefall");
            loadCaretakerConnectedScreen();
        }
    };
    
    private OnClickListener realfallListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("FALLFLOW", "In realfallListener");
            setHelpstatus("realfall");
            //loadCaretakerConnectedScreen();
            loadElderlyLocationScreen();
        }
    };
    
    private OnClickListener falsefallListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("FALLFLOW", "In falsefallListener");
            setHelpstatus("falsefall");
            loadCaretakerConnectedScreen();
        }
    };

    // Helper function to check phone number
    public static boolean isValidPhone(String inputData ) {
    	if( inputData.matches("[-+]?\\d+(\\.\\d+)?") ) {
    		if( inputData.length() == 8 ) {
    			return true;
    		};
    	}
    	return false;
    };

    private void enableBluetooth() {
    	bluetooth=BluetoothAdapter.getDefaultAdapter();
		if( bluetooth == null ) {
			bluetoothToastText = "Bluetooth not supported on this device.";
			bluetoothToast = Toast.makeText(this, bluetoothToastText, toastDuration);
			bluetoothToast.show();
		}
		else {
			if( !bluetooth.isEnabled() ) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			else {
				connectBluetoothTag();
			}
		}
    };
    
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
    	if( resultCode == RESULT_OK ) {
    		connectBluetoothTag();
    	}
    	else {
    		bluetoothToastText = "Bluetooth needs to be enabled.";
			bluetoothToast = Toast.makeText(this, bluetoothToastText, toastDuration);
			bluetoothToast.show();
    	}
    };
    
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    	private int uuidlen;
    	
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
            	Log.d("BLUETOOTHACTION", "action : " + action);
                BluetoothDevice bdevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);              
                //short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);;
                if(bdevice.getBondState() != BluetoothDevice.BOND_BONDED)
                Log.d("BLUETOOTHACTION", "bdevice : " + bdevice.getName());
                newDeviceListArrayAdapter.add("\n" + bdevice.getName() + "\n" + bdevice.getAddress());
                newDeviceListArrayAdapter.notifyDataSetChanged();
            }
            
            if(BluetoothDevice.ACTION_UUID.equals(action)) {
            	uuidlen = connect_device.getUuids().length -1;
            	Log.d("BLUETOOTH", "UUID "+connect_device.getUuids()[uuidlen].getUuid());
            	try {
            		
            		btsocket = connect_device.createRfcommSocketToServiceRecord(connect_device.getUuids()[uuidlen].getUuid());
    	            btsocket.connect();
    	            // Upon connection stop discovery and hide menu.
    	            bluetoothText.setVisibility(View.INVISIBLE);
    	            newDevicesList.setVisibility(View.INVISIBLE);
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	            	Log.d("BLUETOOTHACTION", "Connection failed for " + connect_device.getUuids()[uuidlen].getUuid());
	            }
            }
            
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            	Log.d("BLUETOOTH", "Connected");
            	keepTagConnected = true;
            }
            
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            	Log.d("BLUETOOTH", "Disconnected");
            	
            	if(keepTagConnected) {
            		bluetoothDcText.setVisibility(View.VISIBLE);
                    bluetoothDcButton.setVisibility(View.VISIBLE);
	            	r.play();
            	}
            }
        }
    };
    
    private void connectBluetoothTag() {
    	bluetoothToastText = "In connect bluetooth tag";
		bluetoothToast = Toast.makeText(this, bluetoothToastText, toastDuration);
		bluetoothToast.show();
		
		bluetoothText.setText("Connect Bluetooth Tag");
		
		newDeviceListArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
	    newDevicesList.setAdapter(newDeviceListArrayAdapter);
	    
	    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	    filter.addAction(BluetoothDevice.ACTION_UUID);
        registerReceiver(mReceiver, filter);
        newDeviceListArrayAdapter.clear();
        bluetooth.startDiscovery();
        
        //new_devices_list click
        newDevicesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                bluetooth.cancelDiscovery();
                final String info = ((TextView) arg1).getText().toString();

                //get the device address when click the device item
                String address = info.substring(info.length()-17);
                
	            //connect the device when item is click
	            if( BluetoothAdapter.checkBluetoothAddress(address) ) {
	            	connect_device = bluetooth.getRemoteDevice(address);
	            	
	            	connect_device.fetchUuidsWithSdp();
	
	            }
	            else {
	            	Log.d("BLUETOOTH", "Error in address [" + address + "]");
	            }
            }
        });
    };
    
    public static void setHelpstatus(String value) {
        helpStatus = value;
        //getCaretakerResponse();
    };

    public static String getCaretakerResponse() {
        //Log.d("FALLFLOW", "In getCaretakerResponse helpStatus="+helpStatus);

        // Caretaker chose to ignore fall
        if(helpStatus == "ignorefall") {
            return "ignorefall";
        }
        // Caretaker acknowledge fall is real and is going to help
        if(helpStatus == "realfall") {
            return "realfall";
        }
        // Caretaker verified fall is false alarm
        if(helpStatus == "falsefall") {
            return "falsefall";
        }
        return "";
    };
    
    public static Location getCurrentLocation() {
    	
    	return mLocationClient.getLastLocation();
    };
    
    
    
    public void camera(String elderlycam){ //access ip cam
    	Intent intent = new Intent("android.intent.action.MAIN");
    	Log.d("camera function: ", "test:" + elderlycam);
		intent.setComponent(ComponentName.unflattenFromString("com.rcreations.ipcamviewerBasic/.WebCamViewerActivity"));
		intent.addCategory("android.intent.category.LAUNCHER");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
    }
    
    public void showNotification(){
        // define sound URI, the sound to be played when there's a notification

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        /*
        Intent resultIntent = new Intent(myContext, MpsgStarter.class);
       
        PendingIntent resultPendingIntent =
            PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );*/

        // intent triggered, you can add other intent for other actions

        //Intent intent = new Intent(this, NotificationReceiver.class);

       // PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

        long[] pattern = {0, 100, 1000};

        // this is it, we'll build the notification!

        // in the addAction method, if you don't want any icon, just set the first param to 0

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)

            .setContentTitle("ALERT!")
            .setContentText("ELDERLY FALL DETECTED!")
            .setVibrate(pattern)
            .setSmallIcon(R.drawable.ic_launcher)
            .setSound(soundUri);

        //mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // If you want to hide the notification after it was selected, do the code below

        // myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, mBuilder.build());

    }
 /*   protected void onDestroy() {

        super.onDestroy();
        if(bluetooth != null)
            bluetooth.cancelDiscovery();
        unregisterReceiver(mReceiver);
    }
    
   */
}
