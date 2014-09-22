package psg.consoleUi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import psg.config.PSGConfiguration;
import psg.config.StaticContext;
import psg.kernel.api.PSGFacade;
import psg.service.manager.ContextDataService;
import psg.service.manager.ContextDomain;


public class PSGStarter 
{
	PSGFacade psgKernelFacade = null;	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception 
	{
    	String defaultIP 		= PSGConfiguration.defaultIP;
    	int defaultPort 		= PSGConfiguration.defaultPort;
    	
//    	int appPort = PSG_Configuration.appPort;
    	
//    	String IPPort = defaultIP+":"+defaultPort;
//    	String IPPort = defaultIP+":"+appPort;
    	String defaultMac = PSGConfiguration.defaultMac;
//    	String ID_KEY = "http://"+defaultIP+ ":"+defaultPort+"/xmlrpc";
//    	String ID_KEY 		= "http://"+defaultMac+"/xmlrpc";
    	PSGStarter consoleMgr = new PSGStarter();    	
//    	consoleMgr.psgKernelFacade = new PSGFacade(ID_KEY, defaultIP, defaultPort);
    	
    	// Ivan, 17 Jul 2012: testing
    	ContextDataService cds = new ContextDataService();
//    	String reference = "http://" + defaultIP + ":" + "13001" + "/xmlrpc";
//    	ContextDomain cdInstance = StaticContext.getContextDomain(reference);
//    	String reference1 = "http://localhost:13001/xmlrpc";
//    	ContextDomain cd1 = StaticContext.getContextDomain(reference1);
//    	String reference2 = "http://localhost:13002/xmlrpc";
//    	ContextDomain cd2 = StaticContext.getContextDomain(reference2);
    	

		
    	String MSID = "";
//    	String callerId = "";
//    	String calleeId = "";
    	while(true)
    	{
	    	System.out.println("====================================");
	    	System.out.println("Welcome to Physical Space Gateway");
	    	System.out.println("====================================");
	    	System.out.println("(R) To REGISTER to the middleware");
//	    	System.out.println("(C) To REGISTER CALLBACK to the middleware");
//	    	System.out.println("(M) MOBILITY UPDATE");
	    	System.out.println("(Q) QUERY");
//	    	System.out.println("(D) To DELETE CALLBACK from middleware");
	    	System.out.println("(L) LEAVE middleware");
	    	System.out.println("(A) Abort Program");	    	
	    	System.out.println("====================================");
	    	System.out.print("Please key in your Cmd:");
	    	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String cmd = null;
			try 
			{
				cmd = in.readLine();
				System.out.println("====================================");
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
	        try
	        {
				if(cmd.equals("R"))
				{
					int i=0;
					//for(int i=0; i<10; i++) {
						String reference = "http://" + defaultIP + ":" + (13001 + i) + "/xmlrpc";
						String psgName = "PSGPersonSpace";
						ContextDomain cdInstance = StaticContext.getPersonDomain(psgName, reference, i);
						long beginOne = System.currentTimeMillis();
						cds.register(cdInstance);//.register(IPPort);	
						long endOne = System.currentTimeMillis();
						long psgRegistrationTime = endOne - beginOne;
						System.out.println("The registered PSG MSID is: "+MSID);
						System.out.println("[PSG Registration Time]: "+psgRegistrationTime);
					//}
					
				    // Check for available port from the machine
				    //public int getFreePort() {
				    	ServerSocket s = null;
				        try {
				        	s = new ServerSocket(0);
				      //      return s.getLocalPort();
				        } catch (IOException ex) {
				        //    return 0;
				        }
				    //}
				      /*  for(int i=10; i<20; i++) {
						String reference1 = "http://" + defaultIP + ":" + (13001 + i) + "/xmlrpc";
						String psgName1 = "PSGOfficeSpace";
						ContextDomain cdInstance1 = StaticContext.getOfficeDomain(psgName1, reference1, i);
						long beginOne1 = System.currentTimeMillis();
						cds.register(cdInstance1);//.register(IPPort);	
						long endOne1 = System.currentTimeMillis();
						long psgRegistrationTime1 = endOne1 - beginOne1;
						System.out.println("The registered PSG MSID is: "+MSID);
						System.out.println("[PSG Registration Time]: "+psgRegistrationTime1);
					}*/
			    	
//			    	long beginTwo = System.currentTimeMillis();
//			    	cds.register(cd2);//.register(IPPort);	
//					System.out.println("The registered PSG MSID is: "+MSID);
//			    	long endTwo = System.currentTimeMillis();
//			    	long psgRegistrationTimeTwo = endTwo - beginTwo;
//			    	System.out.println("[PSG Registration Time]: "+psgRegistrationTimeTwo);
			    	
//			    	writeLog("psgRegistration", ""+psgRegistrationTime);
//					String MSID = consoleMgr.psgKernelFacade.register(IPPort);
//					System.out.println("The registered PSG MSID is: "+MSID);
				}
//				else if(cmd.equals("C"))
//				{
////					consoleMgr.psgKernelFacade.leave();
//			    	callerId = "12345:"+MSID;
//			    	calleeId = "12345:"+"home@100000";
//					long beginTwo = System.currentTimeMillis();
//					String callbackRegistrationResult = consoleMgr.psgKernelFacade.registerCallback(ID_KEY, callerId, calleeId);
//					System.out.println("[Callback Registration Result]: "+callbackRegistrationResult);
//					long endTwo = System.currentTimeMillis();
//					long callbackRegistrationTime = endTwo - beginTwo;
//					System.out.println("[Callback Registration Time (Same CSG)]: "+callbackRegistrationTime);
//					writeLog("callbackRegistration",""+callbackRegistrationTime);
//					
//				}
				else if(cmd.equals("Q"))
				{
////					Thread.sleep(1000);
//					int size = 5;
////					for(int size=0; size<30; size++) {
//					TestQuery[] psgArray = new TestQuery[size]; 
//					for(int i=0; i<size; i++) {
//						psgArray[i] = new TestQuery(defaultIP,i);
//					}
//					ExecutorService threadExecutor = Executors.newCachedThreadPool();
//					for(int i=0; i<size; i++) {
//						threadExecutor.execute(psgArray[i]);
//					}
					
//					}
//					System.err.println("[Query Result]: "+result);
//					System.err.println("[Query Time (Same CSG)]: "+queryTime);
//					writeLog("totalTime",queryTime);
					
					
////					consoleMgr.psgKernelFacade.leave();
//
					System.out.print("Please key in your Query:");
					
//			    	BufferedReader in2 = new BufferedReader(new InputStreamReader(System.in));
//					String query = null;				
//					query = in2.readLine();	
//					String query = "select person.speed, person.action, person.mood, person.location,"
//							+ " office.name, office.isMeeting, office.location"
//							+ " from person, office "
//							+ " where ( person.name = \"IVAN\" "
//							+ " AND person.location = \"IDMI\" "
//							+ " AND person.speed = \"fast\" "
//							+ " AND person.isBusy = \"yes\" )"
//							+ " OR ( office.name = \"AMI\""
//							+ " AND office.location = \"IDMI\" ) ";
					String query = "select person.preference from person where person.name = \"testpersonname\"";
//					String query = "select person.acceleration from person where ( person.magnetism = \"positive\" and person.acceleration = \"fast\" )";//LRor ( person.light = \"high\" and shop.light = \"high\" ) or ( person.magnetism= \"high\" and shop.magnetism= \"high\")";
					long beginTwo = System.currentTimeMillis();
			    	String reference = "http://" + defaultIP + ":" + "13001" + "/xmlrpc";
			    	String psgName = defaultMac + "-" + 1;
			    	ContextDomain cdInstance = StaticContext.getPersonDomain(psgName, reference, 1);
					String result =  cds.queryContext(reference,query,cdInstance.getReference());
					long endTwo = System.currentTimeMillis();
					long queryTime = endTwo - beginTwo;
					System.err.println("[Query Result]: "+result);
					System.out.println("[Query Time (Same CSG)]: "+queryTime);
////					writeLog("callbackRegistration",""+queryTime);
				}				
//				else if(cmd.equals("M"))
//				{
//					
//					long beginThree = System.currentTimeMillis();
////					consoleMgr.psgKernelFacade.mobilityUpdate(MSID, IPPort);
//					long endThree = System.currentTimeMillis();
//					long mobilityUpdatingTime = endThree - beginThree;
//					System.out.println("[Mobility Updating and Notification Time (same CSG and one callback)]: "+mobilityUpdatingTime);
//					writeLog("mobilityUpdate",""+mobilityUpdatingTime);
////					System.out.print("Please key in your Query:");
////					
////			    	BufferedReader in2 = new BufferedReader(new InputStreamReader(System.in));
////					String query = null;				
////					query = in2.readLine();
////					consoleMgr.psgKernelFacade.queryMCS(query);
//				}
//				else if(cmd.equals("D")){
//					long beginFour = System.currentTimeMillis();
//					consoleMgr.psgKernelFacade.withdrawCallback(ID_KEY, callerId, calleeId);
//					long endFour = System.currentTimeMillis();
//					long callbackWithdrawingTime = endFour-beginFour;
//					System.out.println("[Callback Withdrawing Time (same CSG)]: "+callbackWithdrawingTime);
//					writeLog("deleteCallback",""+callbackWithdrawingTime);
//				}
				else if(cmd.equals("L")){
			    	long beginFive = System.currentTimeMillis();
//			    	cds.withdraw(cd1.getName());
//					System.out.println("The registered PSG MSID is: "+MSID);
			    	long endFive = System.currentTimeMillis();
			    	long psgLeaveTime = endFive - beginFive;
			    	System.out.println("[PSG De-Registration Time]: "+psgLeaveTime);
			    	long leaveTwoBegin = System.currentTimeMillis();
//			    	cds.withdraw(cd2.getName());
//					System.out.println("The registered PSG MSID is: "+MSID);
			    	long  leaveTwoEnd = System.currentTimeMillis();
			    	long leaveTwo = leaveTwoEnd - leaveTwoBegin;
			    	System.out.println("[PSG De-Registration Time]: "+leaveTwo);
			    	writeLog("leave",""+psgLeaveTime);
				}
				else if(cmd.equals("A"))
				{
					break;
				}
				else
				{
					System.out.println("Incorrect Command, please try again.");
				}
	        }
	        catch (Exception e)
	        {
	        	System.err.println(e.getMessage());
	        }
    	}
	}
	
	//get updating alert
	public String getCurrentIp () throws UnknownHostException{
		String IP = ""+InetAddress.getLocalHost();
		System.out.println("The current IP is: "+IP);
		return IP;
	}
	
	// write experiment data to txt
	public static void writeLog(String fileName, String content)
    {
            try{
                FileWriter fstream = new FileWriter("D:\\Person\\"+fileName+".txt",true);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(content);
                out.write(System.getProperty( "line.separator" ));
                out.write(System.getProperty( "line.separator" ));
                //Close the output stream
                out.close();
                }
            catch (Exception e){//Catch exception if any
             System.err.println("Error: " + e.getMessage());}
    }

}
