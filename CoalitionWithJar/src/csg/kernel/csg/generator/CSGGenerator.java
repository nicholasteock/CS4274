/**
 * @author Ivan
 * @date 27 Jun 2012
 * @Role: this class as a local manager of CSGs created in this device.
 * 
 * Logically, each CSG will register with CSM individually and CSM 
 * will work as a CSG manager, so the operation should be quite 
 * simple and direct by issue request with CSM. 
 * In this class, there is a CSGRegistry variable which may not be
 * appropriate.
 * Meanwhile, how to divide the functionalities between CSGGenerator
 * and CSGInstance is also important.
 * 
 * @functionalities
 * 1) receive instructions from CSM to create new CSGs
 * 2) receive SC Generator registrations
 * 3) functioning as the manager of local CSGs
 * 
 * @description
 * 
 * 
 * */
package csg.kernel.csg.generator;

import java.util.Hashtable;
import java.util.Map;

import kernel.com.reference.NetworkReference;
//import kernel.udp.invoke.RPCConfigure;
import kernel.network.client.UDPClient;

import csg.config.CSGConfiguration;
import csg.kernel.instance.CSGInstance;
import csg.kernel.sc.generators.SCGenManager;



// Written by Ng Wen Long - October 26
public class CSGGenerator {
	// data
	// It starts the CSG_Generator instance.
	// It accepts request to create Context Space Gateways.
	// -- It need to be advertised itself to a CSM first.
	private static Map<String, CSGInstance> csgRegistry = new Hashtable<String, CSGInstance>();
//	private static int size = 0;
//	private CSGInstance accessor;
	private int defaultPort = CSGConfiguration.DEFAULT_PORT;
	private String defaultIP = CSGConfiguration.DEFAULT_IP;
	private String hostURL = CSGConfiguration.CSM_REFERENCE;
	
	private static int portSize = CSGConfiguration.MAX_NUM_CSG;
	private static int[] allPorts = new int[portSize];
	
	// constructor
	public CSGGenerator() {
//		this.initAllPorts();
//		accessor = new CSGInstance(hostURL,"Accessor", defaultIP, defaultPort);
	}	
	
	// methods
	public void initAllPorts(){
		for (int i=0; i<portSize; i++){
			allPorts[i] = 0;
		}
	}
	
	private int nextFreePort(){
		int a = 0;
		while ((allPorts[a]!=0)) {
			a++;
		}
		allPorts[a] = 1;
		return defaultPort + a + 1;
	}	
	
	private void removePort(int port){
		allPorts[port - defaultPort - 1] = 0;
	}
	
	// Ivan, 28 Jun 2012
//	// Advertise CSG_Generator to CSM
//	public void registerToCSM() throws Exception{
//		
//		String result = accessor.register_CSG_Gen();
//		
//		if (!result.startsWith("[Registration Failed]")){
//			
//		   String SC_Gen_Address = result.substring(result.indexOf(":")+1,result.length());
//		   System.out.println("[ "+csg.io.config.config.RESOURNCE_NAME+" ] "+"RegisterToCSM response: "+ result);
////		   SCGenManager.internal_register_SC_Gen(SC_Gen_Address);
//		   
//		}
//		else{
//			
//			System.out.println("[ "+csg.io.config.config.RESOURNCE_NAME+" ] "+result);
//			
//		}
//		
//	}
	
	// Ivan, 28 Jun 2012: register this CSG Generator to CSM
	public String registerToCSM(String myIP, int myPort) {
//		try {
			String myReference = (new NetworkReference(myIP, myPort)).getReference();
		    // make the a regular call
		    Object[] params = new Object[]
		        { new String(myReference) };  
			
			String result = (String) (new UDPClient(hostURL)).execute("CSGGenManager.registerCSGGen", params);
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"CSG_Gen Sends to CSM: "+myIP+" Listening Port: "+myPort);
			return result;
//		} catch (Exception e){
//			System.out.println("[ "+csg.config.Configs.RESOURNCE_NAME+" ] "+"Registration Failed.");
//			return "[Registration Failed]";
//		}
	}
	
	// Ivan, 28 Jun 2012: CSM calls for this method to create new CSG
//	 Create an CSG Instance in CSG_Generator.
	public String createCSG(String csgName) throws Exception{
		csgName = csgName.toUpperCase();
		if (csgRegistry.containsKey(csgName)){
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"CSG Exists already.");
			return csgRegistry.get(csgName).getCSGReference();
//			return "[CSG EXISTED]";
		} else {					
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"CSG Gen: Creating new Context Space Gateway: "+csgName);
					
			int getPort = this.nextFreePort();
			System.out.println("The CSG port second " + getPort);
//			size++;
			CSGInstance instance = new CSGInstance(hostURL, csgName, defaultIP, getPort);	
			csgRegistry.put(csgName, instance);	
			
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"CSG Gen: Registering new Context Space Gateway: "+csgName);
			// Ivan, 28 Jun 2012: since CSG creation is CSM initiated, we do
			// not need to ask CSG instance to re-register with CSM any more.
