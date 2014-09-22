/**
 * @modifiedBy Ivan 
 * @date 1 June 2012
 * @description
 * The methods can be classified into three groups in this calss:
 * 1) port handling methods
 * 2) SC registration methods 
 * 3) PSG related methods (maybe added by Sen)
 * 4) SC create/remove/update methods
 * @functionality
 * 1) Just as CSGGenerator.java in CSG Generator project,
 * this class is also like a local manager of SC instancs.
 * */
package sc.kernel.scgenerator;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import kernel.com.reference.NetworkReference;
//import kernel.udp.invoke.RPCConfigure;
import kernel.network.client.UDPClient;

import sc.config.SCConfiguration;
//import sc.kernel.api.McsAPI;
import sc.kernel.rc.PhysicalSpace;
import sc.kernel.rc.RangeCluster;

// ----------------------------------------------- // 
// | Written by Ng Wen Long - 12th October 2007  | //
// | This class is to generate Semantic Clusters.| //
// ----------------------------------------------- //


public class SCGenerator {
	// data
	// It starts the SCGenerator instance.
	// It accepts request to create Semantic Cluster.
	// -- It need to be advertised itself to a CSG first.
	public static Map<String, SCInstance> scRegistry = new Hashtable<String, SCInstance>();
//	public Map<String, SCInstance> SC_REG_KEY_ADDRESS = new Hashtable<String, SCInstance>();
//	private int size = 0;
//	public McsAPI accessor;
	private int defaultPort = SCConfiguration.DEFAULT_PORT;
	private String defaultIP = SCConfiguration.DEFAULT_IP;
	private String csmReference = SCConfiguration.CSM_REFERENCE;
	//public final String defaultIP = "137.132.81.99";
	
	private static int portSize = SCConfiguration.MAX_NUM_SC;
	private static int[] portAllocationArray = new int[portSize];
	
	// constructor
	// Ivan, 5 Jul 2012: since this class is going to be an 
	// interface class, the constructor should be a dummy one
	public SCGenerator() throws Exception{
//		this.initPortAllocationArray();
//		accessor = new McsAPI("Accessor", defaultIP, defaultPort);
	}
	
//	public SCGenerator(int port) throws Exception{
//		this.initPortAllocationArray();
//		defaultPort = port;
////		accessor = new McsAPI("Accessor", defaultIP, defaultPort);
//	}
	
	// methods
	public void initPortAllocationArray(){
		for (int i=0; i<portSize; i++){
			portAllocationArray[i] = 0;
		}
	}
	
	private int nextFreePort(){
		int a = 0;
		while ((portAllocationArray[a]!=0)) a++;
		portAllocationArray[a] = 1;
		return defaultPort + a + 1;
	}	
	
	private void removePort(int port){
		portAllocationArray[port - defaultPort - 1] = 0;
	}
	
	
	// Ivan, 29 Jun 2012: the following two methods are incorrect,
	// need to revise

	// Ivan, 1 Jun 2012
	// Not sure the correctness of this method, need revise
	// Advertise SCGenerator to a particular CSG
	// Ivan, 17 Jul 2012: not used
	public void registerToCSG(String csgName) throws Exception{
//		SCInstance instance = new SCInstance(csgName, null, defaultIP, defaultPort);
//		instance.registerToCSG();
		// Ivan, 30 Jun 2012
		// communicate with CSM to get random CSG Generator
//		RPCConfigure csmConfig = new RPCConfigure(csmReference);
		UDPClient csmClient = new UDPClient(csmReference);
//		NetworkReference nr = new NetworkReference(defaultIP, defaultPort);
		Object[] params = new Object[] {};
		String csgGenRef = (String)csmClient.execute("CSGGenManager.getRandomCSGGen", params);
		
		// register SC Generator with CSG Generator
//		RPCConfigure csgGenConfig = new RPCConfigure(csgGenRef);
		UDPClient csgGenClient = new UDPClient(csgGenRef);
		NetworkReference nr = new NetworkReference(defaultIP, defaultPort);
		Object[] csgGenParams = new Object[] 
				{new String(nr.getReference())};
		String response = (String)csgGenClient.execute("SCGenManager.registerSCGen", csgGenParams);
	}
	
