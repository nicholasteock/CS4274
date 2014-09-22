/**
 * @modifiedBy Ivan
 * @date 12 Jul 2012
 * @action
 * 1) change all the RPCConfig type variables to reference string
 * 
 * 
 * @description
 * This file is record all the network connection information of a registered PSG.
 * Each set network information contains:
 * One CSM address
 * One CSG address
 * Multiple SC addresses
 * Multiple CP addresses
 * (SCs are supposed to be same with CP, so we use SC only)
 * Multiple neighbor addresses for each SC
 * 
 * All above addresses are in the format:
 * "http://defaultIP:defaultPort/xmlrpc"
 * 
 * @remark
 * 1) each PSG only have one Connection data base, so
 * all the variables are designed as static, but for 
 * testing purpose, we may need to simulate many PSG
 * instances which will generate many ConnectionDB,
 * so we cannot use static variables any more.
 * */
package proxy.kernel.connection;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import proxy.config.MPSGConfiguration;

public class ConnectionDB {
	// data
	private static String csmReference = MPSGConfiguration.csmAddress;
    private static String qpReference = MPSGConfiguration.queryProcessorAddress;
  //format of all addresses: "http://"+defaultIP+ ":"+defaultPort+"/xmlrpc";
	// XMLRPC Client Configurations
    // ?? (do not know why use static here) Ivan, 2 may 2012
    // should use static, otherwise, all PSG instances will share these variables
    // which will make the matter too complicated
    // each PSG instance should have their own network communication information
    // delete all the static properties
    //private RPCConfigure csmConfiguration;
    private String csgReference;	// each registration contains one CSG only
    private String psgReference; // self reference of the PSG instance
//    private Hashtable<String, XmlRpcClientConfigImpl> cpConfigurations = new Hashtable<String, XmlRpcClientConfigImpl>();
    private Hashtable<String, String> scReferences;// = new Hashtable<String, String>();
//    private Hashtable<String, XmlRpcClientConfigImpl> csgGenConfigurations = new Hashtable<String, XmlRpcClientConfigImpl>();
    // this psgConfigurerations are used to record all psg neighbors, so we use psgneighbors instead
//    private Hashtable<String, XmlRpcClientConfigImpl> psgConfigurations = new Hashtable<String, XmlRpcClientConfigImpl>();
    // key of psgneighbors are "csgName.scName"
    private Map<String, Hashtable<String,String>> psgNeighbors;// = new Hashtable<String, List<String>>();

	// constructor
	public ConnectionDB() {
		// Setup HTTP Connection
//		this.csmConfiguration = new RPCConfigure(csmReference);
		this.psgReference = null;
		this.csgReference = null;
		this.scReferences = new Hashtable<String, String>();
//		Map<String, String> temp = new Hashtable<String, String>();
		this.psgNeighbors = new Hashtable<String, Hashtable<String, String>>();
	}
	
	// methods
	// accessors
	public String getCSMRef(){		
        return this.csmReference;
	}
	
	public String getPSGRef(){
		return this.psgReference;
	}
	
	// Get CSG Client
	public String getCSGRef(){					
		return this.csgReference;     
	}
	
	public String getQPRef() {
		return this.qpReference;
	}

	// Get SC_Gen_Client 
	public String getSCRef(String scName){		
		if (scReferences.containsKey(scName)){			
	        return scReferences.get(scName);       
		} else {
			return null;
		}
	}

//	// Get CP Client
//	public UDPClient getCPClient(String cpName){		
//		if (cpConfigurations.containsKey(cpName)){			
//	        return new UDPClient((XmlRpcClientConfigImpl)cpConfigurations.get(cpName));
//		} else {
//			return null;
//		}
//	}
	
	public List<String> getAllNeighbors(String scName) {
		if (psgNeighbors.containsKey(scName)){	
			Collection<String> values= psgNeighbors.get(scName).values(); 
			List<String> neighborList = new Vector<String>();
			for(String neighbor : values) {
				neighborList.add(neighbor);
			}
	        return neighborList;       
		} else {
			return null;
		}
	}
	
//	public String getNeighbor(String scName, String neighborAddress) {
//		if(containsSCNeighbors(scName)) {
//			return new UDPClient(new RPCConfigure(neighborAddress));
//		} else {
//			return null;
//		}
//		
//	}
	
