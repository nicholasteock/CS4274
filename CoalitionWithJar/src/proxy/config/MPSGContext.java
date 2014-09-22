package proxy.config;

import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import proxy.connectionmanager.TCP_Session_Handler;
import proxy.query.processor.PSGQueryObject;
import proxy.query.processor.PSGQueryParser;
import proxy.service.manager.ContextAttribute;
import proxy.service.manager.ContextDataService;
import proxy.service.manager.ContextDomain;

@SuppressWarnings("rawtypes")
public class MPSGContext {
	
	public static final int MPSGTIMEOUT = 60; // 1 minute is the reconnect timeout for any disconnected MPSG
	public static boolean updated = false;
	public static HashMap mpsgContextDomain = new HashMap();
	public static HashMap mpsgContextData = new HashMap();
	
	/**
	 *  Holds the current state information for the MPSG
	 *  created: entry for the MPSG exists in the Proxy
	 *  init: no query request to the MPSG
	 *  active: unresponded query pending for MPSG
	 *  closing: MPSG initiated a close request
	 *  closed: connection closed, after passive state
	 *  closedwithupdate: connection closed with update, after active state
	 */
	public static HashMap mpsgStateInfo = new HashMap(); 
	
	// Variables containing last query/update information, before/during handoff
	public static HashMap mpsgLatestQuery = new HashMap();
	public static HashMap mpsgLastUpdate = new HashMap();

	@SuppressWarnings("unchecked")
	public static int createMPSGContext(String name, String contextType, String contextData) {
		if (!mpsgContextDomain.containsKey(name)) {
			System.out.println("Got a new MPSG: " + name);
			updateContext(name, contextData);
			System.out.println("Updated context information");
			
			mpsgContextDomain.put(name, contextType);
			System.out.println("Updated context type");

			return 0;
		} else {
			System.out.println("Entry already existing");
			return -1;
		}
	}
	
	public static void updateContext(String name, String context) {
		System.out.println("Updating context information for " + name + " with context:" + context);
		HashMap hash = new HashMap();
		for (String eachcontext: context.split(",")){
			String getAttrib[] = eachcontext.split("::");
			System.out.println("Attrib: " + getAttrib[0] + ",Value:" + getAttrib[1]);
			hash.put(getAttrib[0], getAttrib[1]);
		}
		Socket socket = (Socket) MPSGConfiguration.socketList.get(name);
		System.out.println("into update context" + socket.isClosed());
		mpsgContextData.put(name, hash);
		updated = true;
	}
	
	public static void updateContext1(String name, String context) {
		String IP = MPSGConfiguration.ipList.get(name).toString();
		String Port = MPSGConfiguration.portList.get(name).toString();

		// Trim IP to remove any trailing and leading special characters
		if (IP.startsWith("/")) {
			IP = IP.substring(1);
		}
    	String reference = "http://"+IP+ ":" + Port + "/xmlrpc";
		
    	ContextDataService cds = new ContextDataService();
    	ContextDomain cdInstance = cds.getContextDomain(name);
		
		System.out.println("Updating context information for " + name + " with context:" + context);
		HashMap hash = new HashMap();
		for (String eachcontext: context.split(",")){
			String getAttrib[] = eachcontext.split("::");
			System.out.println("Attrib: " + getAttrib[0] + ",Value:" + getAttrib[1]);
			hash.put(getAttrib[0], getAttrib[1]);
			if (cdInstance.containsAttribute(getAttrib[0])) {
				System.out.println("Attribute available");
				cdInstance.updateAttributeValue(getAttrib[0], getAttrib[1]);
			} else {
				System.out.println("Attribute existing:");
				cdInstance.getAttributeList().toString();
			}
		}
		Socket socket = (Socket) MPSGConfiguration.socketList.get(name);
		System.out.println("into update context" + socket.isClosed());
		mpsgContextData.put(name, hash);
		updated = true;
	}
	
