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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import mobile_psg.networkmanagement.NetworkManager;
import mobile_psg.proxysearch.DNSProxySearch;
import mobile_psg.proxysearch.SearchSubnet_PSG;
import mobile_psg.tcpsession.TCP_Session_Handler;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

/**
 * @author Sathiya
 * Class MPSG handles searching of proxy and initiation of connection with Proxy
 */

public class MPSG {
	
	public static boolean inWifi = true;
	public static long lastHandshake = 0;
	public static String statusString = "Connecting"; 
	public static String leaveStatusString = "Disconnecting";
	public static boolean ongoingSession = false; // Change it when a session is ongoing
	public static TCP_Session_Handler conn;
	public static BufferedReader datain;
	static int serverPort = 5000;  // Proxy listens to connections at this port
	static Context basecontext;
	public static boolean sessionStatusFlag = false; // Set to TRUE when there is a ongoing communication with current proxy 

	// Start searching for proxy within subnet
	final static SearchSubnet_PSG search_first_try = new SearchSubnet_PSG();
	static int next = -1;

	// Context information during registration of MPSG
	public static String mpsgName = "MPSGyy";
	//public static String StaticContextData = "person.name::testyy,person.preference::lp,person.location::ion,person.isBusy::yes,person.speed::nil,person.action::eating,person.power::low,person.mood::happy,person.acceleration::nil,person.gravity::nil,person.magnetism::nil";
	public static String StaticContextData2 = "person.name::karwai,person.preference::pc,person.location::home,person.isBusy::yes,person.speed::nil,person.action::eating,person.power::low,person.mood::happy,person.acceleration::nil,person.gravity::nil,person.magnetism::nil";
	public static String ContextType = "ELDERLY";
	public static String StaticContextData = "elderly.name::lala,elderly.status::normal";
	public static HashMap DynamicContextData = new HashMap(); // All updates to sensor information are pushed into this 
	
	// Set by discovery mechanism
	//public static InetAddress[] iplist = new InetAddress[10]; // List of IP returned by DNS
	public static InetAddress[] iplist = new InetAddress[10];
	public static int dnsResultCount = 0;
	public static InetAddress proxyIp = null;
	public static InetAddress prevProxyIP = null; 
	public static boolean subnetSearchStatus = false;
	public static boolean dnsSearchStatus = false;
	public static int discoveryUsage = 0;
	
	static int proxyIndex = 0;
	public static String state = "init";
	public static long queryStart;
	public static int queryData;
	
	public static boolean isQueryReady = false;
	public static String  queryResult = "";
	
	public static String elderlyname="";
	public static String elderlylocation="";
	//declare clientsender
	private static ClientSender clientsender;
	
	private static List<String> nearestContacts = new ArrayList<String>();
	private static List<Pair<String, Double>> userDistancePair = new ArrayList<Pair<String, Double>>();
	private static List<String> contacts = new ArrayList<String>();;
	private static List<String[]> locations = new ArrayList<String[]>();; 

	// Temporary query string to be sent to the proxy
	static String queryString = mpsgName + ";query:select person.preference from person where person.name = \"testmpsgname1\"";
	String updateString = "update::person.name::testmpsgname1,person.preference::pc,person.location::home,person.isBusy::yes,person.speed::nil,person.action::eating,person.power::low,person.mood::happy,person.acceleration::nil,person.gravity::nil,person.magnetism::nil";
	//String queryString = mpsgName + ";query:select person.magnetism from person where person.acceleration = \"fast\" and person.gravity=\"medium\"";
//	String queryString = mpsgName + ";query:select person.location,person.magnetism from person where person.acceleration = \"fast\" and person.name = \"testmpsgname2\"";// ) or ( person.acceleration = \"fast\" and person.magnetism = \"positive\" )";
	