	// Ivan, 1 Jun 2012
	// Not sure the correctness of this method, need revise
	// Advertise SCGenerator to a particular CSG
	// Ivan, 17 Jul 2012: not used
	public void registerToCSM(String myIP, int myPort) throws Exception{
//		SCInstance instance = new SCInstance(null, null, defaultIP, defaultPort);
//		instance.registerToCSM();
		// Ivan, 29 Jun 2012: register to CSM
//		RPCConfigure config = new RPCConfigure(csmReference);
		UDPClient client = new UDPClient(csmReference);
		NetworkReference nr = new NetworkReference(myIP, myPort);
		Object[] params = new Object[] 
				{new String(nr.getReference())};
		String response = (String)client.execute("CSGGenManager.registerSCGen", params);
	}
	
	// Ivan, 5 Jul 2012: register with a random CSG generator
	// Specially for SC Generator registration
	public void  registerToCSGGenerator(String myIP, int myPort)  {
		// Step 1: restrieve a random CSG Generator reference from CSG
		UDPClient csmClient = new UDPClient(csmReference);
		// Ivan, 17 Jul 2012: temprary method since we cannot remotely call methods without any params
		Object[] csgParams = new Object[] {new String("dummy")};
		String csgGenRef = (String)csmClient.execute("CSGGenManager.getRandomCSGGen", csgParams);
		
		// Step 2: register to the random select CSG generator
		NetworkReference nr = new NetworkReference(myIP, myPort);
		UDPClient csgGenClient = new UDPClient(csgGenRef);
		Object[] scGenParams = new Object[] 
				{new String(nr.getReference())};
		String response = (String)csgGenClient.execute("SCGenManager.registerSCGen", scGenParams);
	}
	
	// Has PSG.
	public boolean hasPSG(String psgName, String csgName, String scName ){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
//		return ((SCInstance)scRegistry.get(myID)).myReg.hasPSG(ID); 
		return ((SCInstance)scRegistry.get(myID)).hasPSG(psgName); 
	}
	
	public RangeCluster getRCByValue(String csgName, String scName, String valueType,String value){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
//		return ((SCInstance)scRegistry.get(myID)).myReg.getPSGReg("ALL", valueType, value);
		return ((SCInstance)scRegistry.get(myID)).getRCByValue(valueType, value);
	}
	
	public List<RangeCluster> getRCList(String csgName, String scName){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
//		return ((SCInstance)scRegistry.get(myID)).myReg.getPSGReg("ALL");
		return ((SCInstance)scRegistry.get(myID)).getRCList();
	}

	public Vector<String> getRangeList(String csgName, String scName){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
//		return ((SCInstance)scRegistry.get(myID)).myReg.getRangeStrings();
		return ((SCInstance)scRegistry.get(myID)).getRangeList();
	}

	// Get list of peers from a particular Semantic Cluster.
	public String[] getListOfPSG(String valueType, String value,String csgName, String scName){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
//		return ((SCInstance)scRegistry.get(myID)).myReg.getListOfPSG(valueType, value); 
		return ((SCInstance)scRegistry.get(myID)).getListOfPSG(valueType, value); 
	}
	
	// Ivan, 25 SEP 2013: get whole list of PSG references for each RC
	public String getPSGList(String csgName, String scName) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
		return ((SCInstance)scRegistry.get(myID)).getPSGList();
	}
	
	// Ivan, 17 jul 2012: get PSG reference for exact value query type only
	// The orginal method in PSGManager.queryMCS returns random PSG references for
	// each RC, which not really narrow down the query size of PSGs. In other words,
	// the concept of RC is not well utilized.
	public String getRandomPSG(String csgName, String scName, String valueType, String value) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
		return ((SCInstance)scRegistry.get(myID)).getRandomPSG(valueType, value);
	}
	
	// Ivan, 17 Jul 2012: get list of PSG references for each RC
	public String getRandomPSGList(String csgName, String scName) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
		return ((SCInstance)scRegistry.get(myID)).getRandomPSGList();
	}
	
	
	// Ivan, 16 Jul 2012: called by PSG 
	// public static String registerPSGToSC(String psgName, String csgName,
	// String scName, String valueType, String value, String psgReference, String scReference)
	public String registerPSG(String psgName, String csgName, String scName, String valueType, String value, String psgReference) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		PhysicalSpace ps = new PhysicalSpace(psgName, psgReference, scName, valueType,value);
		String myID = scName + "@" + csgName;
