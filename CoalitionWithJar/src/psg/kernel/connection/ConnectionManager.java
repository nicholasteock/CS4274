/**
 * This file implement functions that call server functions.
 * @modifiedBy Ivan
 * @date 12 Jul 2012
 * @description
 * 1) this class will be as the interface and manager of 
 * connection database.
 * 
 * */
package psg.kernel.connection;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import psg.config.PSGConfiguration;
import psg.service.manager.ContextAttribute;
import psg.service.manager.ContextDomain;


//import messaging.format.MsgExtension;

// This is use to discover the Addresses of CSG and PSG
public class ConnectionManager {
	// data
	
//	private String idKey;
//	private String myName;
//	private String domainType;
	//format of all addresses: "http://"+defaultIP+ ":"+defaultPort+"/xmlrpc"; Ivan, 11 May 2012
	// For Discovering CSM and CSG
//	private ConnectionManager connectionManager;
//	private String myIP; // the IP address of this PSG instance, Ivan, 11 May 2012
//	private int mPort; // the port of this PSG instance, Ivan, 11 May 2012
//	private NetworkConnector networkConnector;
	// Ivan, 12 Jul 2012: since we are going to simulate many different PSGs
	// we store the Connection database of its PSG here
	private static Map<String, ConnectionDB> allPSG = new Hashtable<String, ConnectionDB>();
	// constructor
	// Ivan, 12 Jul 2012: here, this may not be the better way to 
	// define the constructor, but since we need this for simulation
	// in order to make changes as small as possible, we do it like this.
	public ConnectionManager() {};
//	public ComponentsLocator(String psgName) {	
////		this.idKey = idKey;
////		this.myName = myName;
////		this.domainType = domainType;
////		this.connectionManager = new ConnectionManager();	
////		this.networkConnector = new NetworkConnector();
//		if(!allPSG.containsKey(psgName)) {
//			allPSG.put(psgName, new ConnectionManager());
//		} 
//		this.connectionManager = allPSG.get(psgName);
//	}

	// methods	
	// Register itself as a PSG.
	public void registerPSG(ContextDomain cdInstance)  {
//		System.out.println("registerPSG");

		// Step 1: prepare PSG related parameters
		String psgName = cdInstance.getName();
		String csgName = cdInstance.getType();
		String psgReference = cdInstance.getReference();
		
		// Step 2: create new ConnectionDB
		if(!allPSG.containsKey(psgName)) {
			allPSG.put(psgName, new ConnectionDB());
		} 
		ConnectionDB connectionManager = allPSG.get(psgName);
		
		// Step 3: register with Coalition sc by sc
		for(ContextAttribute ca : cdInstance.getAttributeList()) {
//			System.out.println("Calling register PSG");
			registerPSG(psgName, csgName, ca.getName(), ca.getType(), ca.getValue(), psgReference, connectionManager);
		}
	}
	
	// Ivan, 24 Feb 2014: update psg reference
	public void updatePSG(ContextDomain cdInstance) {
		// Step 1: get sc references from ConnnectionDB
		String csgName = cdInstance.getType();
		String psgName = cdInstance.getName();
		String psgReference = cdInstance.getReference();
		ConnectionDB connectionDB = allPSG.get(psgName);
		
		// Step 2: update each sc with new reference
		for(ContextAttribute ca : cdInstance.getAttributeList()) {
//			System.out.println("Calling register PSG");
			String scName = ca.getName();
			String scReference = connectionDB.getSCRef(scName);
			NetworkConnector.updatePSGReference(csgName, scName, psgName, psgReference, scReference);
//			registerPSG(psgName, csgName, ca.getName(), ca.getType(), ca.getValue(), psgReference, connectionManager);
		}
	}
	
	
	// end
	
	
	// unregistration
	public void leavePSG(ContextDomain cdInstance)  {
		// Step 1: prepare PSG related variables
		String psgName = cdInstance.getName();
		String csgName = cdInstance.getType();
		String psgReference = cdInstance.getReference();
		
		// Step 2: get and remove the connection manager
		ConnectionDB connectionDB = allPSG.remove(psgName);
		
		// Step 3: leave Coalition SC by SC
		for(ContextAttribute ca:cdInstance.getAttributeList()) {
			leavePSG(psgName, csgName, ca.getName(), connectionDB);
		}	
	}	

//	public String queryMCS(String psgName, String querySignature, String queryString) {
//		ComponentsLocator temp = new ComponentsLocator(psgName);
//		return temp.queryMCS(querySignature, queryString);
//	}
//	
//	// Ivan, 13 Jul 2012: begin
//	public void queryP2P(String psgName, String querySignature, String queryString, String destinationReference, String scName) {
//		// Step 1: get neighbor references
//		ComponentsLocator componentsLocator = new ComponentsLocator(psgName);
//		
//		// Step 2: forward queries to neighbors
//		componentsLocator.queryP2P(querySignature, queryString, destinationReference, scName);
//	}
//	
//	public void reportResult(String psgName, String querySignature, String queryResult, String destinationReference) {
//		ComponentsLocator temp = new ComponentsLocator(psgName);
//		temp.reportResult(querySignature, queryResult, destinationReference);
//	}
//	
//	public void registerNeighbor(String psgName, String csgName, String scName, String requestorReference) {
//		ComponentsLocator temp = new ComponentsLocator(psgName);
//		temp.registerNeighbor(csgName, scName, requestorReference);
//	}
	
