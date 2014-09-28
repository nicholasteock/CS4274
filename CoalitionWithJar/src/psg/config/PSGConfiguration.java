package psg.config;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PSGConfiguration {

	// data 
	public static final String RESOURCE_NAME = "PSG SIMULATOR";
	public static String defaultName = "NUS Home"; // Name of the PSG

	public static String defaultIP = "192.168.1.69"; // IP address of the PSG; getIPAddress();//
	public static int defaultPort = 130000; // Port Number of the PSG
	public static String defaultMac = "00-1E-4F-A7-DF-B2"; // getMacAddress(); MAC address of the PSG

	public static final String CSM_REFERENCE = "http://192.168.1.69:10001/xmlrpc";
	public static int timeOutForQuery = 5000; // In Milli-Seconds
    public static final String QP_REFERENCE = "http://192.168.1.69:10004/xmlrpc"; // psg IP
    
    
    // constructor
    
    // followings are modified by Ivan 25 April 2012
    // methods
    private static String getMacAddress() {
    	InetAddress ip;
    	String macAddress = null;
    	try {
    		ip = InetAddress.getLocalHost();
    		
    		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
    		
    		byte[] mac = network.getHardwareAddress();
    		
    		// convert mac to string
    		StringBuilder sb = new StringBuilder();
    		for (int i=0; i<mac.length; i++) {
    			sb.append(String.format("%02X%s", mac[i], (i<mac.length-1)? "-":"" ));
    		}
    		macAddress = sb.toString();
    		
    	} catch (UnknownHostException e) {
    		e.printStackTrace();
    	} catch (SocketException e) {
    		e.printStackTrace();
    	}
    	return macAddress;
    } // end of getMacAddres
    
    // get IP address
    private static String getIPAddress() throws UnknownHostException {
    	return InetAddress.getLocalHost().getHostAddress();
    }
    
    
}