//			instance.registerCSGtoCSM(csgName, defaultIP, defaultPort);
			return (new NetworkReference(defaultIP, getPort)).getReference();
//			return  defaultIP +":"+getPort;
		}
		
	}

	// Ivan, 28 Jun 2012: actually, parentCSG based mechanisms are not implemented
	// so methods concerns parentCSG are not really used
	//	 Create an CSG Instance in CSG_Generator.
	public String createCSG(String parentCSG, String csgName) throws Exception{
		String ID = parentCSG+"."+csgName;
		
		if (csgRegistry.containsKey(ID)){
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"CSG Exists already.");
			return "[CSG EXISTED]";
		} else {					
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"CSG Gen: Creating new Context Space Gateway: "+ID);
			
			int getPort = this.nextFreePort();
//			size++;
			CSGInstance instance = new CSGInstance(hostURL, csgName, defaultIP, getPort);	
			csgRegistry.put(ID, instance);	
			
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"CSG Gen: Registering new Context Space Gateway: "+ID);
			// Ivan, 28 Jun 2012: Except this one, other instructions of this method are
			// the same with above createCSG() method
			// how to handle this case should be future revised in the future
			instance.registerCSGToParentCSG(parentCSG);
			
			return  defaultIP +":"+getPort;
		}
		
	}
	
	// Remove a CSG Instance in CSG_Generator
	public void removeCSG(String csgName) throws Exception{
		csgName = csgName.toUpperCase();
		if (csgRegistry.containsKey(csgName)){
			// Ivan, 28 Jun 2012: deletion is also CSM initiated,
			// so this does not need any more
//			((CSGInstance)csgRegistry.get(csgName)).leaveCSM(csgName);
			
			this.removePort(((CSGInstance)csgRegistry.get(csgName)).getPort());
			csgRegistry.remove(csgName);
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"[Removal Done] - "+csgName);
		} else {
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"[Removal Failed]");
		}
		
	}
	
	// Ivan, 28 Jun 2012: this method is id
	// Remove a CSG Instance in CSG_Generator
	public void removeCSG(String parentCSG, String csgName) throws Exception{
		String ID = parentCSG+"."+csgName;
		
		if (csgRegistry.containsKey(ID)){
			((CSGInstance)csgRegistry.get(ID)).leaveCSM(parentCSG);
			this.removePort(((CSGInstance)csgRegistry.get(ID)).getPort());
			csgRegistry.remove(ID);
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"[Removal Done] - "+ID);
		} else {
			System.out.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"[Removal Failed]");
		}
	}
	
	public int getSize(String csgName){
		csgName = csgName.toUpperCase();
		return ((CSGInstance)csgRegistry.get(csgName)).scSize();
	}
	
	public CSGInstance getCSGInstance(String csgName) {
		csgName = csgName.toUpperCase();
		return this.csgRegistry.get(csgName);
	}
	
	public Map<String, CSGInstance> getAllCSG() {
		return this.csgRegistry;
	}
	
	// Ivan, 4 Jul 2012: SC Generators will directly call the 
	// SCGenManager.java class to register/withdraw with CSG Gen
	// so no need to have them here.
//	// Ivan, 28 Jun 2012: beginning of adding
//	public String registerSCGen(String scGenAddress) throws Exception{
//		return (new SCGenManager()).registerSCGen(scGenAddress);
//	}
//	
//	public void deleteSCGen(String scGenAddress) {
//		(new SCGenManager()).deleteSCGen(scGenAddress);
//	}
//	// end of adding
	
	// Ivan, 5 Jul 2012: start of adding
	public String getSCReference(String csgName, String scName) throws Exception {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		System.out.println("CSGGenerator.getSCReference.csgName:" + csgName);
		System.out.println("CSGGenerator.getSCReference.scName:" + scName);
		return this.csgRegistry.get(csgName).getSCReference(scName);
	} 
	
	// for query processor only
	public String readSCReference(String csgName, String scName) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		return this.csgRegistry.get(csgName).readSCReference(scName);
	}
	// end of adding
	
	// Ivan, 20 Jul 2012: remove sc instance
	public String deleteSC(String csgName, String scName) {
		csgName = csgName.toUpperCase();
		scName = scName.toLowerCase();
		// Step 1: remove locally
		CSGInstance csgInstance = csgRegistry.get(csgName);
		csgInstance.removeSC(scName);
		
		// Step 2: check the emptiness of CSG and delete if empty
		if(csgInstance.isEmpty()) {
			// delete from csm
			csgInstance.leaveCSM();
			
			// reuse the socket
			removePort(csgInstance.getPort());
			
			// remove locally
			csgRegistry.remove(csgName);
		}
		return "dummy";
	}
	
}
