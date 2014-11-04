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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

import mobile_psg.sensorMonitor.ContextUpdatingService;
import mobile_psg.tcpsession.TCP_Session_Handler;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile_psg.R;

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
    private Button bluetooth;
    private Button viewfall;
    private Button ignorefall;
    private Button realfall;
    private Button falsefall;
    
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
    
    private int timeout = 10; //10 seconds timeout for connecting
    private static final int SERVERPORT = 5000;
    
    private Context myContext = this;
    private static Handler mHandler;
    private static String resultStr = "";
    private static String connStatus = "Start MPSG";
    private static String resultString = "";
    private static String queryStatus = "invisible";
    private static String helpStatus = "";
    
    private static String userChoice = "";
    
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
        
        
        loadFirstScreen();
        
        mHandler = new Handler();
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
        leaveSend           = (Button) findViewById(R.id.leaveSend);
        viewfall            = (Button) findViewById(R.id.viewfall);
        ignorefall          = (Button) findViewById(R.id.ignorefall);
        realfall            = (Button) findViewById(R.id.realfall);
        falsefall           = (Button) findViewById(R.id.falsefall);
        
        query 				= (Button) findViewById(R.id.query);
        queryInput			= (EditText) findViewById(R.id.queryInput);
        
        leaveSend.setOnClickListener(leaveSendListener);
        viewfall.setOnClickListener(viewfallListener);
        ignorefall.setOnClickListener(ignorefallListener);
        realfall.setOnClickListener(realfallListener);
        falsefall.setOnClickListener(falsefallListener);
        
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
        fallalertText.setVisibility(View.INVISIBLE);
        ignorefall.setVisibility(View.INVISIBLE);
        viewfall.setVisibility(View.INVISIBLE);
        verifyfallText.setVisibility(View.INVISIBLE);
        falsefall.setVisibility(View.INVISIBLE);
        realfall.setVisibility(View.INVISIBLE);
        
        query.setVisibility(View.VISIBLE);
        queryInput.setVisibility(View.VISIBLE);
    };
    
    private void loadElderlyConnectedScreen() {
        connectedText.setText("Connected As Elderly");
        connectedText.setVisibility(View.VISIBLE);
        leaveSend.setVisibility(View.VISIBLE);
        fallalertText.setVisibility(View.INVISIBLE);
        ignorefall.setVisibility(View.INVISIBLE);
        viewfall.setVisibility(View.INVISIBLE);
        verifyfallText.setVisibility(View.INVISIBLE);
        falsefall.setVisibility(View.INVISIBLE);
        realfall.setVisibility(View.INVISIBLE);
        
        query.setVisibility(View.VISIBLE);
        queryInput.setVisibility(View.VISIBLE);
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
        	Intent contextUpdater = new Intent(myContext, ContextUpdatingService.class);
        	startService(contextUpdater);
        	
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
        	leaveThread.start();
        	
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
            setHelpstatus("ignoreFall");
            loadCaretakerConnectedScreen();
        }
    };
    
    private OnClickListener realfallListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("FALLFLOW", "In realfallListener");
            setHelpstatus("realfall");
            loadCaretakerConnectedScreen();
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

    public static void setHelpstatus(String value) {
        helpStatus = value;
        getCaretakerResponse();
    };

    public static void getCaretakerResponse() {
        Log.d("FALLFLOW", "In getCaretakerResponse helpStatus="+helpStatus);

        // Caretaker chose to ignore fall
        if(helpStatus == "ignorefall") {
            return;
        }
        // Caretaker acknowledge fall is real and is going to help
        if(helpStatus == "realfall") {
            return;
        }
        // Caretaker verified fall is false alarm
        if(helpStatus == "falsefall") {
            return;
        }
    };
    
    public static Location getCurrentLocation() {
    	
    	return mLocationClient.getLastLocation();
    };
    
    public String getLocalIpAddress() {//get ipv4 address of device
    	String ip="";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                  //  if (!inetAddress.isLoopbackAddress()) {
                    if(inetAddress.isSiteLocalAddress()){
                    	ip = "local address: "+ inetAddress.getHostAddress() +"\n";
                    }
                }
            }
            return ip;
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public void camera(String elderlycam){ //access ip cam
    	Intent intent = new Intent("android.intent.action.MAIN");
    	Log.d("camera function: ", "test:" + elderlycam);
		//intent.setComponent(ComponentName.unflattenFromString("com.ipc.ipcamera/com.ipc.newipc.LoadActivity"));
		intent.setComponent(ComponentName.unflattenFromString("com.rcreations.ipcamviewerBasic/.WebCamViewerActivity"));
		intent.addCategory("android.intent.category.LAUNCHER");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
    }
    
    //ServerThread server= new ServerThread();
    //server.start();
    public class ServerThread extends Thread { //tcp server
        ServerSocket serverSocket;

        public ServerThread() {
        	
        }

        public void run() {
           // String incomingMsg;
        	String outgoingMsg="";
        	try {
            	
              //  mytext.setText("Starting socket thread...\n");

                serverSocket = new ServerSocket(5123);
                
                Log.d("ServerSocket"," waiting for incoming connections...");
                while(true){
                Socket socket = serverSocket.accept();
                
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
               String incomingMsg=in.readLine();
               // camera(incomingMsg);
                //    mytext.setText("Connection accepted, reading...\n");

                    while (socket.isConnected()) {
                    	
                        Log.d("Message recieved","message:"+ incomingMsg
                                + ". Answering...");
                    	String test="";
                    	//make button visible
                    	if(test=="yes"){
                    	String result="";
                    //	makebuttonvisible();
                        // send a message
                        outgoingMsg = "hello: "
                                + "result: " + result
                                + System.getProperty("line.separator");
                        out.write(outgoingMsg);
                        out.flush();
                    	}

                        Log.d("Message sent","Sent: " + outgoingMsg);
                    }

                    if (socket.isConnected()) Log.d("serversocket status:", "Socket still connected");
                    else{ 
                    	Log.d("Serversocket status: ","Socket not connected");
                    	socket.close();	
                    }
                
                }
            } catch (Exception e) {
            	Log.d("serversocket: ", "Error: " + e.getMessage()+"\n");
                e.printStackTrace();
            }

        }
    }
    
    
}
