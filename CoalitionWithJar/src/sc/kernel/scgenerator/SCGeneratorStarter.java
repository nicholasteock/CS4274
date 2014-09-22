/**
 * 
 */
package sc.kernel.scgenerator;


import java.awt.Font;
import java.util.Vector;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JFrame;
import javax.swing.JTextArea;

//import messaging.format.Instruction;

import sc.config.SCConfiguration;
import sc.demo.CPGUI;
//import sc.kernel.api.*;
import sc.kernel.server.PublicServer;

/**
 * @author Ng Wen Long
 *
 */
public class SCGeneratorStarter {

	public static SCGenerator mySCGenerator;
	public static boolean mySemaphore = false;
	public static boolean SemaphoreCreatingSCInProgress = false;
	public static boolean SemaphoreRoutingSCInProgress = false;  
	
	public static ConcurrentLinkedQueue SCRemovalJobs = new ConcurrentLinkedQueue();	
	/**
	 * @param args
	 */
	public static Vector messages = new Vector();

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// Ivan, 6 Jul 2012
		// initialize sc generator
//		if (args.length==0){
//			mySCGenerator = new SCGenerator();
//		}
//		else{
//			mySCGenerator = new SCGenerator(new Integer(args[0]).intValue());
//		}
		mySCGenerator = new SCGenerator();
		mySCGenerator.initPortAllocationArray();
		
		String myIP = SCConfiguration.DEFAULT_IP;
		int myPort = SCConfiguration.SC_GENERATOR_PORT;
//		mySCGenerator.registerToCSM(myIP, myPort);
		mySCGenerator.registerToCSGGenerator(myIP, myPort);
		
		// start sc generator server
		PublicServer scGenServer = new PublicServer("SC Generator", myPort);
		scGenServer.start();
		
		//mySCGenerator.registerToCSG("person");
		//mySCGenerator.registerToCSG("shop");
		//mySCGenerator.registerToCSG("office");
		
		CPGUI myGUI = new CPGUI("Semantic Clusters", "", mySCGenerator);
		
		
//		RemoveSCThread SCMaintainance = new RemoveSCThread(SCRemovalJobs);
//		SCMaintainance.start();
		
	}

}


//// A Thread for removing Semantic Cluster.
//class RemoveSCThread extends Thread{
//	// data
//	ConcurrentLinkedQueue myQueue;
//	
//	// constructor
//	public RemoveSCThread(ConcurrentLinkedQueue inQueue){
//		
//		myQueue = inQueue;
//		
//	}
//	
//	// methods
//	@Override
//	public void run(){
//		
//		try{
//			int cnt2=0;
//			while (true){
//				
//		
//				try{
//				//	System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"[ Server ] [ Thread Service = Jobs Processing ] [ Number of Jobs = "+SCGeneratorStarter.SCRemovalJobs.size()+" ]");
//					int cnt=0;
//					boolean breakFlag=false;
//					while ((SCGeneratorStarter.mySemaphore)||(SCGeneratorStarter.SCRemovalJobs.size()==0)){
//						sleep(1);
//						//System.out.print(".");
//						cnt++;
//						if (cnt>1000){
//							breakFlag=true;
//							break;
//						}
//					}
//				//	System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"DONE");
//					while ((!SCGeneratorStarter.mySemaphore)&&(SCGeneratorStarter.SCRemovalJobs.size()>0)||(cnt>1000)){
//					
//						Instruction getCMD = (Instruction)SCGeneratorStarter.SCRemovalJobs.poll();
//						System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+"Instruction: RemoveSC "+getCMD.getCSGName()+", "+getCMD.getSCName() + " Size = "+SCGeneratorStarter.mySCGenerator.getSCSize(getCMD.getCSGName(), getCMD.getSCName()));
//						
//						if (SCGeneratorStarter.mySCGenerator.getSCSize(getCMD.getCSGName(), getCMD.getSCName())==0)
//							SCGeneratorStarter.mySCGenerator.removeSC(getCMD.getCSGName(), getCMD.getSCName());
//						
//						if (SCGeneratorStarter.SCRemovalJobs.size()==0) cnt=0;
//					}
//				}
//				catch (Exception e){
//					
//					//System.out.println("[ "+kernel.udp.config.config.resourceName+" ] "+"SC GEN RemoveSC Thread: Error: "+e);
//				}
//						
//			}
//			
//		}
//			catch (Exception e){
//				
//				System.out.println("[ "+Configs.RESOURCE_NAME+" ] "+"SC GEN Error: "+e);
//			}
//		
//	}
//	
//}
//
