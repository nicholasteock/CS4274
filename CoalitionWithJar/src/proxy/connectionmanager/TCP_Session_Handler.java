package proxy.connectionmanager;

//import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import proxy.config.MPSGConfiguration;
import proxy.config.MPSGContext;
import proxy.starter.MPSGStarter;

public class TCP_Session_Handler {

	public void RegisterMPSG(final Socket socket) {
		long mpsgRegisterStart = System.currentTimeMillis();
		MPSGConfiguration mpsgConfig = new MPSGConfiguration();
		
		// Send request to MPSG asking for name
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		} catch (IOException e) {
			System.out.println("Unable to get write socket");
			e.printStackTrace();
		}
		out.println("send name and context");
		
		// Wait for response from MPSG 
		BufferedReader in = null;
		int timeout = 5; // add 5 seconds as timeout for MPSG to respond
		try {
			//in  = new BufferedReader(socket.getInputStream());
			in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			int i = 0;
			while(i < timeout) { // wait for name from MPSG
				@SuppressWarnings("deprecation")
				String line = in.readLine();
				if (line != null) {
					System.out.println("Got some data from MPSG: "  + line + ".");
					if (line.startsWith("close")) {
						String temp[]= line.split(":");
						closeMPSG(temp[1]+"::"+temp[2], socket);
					}
					if (line.startsWith("reconnect")) {
						String temp[] = line.split(";");
						final String name = temp[1];
						final String contextType = temp[1];
						final String contextData = temp[2];
						if (mpsgConfig.updateMPSG(name, socket.getLocalAddress(), socket, in) == 0) {
							Thread startMPSG = new Thread() {
								public void run() {
									MPSGStarter mpsgStarter = new MPSGStarter();
									mpsgStarter.updateMPSG_Coalition(name, contextType, contextData);
								}
							};
							startMPSG.start();
							break;
						}
					}
					String temp[] = line.split(";");
					final String name = temp[0];
					final String contextType = temp[1];
					final String contextData = temp[2];

					// Register or update the MPSG in Proxy
					System.out.println("Updating received data in static information");
					if (mpsgConfig.updateMPSG(name, socket.getLocalAddress(), socket, in) == 0) {
						System.out.println("[EXPERIMENTAL_RESULTS]::[Registration Time with Proxy = " + 
								Math.abs(System.currentTimeMillis() - mpsgRegisterStart) + "]");
						Thread startMPSG = new Thread() {
							public void run() {
								MPSGStarter mpsgStarter = new MPSGStarter();
								System.out.println("Starting MPSG registration with coalition");
								long registerStart = System.currentTimeMillis();
								mpsgStarter.registerMPSG_Coalition(name, contextType, contextData);
								System.out.println("[EXPERIMENTAL_RESULTS]::[Registration Time with Coalition = " + 
										Math.abs(System.currentTimeMillis() - registerStart) + "]");
							}
						};
						startMPSG.start();
					    break;
					}
					i++;
				}
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error in sleep in register MPSG");
				}
			}
			