	// end
	private void registerPSG(String psgName, String csgName, String scName,
			String valueType, String value, String psgReference, ConnectionDB connectionManager) {
		// Step 1: get CSG reference
		while(!connectionManager.hasCSG()) {
			String csmReference = connectionManager.getCSMRef();
			String tempReference = NetworkConnector.getCSGReference(csgName, csmReference);
			connectionManager.setCSGRef(tempReference);
		}
		String csgReference = connectionManager.getCSGRef();

		// Step 2: get SC reference
		while(!connectionManager.containsSC(scName)) {
			String tempReference = NetworkConnector.getSCReference(csgName, scName, csgReference);
			connectionManager.setSCRef(scName, tempReference);
		}
		String scReference = connectionManager.getSCRef(scName);
		// Step 3: register with SC
		String neighborReferences = NetworkConnector.registerPSGToSC(psgName,
				csgName, scName, valueType, value, psgReference, scReference);
//		System.out.println("[ConnectionManager.registerPSG].neighborReferences: " + neighborReferences);
		
		// Step 4: update neighbor connections
//		extractNeighbors(name, csgName, scName, neighborReferences);
		// Ivan, 17 Jul 2012: if no neighbors exist, then exist
		if(!neighborReferences.contains("@")) {
			return;
		}
		String[] allNeighbors = neighborReferences.split("@");
		List<String> neighborList = new Vector<String>();
		for(String element:allNeighbors) {
			neighborList.add(element);
		}
		connectionManager.setNeighbors(scName, neighborList);
		
		// Step 5: update neighbor with this neighborhood relation
		for(String neighbor : allNeighbors) {
//			System.out.println("[ConnectionManager.registerPSG].neighbor: " + neighbor);
			
			NetworkConnector.registerNeighbor(csgName, scName, psgName, psgReference, neighbor);
		}
	}
	
	// Ivan, 12 Jul 2012: re-written
	// ?? this withdraw based on value may not be a good idea, especially values may change
	private String leavePSG(String psgName, String csgName, String scName, String valueType, String value, ConnectionDB connectionManager) {
		// Step 1: get SC reference
		String scReference = connectionManager.getSCRef(scName);
		// Step 2: withdraw from SC
		return NetworkConnector.leavePSG(psgName, csgName, scName,valueType, value, scReference);
	}
	
	// Ivan, 13 Jul 2012: withdraw PSG by name
	private void leavePSG(String psgName, String csgName, String scName, ConnectionDB connectionDB) {
		// Step 1: get SC reference
		String scReference = connectionDB.getSCRef(scName);
		
		// Step 2: widthdraw from SC
		NetworkConnector.leavePSG(psgName, csgName, scName, scReference);
	}
	
	public void registerNeighbor(String psgName, String csgName, String scName, String requestorName,String requestorReference) {
//		System.out.println("[ConnectionManager.registerNeighbor].location check!");
		ConnectionDB connectionManager = allPSG.get(psgName);
		connectionManager.addNeighbor(scName, requestorName, requestorReference);
	}
	
	
	public void refreshNeighborList(String psgName, String scName, String[] neighborArray) {
		ConnectionDB connectionDB = allPSG.get(psgName);
		List<String> neighborList = new Vector<String>();
		for(String element:neighborArray) {
			neighborList.add(element);
		}
		connectionDB.setNeighbors(scName, neighborList);
	}
	
	public String queryMCS(String psgName, String querySignature, String queryString, String issuerReference) {
		// Step 1: get Connection manager
		ConnectionDB connectionManager = allPSG.get(psgName);
		
		// Step 2: get QP reference
		String qpReference = connectionManager.getQPRef();
		
		// Step 3: query mcs
		return NetworkConnector.queryMCS(querySignature, queryString, issuerReference, qpReference);
	}
	
    // Disseminate Query to assigned PSG in a Semantic Cluster.
	public void queryP2P (String psgName, String querySignature, String queryString, String destinationReference, String scName)  {
		// Step 1: get Connection db
		ConnectionDB connectionManager = allPSG.get(psgName);
		
		// Step 1: get neighbor reference
		List<String> neighborList = connectionManager.getAllNeighbors(scName);
		
		// Step 2: forward to neighbors
		if(neighborList == null) {
			return;
		}
		for(String neighbor : neighborList) {
			System.out.println("[ConnectionManager.queryP2P].neighbor: " + neighbor);
			// Ivan, 18 Jul 2012: neighbor name has been added in NetworkCOnnector.queryP2P()
			NetworkConnector.queryP2P(querySignature, queryString, destinationReference, scName, neighbor);
		}
	}
	