//		return ((SCInstance)scRegistry.get(myID)).myReg.addPSG(PS);	
		return ((SCInstance)scRegistry.get(myID)).addPSG(ps);
	}
	
	public String addPSG(String csgName, String scName, PhysicalSpace ps){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
//		return ((SCInstance)scRegistry.get(myID)).myReg.addPSG(PS);	
		return ((SCInstance)scRegistry.get(myID)).addPSG(ps);	
	}
	
	public boolean deletePSG(String psgName, String csgName, String scName, String valueType, String value){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
//		return ((SCInstance)scRegistry.get(myID)).myReg.deletePSG(valueType, value,ID);	
		return ((SCInstance)scRegistry.get(myID)).deletePSG(valueType, value,psgName);	
	}
	
	// Ivan, 25 Feb 2014: update psg reference
	public String updatePSGReference(String csgName, String scName, String psgName, String newReference) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
		return ((SCInstance)scRegistry.get(myID)).updatePSGReference(psgName, newReference);
	}
	
	// end
	
	//Added by Shubhabrata
	public boolean deletePSG(String psgName, String csgName, String scName ){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
		SCInstance scInstance = scRegistry.get(myID);
//		return ((SCInstance)scRegistry.get(myID)).myReg.deletePSG(ID);
		// Ivan, 20 jul 2012: add the SC removement
		// Step 1: remove the psgInstance from SC
		scInstance.deletePSG(psgName);
		
		// Step 2: check the size of SC instance and remove it empty
		if(scInstance.isEmpty()) {
			// remove from CSG
			scInstance.leaveCSG();
			// free the port
			removePort(scInstance.getPort());
			// remove locally
			scRegistry.remove(myID);
		}
		return true;
	}
	

	
	/*
	 * Added by Shubhabrata - Retrieve the cluster sizes
	 */
	public String getClusterSize(String csgName, String scName)	{
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
		
//		return ((SCInstance)scRegistry.get(myID)).myReg.getClusterSize();
		return ((SCInstance)scRegistry.get(myID)).getClusterSize();
	}
	
	public String getRange(String csgName, String scName, String valueType, String value){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;

//		return ((SCInstance)scRegistry.get(myID)).myReg.getRangeString(valueType,value);	
		return ((SCInstance)scRegistry.get(myID)).getRange(valueType,value);	
		
	}
	
	
	
	public int getSCSize(String csgName, String scName){
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String myID = scName + "@" + csgName;
//		return ((SCInstance)scRegistry.get(myID)).myReg.getSize(); 
		return ((SCInstance)scRegistry.get(myID)).getSize(); 
	}
	
	
	
	
	// Create an SC Instance in SCGenerator.
	public String createSC(String csgName, String scName, String csgReference) throws Exception{
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String ID = scName + "@" + csgName;
		
		if (scRegistry.containsKey(ID)){
			
			System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"SC Exists already.");
//			return "[SC EXISTED]";
			// Ivan, 29 Jun 2012
			return scRegistry.get(ID).getReference();
		} else {					
			
			System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"SC: Creating new Semantic Cluster: "+ID);
			
			int getPort = this.nextFreePort();
//			size++;
			SCInstance instance = new SCInstance(csgName, scName, defaultIP, getPort, csgReference);	
			scRegistry.put(ID, instance);	
//			SC_REG_KEY_ADDRESS.put("http://"+defaultIP+":"+getPort+"/xmlrpc", instance);
			
			
			System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"SC: Registering new Semantic Cluster: "+ID);
			// Ivan, 1 Jun 2012
			// ?? why have to use the McsAPI in SCInstance to call method
			// ?? why not use the McsAPI instance directly in this class
			// ?? are these because the parameters are different IP and Port?