			//System.out.println("Socket status: " +socket.isClosed());
			// Start a thread which listens on the socket for any incoming query requests
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error in getting name from MPSG");
		}
	}

	public static void replyRegister(String name, String reply) {
		Socket socket = (Socket) MPSGConfiguration.socketList.get(name);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		} catch (IOException e) {
			System.out.println("Unable to get write socket");
			MPSGContext.mpsgStateInfo.put(name, "disconnected");
			e.printStackTrace();
		}
		out.println(reply);
	}
	
	public static boolean GetUpdates(String name, String request) {
		System.out.println("Into getupdates with name:" + name + ", request: " + request);
		boolean result = false;
		
		Socket socket = (Socket) MPSGConfiguration.socketList.get(name);
		System.out.println("Into getupdates" + socket.isClosed());
		BufferedReader in = (BufferedReader) MPSGConfiguration.instr.get(name);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		} catch (IOException e) {
			System.out.println("Unable to get write socket");
			e.printStackTrace();
		}
		out.println(request);
		
		System.out.println("sent update request" + socket.isClosed());
		MPSGContext.updated = false;
		// Wait for response from MPSG 
		//DataInputStream in = null;
		int timeout = 5; // add 5 seconds as timeout for MPSG to respond
		try {
			int i = 0;
			while(i < timeout) { // wait for update from MPSG
				System.out.println("Waiting for update from MPSG " + socket.isClosed());
				if (MPSGContext.updated) {
					System.out.println("Got update info from MPSG");
					return true;
				}
				Thread.sleep(1000);
			}
			System.out.println("After update context" + socket.isClosed());
			
			// Check if socket connection is alive
			String initstatus = (String) MPSGContext.mpsgStateInfo.get(name);
			if (initstatus.equals("disconnected") || initstatus.equals("closed") || initstatus.equals("closed")) {
				// Old socket is no longer valid
				// Check for MPSG state changes 
				int j = 0;
				while (j < 10) { // Wait for 10 seconds before MPSG reconnects
					String status = (String) MPSGContext.mpsgStateInfo.get(name);
					if (status.equalsIgnoreCase("active") || status.equalsIgnoreCase("closing")) {
						continue;
					}
					if (status.equalsIgnoreCase("closed")) {
						result = false; // report failure for query as MPSG deregistered from Coalition
					}
					if (status.equalsIgnoreCase("closedwithupdate")) {
						// Get the latest update from MPSGContext
						if (MPSGContext.mpsgLastUpdate.containsKey(name)) {
							String update = (String) MPSGContext.mpsgLastUpdate.get(name);
							if (update != null) {
								String temp[] = update.split("::");
								MPSGContext.updateContext(name, temp[1]);
								result = true;
							} else {
								result = false;
							}
						}
					}
					j++;
				}
				if (j >= 10) {
				//	in.close();
					return false;
				}
			}
			//in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in reading update from MPSG");
		}
		
		return result; // returns the result of the update
	}
	
	public static void mpsgListener(String nameinput, Socket socket) {
		System.out.println("Starting MPSG listener");
		final String name = nameinput;
		BufferedReader in = (BufferedReader) MPSGConfiguration.instr.get(name);
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		} catch (IOException e) {
			System.out.println("Unable to get write socket");
			e.printStackTrace();
		}

		try {
			String line = null;
			while (true) {
				// Stop the thread once the socket is found to be disconnected
				if (MPSGContext.mpsgStateInfo.get(name).toString().equalsIgnoreCase("disconnected")) {
					return;
				}
				line = in.readLine();
				if (line != null) {
					//(!MPSGContext.mpsgStateInfo.get(name).toString().equalsIgnoreCase("closed")) && (
					//System.out.println("Got some data for query listener: "  + line + ".");
					if (line.startsWith("leaveCoalition")) {
						closeMPSG(name+"::"+"nonewproxy", socket);
						// Wait for status to change to closed, and then reply to MPSG
						int i = 0;
						int timeout = 10; 
						/*while (!MPSGContext.mpsgStateInfo.get(name).toString().equalsIgnoreCase("closed")) {
							try { Thread.sleep(1000);} catch (Exception e) {}
						}*/
						System.out.println("Back from closeMPSG");
						if (MPSGContext.mpsgStateInfo.get(name).toString().equalsIgnoreCase("closed")) {
							System.out.println("Status closed");
							return; 
						} else {
							MPSGContext.mpsgStateInfo.put(name, "closed");
							System.out.println("Status closed");
							return;
						}
					}
					else if (line.contentEquals("yes there")) {
						//System.out.println("Got response from MPSG - yes there");
						MPSGConfiguration.presenceFlag = true;
					}
					else if (line.startsWith("update::")) {
						String temp[] = line.split("update::");
						MPSGContext.updateContext1(name, temp[1]);
					}
					else if (line.startsWith("empty")) {
						MPSGContext.updated = true;
					}
					else {
						String temp[] = line.split(";");
						//final String name = temp[0];
						final String query = temp[1];
		
						if (temp[1].startsWith("query:")) {
							final long startQuery = System.currentTimeMillis();
							System.out.println("Its a query request from local MPSG");
							String temp1[] = line.split(":");
							final String queryStr = temp1[1];
							// Send the query request to peer to peer network
							Socket sock = (Socket) MPSGConfiguration.socketList.get(name);
							System.out.println("In query listener, socket status? : " + sock.isClosed());
							Thread sendQuery = new Thread() {
								public void run() {
									MPSGStarter mpsgStarter = new MPSGStarter();
									System.out.println("Sending query request over the peer-peer network: " + queryStr);
									mpsgStarter.sendQuery(name, queryStr, startQuery);
								}
							};
							sendQuery.start();
						}
					}
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Error in MPSG socket listener");
			return;
		}
	}
	
	/** 
	 * Reply to MPSG about query reply
	 */
	public static void replyQuery(String name, String reply) {
		System.out.println("Into reply query, name & reply" + name + "," + reply);
		if(reply.contains("\n")) {
			String temp[] = reply.split("\n");
			reply = "";
			for(int i=0; i <temp.length; i++) {
				reply += temp[i];
				if(i != temp.length - 1)
					reply += ",";
			}
			
			
		}
		System.out.println("REPLY: " + reply);
		Socket sock = (Socket) MPSGConfiguration.socketList.get(name);
		System.out.println("status in replyquery, socket isclsed? : " + sock.isClosed());
		Socket socket = (Socket) MPSGConfiguration.socketList.get(name);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		} catch (IOException e) {
			System.out.println("Unable to get write socket");
			e.printStackTrace();
		}
		out.println(reply); // Format of reply "queryString:::replyString"
		// Reset state of MPSG to connected
		MPSGContext.mpsgStateInfo.put(name, "connected");
	}

	/**
	 * closeMPSG will either remove MPSG locally or even unregister it from coalition based on request
	 * @param input
	 */
	@SuppressWarnings("unchecked")
	private static void closeMPSG(String input, Socket socket) {
//		String temp[] = input.split(":");
		String args[] = input.split("::"); // args[0] is mpsg name & args[1] is new proxy information
		
		MPSGContext.mpsgStateInfo.put(args[0], "closing");
		if (args[1].equalsIgnoreCase("nonewproxy")) { // Means MPSG is trying to unregister from coalition
			MPSGStarter.deregisterMPSG(args[0]);
			System.out.println("MPSG left coalition");
			MPSGContext.removeMPSG(args[0]);
			
			// Change state of MPSG to closed
			MPSGContext.mpsgStateInfo.put(args[0], "closed");
			return;
		}
		
		// Check if there was any active session in MPSG
		if (!MPSGContext.mpsgStateInfo.get(args[0]).equals("active")) {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			} 
			catch (Exception e)  {
				System.out.println("Unable to get output writer for socket:" + e.toString());
			}
			out.println("closeok");
			MPSGContext.removeMPSG(args[0]);
		} else {
			// Get the ongoing details before closing the connection
			System.out.println("Getting updates from MPSG for ongoing session");
			BufferedReader in = (BufferedReader) MPSGConfiguration.instr.get(args[0]);
			PrintWriter out = null;
			int timeout = 5; // add 5 seconds as timeout for MPSG to respond
			try {
				//in = new DataInputStream(socket.getInputStream());
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
				
				// Send update query to MPSG
				out.println(MPSGContext.mpsgLatestQuery.get(args[0]));

				int i = 0;
				while(i < timeout) { // wait for update reply from MPSG
					String line = in.readLine();
					if (line != null) {
						if (line.startsWith("update")) {
							System.out.println("Got update response from MPSG");
							
							// Send closeok message to MPSG
							out.println("closeok");
							MPSGContext.mpsgLastUpdate.put(args[0], line);
							MPSGContext.mpsgStateInfo.put(args[0], "closedwithupdate");
							//in.close();
							// MPSG Context will be removed after query reply
							return;
						}
					}
					i++;
				}
				if (i >= timeout) {
					//in.close();
					System.out.println("No response from MPSG");
					MPSGContext.mpsgLastUpdate.put(args[0], null);
				}
				MPSGContext.removeMPSG(args[0]);
			} catch(IOException e) {
				e.printStackTrace();
				System.out.println("Error in query listener");
			}
		}
	}

	public static void socketListener (final String name) {
		Socket socket = (Socket) MPSGConfiguration.socketList.get(name);
		//BufferedReader in = (BufferedReader) MPSGConfiguration.instr.get(name);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		} catch (IOException e) {
			System.out.println("Unable to get write socket");
			e.printStackTrace();
		}

		System.out.println("MPSG name "+name + " checking for socket status. Time: " + System.currentTimeMillis());
		while (true) {
			try {
				MPSGConfiguration.presenceFlag = false;
				out.println("there?");
				int i = 0;
				while (i < MPSGConfiguration.MPSGTIMEOUT/2) { // Wait for 3 seconds for MPSG to respond
					if (MPSGConfiguration.presenceFlag) {
						//System.out.println("Got presence response from MPSG " + name);
						break;
					}
					Thread.sleep(1000);
					i++;
				} 
				if (i >= MPSGConfiguration.MPSGTIMEOUT/2) {
					System.out.println("No presence response after MPSGTIMEOUT");
					break;
				}
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			MPSGConfiguration.presenceFlag = false;
		}
		System.out.println("MPSG " + name + " socket disconnected.");

		// Change the state of the MPSG to disconnected
		MPSGContext.mpsgStateInfo.put(name, "disconnected");

		// Loop for timeout to check if the socket got reconnected, else cleanup
		int i = 0;
		System.out.println("Checking if MPSG " + name + " is reconnecting.");
		while (i < MPSGContext.MPSGTIMEOUT) {
			if (!MPSGContext.mpsgStateInfo.get(name).toString().equalsIgnoreCase("disconnected")) {
				break;
			}
			try { Thread.sleep(1000); } catch (Exception e) {}
			i++;
		}
		if (i >= MPSGContext.MPSGTIMEOUT) {
			System.out.println("Socket for MPSG " + name + " is closed.");
			closeMPSG(name+"::nonewproxy", socket);
			return;
		}
		
		// Start the socket Listener again with new socket
		Thread socklisten = new Thread() {
			public void run() {
				socketListener(name);
			}
		};
		socklisten.start();
	}
}