	public String reportResult(String querySignature, String queryResult, String destinationReference) {
		return NetworkConnector.reportResult(querySignature, queryResult, destinationReference);
	}
		
	
//	private String createSC(String csgName, String scName, String scGenAddress) throws Exception{
//	    setOutgoingSCGenConnection(scGenAddress);
//	    return (String) networkConnector.createSC(csgName, scName, scGenAddress);
	    
//		// make the a regular call
//	    Object[] params = new Object[] { new String(csgName), new String(scName)};      
////	    Object[] params = { new String(csgName), new String(scName)};      
//	   // System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Create SC");
//		if (!existsSCGen(scGenAddress)){
//		//	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Get Outgoing SCGen Connection!");
//			getOutgoingSCGenConnection(scGenAddress);
//		} else {   // have deleted one "if" statement by Ivan on 3 May 2012 
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"NULL");
//		}	
//	    String result = "nothing:nothing";	// ?? strange, Ivan, 3 May 2012
//	    									// Maybe for testing purpose, and should be result = null for declaration
//	    try{
//	    //	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Is it Null? = "+connectionManager.getSCGenClient(scGenAddress));
//	        result = (String) connectionManager.getSCGenClient(scGenAddress).execute("PSGManager.createSC", params);
//	    } catch (Exception e) {
//	   // 	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"UhOh");
//	    	e.printStackTrace();
//	    }
//	   // System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Creates SC: " + result);
//	    return result;		
//	}	
	
	// ?? Ivan, 4 May 2012
	// the following two createCSG(...) methods are actually the same, and parentCSG is not used
//	private String createCSG(String parentCSG, String csgName, String csgGenAddress) throws Exception{
//	    // make the a regular call
//	    Object[] params = new Object[] { new String(csgName)};      
//	  //  System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Create CSG");
//		if (!existsSCGen(csgGenAddress)){
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Get Outgoing SCGen Connection!");
//			getOutgoingCSGGenConnection(csgGenAddress);
//		} else {
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"NULL");
//		}
//	    
//	    String result = "nothing:nothing";
//	    try{
//	    	//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Is it Null? = "+connectionManager.getCSGGenClient(csgGenAddress));
//	        result = (String) connectionManager.getCSGGenClient(csgGenAddress).execute("CPManager.createCSG", params);
//	    } catch (Exception e){
//	    //	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"UhOh");
//	    	e.printStackTrace();
//	    }
//	//    System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Creates SC: " + result);
//	    return result;		
//	}
	
	
	// Ivan, 12 Jul 2012: in the new design structure,
	// we do not need to prepare CSG and SC any more but 
	// just retrieve their reference
