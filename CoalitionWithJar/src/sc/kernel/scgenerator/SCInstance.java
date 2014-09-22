/**
 * Ivan, 1 Jun 2012
 * This class is redefined to handle all stuff related to SC,
 * such registration with CSM, CSG, other SCs, CPs.
 * and manager all PSGs registered under this SC.
 * Two main groups of methods:
 * 1) Communicate with components of same or higher level than SC
 * 2) Communicate with PSGs registered in this SC
 * 3) Accessors and Mutators
 * 
 * */
package sc.kernel.scgenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import experiment.data.ExportData;
import kernel.network.client.UDPClient;
import sc.config.SCConfiguration;
//import sc.kernel.api.McsAPI;
import sc.kernel.rc.PhysicalSpace;
//import sc.kernel.rc.RCManager;
import sc.kernel.rc.RangeCluster;
import sc.kernel.server.PublicServer;
//----------------------------------------------- // 
//| Written by Ng Wen Long - 12th October 2007  | //
//| This class is to generate SC Instances.     | //
//----------------------------------------------- //
public class SCInstance {
	// data
	// public PSGRegistry myReg; - Replaced by SCNetworkPlanner
	private String myID;
	private String myCSGName;
	private String mySCName;
	private String myIP;
	private int myPort;
	private String myCSGReference; // mainly for SC deletion
	
	private PublicServer mySCServer;
	
//	private RCManager myReg;
//	private McsAPI myGateway;	
	//	public Vector scConnections;
//	private SCConnection myConn=null;
//	private Map<String, String> queryTracker = new Hashtable<String, String>();
	
	// Ivan, 17 Jul 2012: 
	private List <RangeCluster> rangeClusters = new Vector <RangeCluster>();
	private int numberOfSplits=0;
	private int numberOfMerges=0;
	private int maxClusterSize = sc.config.SCRanges.MAX_PEERS_PER_SC;
	private int minClusterSize = sc.config.SCRanges.MIN_PEERS_PER_SC;
	
	private double maxValue = sc.config.SCRanges.MAX_VALUE;
	private double minValue = sc.config.SCRanges.MIN_VALUE;

	// constructor
	public SCInstance(String csgName, String scName, String defaultIP, int defaultPort, String csgReference) throws Exception{
		this.myID = csgName + "@" + scName;
		this.myCSGName = csgName;
		this.mySCName = scName;
		this.myPort = defaultPort;
		this.myIP = defaultIP;
		this.myCSGReference = csgReference;
		// - myReg = new PSGRegistry(); - Replaced by SCNetworkPlanner
//		this.myReg = new RCManager(csgName, scName,"SC", myID);
		//		scConnections = new Vector();
//		this.myGateway = new McsAPI(myID, myIP, myPort);
		
		// Ivan, 10 Jul 2012: create a server of this SC
		this.mySCServer = new PublicServer(this.mySCName,this.myPort);
		mySCServer.start();
	}

	// methods
	// Ivan, 20 Jul 2012: remove this sc from csg
	public void leaveCSG() {
		this.mySCServer.close();
		Object[] params = new Object[] {new String(myCSGName), new String(mySCName)};
		UDPClient csgClient = new UDPClient(myCSGReference);
		csgClient.execute("CSGGenerator.deleteSC", params);
	}
	// Ivan, 9 Jul 2012: begin of comment
	// SC instance does not necessarily have to communicate
	// with all others but just receiving requests from PSGs
	// and Query Processors which will handle by SCGenerator
//	// Ivan, 1 Jun 2012
//	// begin of adding
//
//	// methods communicate with CSG and CSM
//	// Advertise SCGenerator to a particular CSG
//	public void registerToCSG() throws Exception{
//		myGateway.registerSCGen(myCSGName);			// needs revise
//	}
//
//	// Advertise SCGenerator to a particular CSG
//	public void registerToCSM() throws Exception{
//		myGateway.registerSCGenToCSM();				// needs revise
//	}
//	
//	// methods of registering/leave CP
//	// Ivan, 3 Jun 2012, change CP to SC
//	public String registerSC() throws Exception{
//		return this.myGateway.registerSC(myCSGName, mySCName);
//	}
//	// Ivan, 3 Jun 2012, change CP to SC
//	public void leaveSC() throws Exception{
//		this.myGateway.leaveSC(myCSGName, mySCName);
//	}
//	
//	// routing queries
//	public void routeToLeft(String csgName, String scName,
//			String address, String requestorIP, int requestorPort,
//			String querySignature) throws Exception {
//		this.myGateway.routeToLeftContact(csgName, scName, address, requestorIP, requestorPort, querySignature);
//	}
//
//	public void routeToRight(String csgName, String scName,
//			String address, String requestorIP, int requestorPort,
//			String querySignature) throws Exception {
//		this.myGateway.routeToRightContact(csgName, scName, address, requestorIP, requestorPort, querySignature);
//	}
	
