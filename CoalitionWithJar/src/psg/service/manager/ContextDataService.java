/*

* Copyright (c) 2007, National University of Singapore (NUS)
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without 
* modification, are permitted provided that the following conditions 
* are met:
*
*   * Redistributions of source code must retain the above copyright notice, 
*     this list of conditions,the authors and the following disclaimer.
*   * Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions,the authors and the following disclaimer in
*     the documentation and/or other materials provided with the distribution.
*   * Neither the name of the university nor the names of its 
*     contributors may be used to endorse or promote products derived from 
*     this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
* POSSIBILITY OF SUCH DAMAGE.
*
* Author: Wenwei Xue (dcsxw@nus.edu.sg)
*
*/
package psg.service.manager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import psg.config.PSGConfiguration;
import psg.kernel.api.PSGFacade;
import psg.kernel.connection.ConnectionManager;
import psg.kernel.server.PublicServer;
import qp.kernel.api.QPManager;


/**
 * This class represents the service manager at a physical space gateway (PSG).
 */
public class ContextDataService {
	// data
//	private ContextDomain cdInstance;
//	private String idKey;
//	private String myName;
//	public ServiceManager cds;
	private static Map<String, ContextDomain> cdMap = new Hashtable<String, ContextDomain>();
	private ConnectionManager mcsInterface;// = new McsAPI();
	
	// constructor
	/**
	 * @param domains names of the context domains the physical space belongs to
	 * @param attrs names of the context attributes in each domain
	 * @param EventServerSocketPort port of the server socket to accept event subscription
	 * @throws Exception 
	 */
	public ContextDataService() {
//		this.cdInstance = cd;
//		this.idKey = cd.getIDKey();
//		this.myName = cd.getName();
		this.mcsInterface = new ConnectionManager();
		/*for enabling GUI*/
	//	cds.psg = new PhysicalSpaceGUI(domains, attrs, cds);
		
		
	}	// ServiceManager
	
	// methods
	// Ivan, 13 Jul 2012: beginning of adding
	public void register(ContextDomain cdInstance) throws Exception {
		// Step 1: put in the local list
		cdMap.put(cdInstance.getName(), cdInstance);
		
		// Step 2: start server
		cdInstance.getServer().start();
		
		// Step 3; register with Coalition 
		mcsInterface.registerPSG(cdInstance);
	}
	
	// Ivan, 24 Feb 2014: updating
	public void updatePsgReference(String psgName, String newReference) {
		// Step 1: get Context Domain instance
		ContextDomain cdInstance = cdMap.get(psgName);
		
		// Step 2: update reference locally
		cdInstance.setReference(newReference);
		
		// Step 3: update with SCs
		mcsInterface.updatePSG(cdInstance);
	}
	
	
	// end of adding
	
	
	// withdraw based on cd instance
	public void withdraw(ContextDomain cdInstance) {
		// Step 1: withdraw from Coalition
		mcsInterface.leavePSG(cdInstance);
		
		// Step 2: close the server
		// ?? not really implemented
		cdInstance.getServer().close();
		
		// Step 3: remove from the list
		cdMap.remove(cdInstance.getName());
	}
	
	// withdraw based on cd name
	public void withdraw(String cdName) {
		// Step 1: remove from list
		ContextDomain cdInstance = cdMap.remove(cdName);
		
		// Step 2: close server
		// not implemented
		cdInstance.getServer().close();
		
		// Step 3: withdraw from Coalition
		mcsInterface.leavePSG(cdInstance);
	}
	
	// Ivan, 13 Jul 2012: since we use return string here, so 
	// no need to put reference in parameters, but for 
	// parallel queries, should put them in.
	public String queryContext(String psgName, String queryString, String issuerReference) {
		// Step 1: generate querySignature
		String querySignature = psgName + "_" + System.currentTimeMillis();
//		System.out.println("[ContextDataService.queryContext].querySignature: " + querySignature);
		// Step 2: submit to Coaition
		//return mcsInterface.queryMCS(psgName, querySignature, queryString, issuerReference);
		// Ivan, 17 Sep 2013: instead of submitting to Coalition
		// query will be forwarded to the local processor
		return (new QPManager()).queryContext(querySignature, queryString, issuerReference);
	}
	
	
	