//	// Ivan, 14 May 2012
//	// Prepare the connection with CSG
//	// create CSG if does not exist
//	private void prepareCSG(String parentCSG, String csgName) throws Exception{
//		// CSG address has been set, return 
//		if(connectionManager.hasCSG()) { return ;}
//		//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Checking CSG!");
////		String getCSGLink = (String) getCSGRef(csgName);
//		String getCSGLink = (String) networkConnector.getCSGReference(csgName);
//	//	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"GetCSG = "+getCSGLink);
//		System.out.println("Data from CSG: " + getCSGLink);
//		// ?? what is the format of getCSGLink? Ivan, 11 May 2012
//		if(!getCSGLink.startsWith("[CSG NOT FOUND]")) {
//			connectionManager.setCSGConfiguration(getCSGLink);
//		} else {
//			// (1) Get CSG Generator address from CSM.
//	    	String csgGenAddress = getCSGLink.substring(getCSGLink.indexOf(":")+1, getCSGLink.length());
//	   // 	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"(1) CSGGen = "+result);
//		
//	    	// (2) Create SC in SCGenerator.
//	    	//  -> Foreign call to SC Generator Server
////	    	String csgAddress = createCSG(csgName, result); // likely format of result: "IP:Port",Ivan 4 May 2012
//	    	// update locally
////	    	setOutgoingCSGConnection(csgGenAddress);
//	    	connectionManager.setCSGConfiguration(csgGenAddress);
//	    	// update with server
//	    	String csgAddress = networkConnector.createCSG(csgName, csgGenAddress);
//	    	
//	    	String csgIPAddress = csgAddress.substring(0, csgAddress.indexOf(":"));
//	    	String csgPort = csgAddress.substring(csgAddress.indexOf(":")+1, csgAddress.length());
//	    	System.out.println("CSG Port is " + csgPort);
//	    	System.out.println("PSG Port is " + csgPort);
//	    	csgAddress = "http://"+csgAddress+"/xmlrpc";
//	  // 	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"(2) csgAddress = "+csgAddress);	     
//	    	
//	    	// (3) Helps to Register CSG at CSM.
//	    	//  -> Foreign call to CSM Server 
////	    	registerCSG(csgName, csgIPAddress, Integer.parseInt(csgPort));
//	    	if(parentCSG == null) {
//	    		networkConnector.registerCSG(csgName, csgIPAddress, Integer.parseInt(csgPort));      
//	    	} else {
//	    		networkConnector.registerCSG(parentCSG, csgName, csgIPAddress, Integer.parseInt(csgPort));
//	    	}
//		} 
//	}
//	
//	private void prepareSC(String csgName, String scName) throws Exception {
//		// if SC address has been set, return
//		if(connectionManager.containsSC(scName)) {return ;}
////		String getCPS = (String) getCoordinatorPeerSystem(csgName, scName);
//		String getCPS = (String) networkConnector.getCoordinatorPeerSystem(csgName, scName);
//		System.out.println("String getCPS is: " + getCPS);
////		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"GEtCP = |"+getS+"|");
//		// If SC Not found
//        if (!getCPS.startsWith("[SC NOT FOUND]")) {
//			connectionManager.setSCConfiguration(scName, getCPS);
//		} else {
//        	
//			// (1) Get SC Generator address from CSG.
//        	String result = getCPS;
//    //    	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Result = " + result);
//        	String scGenAddress = result.substring(result.indexOf(":")+1, result.length());
//    //    	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"(1) SCGen = "+result);
//        	
//        	// (2) Create SC in SCGenerator.
//        	//  -> Foreign call to SC Generator Server
////        	String scAddres = createSC(csgName, scName, scGenAddress); // likely format: "IP:Port" , Ivan 4 May 2012
//        	
////        	setOutgoingSCGenConnection(scGenAddress);
//        	connectionManager.setSCConfiguration(scName, scGenAddress);
//        	String scAddress = (String) networkConnector.createSC(csgName, scName, scGenAddress);
//        	
//        	String scIPAddress = scAddress.substring(0,scAddress.indexOf(":"));
//        	String scPort = scAddress.substring(scAddress.indexOf(":")+1, scAddress.length());
//        	scAddress = "http://"+scAddress+"/xmlrpc";
// //       	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"(2) scAddress = "+scAddress);
//        	
//        	// (3) Helps to Register SC at CSG.
//        	//  -> Foreign call to CSG Server 
//        	
//        	//This is where the new coordinator peer is assigned. Possible changes required here
//        	System.out.println("Before registerCP IP is: " + scIPAddress);
////        	registerCP(csgName, scName, scIPAddress, Integer.parseInt(scPort));
//        	networkConnector.registerCP(csgName, scName, scIPAddress, Integer.parseInt(scPort));
//        	
///*		        Object[] params2 = new Object[]
//	                     	            { new String(scName),  new String(requestorIP),  new Integer(requestorPort), new Boolean(true) };
//	        
//	        System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"1");
//			String res = (String) connectionManager.getCSGClient(csgName).execute("CPManager.registerPSG",params2);
//	        System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"2");				
//			res = "http://"+res+"/xmlrpc";
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"res = "+res);
//*/			
//
//////        	String res = scAddress;
////			try{
////        		connectionManager.addCPConfiguration(csgName+"@"+scName, scAddress);
////   //     		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+""+csgName+"@" +scName +" CP created.");
////        	} catch (Exception e){
////        		e.printStackTrace();
////        //		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: SC cannot be added.");
////        	}				
//		} 		
//	}
	
//	private void createCSG(String csgName, String getCSGLink) throws Exception{
		
	    
//		// make the a regular call
////	    Object[] params = new Object[] { new String(csgName)};      
//	    Object[] params = { new String(csgName)};      
//	  //  System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Create CSG");
//		if (!existsSCGen(csgGenAddress)){
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Get Outgoing SCGen Connection!");
//			getOutgoingCSGGenConnection(csgGenAddress);
//		} else {
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"NULL");
//		}
//	    
//	    String result = "nothing:nothing";
//	    try{
//	    	//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Is it Null? = "+connectionManager.getCSGGenClient(csgGenAddress));
////	        System.out.println("Calling CSG Generator");
//	    	result = (String) connectionManager.getCSGGenClient(csgGenAddress).execute("CPManager.createCSG", params);
////	    	System.out.println(result);
//	    } catch (Exception e){
//	    //	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"UhOh");
//	    	e.printStackTrace();
//	    }
//	//    System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Creates SC: " + result);
//	    return result;		
//	}	


	


	
	
	
	// Ivan, 12 Jul 2012: a new registration method is given above