	// methods to manage PSG connections based on SCNetworkPlanner
	// Ivan, 17 Jul 2012: remove those methods which rely on RCManager.
	// Has PSG.
//	public boolean hasPSG(String psgID){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.hasPSG(psgID); 
//	}
//
//	public RangeCluster getRCByValue(String ValueType,String Value){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.getRCByValue(ValueType, Value);
//	}
//
//	public List<RangeCluster> getRCList(){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.getRCList();
//
//	}
//
//	public Vector<String> getRange(){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.getRangeList();
//	}
//
//	// Get list of peers from a particular Semantic Cluster.
//	public String[] getListOfPSG(String ValueType, String Value){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.getListOfPSG(ValueType, Value); 
//	}
//
//	public boolean addPSG(PhysicalSpace PS){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.addPSG(PS);	
//	}
//
//	public boolean deletePSG(String ValueType, String Value, String ID){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.deletePSG(ValueType, Value,ID);	
//	}
//
//	//Added by Shubhabrata
//	public boolean deletePSG(String ID){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.deletePSG(ID);	
//	}
//
//	/*
//	 * Added by Shubhabrata - Retrieve the cluster sizes
//	 */
//	public String getClusterSize() {
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.getClusterSize();
//	}
//
//	public String getRangeString(String ValueType, String value){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.getRangeString(ValueType,value);	
//	}
//
//	public int getSCSize(){
//		//			String myID = scName + "@" + csgName;
//		return this.myReg.getSize(); 
//	}
//
//	
//	
//	// accessor
//	public RCManager getSCNetworkPlanner() {
//		return this.myReg;
//	}
	
//	public McsAPI getMcsAPI() {
//		return this.myGateway;
//	}
	
	public String getID() {
		return this.myID;
	}

	public String getCSGName() {
		return this.myCSGName;
	}
	
	public String getSCName() {
		return this.mySCName;
	}
	
//	public Map<String, String> getQueryTracker() {
//		return this.queryTracker;
//	}
	
	public String getIP() {
		return this.myIP;
	}
	public int getPort() {
		return this.myPort;
	}
	
	public String getReference() {
		return "http://" + myIP + ":" + myPort + "/xmlrpc/";
	}

	public String getCSGReference() {
		return this.myCSGReference;
	}
	
	// end of adding


	// Ivan, 31 May 2012
	// The following three methods should belong to SCInstanceConnection
	// So commented 
//	public String getLeftContact(){
//
//		return myConn.getLeftSC();
//
//	}
//
//	public String getRightContact(){
//
//		return myConn.getRightSC();
//	}
//
//	public String getLongContact(){
//
//		return myConn.getLongSC();
//	}
//
//	public void setContacts(String left, String right, String Long){
//		// Ivan, 1 Jun 2012, ?? why us CSG Name here?
//		myConn= new SCConnection(myCSGName, left, right, Long);
//		System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+"Updating SC"+myCSGName+"."+mySCName+" Connection \n"+ myConn.toString());
//	}

	// Ivan, 31 May 2012
	// The index i is not reasonable or well programmed here
	public String toDetails(int i){

		String scName = myID.substring(myID.indexOf("@")+1, myID.length());		
		String csName = myID.substring(0, myID.indexOf("@"));

		String result=i+". Semantic Cluster: "+csName+"."+scName+" \n   Coordinator Address: "+""+myIP+":"+myPort+""+"\n";

		//result += "\n< SemanticClusterHead Id='"+myID+"' Address='"+myIP+":"+myPort+"' >\n";	

//		result += myConn+"\n";
		result += toString()+"\n\n";
		//result += "</ SemanticClusterHead Id='"+myID+"' >\n\n\n";

		return result;

	}

	public String toAddress(){
		return "http://"+myIP+":"+myPort+"/xmlrpc";
	}

//	public String getRangeString(String ValueType,String value){
//		return myReg.getRangeString(ValueType, value);
//	}


//	public int toPort(){
//		return myPort;	
//	}
	
	// Ivan, 17 Jul 2012: methods from RC Manager
	
