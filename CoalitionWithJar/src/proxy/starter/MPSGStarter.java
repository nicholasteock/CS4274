package proxy.starter;

import java.net.Socket;

import proxy.config.MPSGConfiguration;
import proxy.config.MPSGContext;
import proxy.connectionmanager.TCP_Session_Handler;
import proxy.service.manager.ContextDataService;
import proxy.service.manager.ContextDomain;

public class MPSGStarter {
	
	static ContextDataService cds = new ContextDataService();

	@SuppressWarnings({ "unchecked", "unchecked" })
	public void registerMPSG_Coalition(String nameinput, String contextType, String contextData) {
		final String name = nameinput;
		String IP = MPSGConfiguration.ipList.get(name).toString();
		String Port = MPSGConfiguration.portList.get(name).toString();

		// Trim IP to remove any trailing and leading special characters
		if (IP.startsWith("/")) {
			IP = IP.substring(1);
		}
    	String reference = "http://"+IP+ ":"+Port+"/xmlrpc";
    	
    	System.out.println("Registering MPSG: " + name + ", Context type: " + contextType + ", Data: " + contextData);
    	System.out.println("Reference: " + reference);

    	// Add entry for new MPSG into the Context database
    	System.out.println("Adding context information into MPSGContext");
    	if (MPSGContext.createMPSGContext(name, contextType, contextData) == -1) {
    		MPSGContext.mpsgStateInfo.put(name, "init");
    		TCP_Session_Handler.replyRegister(name, "Success: Updated socket info");
    		MPSGContext.mpsgStateInfo.put(name, "connected");
    		Thread listener = new Thread() {
				public void run() {
					TCP_Session_Handler.mpsgListener(name, (Socket) MPSGConfiguration.socketList.get(name));
				}
			};
			listener.start();
    		// Existing MPSG, reconnect
    		Thread socklisten = new Thread() {
    			public void run() {
    				try { Thread.sleep(3000); } catch (Exception e) {}
    				TCP_Session_Handler.socketListener(name);
    			}
    		};
    		socklisten.start();
    		return;
    	}
    	
    	System.out.println("Getting domain info for MPSG");
    	ContextDomain cdInstance = MPSGContext.getDomainInfo(name, reference);
    	
    	
    	System.out.println("Got domain info and ContextDomain object cdInstance populated for registration");
		long beginOne = System.currentTimeMillis();
		try {
			cds.register(cdInstance);
		} catch (Exception e) {
			System.out.println("Error in registering with Coalition");
			e.printStackTrace();
			TCP_Session_Handler.replyRegister(name, "Failed");
			return;
		}
		long endOne = System.currentTimeMillis();
		long psgRegistrationTime = endOne - beginOne;
		System.out.println("[PSG Registration Time]: "+psgRegistrationTime);
		
		// Set state of MPSG to passive
		MPSGContext.mpsgStateInfo.put(name, "init");
		
		TCP_Session_Handler.replyRegister(name, "Success: Registration Time = " + psgRegistrationTime);
		MPSGContext.mpsgStateInfo.put(name, "connected");
		
		Thread listener = new Thread() {
			public void run() {
				TCP_Session_Handler.mpsgListener(name, (Socket) MPSGConfiguration.socketList.get(name));
			}
		};
		listener.start();
		
		Thread socklisten = new Thread() {
			public void run() {
				try { Thread.sleep(3000); } catch (Exception e) {}
				TCP_Session_Handler.socketListener(name);
			}
		};
		socklisten.start();
	}

	/**
	 * This function will get called when the MPSG moves into this proxy during its operation
	 * No register with Coalition, only update of reference happens
	 * @param name
	 */
	public void updateMPSG_Coalition(String nameinput, String contextType, String contextData) {
		final String name = nameinput;
		String IP = MPSGConfiguration.ipList.get(name).toString();
		String Port = MPSGConfiguration.portList.get(name).toString();

		// Trim IP to remove any trailing and leading special characters
		if (IP.startsWith("/")) {
			IP = IP.substring(1);
		}
    	String reference = "http://"+IP+ ":"+Port+"/xmlrpc";
    	
    	System.out.println("Updating MPSG: " + name + " to reference: " + reference);

    	cds.updatePsgReference(name, reference);
    	
    	// Add entry for new MPSG into the local Context database
    	System.out.println("Adding context information into MPSGContext");
    	MPSGContext.createMPSGContext(name, contextType, contextData);
    	MPSGContext.mpsgStateInfo.put(name, "init");
		
		TCP_Session_Handler.replyRegister(name, "Success: Registration Time = 0");
		MPSGContext.mpsgStateInfo.put(name, "connected");
		
		Thread listener = new Thread() {
			public void run() {
				TCP_Session_Handler.mpsgListener(name, (Socket) MPSGConfiguration.socketList.get(name));
			}
		};
		listener.start();
		
		Thread socklisten = new Thread() {
			public void run() {
				try { Thread.sleep(3000); } catch (Exception e) {}
				TCP_Session_Handler.socketListener(name);
			}
		};
		socklisten.start();
	}
	
	public void sendQuery(String name, String query, long startTime) {
		// Change state of MPSG to active
		MPSGContext.mpsgStateInfo.put(name, "active");
		Socket sock = (Socket) MPSGConfiguration.socketList.get(name);
		System.out.println("Before query sending, socket isclsed? : " + sock.isClosed());
		long beginTwo = System.currentTimeMillis();
		
		String IP = MPSGConfiguration.ipList.get(name).toString();
		String Port = MPSGConfiguration.portList.get(name).toString();

		// Trim IP to remove any trailing and leading special characters
		if (IP.startsWith("/")) {
			IP = IP.substring(1);
		}
    	String reference = "http://"+IP+ ":" + Port + "/xmlrpc";
    	
    	//String reference = "http://" + defaultIP + ":" + "13001" + "/xmlrpc";
    	//System.out.println("Before getting domain info: " + sock.isClosed());
    	ContextDomain cdInstance = MPSGContext.getDomainInfo(name, reference);
    	//System.out.println("Before getting query context: " + sock.isClosed());
    	System.out.println("Time to start processing query in Proxy: " + Math.abs(System.currentTimeMillis() - startTime));
		String result =  cds.queryContext(reference, query, cdInstance.getReference());
		//System.out.println("After getting query context: " + sock.isClosed());
		long endTwo = System.currentTimeMillis();
		long queryTime = endTwo - beginTwo;
		System.out.println("[Query Result]: "+result);
		System.out.println("[Query Time in Coalition]: "+queryTime);
		//System.out.println("Before replying for query, socket isclsed? : " + sock.isClosed());
		TCP_Session_Handler.replyQuery(name, query+":::"+result);
	}
	
	public static void deregisterMPSG(String mpsgName) {
		long begin = System.currentTimeMillis();
    	cds.withdraw(mpsgName);
    	long end = System.currentTimeMillis();
    	long psgLeaveTime = end - begin;
    	System.out.println("[PSG De-Registration Time]: "+psgLeaveTime);
	}
}