//	public void registerPSG(String name, String csgName, String scName,
//			String requestorIP, int requestorPort, String myName) {
//		// ??what is the format of myName? Ivan, 15 May 2012
////		int ch = '?';
////		int ch1 = '=';
////		int index = myName.indexOf(ch1);
////		String temp = myName.substring(myName.indexOf("=")+1, myName.length());
//		String temp = myName.substring(myName.indexOf("=")+1);
////		index = temp.indexOf(ch);
//		String psgID = temp.substring(0, temp.indexOf("?"));
//		
////		String id = csgName+"@"+scName;
//		String psgResponse = "";
//	//	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Compo Locate: RegPSG: "+ ID);
//		
//		try{
//			prepareCSG(null, csgName);
//			
//	//		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Checking SC!");
//			//createSC(csgName, scName, "http://localhost:9081/xmlrpc/");
//			
//			String querySignature = "["+requestorIP+":"+requestorPort+"]"
//									+ "Get Coordinator Peer"
//									+ "["+System.currentTimeMillis()+"]";
//			
//	//		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"  Query Signature: "+ querySignature);			
//			// Add a lookup request so that the API can monitor the returned results.
////			((McsAPI)PSGFacade.allPSGs.get(name)).myRequests.addLookupCPRequest(querySignature);            
//			
//			prepareSC(csgName, scName);
//	        
//	        //Changes made for the purpose of improving reliability - Shubhabrata
////	        registerPSGToCP(requestorIP, Integer.toString(requestorPort), csgName, scName, psgID);
//	        networkConnector.registerPSGToCP(requestorIP, Integer.toString(requestorPort), csgName, scName, psgID);
//	        
//	        //Change made by Shubhabrata - Register PSG information to the CSG for use in tracking
////	        registerPSGToCSG(psgID,csgName, requestorIP, requestorPort );
//	        networkConnector.registerPSGToCSG(psgID,csgName, requestorIP, requestorPort );
//	        
//	        /*Change made by Shubhabrata - Register the information about each PSG and the semantic clusters
//	         * associated with it*/
////	  	    registerPSGToSC(psgID, csgName, scName);
//	        networkConnector.registerPSGToSC(psgID, csgName, scName);
//	        
//	        //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"My Name = "+myName);
//	        // Make a regular call
//	        /* Check whether the clustering algorithm needs to be invoked by checking for the numeric data type. If yes, invoke and determine the
//	         * cluster to join and pass this while registering the PSG*/
////	        Object[] params = new Object[] { new String(csgName), new String(scName),  
////	        		new String(requestorIP),  new Integer(requestorPort), new String(myName) };
//	        // return a string contains a list of IP addresses of registered PSGs, Ivan, 11 May 2012
////	        psgResponse = (String) connectionManager.getCPClient(id).execute("PSGManager.registerPSG", params);
//			psgResponse = (String) networkConnector.registerPSG(name, csgName,
//					scName, requestorIP, requestorPort, myName);
//	        extractNeighbors(name, csgName, scName, psgResponse);
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+csgName+" CSG, get reg PSG response: "+"\nResponse: " + psgResponse+"\n");
//		} catch (Exception e){
//			e.printStackTrace();
//		//	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: Cannot register PSG1");
//		}
////		return psgResponse;
//	}
//	
//
//	public void registerPSG(String parentCSG, String name, String csgName,
//			String scName, String requestorIP, int requestorPort, String myName) {
//		
//		String temp = myName.substring(myName.indexOf("=")+1);
//		String psgID = temp.substring(0, temp.indexOf("?"));
//		
////		String id = csgName+"@"+scName;
//		String psgResponse = "";
//		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Compo Locate: PARENT RegPSG: "+ csgName+"@"+scName);
//		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+parentCSG+"; "+csgName);
//		try{
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Checking CSG!");
//			
//			prepareCSG(parentCSG, csgName);
//			
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Checking SC!");
//			//createSC(csgName, scName, "http://localhost:9081/xmlrpc/");
//			
//			String querySignature = "["+requestorIP+":"+requestorPort+"]"
//									+ "Get Coordinator Peer"
//									+ "["+System.currentTimeMillis()+"]";
//			
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"  Query Signature: "+ querySignature);			
//			// Add a lookup request so that the API can monitor the returned results.
////			((McsAPI)PSGFacade.allPSGs.get(name)).myRequests.addLookupCPRequest(querySignature);            
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"GET CootdinatorPeerSys: "+csgName+"."+scName);
//			
//			prepareSC(csgName, scName);
//	        
//			
//	        //Changes made for the purpose of improving reliability - Shubhabrata
////	        registerPSGToCP(requestorIP, Integer.toString(requestorPort), csgName, scName, psgID);
//	        networkConnector.registerPSGToCP(requestorIP, Integer.toString(requestorPort), csgName, scName, psgID);
//	        
//	        //Change made by Shubhabrata - Register PSG information to the CSG for use in tracking
////	        registerPSGToCSG(psgID,csgName, requestorIP, requestorPort );
//	        networkConnector.registerPSGToCSG(psgID,csgName, requestorIP, requestorPort );
//	        
//	        /*Change made by Shubhabrata - Register the information about each PSG and the semantic clusters
//	         * associated with it*/
////	  	    registerPSGToSC(psgID, csgName, scName);
//	        networkConnector.registerPSGToSC(psgID, csgName, scName);
//	        
//	      //  System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"My Name = "+myName);
//	        // Make a regular call
////	        Object[] params = new Object[] { new String(csgName), new String(scName),  
////	        		new String(requestorIP),  new Integer(requestorPort), new String(myName) };
////	        psgResponse = (String) connectionManager.getCPClient(id).execute("PSGManager.registerPSG", params);
//			psgResponse = networkConnector.registerPSG(parentCSG, name,
//					csgName, scName, requestorIP, requestorPort, myName);
//	        extractNeighbors(name, csgName, scName, psgResponse);
//
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From " + csgName
//					+" CSG, get reg PSG response: "+"\nResponse: " + psgResponse+"\n");
//		} catch (Exception e){
//			e.printStackTrace();
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: Cannot register PSG1");
//		}
////		return psgResponse;
//	}
//		
	

	


	

	// Ivan, 13 Jul 2012: those neighbor related methods are not used
	// methods related with neighbors, Ivan, 11 May 2012
	//	 Client Side P2P neighbor Connection Initialization Service
