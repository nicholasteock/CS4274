/**
 * @modifiedBy Ivan
 * @date 3 Jul 2012
 * @description
 * 1) This class is designed to handle network communication with 
 * other components like CSM, CSG and SC.
 * 2) This class also contains several tables to cache CSG and SC
 * references.
 * @actions
 * 1) Remove the XmlOutgoingConnection variable and implement the 
 * functionalities inside
 * 2) Add in variable to cache references of CSGs and SCs
 * 
 * */

package qp.kernel.locator;

import java.net.URL;
import java.util.Map;
import java.util.Hashtable;

import qp.config.QPConfiguration;

//import qp.kernel.connector.config.XmlRpcOutgoingConnection;

import kernel.network.client.UDPClient;

// This is use to discover the Addresses of CSG and PSG
public class ComponentsLocator {
	// data
	// Ivan, 3 Jul 2012
	private final String csmReference = qp.config.QPConfiguration.CSM_REFERENCE;
	private static Map<String, String> csgReferences = new Hashtable<String, String>();
	private static Map<String, String> scReferences = new Hashtable<String, String>();
	
	// For Discovering CSM and CSG
//	private XmlRpcOutgoingConnection myDiscoverer;

	// constructor
	public ComponentsLocator() {
//		System.out.println("[ "+QPConfiguration.resourceName+" ] "+"Wat's wrong? T___T");
//		myDiscoverer = new XmlRpcOutgoingConnection();
	}

	// methods

	// (1) Ask CSM for CSG address.
	// (2) Set up connection with CSG.
	private void retrieveCSGReference(String csgName) {
		csgName = csgName.toUpperCase();
        // Ask CSM for CSG Address
        Object[] params = new Object[]
            { new String(csgName)};

        // Retrieve CSG Address from CSM
        UDPClient csmClient = new UDPClient(csmReference);
        String csgReference = (String) csmClient.execute("CSGManager.readCSGReference", params);

        //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"Request : From CSM, get "+csgName+" CSG Address: " +"\nResponse: " +csgReference+"");
        //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"Updated CSG references in Cache.");

        if (csgReference.startsWith("[CSG NOT FOUND]")) {
        	System.out.println("[ "+QPConfiguration.resourceName+" ] "+"Error: CSG Not Found! (CSG not registered)");
        } else {
        	csgReferences.put(csgName, csgReference);
//        	// Establish connection with CSG.
//        	try{
//        		myDiscoverer.addCSG_Configuration(csgName, csgReference);
//        	//	System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"Application: Connection Established with "+csgName +" CSG.");
//        	}
//        	catch (Exception e){
//        	//	System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"Error: CSG FOUND. CANNOT CONNECT TO CSG.");
//        	}
        }

	}

	public String getCSGReference(String csgName) {
		csgName = csgName.toUpperCase();
		if(!csgReferences.containsKey(csgName)) {
			retrieveCSGReference(csgName);
		}
		return csgReferences.get(csgName);
	}
	
	private void retrieveSCReference(String csgName, String scName) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		// retrieve SC reference from CSG
		Object[] params = new Object[]
	            { new String(csgName), new String(scName)};
		UDPClient csgClient = new UDPClient(getCSGReference(csgName));
		String scReference = (String) csgClient.execute("CSGGenerator.readSCReference", params);
		