	MPSG(Context context, int port, HashMap<String, String> registerParams) { //pass in Object of Elderly/Caretaker
		serverPort = port;
		basecontext = context;
		conn = new TCP_Session_Handler();
		// Keep searching for proxy whenever there is a network change detected in the mobile
		Log.d("MPSG", "Starting a service for monitoring network changes");
		Intent networkManager = new Intent(basecontext, NetworkManager.class);
		basecontext.startService(networkManager);
		
		register( registerParams );
		
		// Temporarily assign ip of proxy for testing
		/*
		try {
			proxyIp = InetAddress.getByName("192.168.173.1");
		} catch (Exception e) {}*/
	} 
	
public void register(HashMap<String, String> RegisterData) {
		
		mpsgName = "MPSG" + RegisterData.get("username");
		ContextType = RegisterData.get("identity");
		
		if(ContextType == "elderly") {
			StaticContextData = "elderly.name::" + RegisterData.get("username") + ",elderly.status::normal,elderly.phonenum::" + RegisterData.get("userPhone") + ",elderly.noknum::" + RegisterData.get("nokPhone")+ ",elderly.ipaddress::nil,elderly.location::nil";
			elderlyname= RegisterData.get("username");
		}
		else {
			StaticContextData = "caretaker.name::" + RegisterData.get("username") + ",caretaker.phonenum::" + RegisterData.get("userPhone") + ",caretaker.location::nil,caretaker.ipaddress::nil";
	       
			Thread serverthread= new Thread(){
				public void run()
				{
					serverstart();
				}
			};
	        serverthread.start();
	        
			if(RegisterData.get("isFamily") == "true") {
				
				StaticContextData += ",caretaker.identity::caretaker";
				StaticContextData += ",caretaker.elderlynum::" + RegisterData.get("familyMemberPhone");
			} 
			else {
				StaticContextData += ",caretaker.identity::caretaker";
				StaticContextData += ",caretaker.elderlynum::nil";
			}
			
		}
		
		
		//Change the StaticContext and Context Type based on object passed in
	}
	/**
	 * Perform DNS Proxy search and selects the proxy 
	 * Starts a new thread to monitor connection status with proxy
	 * @return void
	 */
	public static void searchProxy() {
		
		// Reset all values before starting search
		proxyIp = null;
		subnetSearchStatus = false;
		proxyIndex = 0;
		statusString = "Connecting"; 
		leaveStatusString = "Disconnecting";
		//ongoingSession = false; // Change it when a session is ongoing
		conn = null;
		datain = null;
		discoveryUsage = 0; // reset discovery data usage to zero
		//sessionStatusFlag = false; // Set to TRUE when there is a ongoing communication with current proxy 
		
		// Time parameters
		long dnsStart = 0;
		long subnetStart = 0;
		long dnsSearchTime = 0;
		long subnetSearchTime = 0;
		long totalSearchTime = 0;
		
		boolean proxySelectedUsing = true; // true if selected using UDP subnet search
		Log.d("MPSG", "Starting proxy search");
    	
    	Log.d("MPSG", "Starting Subnet search");
    	Thread subnetSearch = new Thread() {
    		public void run() {
    			search_first_try.init();
    		}
    	};
    	subnetStart = System.currentTimeMillis();
    	subnetSearch.start();	
    	while (!subnetSearchStatus) {
    		if (!inWifi) {
    			inWifi = true;
    			return;
    		}
    	} // Wait until subnet search completes
    	inWifi = true;
     	Log.d("MPSG", "Subnet search done. Checking if proxyIP got set");
    	if (proxyIp == null) {
    		proxySelectedUsing = false; // Change selection through DNS
    		// Subnet search failed. Trigger DNS search 
    		Log.d("MPSG", "No proxy within subnet. Starting DNS search");
    		Thread dnsSearch = new Thread() {
    			public void run() {
    				DNSProxySearch.search("coalition.yjwong.name");
    			}
    		};
    	   	dnsStart = System.currentTimeMillis();
    		dnsSearch.start();
    		while (!dnsSearchStatus) {} // Wait until DNS search completes
    		//TODO: Hack for changing selected Proxy
    		next++;
    		if (next >= dnsResultCount) { next = 0; }
    		Log.d("MPSG", "Value of next:" + next + ", iplist length: " + iplist.length);
    		Log.d("MPSG", "Selected proxy from DNS: " + iplist[next]);
    		Log.d("EXPERIMENTAL_RESULTS", "Selected proxy from DNS: " + iplist[next]);
    		proxyIp = iplist[next];
    		if (proxyIp == null) {
    			statusString = "Failed in discovering proxy";
    		}
    	   	dnsSearchTime = Math.abs(System.currentTimeMillis() - dnsStart);
    	   	totalSearchTime = Math.abs(System.currentTimeMillis() - subnetStart);
    		
    	} else {
    		Log.d("MPSG", "Selected proxy from subnet: " + proxyIp);
    		Log.d("EXPERIMENTAL_RESULTS", "Selected proxy from subnet: " + proxyIp);
    	   	subnetSearchTime = Math.abs(System.currentTimeMillis() - subnetStart);
    	}
    	
    	if (proxySelectedUsing) {
    		Log.d("MPSG", "Discovery time using UDP subnet search : " +  subnetSearchTime);
    		Log.d("EXPERIMENTAL_RESULTS", "Discovery time subnet search : " +  subnetSearchTime);
    	} else {
    		Log.d("MPSG", "Discovery time from DNS search: " +  dnsSearchTime);
    		Log.d("MPSG", "Total search time: " +  totalSearchTime);
    		Log.d("EXPERIMENTAL_RESULTS", "Discovery time from DNS search: " +  dnsSearchTime);
    		Log.d("EXPERIMENTAL_RESULTS", "Total search time: " +  totalSearchTime);
    	}
    	
    	if (prevProxyIP != null) {
    		Log.d("MPSG", "Have a previous proxy");
	    	if (prevProxyIP.getHostAddress().equals(proxyIp.getHostAddress())) {
	    		Log.d("MPSG", "New proxy and old proxy same. Reconnect");
	    	}
	    	
	    	else {
		    	// Start a new thread & communicate with old proxy to perform graceful connection close
		    	Thread closeOld = new Thread() {
		    		public void run() {
		    			long closeOldStart = System.currentTimeMillis();
		    			TCP_Session_Handler oldconn = new TCP_Session_Handler();
		    			oldconn.closeSessionWithOldProxy(mpsgName, proxyIp, prevProxyIP);
		    			long closeOldEnd = System.currentTimeMillis();
		    			Log.d("EXPERIMENTAL_RESULTS", "Time for closing session with old proxy: " + Math.abs(closeOldEnd - closeOldStart));
		    		}
		    	};
		    	closeOld.start();
	    	}
    	}
    	
    	// Set prevProxy as this proxy & connect to new proxy
    	prevProxyIP = proxyIp;
    	Thread startConnection = new Thread() {
    		public void run() {
    			connect();		
    		}
    	};
    	startConnection.start();
    	
		return;
	}
	