//	private void foreignRegisterAsNeighbor(String csgName, String scName,
//			String neighborAddress, String requestor) throws Exception {
////		String idKey = csgName +"."+ scName;
//		if (!connectionManager.containsNeighbor(scName,neighborAddress)){
//			//	connectionManager.getOutgoingPSGConnection(destination);			
////			setOutgoingPSGConnection(csgName, scName, neighborAddress);	
//			connectionManager.addNeighbor(scName, neighborAddress);	
//			registerNeighbor(csgName, scName, neighborAddress);
//		}	
//
//		// connectionManager.registerNeighbor(name, destination, getAddress());
//		// Added so that the other PSG Simulator knows where to register to. 
//		//System.err.println("CSG = "+csgName + "; SC="+scName+"; Dest="+destination+"getAdd="+getAddress());
//		// Ivan, 11 May 2012: format of requestorAddress:
//		// http://"+defaultIP+ ":"+defaultPort+"/xmlrpc
////		registerNeighbor(csgName, scName, requestor, neighborAddress, requestor);
//		networkConnector.registerNeighbor(csgName, scName, requestor, neighborAddress, requestor);
//		//return "";	// ?? nothing returned at all, why is it needed? Ivan, 4 May 2012
//	}
//	
//	//	 Server Side P2P Manager neighbor Connection Initialization Service
//	public String registerNeighbor(String csgName, String scName, String requestorAddress){
//
//		//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"PSG SIM: Registerneighbor at "+ appIP+":"+appPort+"; Requestor = "+requestorAddress);
////		String idKey = csgName +"@"+ scName;
////		if (!connectionManager.containsSCNeighbors(scName)){
////			connectionManager.resetNeighbors(scName);
////			connectionManager.addNeighbor(idKey, requestorAddress);
////		} else if(!connectionManager.containsNeighbor(scName, requestorAddress)) {
////			connectionManager.addNeighbor(idKey, requestorAddress);
////		}
//		connectionManager.addNeighbor(scName, requestorAddress);
//		return "[neighbor CONNECTION ESTABLISHED]";
//	}
//	
//	private void clearAllNeighbors(String csgName, String scName){
//		//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"ClearConnections: "+appPort+" at "+csgName+"."+scName);
////		this.myNB.remove(csgName+"."+scName); // seems this one is no need, Ivan, 4 May 2012
//		this.connectionManager.resetNeighbors(scName);
//	}
//	
//	private void extractNeighbors(String name, String csgName, String scName, String getResponse) throws Exception {
//		// does not have such kind of reply, Ivan, 11 May 2012
//		if (getResponse.startsWith("[WAIT FOR SPLIT]")) return;
//
//		MsgExtension replyExt = new MsgExtension();
//		replyExt.loadAdditionalString(getResponse);
//
//		String result = replyExt.getValue("PeerList");
//
//		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"  Register PSG: Get References: " + result);
//		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Min = "+replyExt.getValue("minValue")+"; Max = "+replyExt.getValue("maxValue"));
////		minValues.put(csgName+"."+scName, new Double(replyExt.getValue("minValue")));
////		maxValues.put(csgName+"."+scName, new Double(replyExt.getValue("maxValue")));
//		// For Parsing String into multiple neighbors.
//		int posi = 0;
//		int posi2;
//		String getNeighboringAddress="";								
//		while ((posi!=-1) && (posi<result.length()-1)){				
//			posi2 = result.indexOf(";",posi+1);				
//			if (getNeighboringAddress.equals("")) {
//				getNeighboringAddress = result.substring(posi,posi2);
//			} else { 
//				getNeighboringAddress = result.substring(posi+1,posi2);
//			}	
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"- |"+getneighboringAddress+"|");
//
//			//myneighbors.add(getneighboringAddress);
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Register NB Result: "+);
//			registerNeighbor(csgName, scName, getNeighboringAddress);
//
//			// Set up connection with neighboring Peer.
//			System.err.println("MCSAPI.ForeignRegisterAsneighbor; "+"name="+name+"; NBAdd="+getNeighboringAddress);
//			foreignRegisterAsNeighbor(csgName, scName, name, getNeighboringAddress);
//			posi = posi2;
//		}
//		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"</PSG Registration Request>\n");
//		//System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"SC: "+scName+" Listening Port: "+appPort);
//
//	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// Ivan, 12 Jul 2012: remove the mobility updating methods first		
//	//Added by Shubhabrata - Get session id
//	public String getSessionId(String name, String csg, String ipPort) {
//		String id = "NULL";
//		try{
//	        // Make a regular call
//	        Object[] params = new Object[] { new String(name), new String(csg), new String(ipPort)};
//		    //String psgResponse = (String) connectionManager.getCSGClient(csgName).execute("CPManager.registerCP", params);
//	         id = (String)connectionManager.getCSGClient().execute("CPManager.getSessionId", params);
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+csgName+" CSG, get reg CP response: "+"\nResponse: " + psgResponse+"\n");		
//		} catch (Exception e){
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: Cannot register CP");
//		}
//		return id;
//	}
//	public void deleteSessionId(String name, String msid) {
//		String id = "NULL";
//		String csg = msid.substring(0, msid.indexOf("@"));
//		try{
//	        // Make a regular call
//	        Object[] params = new Object[] { new String(name), new String(csg), new String(msid)};
//	        
//		    //String psgResponse = (String) connectionManager.getCSGClient(csgName).execute("CPManager.registerCP", params);
//	         connectionManager.getCSGClient().execute("CPManager.deleteSessionId", params);
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+csgName+" CSG, get reg CP response: "+"\nResponse: " + psgResponse+"\n");		
//		} catch (Exception e){
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: Cannot register CP");
//		}
//		//return id;
//	}
//	
//	//Added by Shubhabrata - Register the callback session
//	public String registerCallback(String callerId, String calleeId) {
//		String response = "NULL";
//		
//		//here we need to extract callee CSG
////		System.out.println("Received callee and caller: "+calleeId+" "+callerId);
//		String callerMSID = callerId.substring((callerId.indexOf(":")+1), callerId.length());
//		String csg = callerMSID.substring(0, callerMSID.indexOf("@"));
////		CSG = "person";
//		/*
//		 * Get CSG name from the calleeID (MSID + ASID). Also check whether both the caller and
//		 * callee MSID are valid by checking the middleware (which will be verified inside CPManager.java)
//		 */
//		try{
//	        // Make a regular call
//	        Object[] params = new Object[] { new String(csg), new String(callerId), new String(calleeId)};
//	        
//		    //String psgResponse = (String) connectionManager.getCSGClient(csgName).execute("CPManager.registerCP", params);
//	         response = (String)connectionManager.getCSGClient().execute("CPManager.registerCallback", params);
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+csgName+" CSG, get reg CP response: "+"\nResponse: " + psgResponse+"\n");		
//		} catch (Exception e){
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: Cannot register CP");
//		}
//		return response;
//	}
//	//Ivan 23 June 2010
//	public String withdrawCallback(String callerId, String calleeId) {
//		String response = "NULL";
//		//extract callee CSG
//		String callerMSID = callerId.substring(callerId.indexOf(":")+1);
//		String CSG = callerMSID.substring(0, callerMSID.indexOf("@"));
//		try {
//			Object[] params = new Object[] { new String(callerId),new String(calleeId)};
//			
//			response = (String)connectionManager.getCSGClient().execute("CPManager.withdrawCallback", params);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return response;
//	}
//	
//	//Added by Shubhabrata
//	public String mobilityUpdate(String name, String sessionId, String ipPort) {
//		String response = "NULL";
//		String csg = sessionId.substring(0, sessionId.indexOf("@"));
//		try{
//	        // Make a regular call
//	        Object[] params = new Object[] { new String(name), new String(csg), 
//	        		new String(sessionId), new String(ipPort)};
//	        
//		    //String psgResponse = (String) connectionManager.getCSGClient(csgName).execute("CPManager.registerCP", params);
//	        long beginOne = System.currentTimeMillis(); 
//	        response = (String)connectionManager.getCSGClient().execute("CPManager.mobilityUpdate", params);
//	        long endOne = System.currentTimeMillis();
//	        long mobilityUpdatingTime = endOne - beginOne;
//	        System.out.println("[Server handling mobility updating time]: "+mobilityUpdatingTime);
////	        writeLog("mobilityServerTime",""+mobilityUpdatingTime);
//	         //mobility updating will activate the notification generation process
//	        long beginTwo = System.currentTimeMillis();
//	         connectionManager.getCSGClient().execute("CPManager.callbackNotification", params);
//	        long endTwo = System.currentTimeMillis();
//	        long callbackServerTime = endTwo - beginTwo;
//	        System.out.println("[Server handling callback service]: "+callbackServerTime);
////	        writeLog("callbackServerTime",""+callbackServerTime);
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+csgName+" CSG, get reg CP response: "+"\nResponse: " + psgResponse+"\n");		
//		} catch (Exception e){
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: Cannot register CP");
//		}
//		return response;
//	}



	
	
