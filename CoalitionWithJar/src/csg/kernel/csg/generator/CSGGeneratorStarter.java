package csg.kernel.csg.generator;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Vector;

//import messaging.format.Instruction;

import csg.config.CSGConfiguration;
import csg.kernel.instance.CSGInstance;
import csg.kernel.server.PublicServer;


// Written October 26.
public class CSGGeneratorStarter{
	// data
	public static boolean mySemaphore = false;
	public static CSGGenerator myCsgGen; 
	public static ConcurrentLinkedQueue CSGRemovalJobs = new ConcurrentLinkedQueue();
	
	// constructor
	
	// methods
	public static void main(String[] args) throws Exception {
		// initialize generator
		String myIP = CSGConfiguration.DEFAULT_IP;
		int myPort = CSGConfiguration.GENERATOR_PORT;
		myCsgGen = new CSGGenerator();
		myCsgGen.initAllPorts();
		myCsgGen.registerToCSM(myIP, myPort);
		
		// start server
		PublicServer csgGenServer = new PublicServer("CSG Generator",myPort);
		csgGenServer.start();
		
//		RemoveCSG_Thread CSG_Maintainance = new RemoveCSG_Thread(CSGRemovalJobs);
//		CSG_Maintainance.start();
	}

}


// Ivan, 5 Jul 2012: still not sure the purpose of following class
//A Thread for removing Semantic Cluster.
//class RemoveCSG_Thread extends Thread{
//	// data
//	ConcurrentLinkedQueue myQueue;
//	
//	// constructor
//	public RemoveCSG_Thread(ConcurrentLinkedQueue inQueue){
//		myQueue = inQueue;
//	}
//	
//	// methods
//	public void run(){
//		
//		try{
//			
//			while (true){
//				String CSG_Name="";
//				Vector CSG_To_Update = new Vector();
//				
//				while ((!CSGGeneratorStarter.mySemaphore)&&(CSGGeneratorStarter.CSGRemovalJobs.size()>0)){
//					
//					for (int i=0; i<CSGGeneratorStarter.CSGRemovalJobs.size(); i++){
//						
//						System.out.println("[ "+csg.config.Configs.RESOURNCE_NAME+" ] "+CSGGeneratorStarter.CSGRemovalJobs.toArray()[i]);
//						
//					}
//					Instruction getCMD = (Instruction)CSGGeneratorStarter.CSGRemovalJobs.poll();
//					
//					System.out.println("[ "+csg.config.Configs.RESOURNCE_NAME+" ] "+"Instruction: RemoveSC "+getCMD.getCSGName()+", "+getCMD.getCSGName());
//					
//					
//					
//					   
//					if ((!CSG_Name.equals(getCMD.getCSGName())))
//						CSG_To_Update.add(getCMD.getCSGName());
//					
//					CSG_Name = getCMD.getCSGName();
//					String CPName = getCMD.getCSGName(); 
//					
//					// Delete Semantic Cluster away
//					// Ivan, 27 Jun 2012: comment
////					((CSGInstance)csg.kernel.csg_generator.CSGGeneratorStarter.myCsgGen.CSG_Registry.get(CSG_Name)).myRegistry.deleteSemanticCluster(CPName);
//					CSGGeneratorStarter.myCsgGen.getAllCSG().get(CSG_Name).removeSC(CPName);
//					
//					// Update Network Graph
////					((CSGInstance)csg.kernel.csg_generator.CSGGeneratorStarter.myCsgGen.CSG_Registry.get(CSG_Name)).ng.removeCPNode(CPName);
//					
//					
//					// If Semantic Cluster is Empty, Remove it.					
//					if (CSGGeneratorStarter.myCsgGen.getSize(getCMD.getCSGName())==0){
//						CSGGeneratorStarter.myCsgGen.removeCSG(getCMD.getCSGName());
//						
//					}
//					
//					
//				}
//				
//				
//				for (int i=0; i<CSG_To_Update.size(); i++){
//					String myCSG_Name = (String) CSG_To_Update.get(i);
//					if (CSGGeneratorStarter.myCsgGen.getAllCSG().containsKey(myCSG_Name)){	
//						
//						System.out.println("[ "+csg.config.Configs.RESOURNCE_NAME+" ] "+"Updating Affected Network");
////						((CSGInstance)csg.kernel.csg_generator.CSGGeneratorStarter.myCsgGen.CSG_Registry.get(myCSG_Name)).ng.toUpdateNetworkOverlay();
//						
//					}
//				}
// 
//				
//				sleep(2000);
//				
//				
//			}
//			
//		}
//		catch (Exception e){
//			
//			System.out.println("[ "+csg.config.Configs.RESOURNCE_NAME+" ] "+"Error: "+e);
//		}
//		
//	}
//	
//}
//
//