	/**
	 * Initiate a connection with the Proxy and return the status
	 * @return boolean 
	 */
	public static void connect() {
		long registerStartWithProxy = System.currentTimeMillis();
		long registerEndWithProxy = 0;
		conn = null;
		conn = new TCP_Session_Handler();
		
		/*
		try {
		proxyIp = InetAddress.getByName("192.168.173.1");
		} catch(Exception e) {}*/
		
		// Create socket connection to the proxy
        try {
            Log.d("MPSG", "serverAddr " + proxyIp + ", port: " + serverPort);
            conn.connectServer(proxyIp, serverPort);
        } catch(Exception e) {
        	Log.d("MPSG", "Unable to connect to server address " + proxyIp + ", error: " + e.toString());
        }
        
        // Register context data with proxy
        try {
        	Log.d("MPSG", "Registering with proxy for the context");
        	String res = conn.registerWithProxy(mpsgName, ContextType, StaticContextData); 
        	if (res.startsWith("MPSG Registration")) {
        		Log.d("MPSG", res);
        		statusString = "Connected";
        	} else {
        		Log.d("MPSG", res);
        		statusString = res + ", proxy: " + proxyIp;
        	}
        } catch (Exception e) {
        	Log.d("MPSG", "Error in receiving 'OK' response from proxy for registration request");
        	statusString = "Failed in registering with proxy " + proxyIp;
        }
        registerEndWithProxy = System.currentTimeMillis();
        Log.d("EXPERIMENTAL_RESULTS", "Time to Register:" + Math.abs(registerEndWithProxy - registerStartWithProxy));
        
        //StartAccelerationMonitor();
	}
	