	// methods
		// Ivan, 3 Jun 2012
		// this method returns the range pairs of each cluster
		public Vector<String> getRangeList(){
			Vector<String> rString = new Vector<String>();
			for (int i=0; i<rangeClusters.size(); i++){
				// Ivan, 31 May 2012
				// what does this minValueInCluster and maxValuesInCluster used for?
//				rString.add("Range"+i+"="+minValueInCluster.get(i)+"|"+maxValueInCluster.get(i)+"?");			
				rString.add("Range"+i+"="+rangeClusters.get(i).getRange());			
			}
			return rString; 
			
		}
		
		// Ivan, 3 Jun 2012
		// this method returns size of each sub-cluster of a cluster
		/*
		 * Added by Shubhabrata 
		 */
		public String getClusterSize(){
			String RString = new String();
			for (int i=0; i<rangeClusters.size(); i++){
				// Ivan, 10 Jul 2012: ?? should add in RC + i to 
				// identify each RC
				RString +=  rangeClusters.get(i).size() + "|";			
			}
			return RString; 
		}
		
		// Ivan, 3 Jun 2012
		// this methods returns the range for a specific value
		public String getRange(String valueType, String valueStr){
			double value = hashValue(valueType,valueStr);
			// Ivan, 10 Jul 2012
			int index = locateRCByValue(value);
			return rangeClusters.get(index).getRange();
//			return "minValue="+minValueInCluster.get(locateRangeCluster(value))+"?maxValue="+maxValueInCluster.get(locateRangeCluster(value));
		}	
		
		// Ivan, 3 Jun 2012
		// check existence of a specific PSG
		public boolean hasPSG(String psgName){
			for (int i=0; i<rangeClusters.size(); i++){
				if (rangeClusters.get(i).hasPSG(psgName)){
					return true;
				}
			}
			return false;
		}
		
		// Ivan, 24 Feb 2014: update psgReference
		public String updatePSGReference(String psgName, String newReference) {
			for (int i=0; i<rangeClusters.size(); i++){
				if (rangeClusters.get(i).hasPSG(psgName)){
					rangeClusters.get(i).getPSG(psgName).setReference(newReference);
				}
			}
			return "Dummy";
		}
		
		// end
		
		// Ivan, 3 Jun 2012
		// if their are more than one sub-cluster
		// return the number of subclusters
		// else return the number of PSGs
		public int getSize(){
			if (rangeClusters.size()>1) {
				return rangeClusters.size();
			} else {
				return rangeClusters.get(0).size();	// Ivan, 31 May 2012, ?? feel strange?
			}
		}
		
		// Ivan, 20 Jul 2012: check the emptiness of SC instance
		public boolean isEmpty() {
			return getSize() == 0;
		}
		
		// - Hash function for integrating all valueType 
		public double hashValue(String valueType, String value){
			if (valueType.equals("Integer")){
				return new Integer(value).intValue();
			} else if (valueType.equals("Double")){
				return new Double(value).doubleValue();
			} else if (valueType.equals("String")){
				return value.hashCode();
			} else {
				return -999;
			}
		}
		
		// Ivan, 3 Jun 2012
		// the whole cluster is divided into three cases:
		// 1) cluster is empty
		// 2) cluster contains only one sub-cluster
		// 3) cluster contains more than one sub-clusters
		// Ivan, 10 Jul 2012: re-written
		private int locateRCByValue(double value){
			for(int i=0; i<rangeClusters.size(); i++) {
				if(rangeClusters.get(i).inRange(value)) {
					return i;
				}
			}
			return -1;
		}
		
		// Ivan, 10 Jun 2012
		private int locateRCByName(String psgName) {
			for(int i=0; i<rangeClusters.size(); i++) {
				if(rangeClusters.get(i).hasPSG(psgName)) {
					return i;
				}
			}
			return -1;
		}
		
		// Ivan, 3 Jun 2012
		// return all the PSGRegistry, namely all the registered PSGs
		public List<RangeCluster> getRCList(){		
			return rangeClusters;
		}
		
		// Ivan, 3 Jun 2012
		// return a specific sub-cluster (PSGRegistry) for a specific value
		// Ivan, 10 Jul 2012: the return result is not a list, but a 
		// single RangeCluster ??
		public RangeCluster getRCByValue(String valueType, String value){
//			Vector<RangeCluster> qualifiedRangeClusters = new Vector<RangeCluster>();
//			qualifiedRangeClusters.add(this.rangeClusters.get(locateRCByValue(hashValue(valueType, value))));
			return this.rangeClusters.get(locateRCByValue(hashValue(valueType, value)));
//			return qualifiedRangeClusters;
		}
		