        if (scReference.startsWith("[SC NOT FOUND]")) {
        	System.out.println("[ "+QPConfiguration.resourceName+" ] "+"Error: CSG Not Found! (CSG not registered)");
        } else {
        	String scID = csgName + "." + scName;
        	scReferences.put(scID, scReference);
        }

	}
	
	public String getSCReference(String csgName, String scName) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String scID = csgName + "." + scName;
		if(!scReferences.containsKey(scID)) {
			retrieveSCReference(csgName, scName);
		}
		return scReferences.get(scID);
	}
	
	// Ivan, 25 SEP 2013: retrieve the whole list of PSG references for each RC in the SC instance.
	public String getPSGList(String csgName, String scName) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String scReference = getSCReference(csgName, scName);

		Object[] params = new Object[] 
				{new String(csgName), new String(scName)};
		System.out.println("Calling UDPClient from ComponentsLocator getPSGList");
		UDPClient scClient = new UDPClient(scReference);
		String psgReference = (String) scClient.execute("SCGenerator.getPSGList", params);
		return psgReference;
	}

	// Ivan, 17 Jul 2012: retrieve a list of PSG references for each RC in the SC instance.
	public String getRandomPSGList(String csgName, String scName) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String scReference = getSCReference(csgName, scName);
		
		Object[] params = new Object[] 
				{new String(csgName), new String(scName)};
		System.out.println("Calling UDPClient from ComponentsLocator getRandomPSGList");
		UDPClient scClient = new UDPClient(scReference);
		String psgReference = (String) scClient.execute("SCGenerator.getRandomPSGList", params);
		return psgReference;
	}
	
	// retrieve a random PSG reference from SC based on value
	public String getRandomPSG(String csgName, String scName, String valueType, String value) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String scReference = getSCReference(csgName, scName);
		
		Object[] params = new Object[] 
				{new String(csgName), new String(scName), new String(valueType), new String(value)};
		System.out.println("Calling UDPClient from ComponentsLocator getRandomPSG");
		UDPClient scClient = new UDPClient(scReference);
		String psgReference = (String) scClient.execute("SCGenerator.getRandomPSG", params);
		return psgReference;
	}


	// Ivan, 4 Jul 2012: ?? may not be very necessary
	// Ivan, 17 Jul 2012: not used in the new implementation
	/*
	 * Added by Shubhabrata - Get the cluster size
	 */
	public String getClusterSize(String csgName, String scName) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String sizes = "";
		try{
			// Make a regular call
			Object[] params3 = new Object[]{new String(csgName), new String(scName)};
			//System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"\n<Remote QueryMCS Call Request>");
//			sizes = (String) myDiscoverer.getCP_Client(ID).execute("PSGManager.getClusterSize", params3);
			UDPClient scClient = new UDPClient(getSCReference(csgName, scName));
			sizes = (String) scClient.execute("PSGManager.getClusterSize", params3);
			//System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"  Request : From "+csgName+" CSG, get reg CP "+ID+" response: "+"\n  Response: " + response+"\n");
			// System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"</Remote QueryMCS Call Request>\n");
//			return sizes;
		} catch (Exception e){
			System.out.println("[ "+QPConfiguration.resourceName+" ] "+"Error: QueryMCS 1");
		} 
		return sizes;
	}








    // Exists CSG_Gen.
    public boolean hasCSGReferebce(String csgName){
		csgName = csgName.toUpperCase();
		return csgReferences.containsKey(csgName);
	}

    // Exists SC_Gen.
    public boolean hasSCReference(String csgName, String scName){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
    	String scID = csgName + "." + scName;
		return scReferences.containsKey(scID);

	}

//    public boolean existsPSG(String PSG_Address){
////		return myDiscoverer.existsPSG(PSG_Address);
//    	return false;
//	}