	public static ContextDomain getDomainInfo(String name, String reference) {
		/* Sample Context String:
		 * 
		 * domainType: SHOP
		 * name::bookshop,type::book,location::vivo,crowdness::low,brightness::high,noiseness::low
		 *
		 * domainType: PERSON
		 * name::personname,preference::pc,location::ion,isBusy::yes,speed::nil,action::eating,power::low,mood::happy
		 * 
		 * domainType: OFFICE
		 * name::officename,projector::no,location::IDMI,isMeeting::no,lightOn::yes,sound::low
		 * 
		 * domainType::HOME
		 * name::myhome,temperature::25,location::clementi,isOccupied::yes,lightOn::yes,noiseness::medium
		 * 
		 * domainType: CLINIC
		 * name::nuhclinic,type::general,location::nus,isOpen::yes,brightness::high,noiseness::high 
		 */
		
		ContextDomain cdInstance = new ContextDomain(name, (String) mpsgContextDomain.get(name), reference);
		
		HashMap hash = new HashMap();
		System.out.println("Getting context data for " + name + ", reference " + reference);
		hash = (HashMap) mpsgContextData.get(name);
		System.out.println("Context data retreived: " + hash);
		Set attribList = hash.keySet();
		Iterator iter = attribList.iterator();
		while (iter.hasNext()) {
			String attributeName = (String) iter.next();
			String attributeValue = (String) hash.get(attributeName); 
			cdInstance.addAttribute(new ContextAttribute(attributeName, "String", attributeValue));
		}
		return cdInstance;
	}
	
	@SuppressWarnings("unchecked")
	public static boolean updateAttributes(String name, String query) {
		Socket sock = (Socket) MPSGConfiguration.socketList.get(name);
		boolean result = true;
		// Change state to active as a query is to be responded by MPSG
		mpsgStateInfo.put(name,  "active");
		
		String requestAttrib = "update::";
		// Get the list of attributes in the query
		PSGQueryObject queryObject = PSGQueryParser.parseQuery(query);
		System.out.println("Checking if there are dynamic attrib in the query");
		System.out.println("Query condition:" + queryObject.getQueryConditions().getCondition());
		/*Pattern pattern = Pattern.compile("/\\w+/");
		Matcher matcher = pattern.matcher(queryObject.getQueryConditions().getCondition());
		if (matcher.find()) {
		    System.out.println(matcher.group(0)); 
		} else {
		    System.out.println("Match not found");
		}*/
		//String temp1[] = queryObject.getQueryConditions().getCondition().split("and");
		
		// Check if there are any dynamic attributes and trigger update from MPSG
		for (int i=0; i<queryObject.getAttributes().size(); i++) {
			String attrib = queryObject.getAttributes().get(i);
			System.out.println("Got the nest attribute, name:" + attrib);
			//String temp[] = attrib.split(".");
			//System.out.println("domain:" + temp);
			List<String> list = (List<String>) MPSGConfiguration.dynAttribList.get(attrib.split("\\.")[0]);
			if (list.contains(attrib)) {
				System.out.println("Found a dyn attrib: " + attrib);
				requestAttrib = requestAttrib.concat(attrib);
				requestAttrib = requestAttrib.concat(";");
				System.out.println("Request for update: " + requestAttrib);
			}
		}
		
		if (!requestAttrib.contentEquals("update::")) {
			System.out.println("Dyn attrib update needed");
			final String updateReq = requestAttrib;
			// Update the latest query to the hash, incase session recovery needed
			mpsgLatestQuery.put(name, updateReq);
			result = TCP_Session_Handler.GetUpdates(name, updateReq);
		}
		
		// Reset MPSG state to "connected" and also clear latest query info
		mpsgStateInfo.put(name, "connected");
		mpsgLatestQuery.put(name, null);
		
		return result; // returns the status of the update
	}

	/** 
	 * Function to remove the mapping of MPSG in Proxy and to clear all the ports and memory 
	 * associated with that MPSG
	 * @param string
	 */
	public static void removeMPSG(String name) {
		System.out.println("Cleaning up data associated with MPSG: "+name);
		mpsgContextDomain.remove(name);
		mpsgContextData.remove(name);
		
		// Remove socket, ip and port associations also
		MPSGConfiguration.ipList.remove(name);
		MPSGConfiguration.portList.remove(name);
		MPSGConfiguration.socketList.remove(name);
	}
}