		// Get list of peers from a particular sub Semantic Cluster.
		public String[] getListOfPSG(String valueType, String value){				
			if (rangeClusters.size()== 0)
//				return new String[10];			
				return null;			
			else{
			//	PSG_Registry reg = rangeClusters.get(locateRangeCluster(HashValue(valueType,value)));
				return rangeClusters.get(locateRCByValue(hashValue(valueType,value))).getListOfPSG();
			}
		}
		
		// Ivan, 25 SEP 2013: get the entire list of PSG references
		public String getPSGList() {
			String result="";
//			List <RangeCluster> myVector = SCGeneratorStarter.mySCGenerator.getRCList(csgName, scName);
//			Vector <String> ClusterRanges = SCGeneratorStarter.mySCGenerator.getRangeList(csgName, scName);
			for(int i=0; i<rangeClusters.size(); i++){
				RangeCluster rc = rangeClusters.get(i);
//				if (!(rc.size()<1)){
				// Ivan, 13 Dec 2013: should only one copy of the range information is represented
//				result+= "RC"+i+"=";
				for(PhysicalSpace ps : rc.getPSGList()) {
					String name = ps.getName();
					String reference = ps.getReference();
//					System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+rc.size()+": Result = "+reference);
//					"Range"+i+"="+rangeClusters.get(i).getRange()
//					result+= "RC"+i+"="+reference+"@"+"Range"+i+"="+rc.getRange(); //ClusterRanges.get(i);
					result+= name + "_" + reference + "@";
					
				}
				
				result = result.substring(0, result.length()-1);
//				result+= "_"+"Range"+i+"="+rc.getRange();
//				ExportData.writeLog("sc_instance_psg_list", result.length() + " " + result);
			}
			return result;
		}
		
		// Ivan, 12 Jul 2012: get a random PSG reference based on value
		public String getRandomPSG(String valueType, String value) {
			int index = locateRCByValue(hashValue(valueType, value));
			return rangeClusters.get(index).getRandomPeerReference();
		}
		
		// Ivan, 17 Jul 2012: get a list of PSG references that randomly
		// selected from each RC
		public String getRandomPSGList() {
			String result="";
//			List <RangeCluster> myVector = SCGeneratorStarter.mySCGenerator.getRCList(csgName, scName);
//			Vector <String> ClusterRanges = SCGeneratorStarter.mySCGenerator.getRangeList(csgName, scName);
			for(int i=0; i<rangeClusters.size(); i++){
				RangeCluster rc = rangeClusters.get(i);
				if (!(rc.size()<1)){			
					String reference = rc.getRandomPeerReference();
					System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+rc.size()+": Result = "+reference);
//					"Range"+i+"="+rangeClusters.get(i).getRange()
					result+= "RC"+i+"="+reference+"@"+"Range"+i+"="+rc.getRange(); //ClusterRanges.get(i);
				}
			}
			return result;
		}
		
		// Ivan, 31 May 2012
		// through this adding operation, we can guess that
		// each semantic cluster contains several small semantic
		// clusters to implement the ranged based indexing
		// and that why a list<PSGRegistry> is used rather than 
		// a single PSGRegistry
		public String addPSG(PhysicalSpace ps){
			String neighborList = null;
			if (this.rangeClusters.size()==0){
				rangeClusters.add(new RangeCluster(minValue, maxValue));
				// Ivan, 10 Jul 2012: these two variable are not needed
				// any more because range parameters have become native
				// parameters of Range Cluster
//				maxValueInCluster.add(maxValue);
//				minValueInCluster.add(minValue);
			}
			
			if (this.rangeClusters.size()==1){
				System.err.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"R.size = "+rangeClusters.get(0).size());
				//writeFile("Testing Beta 1", "Joining of PSG["+PS.myName+"] to "+csgName+"."+scName+" RC["+0+"] -> "+ minValueInCluster.get(0)+ " to "+ maxValueInCluster.get(0));
				neighborList = rangeClusters.get(0).addPSG(ps);
				
				// Ivan, 31 May 2012
				// Guess the aim: 
				// if the cluster size excesses certain number, the cluster will be splitted.
				if (rangeClusters.get(0).size() > this.maxClusterSize){	
					System.err.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"REACHEDREACHEDREACHEDREACHEDREACHEDREACHEDREACHEDREACHEDREACHEDREACHEDREACHED");
//					SplitCluster(0);
					splitCluster(0);
					//writeFile("Testing Beta 1", toClustersDetails());
//					return false;
				} // end of if