//			String result = instance.myGateway.registerCP(csgName, scName);
			// Ivan, 3 Jun 2012, change CP to SC
			// Ivan, 29 jun 2012: in the case that CSGs initialize SC creation, we do not need following 
			// statements any more, but just return the new reference to CSG
//			String result = instance.registerSC();
//			while (result.startsWith("[ERROR]")){
//				// Ivan, 1 Jun 2012
////				result = instance.myGateway.registerCP(csgName, scName);
//				// Ivan, 3 Jun 2012, change CP to SC
//				result = instance.registerSC();
//			}
//			
//			if (result.startsWith("[SC SUCESSFULLY ADDED!]")){
//				
//				String getFull = result.substring(result.indexOf("]")+1, result.length());
//				String Self = getFull.substring(getFull.indexOf("=")+1,getFull.indexOf("|"));
//				getFull = getFull.substring(getFull.indexOf("|")+1, getFull.length());
//				String Left = getFull.substring(getFull.indexOf("=")+1,getFull.indexOf("|"));
//				getFull = getFull.substring(getFull.indexOf("|")+1, getFull.length());
//				String Right="";
//				String Long="";
//				// If there are Long Contacts,
//				if (getFull.indexOf("|")>0){
//					Right = getFull.substring(getFull.indexOf("=")+1,getFull.indexOf("|"));
//					getFull = getFull.substring(getFull.indexOf("|")+1, getFull.length());
//					Long = getFull.substring(getFull.indexOf("=")+1,getFull.length());
//					updateSC(csgName, scName, Left, Right, Long);
//				}
//				else{
//					Right = getFull.substring(getFull.indexOf("=")+1,getFull.length());
//					updateSC(csgName, scName, Left, Right, "null");
//					
//				}
//				System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+" Left Contact = "+ Left);
//				System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+"Right Contact = "+ Right);
//				System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+" Long Contact = "+ Long);
//				
//			}
//			NetworkReference nr = new NetworkReference(defaultIP, getPort);
//			return  defaultIP +":"+getPort;
			return  (new NetworkReference(defaultIP, getPort)).getReference();
			
		}
		
	}
	
	// Remove an SC Instance in SCGenerator
	public void removeSC(String csgName, String scName) throws Exception{
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		String ID = scName + "@" + ""+csgName;
		System.out.println("MYID="+ID);
		System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"Remove ID SC = "+ ID);
		for (int i=0; i<scRegistry.size();i++)
			System.out.println("SC REG [ "+i+" ]"+scRegistry.keySet().toArray()[i]);
		if (scRegistry.containsKey(ID)){
			// Ivan, 1 Jun 2012
//			((SCInstance)scRegistry.get(ID)).myGateway.leaveCP(csgName, scName);
			// Ivan, 3 Jun 2012, change CP to SC
			// Ivan, 10 Jul 2012: comment this leaveSC() calling
//			((SCInstance)scRegistry.get(ID)).leaveSC();
			String address = ((SCInstance)scRegistry.get(ID)).toAddress();
			// To remove port
			this.removePort(((SCInstance)scRegistry.get(ID)).getPort());
			scRegistry.remove(ID);			
//			SC_REG_KEY_ADDRESS.remove(address);

			System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"[Removal Done] - "+ID);
			
		}
		else
			System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"[Removal Failed]");
		
		
	}

	// Ivan, 11 Jul 2012: inconsistent with the structure that SC does 
	// not have left or right neighbors
//	// Update an SC Instance in SCGenerator
//	public void updateSC(String csgName, String scName, String left, String right, String Long) throws Exception{
//		
//		String ID = scName + "@" + csgName;
//		
//		if (scRegistry.containsKey(ID)){
//			
//			((SCInstance)scRegistry.get(ID)).setContacts(left, right, Long);
//			//scRegistry.remove(ID);
//			
//			System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+"[Update Done] - "+ID);
//			
//		}
//		else
//			System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+"[Update Failed]");
//		
//		
//	}
	
	
	public String toDetails(){
		
		String result="\n";
		
		for (int i=0; i< scRegistry.size(); i++){
			result += ((SCInstance)scRegistry.values().toArray()[i]).toDetails(i+1);
		}
		
		return result;
	}
	
	
	
}
