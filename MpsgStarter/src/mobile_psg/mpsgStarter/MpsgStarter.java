package mobile_psg.mpsgStarter;

import java.io.BufferedReader;
import java.net.InetAddress;
import java.util.HashMap;

import mobile_psg.sensorMonitor.ContextUpdatingService;
import mobile_psg.tcpsession.TCP_Session_Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.example.mobile_psg.R;

public class MpsgStarter extends Activity {

	public static MPSG mpsg = null;
    
	private ProgressBar mProgress;
	
	private CheckBox isFamilyMember;
	
	private Button elderlyOption;
    private Button caretakerOption;
    private Button optionBack;
    private Button registerPerson;
    private Button leaveSend;
    
    private EditText name;
    private EditText userPhone;
    private EditText nokPhone;
    private EditText familyMemberPhone;
    
    private static TextView connectedText;
    private static TextView welcomeText;
    private static TextView personalText;
    private static TextView nokText;
    private static TextView elderlyText;
    private static TextView errorText;
    
    private int timeout = 10; //10 seconds timeout for connecting
    private static final int SERVERPORT = 5000;
    
    private Context myContext = this;
    private static Handler mHandler;
    private static String resultStr = "";
    private static String connStatus = "Start MPSG";
    private static String resultString = "";
    private static String queryStatus = "invisible";
    
    private static String userChoice = "";
    