//				return neighborList;
			} else if (this.rangeClusters.size()>1){
//				int index = this.locateRangeCluster(hashValue(PS.getValueType(),PS.getValue()));
				int index = this.locateRCByValue(ps.getHashValue());
				
				//writeFile("Testing Beta 1", "Joining of PSG["+PS.myName+"] to "+csgName+"."+scName+" RC["+index+"] -> "+ minValueInCluster.get(index)+ " to "+ maxValueInCluster.get(index));
				//writeFile("Testing Beta 1", "Joining of PSG["+PS.myName+"] to Cluster ["+index+"] -> "+ minValueInCluster.get(index)+ " to "+ maxValueInCluster.get(index));
				System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"---------------> " +ps.getValue()+"; INDEX = "+ index);
				neighborList = rangeClusters.get(index).addPSG(ps);
				
				if (rangeClusters.get(index).size() > this.maxClusterSize){
//					SplitCluster(index);
					splitCluster(index);
					//writeFile("Testing Beta 1", toClustersDetails());
//					return false;
				} // end if
			} // end if
			
			//writeFile("Testing Beta 1", toClustersDetails());		
			return neighborList;
		}
		
		// Ivan, 10 Jul 2012 ?? : this deletion method is not correct
		// since it does not have the case for rangeClusters size > 1
		// and the most important part is it needs a method to 
		// locate a RC based on PSG name
		// so re-write it
		//Added by Shubhabrata
		public boolean deletePSG(String psgName) {
			int index = locateRCByName(psgName);
			if(index >= 0) {
				rangeClusters.get(index).deletePSG(psgName);
				// Ivan, 10 Jul 2012: re-write those statements to find the proper RC index for merging
				if (index > 0 && rangeClusters.get(index).size() < this.minClusterSize){
					System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"INDEX = "+ index+"; rangeClusters size = "+rangeClusters.size());
					// identify the two RCs to be merged
					int first, second;
					if (index==rangeClusters.size()-1){
//						mergeCluster(index-1, index);
						first = index -1;
						second = index;
					} else if(index == 0) {
						first = index;
						second = index + 1;
					} else {
						if (rangeClusters.get(index+1).size() <= rangeClusters.get(index-1).size()) {
//							mergeCluster(index, index+1);
							first = index;
							second = index + 1;
						} else {
//							mergeCluster(index-1, index);
							first = index -1;
							second = index;
						}
					} // end of if..else if
					
					mergeCluster(first, second);
				} // end of if
				return true;
			} else {
				return false;
			}
		}
		
		public boolean deletePSG(String valueType, String value, String psgName){
			if (rangeClusters.size()<=0){
				// - Do nothing.
				return false;
			} else if (rangeClusters.size()==1){
//				writeFile("Testing Beta 1", "Leaving of PSG["+psgName+"] from "+csgName+"."+scName+" RC["+0+"] -> "+ minValueInCluster.get(0)+ " to "+ maxValueInCluster.get(0));			
				rangeClusters.get(0).deletePSG(psgName);
				return true;
			} else {
				// Ivan, 10 Jul 2012: using locatRCByValue to delete PSGs may not be
				// very good since the values may different from the registration moment
				// so locateRCByName may be a better choice ??
				int index = this.locateRCByValue(hashValue(valueType,value));
//				System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+"DELETE PSG "+value +" Index: "+index +"; Range = "+minValueInCluster.get(index)+" - "+maxValueInCluster.get(index) + "; HashValue="+hashValue(valueType,value));
//				writeFile("Testing Beta 1", "Leaving of PSG["+ID+"] from "+csgName+"."+scName+" RC["+index+"] -> "+ minValueInCluster.get(index)+ " to "+ maxValueInCluster.get(index));
				rangeClusters.get(index).deletePSG(psgName);
				
				// Ivan, 10 Jul 2012: re-write those statements to find the proper RC index for merging
				if (rangeClusters.get(index).size() < this.minClusterSize){
					System.out.println("[ "+SCConfiguration.RESOURCE_NAME+" ] "+"INDEX = "+ index+"; rangeClusters size = "+rangeClusters.size());
					// identify the two RCs to be merged
					int first, second;
					if (index==rangeClusters.size()-1){
//						mergeCluster(index-1, index);
						first = index -1;
						second = index;
					} else if(index == 0) {
						first = index;
						second = index + 1;
					} else {
						if (rangeClusters.get(index+1).size() <= rangeClusters.get(index-1).size()) {
//							mergeCluster(index, index+1);
							first = index;
							second = index + 1;
						} else {
//							mergeCluster(index-1, index);
							first = index -1;
							second = index;
						}
					} // end of if..else if
					
					mergeCluster(first, second);
				} // end of if
			
				return true;
			}
			//writeFile("Testing Beta 1", toClustersDetails());		
		}	
		
		// Ivan, 10 Jul 2012: mergeCluster()
		private void mergeCluster(int first, int second) {
			// Step 1: remove the second RC
			RangeCluster secondRC = rangeClusters.remove(second);
			
			// Step 2: merge with the first
			rangeClusters.get(first).mergeCluster(secondRC);
		}
		
		// Ivan, 10 Jul 2012: split a range cluster
		private void splitCluster(int index) {
			// Step 1: retrieve the RC going to be splitted
			RangeCluster splittedRC = rangeClusters.get(index);
			
			// Step 2: split the RC
			RangeCluster newRC = splittedRC.getSplittedCluster();
			
			// Step 3: put the new RCs into rangeClusters
//			rangeClusters.add(index, newRC.get(0));
//			rangeClusters.addAll(index, newRC);
			rangeClusters.add(index+1, newRC);
		}


		public String toString(){
			String result="";
			
			int noPSGs = 0;
			for (int i=0;i<rangeClusters.size();i++) {
				noPSGs+=rangeClusters.get(i).size();
			}
			
			result+= "     Number of PSGs   = "+noPSGs+"\n";
			result+= "     Number of Ranges = "+rangeClusters.size()+"\n";
			result+= "     Number of Splits = "+numberOfSplits+"\n";
			result+= "     Number of Merges = "+numberOfMerges+"\n\n";
			
			for (int i=0;i<rangeClusters.size();i++){
				result += "     RANGE = [" + rangeClusters.get(i).getLowerBound()
						+ " to " + rangeClusters.get(i).getUpperBound() + "] has "
						+ rangeClusters.get(i).size() + " PSGs.\n";
			}
			return result;
		}
		
		public String toClustersString(){
			String result = "";
			for (int i=0;i<rangeClusters.size();i++){
				result += "     RANGE = [" + rangeClusters.get(i).getLowerBound()
						+ " to " + rangeClusters.get(i).getUpperBound() + "]\n"
						+ "\n";
			}
		
			return result;
		}
		
		public String toClustersDetails(){
			String result = "["+ " Range Cluster Details ]\n\n";
			for (int i=0;i<rangeClusters.size();i++){
				result += "                           [" + myCSGName.toUpperCase()
						+ "." + mySCName.toUpperCase() + "] - " + (i + 1) + ". "
						+ "Range[" + rangeClusters.get(i).getLowerBound() + " to "
						+ rangeClusters.get(i).getUpperBound() + "] - ("
						+ rangeClusters.get(i).size() + " PSGs)\n" + "\n";
				for (int j=0; j<rangeClusters.get(i).size();j++){
					result += "                           -> " + "(" + (j + 1)
							+ ") " + rangeClusters.get(i).getSortedList().get(j)
							+ "\n";
				}
				
			}
			return result;
		}
		


		public void writeFile(String dirName, String content){

			try{

				boolean status = new File("c:/Startup/logs/"+dirName+"/"+myCSGName+"-"+mySCName).mkdirs();  // should succeed			    	        
				// Create file 
				System.out.println("[ " + SCConfiguration.RESOURCE_NAME + " ] "
						+ "Creating..  c:\\Startup\\logs\\" + dirName + "\\"
						+ myCSGName + "-" + mySCName + "\\log_"
						+ (System.currentTimeMillis() / 100000) + ".file");
				FileWriter fstream = new FileWriter("c:\\Startup\\logs\\" + dirName
						+ "\\" + myCSGName + "-" + mySCName + "\\log_"
						+ (System.currentTimeMillis() / 100000) + ".file", true);
				BufferedWriter out = new BufferedWriter(fstream);

				out.write(new Date(System.currentTimeMillis())
				.toLocaleString()
				.replace(" PM",
						":" + (System.currentTimeMillis() % 1000) + " PM")
						.replace(", 20", "")
						+ " -> " + content + "\n\n");
				//Close the output stream
				out.close();
			}
			catch(Exception e){
				e.printStackTrace();

			}
		}
	
	
	
	// end 
	
}
