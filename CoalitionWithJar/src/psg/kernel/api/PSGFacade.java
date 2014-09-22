/**
 * This is the PSG interface class.
 * This class provides interfaces for applications to register and withdraw 
 * PSGs with Coalition server.
 * This class will store all the registered PSGs. (allPSGs)
 * This class will call methods of McsAPI.java class to proceed with 
 * registration, withdrawal and query.
 * Ivan, 7 May 2012
 */
/**
 * @modifiedBy Ivan
 * @date 20 Jul 2012
 * @actions
 * 1) logically deleted, reserved for future reference -- 20 Jul 2012
 * */

package psg.kernel.api;

import java.util.Hashtable;

import psg.kernel.connection.ConnectionManager;
import psg.service.manager.ContextDataService;
import psg.service.manager.ContextDomain;

public class PSGFacade {
	// data
	private static ContextDataService myPSG = null;
	private String myID="";
	private ContextDomain cdInstance;
//	public static Hashtable<String, ServiceManager> allPSGs = new Hashtable<String, McsAPI>();
	
	// - Constructor - ID_KEY = http://192.168.10.10:8080/xmlrpc; [ Listening Port ]  
	public PSGFacade(String idKey, String ip, int port) throws Exception {
		this.myID = idKey;
		this.cdInstance = null;
//		this.myPSG = new ContextDataService(cdInstance, ip, port);
		this.myPSG = new ContextDataService();
//		this.allPSGs.put(myID, myPSG);
	}
	
	public void assign(String domain, String attr, String value){
//		myPSG.assignAttributeValue(domain, attr, value);
	}
	
	public String getContext(String domain, String attr){
//		return myPSG.getAttributeValue(domain, attr);
		return "";
	}

	// - Leave PSG
	public void leave(String msid){
//		myPSG.leave(myID,msid);
	}

	// - Register PSG
	public String register(String ipPort){
//		return myPSG.register(myID, ipPort);
		return "";
	}

	
	// - Query MCS	
	public String queryMCS(String query, String idKey){
		String res=null;
		try{
//			res = QueryClient.queryMCS(query,idKey);
		} catch(Exception e){
			e.printStackTrace();
		}		
		return res; 
	}
	
	
	//Ivan 25 June 2010
	public void mobilityUpdate(String msid, String newIPPort){
//		myPSG.mobilityUpdate(myID, msid, newIPPort);
	}
	
	public String registerCallback(String name, String callerId, String calleeId){
//		return myPSG.registerCallback(name, callerId, calleeId);
		return null;
	}
	
	public String withdrawCallback(String name, String callerId, String calleeId){
//		return myPSG.withdrawCallback(name, callerId, calleeId);
		return null;
	}
	
}