	/**
	 * Initiate a query request to the proxy
	 * 
	 */
	public void sendQuery(String message) {
		// conn object will be set during connect call
		String temp[]= message.split(" ");
		
		String name;
		if(temp[0].contains("MPSGYungyi"))
			name = "testmpsgnameyy";
		else
			name = "testmpsgnamekw";
		
		//queryString = temp[0] + ";query:select person." + temp[1] + " from person where person.name = \"" + name + "\"";
		//queryString = mpsgName + ";query:select person." + temp[1] + " from person where person.name = \"" + temp[0] + "\"";
		if(ContextType == "elderly")
			queryString = mpsgName + ";query:select elderly." + temp[1] + " from elderly where elderly.name = \"" + temp[0] + "\"";
		else
			queryString = mpsgName + ";query:select caretaker." + temp[1] + " from caretaker where caretaker.name = \"" + temp[0] + "\"";
		// Send the query through the socket connection with proxy
		try {
			Log.d("MPSG", "Sending the query to the proxy");
			conn.sendQuery(queryString);
		} catch (Exception e) {
			Log.d("MPSG", "Error in sending query to the proxy");
		}
		/*
		try {
			Log.d("MPSG", "Sending the query to the proxy");
			conn.sendUpdate(updateString);
		} catch (Exception e) {
			Log.d("MPSG", "Error in sending query to the proxy");
		}*/
	}
	
	public void updateContext() {
		
		try {
			Log.d("MPSG", "Sending the query to the proxy");
			conn.sendUpdate(updateString);
		} catch (Exception e) {
			Log.d("MPSG", "Error in sending query to the proxy");
		}
		
	}
	