//    public boolean existsCP(String csgName, String scName){
//		return connectionManager.existsCP(scName);
//	}

//    // Exists CSGGen.
//    public boolean existsCSGGen(String address){
//		return connectionManager.existsCSGn();
//	}
//
//    // Exists SCGen.
//    public boolean existsSC(String scGenAddress){
//		return connectionManager.containsSC(scGenAddress);
//	}
//
//    public boolean isNeighbor(String scName, String neighborAddress){
////		return connectionManager.existsPSG(psgAddress);
//		return connectionManager.containsNeighbor(scName, neighborAddress);
//	}
    /*
    // Disseminate Query to assigned PSG in a Semantic Cluster.
    public String queryPSG(String destinationAddress, String QueryString, String listeningPortAddress){
    	
		String response="Error: QueryPSG";
		String ID = destinationAddress;
		try{
			// Make a regular call
	        Object[] params3 = new Object[]
                   { new String(QueryString)};        
            response = (String) connectionManager.getPSGClient(ID).execute("QueryP2P.queryPSG", params3);
            System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"PSG ID: "+ID);
            System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+listeningPortAddress+" PSG, get reg CP of "+ID+" response: "+"\nResponse: " + response+"\n");	
		}
		catch (Exception e){
			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: QueryPSG");
		}
		finally{			
			  return response;
		}
		  
    	//return "";
    	
    }
    */