	// accessor in XmlRpcClientConfigImpl for
	
//	public RPCConfigure getCSMConfiguration(){
//		return csmConfiguration;
//	}
//	
//	public RPCConfigure getCSGConfiguration(){
//		return this.csgReference;	
//	}
//	
//	public RPCConfigure getSCConfiguration(String scName){
//		return (RPCConfigure) scReferences.get(scName);
//	}
	
//	public XmlRpcClientConfigImpl getCPConfiguration(String scName){
//		return (XmlRpcClientConfigImpl) cpConfigurations.get(scName);
//	}
	
//	public List<RPCConfigure> getNeighborConfiguration(String scName){
//		return psgNeighbors.get(scName);
//	}
//	
	// mutators
//	public void setCSMRef(String csmAddress) {
//		this.csmConfiguration = new RPCConfigure(csmAddress);
//	}
	
	public void setPSGRef(String psgReference) {
		this.psgReference = psgReference;
	}
	
	public void setCSGRef(String csgAddress) {   
		this.csgReference = csgAddress;
	}
	
	public void setSCRef(String scName, String scAddress) {
		this.scReferences.put(scName, scAddress);
	}
	
//	public void setCPConfiguration(String scName, String scAddress) {
//		this.cpConfigurations.put(scName, new XmlRpcClientConfigImpl(scAddress));
//	}
	
	public void setNeighbors(String scName, List<String> neighborList) {
		// Ivan, 25 Feb 2014: check whether neighborList exist or not
		if(!psgNeighbors.containsKey(scName)) {
			resetNeighbors(scName);
		}
		
		Hashtable<String, String> scNeighborList = psgNeighbors.get(scName);
		for(String eachNeighbor : neighborList) {
			System.out.println("[ConnectionDB.setNeighbors].neighborInfor: " + eachNeighbor);
			String[] neighborInfor = eachNeighbor.split("_");
			System.out.println("[ConnectionDB.setNeighbors].neighborInfor.size: " + neighborInfor.length);
			System.out.println("[ConnectionDB.setNeighbors].neighborName: " + neighborInfor[0]);
			System.out.println("[ConnectionDB.setNeighbors].neighborReference: " + neighborInfor[1]);
			scNeighborList.put(neighborInfor[0], neighborInfor[1]);
//			psgNeighbors.get(scName).put(neighborInfor[0], neighborInfor[1]);
		}
		System.out.println("[ConnectionDB.setNeighbors].neighborSize: " + psgNeighbors.get(scName).size());
//		this.psgNeighbors.put(scName, neighborList);
	}
	
	public void resetNeighbors(String scName) {
		this.psgNeighbors.put(scName, new Hashtable<String,String>());
	}
	
	
	// add
	public void updateSCRef(String scName, String scAddress){		
		scReferences.put(scName, scAddress);
	}
	
//	// Setup HTTP Connection with Coordinator Peer
//	public void addCPConfiguration(String cpName, String cpAddress) throws Exception{		
//		cpConfigurations.put(cpName, new XmlRpcClientConfigImpl(cpAddress));
//	}
	
	// Ivan, 11 May 2012
	public void addNeighbor(String scName, String neighborName, String neighborAddress) {
//		System.out.println("[ConnectionDB.addNeighbor].neighborName: " + neighborName);
		if(containsSCNeighbors(scName)) {
			if(!psgNeighbors.get(scName).contains(neighborName)
					&& psgNeighbors.get(scName).size() < 8) {
				this.psgNeighbors.get(scName).put(neighborName, neighborAddress);
			}
		} else {
			resetNeighbors(scName);
			this.psgNeighbors.get(scName).put(neighborName, neighborAddress);
		}
	}
	
	// remove
	public void removeSCRef(String scName) {
		this.scReferences.remove(scName);
		// Ivan, 12 Jul 2012: also remove neighbors
		this.psgNeighbors.remove(scName);
	}
//	// remove elements from cpConfiguration
//	public void removeCPConfiguration(String cpName) {
//		this.cpConfigurations.remove(cpName);
//	}
	
	public void removeNeighbor(String scName, String neighborAddress) {
		this.psgNeighbors.get(scName).remove(neighborAddress);
	}
	
	
	// verifier
	public boolean hasCSG() {
		return this.csgReference != null;
	}
	
	public boolean containsSC(String scName) {
		return this.scReferences.containsKey(scName);
	}
	
//	public boolean containsCP(String csgName, String scName){	
//		return cpConfigurations.containsKey(csgName+"@"+scName);	
//	}
	
	// check whether the specific sc is created in psgneighbors
	public boolean containsSCNeighbors(String scName) {
		return this.psgNeighbors.containsKey(scName);
	}
	
	public boolean containsNeighbor(String scName, String neighborAddress) {
		if(containsSCNeighbors(scName)) {
			return this.psgNeighbors.get(scName).contains(neighborAddress);
		} else {
			return false;
		}
	}
	
	// end of add
}