	public ContextDomain getContextDomain(String cdName) {
		return cdMap.get(cdName);
	}
	
	// Ivan, 19 Jul 2012: print content of cdMap
	public void print() {
		System.out.println("[ContextDataService.print].Number of registered PSGs: " + cdMap.size());
		for(ContextDomain cd : cdMap.values()) {
			System.out.println("[ContextDataService.print].Name of PSG: " + cd.getName());
		}
	}
	
	// end of adding
	
//	// Ivan, 10 may 2012
//	// name convention or identification:
//	// domainName == csgName, attributeName == scName, idKey(e) == idKey
//	private void updateRegistration(String csgName, String scName,String idKey){
//		System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"UPDATE RE REGISTER: START");
////		leave(idKey);
////		clearConnections();
//		try{
//			Thread.sleep(200000);
//		} catch(Exception e){
//			e.printStackTrace();
//		}
////		//Ivan 23 June 2010
////		String ipPort = "192.168.110.255:88888";	// testing only, should not be like this, Ivan 10 May 2012
////		register(idKey, ipPort);
//	}
//		
////	public void reRegister(){
////		clearConnections();
////		
////		String result = refList;
////		// For Parsing String into multiple neighbours.
////		//System.out.println("PSG Re-registering: RefList="+refList);
////		int posi = 0;
////		int posi2;
////		String getNeighbourAddress="";				
////		Vector<String> possibleNeighbours = new Vector<String>();
////
////		while ((posi!=-1) && (posi<result.length()-1)){				
////			posi2 = result.indexOf(";",posi+1);				
////			if (getNeighbourAddress.equals("")) {
////				getNeighbourAddress = result.substring(posi,posi2);
////			} else {
////				getNeighbourAddress = result.substring(posi+1,posi2);	
////			}	
////			//	System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"- |"+getNeighbourAddress+"|");
////
////			//myNeighbours.add(getNeighbourAddress);
////			if (getNeighbourAddress.startsWith("http://")) {
////				possibleNeighbours.add(getNeighbourAddress);
////			}	
////			posi = posi2;			
////		} // end while
////
////		Random randomGenerator = new Random();
////		randomGenerator.setSeed(System.currentTimeMillis());
//		
////		for (int i=0; i<10; i++){
////			getNeighbourAddress = possibleNeighbours.get(randomGenerator.nextInt(possibleNeighbours.size()));
////			String regResults = mcsInterface.registerNeighbour(csgName, scName, getNeighbourAddress);
////			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Register NB Result: "+regResults);
////			
////			// Set up connection with Neighbouring Peer.
////			try{
////				mcsInterface.foreignRegisterAsNeighbour(csgName, scName, idKey, getNeighbourAddress);
////			} catch (Exception e){
////				e.printStackTrace();
////			}
////		}
//		
//		//register(idKey, csgName, scName, cds.getAttributeValue(csgName, scName));
//
//		//String idKey = csgName +"."+ scName;
//		//System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"idKey = "+ "|"+idKey+ "|; name = " + idKey);
//		//System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"After Registering, MyNeighBoursListSize = "+((Vector)(((app.kernel.api.McsAPI)PsgFacade.allPSGs.get(idKey)).myNB).get(idKey)).size());
//		
////	}
//	
////	public void reRegister(String csgName, String scName, String idKey){
//	public void reRegister(ContextDomain cdInstance){
////		clearConnections();
//		register();
////		String idKey = csgName +"."+ scName;
//		//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"idKey = "+ "|"+idKey+ "|; name = " + idKey);
//		//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"After Registering, MyNeighBoursListSize = "+((Vector)(((app.kernel.api.McsAPI)PsgFacade.allPSGs.get(idKey)).myNB).get(idKey)).size());
//	}
//	
////	// clear connections with all SCs for this cdInstance
////	private void clearConnections(){
////		//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"ClearConnections: "+idKey+" at "+csgName+"."+scName);
////		for(ContextAttribute ca:cdInstance.getAttributeList()) {
//////			mcsInterface.clearAllNeighbours(cdInstance.getName(), ca.getName());
////		}
////		//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"After Clearing Connections, MyNeighBoursListSize = "+((Vector)((app.kernel.api.McsAPI)PsgFacade.allPSGs.get(idKey)).myNB.get(csgName+"."+scName)).size());
////	}
//	
//	private void writeFile(String dirName, String content, String csgName, String scName){
//		try{
//	        boolean status = new File("c:/Startup/logs/"+dirName+"/"+csgName+"-"+scName).mkdirs();  // should succeed			    	        
//	        // Create file 
//	        System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Creating..  c:\\Startup\\logs\\"+dirName+"\\"+csgName+"-"+scName+"\\log_"+(System.currentTimeMillis() / 100000)+".file");
//	        FileWriter fstream = new FileWriter("c:\\Startup\\logs\\"+dirName+"\\"+csgName+"-"+scName+"\\log_"+(System.currentTimeMillis()/100000)+".file",true);
//	            BufferedWriter out = new BufferedWriter(fstream);
//	        out.write(new Date(System.currentTimeMillis()).toLocaleString().replace(" PM", ":"+(System.currentTimeMillis()%1000)+" PM").replace(", 20","")+" -> "+content+"\n\n");
//	        //Close the output stream
//	        out.close();
//		} catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//	
//	// ?? the format of "idKey" is quite confusing? Ivan, 9 May 2012
//	public void register(){
//		mcsInterface.registerPSG();
//////		long startTime = System.currentTimeMillis();
//////		Hashtable<String,String> cvTypesList = getContextAttributeTypes();
////		try{
//////			String varType="";
//////			varType = cvTypesList.get(csgName+"."+scName);
////			String domainName = cdInstance.getName();
////			System.out.println("[ "+PSGConfiguration.defaultName+" ] "+""+domainName);
////			// name has two types: Ivan, 9 May 2012
////			// type 1: http://192.168.10.10:8080/xmlrpc
////			// type 2: http://192.168.10.10:8080/xmlrpc; [ Listening Port ]
////			if (domainName.indexOf(";")<0){
//////				for(ContextAttribute ca:cdInstance.getAttributeList()) {
//////				mcsInterface.registerPSG(idKey, cdInstance.getName(),
//////						ca.getName(), ca.getType(), ca.getValue());
//////				}
////				mcsInterface.registerPSG();
////				//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Parent=CSM"+"; "+idKey);
////			} else {
////				// ?? not know the formating? Ivan, 9 May 2012
////				String parentCSG = domainName.substring(0, domainName.indexOf(";"));
////				String rName = domainName.substring(domainName.indexOf(";")+1, domainName.length());
////				//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Parent="+ParentCSG+"; "+rName);
//////				((McsAPI) PSGFacade.allPSGs.get(rName)).registerPSG(parentCSG,
//////						rName, csgName, scName, varType,
//////						cds.getAttributeValue(csgName, scName));
//////				for(ContextAttribute ca:cdInstance.getAttributeList()) {
//////					mcsInterface.registerPSG(parentCSG, rName, cdInstance.getName(), 
//////						ca.getName(), ca.getType(), ca.getValue());
//////				}
////				mcsInterface.registerPSG();
////			}
////			Vector<String> nb =(Vector<String>)mcsInterface.myNB.get(cdInstance.getName()+"."+scName);
////			writeFile("EXPERIMENT - PSG SIM", "PSG Re-Registration Time Take: "+(System.currentTimeMillis()-startTime), csgName, scName);										
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"After Registering, Neighbours Details: "+nb.size());
//			//	for (int j=0; j<nb.size();j++){
//			//System.out.println("[ "+PSGConfiguration.defaultName+" ] "+(j+1)+". "+nb.get(j));
//			//	}
////		} catch (Exception e){			
////			e.printStackTrace();
////		}		
//
//	}	
//	
////	public String register(String idKey, String ipPort){
////		//format of name:
////		//"http://"+defaultIP+ ":"+(defaultPort+(i+calib))+"/xmlrpc"
////		Hashtable<String,String> cvTypesList = getContextAttributeTypes();
////		String id = "test";
////		String csg = "test";
////		System.out.println("in register");
////		try{
////			for(int i=0; i<domains.length; i++) {
////				for(int j=0; j<attrs[i].length; j++) {
////					String varType="";
////					varType = cvTypesList.get(domains[i]+"."+attrs[i][j]);
////					System.out.println("[1 "+PSGConfiguration.defaultName+" ] "+""+idKey);
////					if (idKey.indexOf(";")<0){
////						System.out.println("in if");
////						/*Check if the value type is numeric. If yes, then pass the mean and variance values here instead of the actual value
////						 * To do this, either add a new function taking mean and variance or insert two new fields in the existing code. Have a 
////						 * flag saying that instead of the actual value the mean and value pairs should be taken*/
////						if(varType.equals("Double"))
////							System.out.println("Numeric");
////						csg = domains[i];
////						mcsInterface.registerPSG(idKey, domains[i], attrs[i][j], varType,
////								cds.getAttributeValue(domains[i], attrs[i][j]));
////						System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Parent=CSM"+"; "+idKey);
////					}else{
////						//?? not know the formating? Ivan, 9 May 2012
////						String parentCSG = idKey.substring(0, idKey.indexOf(";"));
////						String rName = idKey.substring(idKey.indexOf(";")+1, idKey.length());
////						System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Parent="+domains[i]+"; "+parentCSG+"; "+rName);
////						mcsInterface.registerPSG(domains[i], rName, parentCSG, attrs[i][j], varType,
////								cds.getAttributeValue(domains[i], attrs[i][j]));
////
////					}
////					//System.err.println("[ "+kernel.udp.config.config.resourceName+" ] "+domains[i]+"."+attrs[i][j]+"="+cds.getAttributeValue(domains[i], attrs[i][j]));
////				}
////
////			}
////			// not reasonable, may modify them in the future, Ivan, 9 May 2012
////			id = ((app.kernel.api.McsAPI)PSGFacade.allPSGs.get(idKey)).getSessionId(idKey, csg, ipPort);
////		} catch (Exception e){			
////			e.printStackTrace();
////		}
////		//Modified by Shubhabrata - Add return value here to return session id to the user
////		return id;
////	}
//	
//	
////	private Vector<String> getListOfContextAttributes(){
////		return null;
////		return cdInstance.getAttributeList();
////		Vector<String> res = new Vector<String>();
////		for (int i=0; i< domains.length; i++){
////			for (int j=0; j<attrs[i].length; j++){
////				res.add(domains[i]+"."+attrs[i][j]);
////			}
////		}
////		return res;
////	}
//	
////	private Hashtable<String, String> getContextAttributeTypes(){
////		return null;
////		Hashtable<String, String> contextVarTypes = new Hashtable<String, String> ();
////		for (int i=0; i< domains.length; i++){
////			for (int j=0; j<attrs[i].length; j++){
////				contextVarTypes.put(domains[i]+"."+attrs[i][j], valueTypes[i][j]);
////			}
////		}
////		return contextVarTypes;
////	}
//	
//	public void leave(String idKey, String msid) throws Exception {
//		this.mcsInterface.leavePSG();
////		Hashtable<String,String> cvTypesList = getContextAttributeTypes();
////		System.out.println("* LEFT *");
////		try{
////			for (int i=0; i<domains.length; i++){
////				for (int j=0; j<attrs[i].length; j++){
////					String varType="";
////					varType = cvTypesList.get(domains[i]+"."+attrs[i][j]);
////		for (ContextAttribute ca : cdInstance.getAttributeList()) {
////			mcsInterface.leavePSG(idKey, cdInstance.getName(), ca.getName(),
////					ca.getType(), ca.getValue());
////					System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Leaving PSG: "+varType+"; "+domains[i]+"."+attrs[i][j]);
////		}
////			}	
////			((app.kernel.api.McsAPI)PSGFacade.allPSGs.get(idKey)).deleteSessionId(idKey,msid);
////		} catch (Exception e){			
////			e.printStackTrace();
////		}		
//		
//	}
//	
////	// ?? why do we need this type of leave? Ivan, 9 May 2012
////	private void leave(McsAPI m, String idKey){
////		Hashtable<String,String> cvTypesList = getContextAttributeTypes();
////		System.out.println("* LEFT *");
////		try{
////			for (int i=0; i<domains.length; i++){
////				for (int j=0; j<attrs[i].length; j++){
////					String varType="";
////					varType = cvTypesList.get(domains[i]+"."+attrs[i][j]);
////					m.leavePSG(idKey, domains[i], attrs[i][j], varType, cds.getAttributeValue(domains[i], attrs[i][j]));
////					System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Leaving PSG: "+varType+"; "+domains[i]+"."+attrs[i][j]);
////				}
////			}								
////		} catch (Exception e){			
////			e.printStackTrace();
////		}		
////		
////	}
//
////	public void leave(String idKey, String csgName, String scName, String previousValue){
////		Hashtable<String,String> cvTypesList = getContextAttributeTypes();
////		try{
////			String varType="";
////			varType = cvTypesList.get(csgName+"."+scName);
////			((McsAPI)PSGFacade.allPSGs.get(idKey)).leavePSG(idKey, csgName, scName, varType, previousValue);
////		} catch (Exception e){			
////			e.printStackTrace();
////		}		
////		
////	}
//	
////	/*
////	 * Added by Shubhabrata - This function will be used for registering callbacks by a PSG. A PSG will
////	 * give two session identifiers for registering the callback. The first session 
////	 */
////	
////	
////	public String registerCallback(String idKey, String callerId, String calleeId) {
////		return mcsInterface.registerCallback(callerId, calleeId);
////	}
////	/*
////	 * Ivan 23 June 2010
////	 */
////	public String withdrawCallback(String idKey, String callerId, String calleeId){
////		return mcsInterface.withdrawCallback(callerId, calleeId);
////	}
////	
////	/*
////	 * Added by Shubhabrata - This function is used to register the mobility update function and inform
////	 * the middleware about a change in IP address
////	 */
////	
////	public void mobilityUpdate(String idKey, String sessionId, String ipPort)
////	{
////		mcsInterface.mobilityUpdate(idKey, sessionId, ipPort);
////	}
//	/*// debugging purpose
//	public static void main (String argvs []) {
//		
//		//String attrs [] = new String [ContextAttribute.attrNames.length + ContextAttribute.eventNames.length];
//		//int i,j;
//		//for (i=0;i<ContextAttribute.attrNames.length;i++)
//			//attrs[i] = ContextAttribute.attrNames[i];
//		//for (j=0;j<ContextAttribute.eventNames.length;j++)
//			//attrs[i++] = ContextAttribute.eventNames[j];
//		//System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"Attribute list:");
//		//for (i=0;i<attrs.length;i++)
//			//System.out.print(attrs[i] + " ");
//		//System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+);
//		String domains [] = {"person", "shop", "office"};
//		String attrs [][] = {{"name","location", "user_profile"}, 
//							 {"name", "hasAdvertisement", "crowd_level"}, 
//							 {"room_status","isVacant"},};
//		int EventServerSocketPort = 9999;
//		
//		ServiceManager sm = new ServiceManager(domains, attrs, EventServerSocketPort);
//		
//		sm.cds.psg.PSFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//								
//	}	// main*/
	
}	// class