    HashMap<String, String> registerParams = new HashMap<String, String>();
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpsg_starter);

        mProgress 			= (ProgressBar) findViewById(R.id.progressBar1);
        isFamilyMember 		= (CheckBox) findViewById(R.id.familyMember);
        elderlyOption 		= (Button) findViewById(R.id.elderlyOption);
        caretakerOption 	= (Button) findViewById(R.id.caretakerOption);
        optionBack 			= (Button) findViewById(R.id.optionBack);
        registerPerson 		= (Button) findViewById(R.id.registerPerson);
        leaveSend 			= (Button) findViewById(R.id.leaveSend);
        connectedText 		= (TextView) findViewById(R.id.connectedText);
        welcomeText 		= (TextView) findViewById(R.id.welcomeText);
        personalText 		= (TextView) findViewById(R.id.personalText);
        nokText 			= (TextView) findViewById(R.id.nokText);
        elderlyText 		= (TextView) findViewById(R.id.elderlyText);
        errorText 			= (TextView) findViewById(R.id.errorText);
        name 				= (EditText) findViewById(R.id.name);
        userPhone 			= (EditText) findViewById(R.id.userPhone);
        nokPhone 			= (EditText) findViewById(R.id.nokPhone);
        familyMemberPhone 	= (EditText) findViewById(R.id.familyMemberPhone);
        
        isFamilyMember.setOnClickListener(isFamilyMemberListener);
        elderlyOption.setOnClickListener(elderlyOptionListener);
        caretakerOption.setOnClickListener(caretakerOptionListener);
        optionBack.setOnClickListener(optionBackListener);
        registerPerson.setOnClickListener(registerPersonListener);
        leaveSend.setOnClickListener(leaveSendListener);
        
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
//	        	connect.setVisibility(View.INVISIBLE);
	        	loadConnectedScreen();
	        }
	  
	        String myQueryStatus = savedInstanceState.getString("queryStatus");
	        Log.d("MPSG", "querystatus="+myQueryStatus);
	        if (myQueryStatus.contentEquals("visible")) {
//	        	query.setVisibility(View.VISIBLE);
//	        	leave.setVisibility(View.VISIBLE);
//	        	update.setVisibility(View.VISIBLE);
//	        	edit.setVisibility(View.VISIBLE);
	        	loadFirstScreen();
	        }
        }
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
    	connectedText.setVisibility(View.INVISIBLE);
    	leaveSend.setVisibility(View.INVISIBLE);
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
        errorText.setText("");
    };
    
    private void loadElderlyRegisterScreen() {
    	connectedText.setVisibility(View.INVISIBLE);
    	leaveSend.setVisibility(View.INVISIBLE);
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
        errorText.setText("");
    };
    
    private void loadCaretakerRegisterScreen() {
    	connectedText.setVisibility(View.INVISIBLE);
    	leaveSend.setVisibility(View.INVISIBLE);
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
        errorText.setText("");
    };
    
    private void loadConnectedScreen() {
    	connectedText.setVisibility(View.VISIBLE);
    	leaveSend.setVisibility(View.VISIBLE);
    	welcomeText.setVisibility(View.INVISIBLE);
        elderlyOption.setVisibility(View.INVISIBLE);
        caretakerOption.setVisibility(View.INVISIBLE);
        optionBack.setVisibility(View.INVISIBLE);
        registerPerson.setVisibility(View.INVISIBLE);
        personalText.setVisibility(View.INVISIBLE);
        nokText.setVisibility(View.INVISIBLE);
        elderlyText.setVisibility(View.INVISIBLE);
        name.setVisibility(View.INVISIBLE);
        userPhone.setVisibility(View.VISIBLE);
        nokPhone.setVisibility(View.INVISIBLE);
        familyMemberPhone.setVisibility(View.INVISIBLE);
        mProgress.setVisibility(View.INVISIBLE);
        isFamilyMember.setVisibility(View.INVISIBLE);
        errorText.setText("");
    };
    
    private void loadRegisteringScreen() {
    	connectedText.setVisibility(View.INVISIBLE);
    	leaveSend.setVisibility(View.INVISIBLE);
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

    // @KAR WAI: This function is called after validation of user inputs
    // Suggesting this to be used for registering the user, similar to
    // the connectListener functionality in the previous 'connect' button.
    private void registerUser( HashMap<String, String> registerParams ) {
    	loadRegisteringScreen();
    }
    
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
    			
    			// @KAR WAI: I left off from here.
    			registerUser( registerParams );
    		}
    	}
    };
    
    private OnClickListener connectListener = new OnClickListener() { 
        @Override
        public void onClick(View v) {
        	mProgress.setVisibility(View.VISIBLE);
        	mpsg = new MPSG(getBaseContext(), SERVERPORT);
        	
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
//	        	connect.setText(connStatus);
//	        	connect.setVisibility(View.INVISIBLE);
//	        	queryStatus = "visible";
//	        	query.setVisibility(View.VISIBLE);
//	        	leave.setVisibility(View.VISIBLE);
//	        	update.setVisibility(View.VISIBLE);
//	        	edit.setVisibility(View.VISIBLE);
	        	
	        	loadConnectedScreen();
	        	
        	} else {
        		registerEndTime = System.currentTimeMillis();
//        		errorText.setText("FAILED: "+ MPSG.statusString);
        		// TODO: Code for starting MPSG old directly without proxy
        	}
        	Log.d("EXPERIMENTAL_RESULTS", "Total response time for registration: " + Math.abs(registerEndTime - registerStartTime));
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
    
//    private OnClickListener querySendListener = new OnClickListener() { 
//        @Override
//        public void onClick(View v) {
//        	
//        	EditText editText = (EditText) findViewById(R.id.edit_message);
//            final String message = editText.getText().toString();
//            
//            System.out.println(message);
//        	// Start a new thread to send out the query
//        	Thread queryThread = new Thread() {
//        		public void run() {
//        			mpsg.sendQuery(message);
//        		}
//        	};
//        	queryThread.start();
//        }      
//    };
    
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
//	        	connect.setText("Start Mobile PSG");
//	        	connect.setVisibility(View.VISIBLE);
//	        	queryStatus = "invisible";
//	        	query.setVisibility(View.INVISIBLE);
//	        	leave.setVisibility(View.INVISIBLE);
//	        	update.setVisibility(View.INVISIBLE);
//	        	edit.setVisibility(View.INVISIBLE);
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
    
    // Helper function to check phone number
    public static boolean isValidPhone(String inputData ) {
    	if( inputData.matches("[-+]?\\d+(\\.\\d+)?") ) {
    		if( inputData.length() == 8 ) {
    			return true;
    		};
    	}
    	return false;
    };
}
