/**
 * @author ivan
 * @date 23 SEP 2013
 * @objective
 * 1) UDP receiver for remote control of PSGs
 * */
package psg.consoleUi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import experiment.data.ExportData;
import psg.config.PSGConfiguration;
import psg.config.StaticContext;
import psg.service.manager.ContextDataService;
import psg.service.manager.ContextDomain;



public class PSGReceiver implements Runnable {
	// data
	private Thread thread;
	private int port;
	private DatagramSocket socket;
	private boolean alive;
	private int size=0;
	// constructor
	public PSGReceiver(int port) {
		this.port = port;
		try {
			this.socket = new DatagramSocket(port);
		} catch(SocketException e) {
			e.printStackTrace();
		}
		this.alive = true;
		thread = new Thread(this);
		thread.start();
	}

	// methods
	@Override
	public void run() {
		byte[] buffer = new byte[4096];
		System.out.println("[ " + "UDP CLIENT" + " ] " + "UDP Server["
				+ port + "] is started.\n");
		while (alive) {
			try {

				// Receive request from client
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				InetAddress clientAddress = packet.getAddress();
				int clientPort = packet.getPort();

				System.out.println("[ " + "UDP CLIENT" + " ] "
						+ "[Incoming Req] " + new String(buffer) + " from "
						+ clientAddress + ":" + clientPort);

				String reqString = new String(buffer).trim();
				System.out.println("[PSGReceiver.run].reqString: " + reqString);
				processMessage(reqString);
				//				reqString = reqString.substring(0, reqString.indexOf("<?>"));

				//				RequestProcessor myHandler = new RequestProcessor(reqString,
				//						socket, clientAddress, clientPort, myHandlerManager);
				//				myHandler.start();


			} catch (UnknownHostException ue) {
				ue.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("[ " + "UDP CLIENT" + " ] " + e);
			}
		}
	}

	public void close() {
		this.alive = false;
		this.socket.close();
	}

	private void processMessage(String command) {
		String defaultIP = PSGConfiguration.defaultIP;
		int defaultPort = PSGConfiguration.defaultPort;
		String defaultMac = PSGConfiguration.defaultMac;


		// Ivan, 17 Jul 2012: testing
		ContextDataService cds = new ContextDataService();
		//    	String reference = "http://" + defaultIP + ":" + "13001" + "/xmlrpc";
		//    	ContextDomain cdInstance = StaticContext.getContextDomain(reference);
		//    	String reference1 = "http://localhost:13001/xmlrpc";
		//    	ContextDomain cd1 = StaticContext.getContextDomain(reference1);
		//    	String reference2 = "http://localhost:13002/xmlrpc";
		//    	ContextDomain cd2 = StaticContext.getContextDomain(reference2);
		try
		{
			if(command.equals("R"))
			{
				int quantity = 9;
				int i =0;
				for( ;i<quantity*1; i++) {
					String reference = "http://" + defaultIP + ":" + (13001 + i) + "/xmlrpc";
					String psgName = defaultMac + "-" + i;
					ContextDomain cdInstance = StaticContext.getPersonDomain(psgName, reference, i);
					long beginOne = System.currentTimeMillis();
					cds.register(cdInstance);//.register(IPPort);	
					long endOne = System.currentTimeMillis();
					long psgRegistrationTime = endOne - beginOne;
					System.out.println("[PSG Registration Time]: "+psgRegistrationTime);
				}
				for(; i<quantity*2; i++) {
					String reference = "http://" + defaultIP + ":" + (13001 + i) + "/xmlrpc";
					String psgName = defaultMac + "-" + i;
					ContextDomain cdInstance = StaticContext.getShopDomain(psgName, reference, i);
					long beginOne = System.currentTimeMillis();
					cds.register(cdInstance);//.register(IPPort);	
					long endOne = System.currentTimeMillis();
					long psgRegistrationTime = endOne - beginOne;
					System.out.println("[PSG Registration Time]: "+psgRegistrationTime);
				}
//				for(; i<quantity*3; i++) {
//					String reference = "http://" + defaultIP + ":" + (13001 + i) + "/xmlrpc";
//					ContextDomain cdInstance = StaticContext.getOfficeDomain(reference, i);
//					long beginOne = System.currentTimeMillis();
//					cds.register(cdInstance);//.register(IPPort);	
//					long endOne = System.currentTimeMillis();
//					long psgRegistrationTime = endOne - beginOne;
//					System.out.println("[PSG Registration Time]: "+psgRegistrationTime);
//				}
//				for(; i<quantity*4; i++) {
//					String reference = "http://" + defaultIP + ":" + (13001 + i) + "/xmlrpc";
//					ContextDomain cdInstance = StaticContext.getHomeDomain(reference, i);
//					long beginOne = System.currentTimeMillis();
//					cds.register(cdInstance);//.register(IPPort);	
//					long endOne = System.currentTimeMillis();
//					long psgRegistrationTime = endOne - beginOne;
//					System.out.println("[PSG Registration Time]: "+psgRegistrationTime);
//				}
//				for(; i<quantity*5; i++) {
//					String reference = "http://" + defaultIP + ":" + (13001 + i) + "/xmlrpc";
//					ContextDomain cdInstance = StaticContext.getClinicDomain(reference, i);
//					long beginOne = System.currentTimeMillis();
//					cds.register(cdInstance);//.register(IPPort);	
//					long endOne = System.currentTimeMillis();
//					long psgRegistrationTime = endOne - beginOne;
//					System.out.println("[PSG Registration Time]: "+psgRegistrationTime);
//				}
			}
			else if(command.equals("Q"))
			{
				// Case 1: discrete 5~ 50
//				size++;
				size = 1;
				int queryNumber = size;
				// Case 2: continuous 1~10
//				String[] cmdArr = command.split("_");
//				int queryNumber = Integer.parseInt(cmdArr[1]);
//				int size = queryNumber / 5;
//				int remainder = queryNumber % 5;
//				if(remainder != 0 && remainder >= DEVICE_ID ) {
//					size++;
//				}
				
				String directory = "D:\\Experiment\\" + defaultIP + "\\version-3.1-"+queryNumber;
				ExportData.setDir(directory);
				
				// Emulate different number of queries
				TestQuery[] psgArray = new TestQuery[size]; 
				for(int i=0; i<size; i++) {
					psgArray[i] = new TestQuery(defaultMac, defaultIP,i);
				}
				ExecutorService threadExecutor = Executors.newCachedThreadPool();
				for(int i=0; i<size; i++) {
					threadExecutor.execute(psgArray[i]);
				}
				threadExecutor.shutdown();

			}				
			else if(command.equals("L")){
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
				//				writeLog("leave",""+psgLeaveTime);
			} else if(command.equals("A")) 	{
				//				break;
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

	// write experiment data to txt
	public static void writeLog(String fileName, String content) {
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

	// main method
	public static void main(String[] args) {
		//		// Step 1: register PSG device with server
		//		String myIP = PSGConfiguration.defaultIP;
		//		int myPort = 16000;
		//		String message = myIP + ":" + myPort;
		//		String serverIP = "";
		//		int serverPort = 16001;
		//		(new PSGSender(serverIP, serverPort)).send(message);

		// Step 2: wait for server command
		new PSGReceiver(16000);
	}

}