//    // Disseminate Query to assigned PSG in a Semantic Cluster.
//    public String queryPSG(String destinationAddress, String QueryString, String listeningPortAddress){
//
//		String response="Error: QueryPSG";
//		String ID = destinationAddress;
//		try{
//			// Make a regular call
//	        Object[] params3 = new Object[]
//                   { new String(QueryString)};
//            response = (String) myDiscoverer.getPSG_Client(ID).execute("QueryP2P.queryPSG", params3);
//            System.out.println("[ "+Settings.resourceName+" ] "+"PSG ID: "+ID);
//            System.out.println("[ "+Settings.resourceName+" ] "+"Request : From "+listeningPortAddress+" PSG, get reg CP of "+ID+" response: "+"\nResponse: " + response+"\n");
//		}
//		catch (Exception e){
//			System.out.println("[ "+Settings.resourceName+" ] "+"Error: QueryPSG");
//		}
//		finally{
//			  return response;
//		}
//
//    	//return "";
//
//    }
//
//    // Disseminate Query to assigned PSG in a Semantic Cluster.
//    public String foreignQueryP2P(String csgName, String scName, String myID, String signature, String destinationAddress, String QueryString, String listeningPortAddress,String ID_KEY) throws Exception{
//
//		String response="Error: QueryP2P 1";
//		String ID = destinationAddress;
//
//
//		try{
//			//System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"\n<Components Locator Start - foreignQueryP2P>");
//			// Make a regular call
//	        Object[] params3 = new Object[]
//                   {new String("Query Issuer"), new String(csgName),new String(scName), new String(ID_KEY), new String(signature),new String(QueryString),new String(listeningPortAddress), new Integer(10)};
//
//	        //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"  <signature> "+signature+" </signature>");
//	        //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"  <destination> ["+destinationAddress+"] </destination>");
//	        //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"ClusterID="+myID);
//			if (!existsPSG(ID)){
//				get_Outgoing_PSG_Connection(ID);
//			}
//			myDiscoverer.getPSG_Client(ID);
//	        //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"   <Testing> Get Client connection: "+myDiscoverer.getPSG_Client(ID)+" </Testing>");
//
//            response = (String) myDiscoverer.getPSG_Client(ID).execute("RequestsManager.queryP2P", params3);
//	        //response = (String) myDiscoverer.getPSG_Client(ID).execute("RequestsManager.testcase1", params3);
//
//	        //response = myDiscoverer.getPSG_Client(ID).toString();
//
//	        System.out.println("[ "+Settings.resourceName+" ] "+response);
//	        //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"</Components Locator End>\n");
//            //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"Request : From "+listeningPortAddress+" PSG, get reg CP of "+ID+" response: "+"\nResponse: " + response+"\n");
//		}
//		catch (Exception e){
//			e.printStackTrace();
//			//System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"Error: QueryP2P 2");
//		}
//		finally{
//			  return response;
//		}
//
//    	//return "";
//
//    }

    
    // forward context query to a random selected PSG
    public void forwardQuery(String querySignature, String contextQuery, String processorReference, String psgName,String psgReference, String scName) {
    	// Ivan, 17 Jul 2012: psg.RequestManager.queryP2P needs the name of PSG 
    	// however, we do not return the PSG name when get random PSG references
    	// so we use the psgReference for shortcut here
    	scName = scName.toLowerCase();
//    	String psgName = psgReference;
    	Object[] params = new Object[] 
    			{new String(psgName), new String(querySignature), new String(contextQuery), 
    			new String(processorReference), new String(scName)};
    	System.out.println("Calling UDPClient from ComponentsLocator forwardQuery");
    	UDPClient psgClient = new UDPClient(psgReference);
    	//Ivan, 25 SEP 2013: change to no p2p
//    	psgClient.execute("RequestsManager.queryP2P", params);
//    	psgClient.execute("RequestsManager.queryPSG", params);
    	// Ivan, 6 Nov 2013: change UDPClient without replying
    	System.out.println("Back from UDPClient");
    	psgClient.executeNoReply("RequestsManager.queryPSG", params);
    }
    
    
    // Ivan, 17 Jul 2012: this method is not used
    // Support RDF Query Currently, Find and Contact CP
	public String queryMCS(String csgName, String scName, String QueryString, String requestorAddress){
		String response="Error: QueryMCS 2";
		String ID = csgName+"@"+scName;
		try{
			// Make a regular call
	        Object[] params3 = new Object[]
                   {new String(csgName), new String(scName),  new String(QueryString),new String(requestorAddress)};
	        //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"\n<Remote QueryMCS Call Request>");
//            response = (String) myDiscoverer.getCP_Client(ID).execute("PSGManager.queryMCS", params3);
	        String scReference = getSCReference(csgName, scName);
	        System.out.println("Calling UDPClient from ComponentsLocator queryMCS");
	        UDPClient scClient = new UDPClient(scReference);
	        response = (String) scClient.execute("PSGManager.queryMCS", params3);
            //System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"  Request : From "+csgName+" CSG, get reg CP "+ID+" response: "+"\n  Response: " + response+"\n");
	       // System.out.println("[ "+chung.queryprocessor.io.config.config.resourceName+" ] "+"</Remote QueryMCS Call Request>\n");
		} catch (Exception e){
			System.out.println("[ "+QPConfiguration.resourceName+" ] "+"Error: QueryMCS 1");
		}
		return response;
	}
	
	// Ivan, 3 Jul 2012: this methods seems should not appear in QP but just in PSG or intermediate processors
    // Support RDF Query Currently, Find and Contact CP
//	public String reportResults(String destination, String Result, String querySignature, String requestorAddress){
//
//		String response="Error: Report Results 2";
//		String ID = destination;
//		try{
//			// Make a regular call
//	        Object[] params3 = new Object[]
//                   { new String(Result),new String(querySignature)};
//	        System.out.println("[ "+Settings.resourceName+" ] "+"\n<Remote Report Results Call Request>");
//            response = (String) myDiscoverer.getPSG_Client(ID).execute("RequestsManager.registerResult", params3);
//            System.out.println("[ "+Settings.resourceName+" ] "+"  Request : From "+requestorAddress+" PSG, report results to PSG "+ID+" response: "+"\n  Response: " + response+"\n");
//	        System.out.println("[ "+Settings.resourceName+" ] "+"</Remote Report Results Call Request>\n");
//		} catch (Exception e){
//			System.out.println("[ "+Settings.resourceName+" ] "+"Error: Report Results 1");
//		}
//		return response;
//	}




}