//    // Disseminate Query to assigned PSG in a Semantic Cluster.
//	public void foreignQueryP2P(String querySignature, String queryString, String destinationReference, String scName)  {
//    	
////		String response="Error: QueryP2P 1";
////		String id = destinationAddress;
////		try{
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"\n<Components Locator Start - foreignQueryP2P>");
//			// Make a regular call
////	        Object[] params = new Object[]  {new String(querySignature),
////	        		new String(queryString), new String(destinationReference), new String(scName)};
//	        //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"  <signature> "+signature+" </signature>");
//	        //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"  <destination> ["+destinationAddress+"] </destination>");
////			if (!isNeighbor(scName, id)){
//////				setOutgoingPSGConnection(id);				
////			}	
//	        //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"   <Testing> Get Client connection: "+connectionManager.getPSGClient(ID)+" </Testing>");
//            //Ivan, 12 Jul 2012: get all neighbors
//	        List<String> neighborList = connectionManager.getAllNeighbors(scName);
//	        for(String neighbor : neighborList) {
//	        	NetworkConnector.queryP2P(querySignature, queryString, destinationReference, scName, neighbor);
//	        }
////	        connectionManager.getNeighbor(scName, id).execute("RequestsManager.queryP2P", params);
//	        //response = (String) connectionManager.getPSGClient(ID).execute("RequestsManager.testcase1", params3);
//	        //response = connectionManager.getPSGClient(ID).toString();
//	        //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+response);
//	        //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"</Components Locator End>\n");
//            //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+listeningPortAddress+" PSG, get reg CP of "+ID+" response: "+"\nResponse: " + response+"\n");	
////		} catch (Exception e){
////			e.printStackTrace();
////			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: QueryP2P 2");
////		} 
////		return response;
//    }
//    // Disseminate Query to assigned PSG in a Semantic Cluster.
//	public String foreignQueryP2P(String csgName, String scName, String name,
//			String signature, String destinationAddress, String queryString,
//			String listeningPortAddress, int ttl) throws Exception {
//    	
//		String response="Error: QueryP2P 1";
////		String id = destinationAddress;
//		try{
//		//	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"\n<Components Locator Start - foreignQueryP2P>");
//			// Make a regular call
//	        Object[] params = new Object[]  { new String("Query issuer"),new String(csgName), 
//	        		new String (scName), new String(destinationAddress), new String(signature),
//	        		new String(queryString),new String(listeningPortAddress), new Integer(ttl)};
//	       // System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"  <signature> "+signature+" </signature>");
//	       // System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"  <destination> ["+destinationAddress+"] </destination>");
//			if (!isNeighbor(scName, destinationAddress)){
////				getOutgoingPSGConnection(destinationAddress);				
//			}	
//	        //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"   <Testing> Get Client connection: "+connectionManager.getPSGClient(ID)+" </Testing>");
////            response = (String) connectionManager.getNeighbor(scName, destinationAddress).execute("RequestsManager.queryP2P", params);
//	        //response = (String) connectionManager.getPSGClient(ID).execute("RequestsManager.testcase1", params3);
//	        //response = connectionManager.getPSGClient(ID).toString();
//	       // System.out.println("[ "+PSGConfiguration.defaultName+" ] "+response);
//	       // System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"</Components Locator End>\n");
//           // System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+listeningPortAddress+" PSG, get reg CP of "+ID+" response: "+"\nResponse: " + response+"\n");	
//		} catch (Exception e){
//			e.printStackTrace();
//			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: QueryP2P 2");
//		}
//		return response;
//    }
        

	// write data to file
	private void writeLog(String fileName, String content) {
		try{
			FileWriter fstream = new FileWriter("D:\\Person\\"+fileName+".txt",true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			out.write(System.getProperty( "line.separator" ));
			out.write(System.getProperty( "line.separator" ));
			//Close the output stream
			out.close();
		} catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
    }
			
	// end of file
}
