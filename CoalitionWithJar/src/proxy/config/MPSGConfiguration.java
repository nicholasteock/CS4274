package proxy.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import proxy.starter.ProxyStarter;

@SuppressWarnings("rawtypes")
public class MPSGConfiguration {

	public static final int MPSGTIMEOUT = 60;
	// Data Section, common for all MPSG
	public static String coalitionIP = "192.168.0.14"; // IP of Coalition server
	public static HashMap ipList = new HashMap();
	public static HashMap portList = new HashMap();
	public static HashMap socketList = new HashMap();
	public static HashMap instr = new HashMap();
	public static int timeOutForQuery = 5000; // In Milli-Seconds
	public static String csmAddress = "http://" + coalitionIP + ":10001/xmlrpc";
    public static String queryProcessorAddress = "http://" + coalitionIP + ":10004/xmlrpc";
    public static HashMap dynAttribList = new HashMap();
	public static boolean presenceFlag = true;
    
    // Populate dynamic attributes list for each context during load
    @SuppressWarnings("unchecked")
	public MPSGConfiguration() {
    	List<String> personList = new ArrayList<String>();
    	personList.add("person.location");
    	personList.add("person.speed"); 
    	personList.add("person.action");
    	personList.add("person.power");
    	personList.add("person.mood");
    	personList.add("person.acceleration");
    	personList.add("person.magnetism");
    	personList.add("person.gravity");
    	
    	List<String> elderlyList = new ArrayList<String>();
    	elderlyList.add("elderly.ipaddress");
    	elderlyList.add("elderly.location");
    	
    	List<String> caretakerList = new ArrayList<String>();
    	caretakerList.add("caretaker.ipaddress");
    	caretakerList.add("caretaker.location");
   
    	dynAttribList.put("person", personList); // Map dynamic attributes of Person space to personList
    	dynAttribList.put("elderly", elderlyList);
    	dynAttribList.put("caretaker", caretakerList);
    	//TODO: More static information to be added here, for other context spaces
    }
	
    // Add new PSG into hash
    @SuppressWarnings("unchecked")
	public int updateMPSG(String name, InetAddress ip, Socket socket, BufferedReader in) {

    	// Get new port if it is a new registration, else skip port update
    	if (portList.get(name) == null) {
	    	// Check if there is a free port available for the new MPSG
	    	int port = getFreePort();
	    	if (port != 0) {
	    		portList.put(name, port);
	    	} else {
	    		System.out.println("No free port was found");
	    		return -1; // Send error status to MPSG client
	    	}
    	}
    	
    	// Create or update IP of MPSG
    	ipList.put(name, ip);
    	
    	// Create or update socket of MPSG
    	socketList.put(name, socket);
    	
    	// Create or update input stream of MPSG
    	instr.put(name, in);
    	System.out.println("Added into socketlist, socketlist= " + socketList);
    	
    	return 0; // Send success status to MPSG client
    }
    
    // Check for available port from the machine
    public int getFreePort() {
    	ServerSocket s = null;
        try {
        	s = new ServerSocket(0);
            return s.getLocalPort();
        } catch (IOException ex) {
            return 0;
        }
    }

    // get proxy IP address
    private static String getIPAddress() {
    	try {
    		return ProxyStarter.proxyIP;
			//return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
    }
    
}