	/**
	 * Initiate a connection removal with the Proxy and return the status
	 * @return boolean 
	 */
	public static void disconnect() {
		long startLeave = System.currentTimeMillis();
        // De-Register context data with proxy
        try {
        	Log.d("MPSG", "De-Registering with coalition through proxy");
        	String res = conn.removeFromCoalition(); 
        	if (res.startsWith("leavesuccess")) {
        		Log.d("MPSG", res);
        		leaveStatusString = "Disconnected";
        	} else {
        		Log.d("MPSG", res);
        		leaveStatusString = res + ", proxy: " + proxyIp;
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        	Log.d("MPSG", "Error in de-registration request");
        	leaveStatusString = "Failed in de-registering with coalition, proxy:" + proxyIp;
        }
        long endLeave = System.currentTimeMillis();
        Log.d("EXPERIMENTAL_RESULTS", "Time to Deregister: "+Math.abs(endLeave - startLeave));
	}
	
	/**
	 * Starts connection status monitor of MPSG
	 *
	 */
	private void start() {
        try {
        	conn.enableKeepalive();
        	if (!conn.isAlive()) {
        		searchProxy();
        	}
        } catch (Exception e) {
        	Log.d("MPSG", "Unable to start Keepalive and status check");
        }
    }
	
	public static void setQueryResult (String result) {
	   	
	   	queryResult = result;
	    isQueryReady = true;	
	}
	
	//Elderly(Client) Side Code
	public static void runFallDetectedSequence() {
		
		//queryString = mpsgName + ";query:select elderly.location from elderly where elderly.name = \"kw\"";
		//queryString = mpsgName + ";query:select elderly.location from elderly";
		
		queryString = mpsgName + ";query:select caretaker.name from caretaker where caretaker.identity = \"caretaker\"";
		conn.sendQuery(queryString);
		
		while(isQueryReady == false) {}
		
		isQueryReady = false;
		
		Log.d("QUERY", queryResult);
		
		//List<String> contacts = new ArrayList<String>();
		//List<String[]> locations = new ArrayList<String[]>();
		//contacts = new ArrayList<String>();
		//locations = new ArrayList<String[]>();
		if(!contacts.isEmpty())
			contacts.clear();
		if(!locations.isEmpty())
			locations.clear();
		
		String temp[] = queryResult.split(",");
		
		for(int i=0; i<temp.length; i++){
			String temp2[] = temp[i].split("=");
			Log.d("QUERYNAME", temp2[1]);
			contacts.add(temp2[1]);			
		}
		
		for(int i=0; i<contacts.size(); i++) {
			queryString = mpsgName + ";query:select caretaker.location from caretaker where caretaker.name = \"" + contacts.get(i) + "\"";
			
			conn.sendQuery(queryString);
			
			while(isQueryReady == false) {}
			
			isQueryReady = false;
			
			Log.d("QUERY2 " + Integer.toString(i), queryResult);
			
			temp = queryResult.split("=");
			locations.add(temp[1].split(" "));
			Log.d("location", temp[1]);
			
		}
		
		//List<String> sortedContacts = 
		getContacts();
		
		int result = contactUsers();
		//if result is 1 then caretaker is contacted and its a real fall
		//if result is 0 then not fall 
		//if result is -1 then is ignored and caretaker list exhausted
		
		Log.d("isfall","Result: "+result);
		
	
		
		
				
	}
	
	private static void getContacts() {

		//after querying i have 2 lists of name and location
		String elderlyLoc[] = new String[2];
		Location curr = MpsgStarter.getCurrentLocation();
		elderlyLoc[0] = Double.toString(curr.getLatitude());
		elderlyLoc[1] = Double.toString(curr.getLongitude());
		
		//List<String> contacts = new ArrayList<String>();
		//List<String[]> locations = new ArrayList<String[]>();
		
		//List<Pair<String, Double>> userDistancePair = getDistances(contacts, locations, elderlyLoc);
		if(!userDistancePair.isEmpty())
			userDistancePair.clear();
		getDistances(elderlyLoc);
		
		// do sorting
		Pair<String, Double> temp;
		
		for (int i = 0; i < userDistancePair.size(); i++) {
			for (int v = 1; v < (userDistancePair.size() - i); v++) {
				if (userDistancePair.get(v-1).second > userDistancePair.get(v).second) {
					temp = userDistancePair.get(v-1);
			        userDistancePair.set(v-1, userDistancePair.get(v));
			        userDistancePair.set(v, temp);
			     }
			 }
		}  
		
		if(!nearestContacts.isEmpty())
			nearestContacts.clear();
		//nearestContacts = new ArrayList<String>();
		
		for (int i = 0; i < userDistancePair.size(); i++) {
			nearestContacts.add(userDistancePair.get(i).first);
			Log.d("CONTACT", nearestContacts.get(i));
		}
		
		//userDistancePair = null;
		return;
		
	}
	
	private static void getDistances(String[] elderlyLoc) {
		
		//List<Pair<String, Double>> userDistancePair = new ArrayList<Pair<String, Double>>();
		//userDistancePair = new ArrayList<Pair<String, Double>>();
		double distance;
		
		
		for( int i = 0; i < contacts.size(); i++) {
			
			distance = calcDistance(elderlyLoc, locations.get(i));
			userDistancePair.add(Pair.create(contacts.get(i), distance));
			Log.d("NAME", userDistancePair.get(i).first);
			Log.d("DISTANCE", Double.toString(userDistancePair.get(i).second));
			
		}
		
		return;
	}
	
	private static double calcDistance(String[] latAndLong1, String[] latAndLong2) {
		
		
		double lat1Deg = Double.parseDouble(latAndLong1[0]);
		double long1Deg = Double.parseDouble(latAndLong1[1]);
		double lat2Deg = Double.parseDouble(latAndLong2[0]);
		double long2Deg = Double.parseDouble(latAndLong2[1]);
		
		double R = 6371;
		double lat1Rad = Math.toRadians(lat1Deg);
		double lat2Rad = Math.toRadians(lat2Deg);
		double diffLat = Math.toRadians(lat2Deg - lat1Deg);
		double diffLong = Math.toRadians(long2Deg - long1Deg);
		
		double a = Math.sin(diffLat/2)* Math.sin(diffLat/2) +
			    Math.cos(lat1Rad) * Math.cos(lat2Rad) * 
			    Math.sin(diffLong/2) * Math.sin(diffLong/2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		double distance =  R * c;
		
		return distance;
	}
	
	public String[][] calculateUsersProximity() {
			//Querys for location context of registered users and return array of user + proximity
		String[][] test = new String[100][1];
		return test;
	}
		
	public static int contactUsers() {
			//TCP client to connect to caretakers TCP server
			//Loop until positive response from a caretaker
			//Once positive response send message to caretaker server to invoke IP camera
			//get response from caretaker and return true/false of fall
		Log.d("contactuser", "attempting to connect");
		String check="testing";
		String result="ignorefall";
		int i =0;
		while(result.contains("ignorefall")&& i<nearestContacts.size()){
			
			queryString = mpsgName + ";query:select caretaker.ipaddress from caretaker where caretaker.name = \"" + nearestContacts.get(i) + "\"";
			conn.sendQuery(queryString);
			
			while(isQueryReady == false) {}
			
			isQueryReady = false;
			
			Log.d("contactuser","query result:"+queryResult);
			String ipadd[]= queryResult.split("=");
			Log.d("contactuser","ipadd:"+ipadd[1]);
			clientsender= new ClientSender(ipadd[1]);
			try {
				result=clientsender.execute("192.168.1.1:8091,"+elderlyname+"\n").get();
				//check=result;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d("contactuser","result is: "+ result);
			
			if(result.contains("ignorefall"))//ignore fall increment to nxt caretaker
			{
				Log.d("increment","add i");
				i++;
			}
			
		}
		
		if(result.contains("falsefall")) //false alarm
		{
			return 0;
		}
		
		if(result.contains("realfall")) //real fall
		{
			return 1;
		}
		return -1;//list of caretakers exhausted
	}
	
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
	 
	private void serverstart(){
		ServerSocket serverSocket;
		String outgoingMsg="";
    	try {        	
            	//Log.d("ServerSocket",getLocalIpAddress());
            while(true){
            	serverSocket = new ServerSocket(5123);
                
                Log.d("ServerSocket"," waiting for incoming connections...");
            	Socket socket = serverSocket.accept();
            	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream()));

            	BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            	String incomingMsg=in.readLine();
            	
            	Log.d("ServerSocket","Connected to server");
        		Log.d("Message recieved","message:"+ incomingMsg
                        + ". Answering...");
        		
        		String message[]=incomingMsg.split(",");
        		Log.d("Message recieved","Split message:"+ message[0]
                        + "Next: " + message[1]);
        		
        		queryString = mpsgName + ";query:select elderly.location from elderly where elderly.name = \"" + message[1] + "\"";
    			conn.sendQuery(queryString);
    			
    			while(isQueryReady == false) {}
    			
    			isQueryReady = false;
    			
    			elderlyname=message[1];
    			elderlylocation=queryResult.split("=")[1];
    			Log.d("Elderly location:",elderlylocation);
            	MpsgStarter.mHandler.sendEmptyMessage(0);

            	while(true)
            	{
            		String test=MpsgStarter.getCaretakerResponse();
                	if(test.equals("realfall")||test.equals("falsefall")||test.equals("ignorefall")){

                	String result=MpsgStarter.getCaretakerResponse();
                	Log.d("helpstatus",result);
                	
                    // send a message
                    outgoingMsg =  result
                            + System.getProperty("line.separator");
                    
                    out.write(outgoingMsg);
                    out.flush();
                    MpsgStarter.setHelpstatus("");
                    Log.d("Message sent","Sent: " + outgoingMsg);
                    break;
                	}
            	}
            	Log.d("serversocket status:","out of loop");
                if(socket.getInputStream().read()!=-1) {	                    
                	Log.d("serversocket status:", "Socket still connected");
                }
                else
                {
                	Log.d("Serversocket status: ","Socket not connected");
                	socket.close();	
                }
                
                serverSocket.close();
            }
            
            
        } catch (Exception e) {
        	Log.d("serversocket: ", "Error: " + e.getMessage()+"\n");
            e.printStackTrace();
        }
    	
	}
	private static class ClientSender extends AsyncTask<String, Void, String> { //tcpclient
        private String SERVER_IP = null;
		private Socket socket;
        private String answer;
       // private Context context;
        private BufferedWriter out;
        private BufferedReader in;

        public ClientSender(String ip) {
            //this.context = context;
            socket = null;
            out = null;
            in = null;
            SERVER_IP=ip;
        }
        
        @Override
        protected String doInBackground(String... params) {
            try {
                if (socket == null) {
                    socket = new Socket(SERVER_IP, 5123);

                    out = new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream()));
                    in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                }
                Log.d("clientsocket","sent to server:" + params[0]);
                out.write(params[0]);
                out.flush();
                
                answer = in.readLine() + System.getProperty("line.separator");
                Log.d("clientsocket","reply from server:" + answer);
                socket.close();
                return answer;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "";
        }

        protected void onPostExecute(String answer) {

        	Log.d("clientsocket", "at execute " +answer);
        	/*try {
				socket.close();
				Log.d("clientsocket", "closing socket");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

        }
    }
	
}