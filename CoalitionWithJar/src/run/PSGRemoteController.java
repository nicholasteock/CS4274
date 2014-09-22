package run;

import java.util.List;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class PSGRemoteController {
	// data
	public static List<String> psgList = new Vector<String>();
	// constructor
	
	// methods
	public static void main(String[] args) throws Exception {
		// add in all PSGs
		psgList.add("172.29.33.235:16000");
		psgList.add("172.29.33.99:16000");
		psgList.add("172.29.33.141:16000");
		
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
			if(cmd.equalsIgnoreCase("Q")) {
				for(int i=0; i<1; i++) {
					sendCommand(cmd);
					// Case 1: discrete number
					Thread.sleep(10000);
				}
			} else {
	        	sendCommand(cmd);
			}

    	}
	}
	
	
	// sending commands
	private static void sendCommand(String command) {
		System.out.println("[PSGRemoteController.sendCommand].command: " + command);
//		List<String> psgList = CoalitionReceiver.psgList;
//		System.out.println("[PSGRemoteController.sendCommand].psgList: " + psgList.size());
		for(String psg : psgList) {
			System.out.println("[PSGRemoteController.sendCommand].psg: " + psg);
			String[] address = psg.split(":");
			String receiverIP = address[0];
			int receiverPort = Integer.parseInt(address[1]);
			CoalitionSender udpSender = new CoalitionSender(receiverIP, receiverPort);
			udpSender.send(command);
		}
 	}
	
	
	//get updating alert
//	public String getCurrentIp () throws UnknownHostException{
//		String IP = ""+InetAddress.getLocalHost();
//		System.out.println("The current IP is: "+IP);
//		return IP;
//	}
	
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
