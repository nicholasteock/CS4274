/**
 * @modifiedBy Ivan
 * @date 12 Jul 2012
 * @action
 * 1) remove the dependency with ConnectionDB.java
 * @description
 * 1) This class simply handles remote method invocation and network
 * references are given as parameters. As a result, this class will 
 * work as an independent class for connector usage only.
 * 2) Since it is an individual class as facility, all methods can
 * be designed as static
 * */
package psg.kernel.connection;

import kernel.network.client.UDPClient;

public class NetworkConnector {
	// data
	// private ConnectionManager myDiscoverer;
	// constructor
	public NetworkConnector() {
		// this.myDiscoverer = myDiscoverer;
	}

	// methods
	public static String getCSGReference(String csgName, String csmReference) {
		// Ask CSM for CSG address
		Object[] params = new Object[] { new String(csgName) };
		// Retrieve CSG address from CSM
		UDPClient csmClient = new UDPClient(csmReference);
		return (String) csmClient.execute("CSGManager.getCSGReference", params);
	}

	public static String getSCReference(String csgName, String scName,
			String csgReference) {
		Object[] params = new Object[] { new String(csgName),
				new String(scName) };
		UDPClient csgClient = new UDPClient(csgReference);
		return (String) csgClient
				.execute("CSGGenerator.getSCReference", params);
	}

	public static String registerPSGToSC(String psgName, String csgName,
			String scName, String valueType, String value, String psgReference,
			String scReference) {
		Object[] params = new Object[] { new String(psgName),
				new String(csgName), new String(scName), new String(valueType),
				new String(value), new String(psgReference) };
		UDPClient scClient = new UDPClient(scReference);
		return (String) scClient.execute("SCGenerator.registerPSG", params);
	}

	// Ivan, 25 Feb 2014: update psg reference
	public static String updatePSGReference(String csgName, String scName,
			String psgName, String psgReference, String scReference) {
		Object[] params = new Object[] { new String(csgName),
				new String(scName), new String(psgName),
				new String(psgReference) };
		UDPClient scClient = new UDPClient(scReference);
		return (String) scClient.execute("SCGenerator.updatePSGReference",
				params);
	}

	// end

	public static String leavePSG(String psgName, String csgName,
			String scName, String valueType, String value, String scReference) {
		Object[] params = new Object[] { new String(psgName),
				new String(csgName), new String(scName), new String(valueType),
				new String(value), };
		UDPClient scClient = new UDPClient(scReference);
		return (String) scClient.execute("SCGenerator.deletePSG", params);
	}

	public static void leavePSG(String psgName, String csgName, String scName,
			String scReference) {
		Object[] params = new Object[] { new String(psgName),
				new String(csgName), new String(scName) };
		UDPClient scClient = new UDPClient(scReference);
		scClient.execute("SCGenerator.deletePSG", params);
	}

	// Initiate Connection with neighbour
	public static void registerNeighbor(String csgName, String scName,
			String selfName, String selfReference, String neighborReference) {
		// System.out.println("[NetworkConnector.registerNeighbor].csgName: " +
		// csgName);
		// System.out.println("[NetworkConnector.registerNeighbor].scName: " +
		// scName);
		// System.out.println("[NetworkConnector.registerNeighbor].selfName: " +
		// selfName);
		// System.out.println("[NetworkConnector.registerNeighbor].selfReference: "
		// + selfReference);
		// System.out.println("[NetworkConnector.registerNeighbor].neighborReference: "
		// + neighborReference);
		// System.out.println("[NetworkConnector.registerNeighbor].");
		// Ivan, 13 Jul 2012: simulation and testing reason
		String[] neighborInfor = neighborReference.split("_");
		String receiverName = neighborInfor[0];
		String receiverReference = neighborInfor[1];
		// System.out.println("[NetworkConnector.registerNeighbor].receiverName: "
		// + receiverName);
		// System.out.println("[NetworkConnector.registerNeighbor].receiverReference: "
		// + receiverReference);
		Object[] params = new Object[] { new String(receiverName),
				new String(csgName), new String(scName), new String(selfName),
				new String(selfReference) };
		UDPClient neighborClient = new UDPClient(receiverReference);
		neighborClient.execute("RequestsManager.registerNeighbour", params);
	}

	public static String queryMCS(String querySignature, String queryString,
			String issuerReference, String qpReference) {
		Object[] params = new Object[] { new String(querySignature),
				new String(queryString), new String(issuerReference) };
		UDPClient qpClient = new UDPClient(qpReference);
		return (String) qpClient.execute("QPManager.queryContext", params);
	}

	public static void queryP2P(String querySignature, String queryString,
			String destinationReference, String scName, String neighborReference) {
		// Ivan, 13 Jul 2012: for simulation and testing purpose,
		// use receiver reference as receiver name and add in here
		String receiverName = neighborReference;
		Object[] params = new Object[] { new String(receiverName),
				new String(querySignature), new String(queryString),
				new String(destinationReference), new String(scName) };
		UDPClient neighborClient = new UDPClient(neighborReference);
		neighborClient.execute("RequestsManager.queryP2P", params);
	}

	// Support RDF Query Currently, Find and Contact CP
	public static String reportResult(String querySignature,
			String queryAnswer, String qpReference) {
		// Make a regular call
		Object[] params = new Object[] { new String(querySignature),
				new String(queryAnswer) };
		// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"\n<Remote Report Results Call Request>");
		UDPClient qpClient = new UDPClient(qpReference);
		// Ivan, 20 SEP 2013: report result to intermediate processor
		// begin
		return (String) qpClient.execute("QPManager.reportContext", params);
		// end
		// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"  Request : From "+requestorAddress+" PSG, report results to PSG "+ID+" response: "+"\n  Response: "
		// + response+"\n");
		// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"</Remote Report Results Call Request>\n");
	}

	//
	//
	// // Ivan, 17 Jul 2012: all the following methods are not used, but kept
	// for future reference
	// // Ivan, 12 Jul 2012: not sure the purpose of this method yet
	// // Support RDF Query Currently, Find and Contact CP
	// public void queryMCS(String querySignature, String queryString, String
	// destinationReference, String csgName, String scName, String reference) {
	// Object[] params = new Object[] {new String(querySignature), new
	// String(queryString),
	// new String(destinationReference), new String(csgName), new String(scName)
	// };
	// //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"\n<Remote QueryMCS Call Request>");
	// UDPClient client = new UDPClient(reference);
	// client.execute("PSGManager.queryMCS", params);
	// //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"  Request : From "+csgName+" CSG, get reg CP "+id+" response: "+"\n  Response: "
	// + response+"\n");
	// //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"</Remote QueryMCS Call Request>\n");
	// }
	//
	//
	//
	//
	//
	//
	// // // Ivan, 11 May 2012
	// // // return format:
	// // // "http://"+""+metadata.getIP() + ":" +
	// metadata.getListeningPort()+"/xmlrpc";
	// // // or: "[CSG NOT FOUND]"+":"+CSGGenAddress(IP:Port);
	// // public String getCSGRef(String csgName){
	// // String response="ERROR: GetCoordinator Peer\n";
	// // try{
	// // // Make a regular call
	// // Object[] params = new Object[] { new String(csgName)};
	// //
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"<Get Coordinator Peer>");
	// // response = (String)
	// myDiscoverer.getCSMClient().execute("CsgManager.getCSGReference",
	// params);
	// //
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From CSM, get reg CSG of "+csgName+" response: "+"\nResponse: "
	// + response+"\n");
	// //
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"</Get Coordinator Peer>");
	// // } catch (Exception e){
	// //
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: GetCoordinator Peer");
	// // }
	// // return response;
	// // }
	//
	// // public String getCoordinatorPeerSystem(String csgName, String scName,
	// String csgReference) {
	// // String response="ERROR: GetCoordinator Peer\n";
	// //// try{
	// // // Make a regular call
	// // Object[] params = new Object[] { new String(csgName), new
	// String(scName)};
	// // //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"<Get Coordinator Peer System Side>");
	// //
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"GCPS --------> "+csgName+"  @ "+scName);
	// // UDPClient csgClient = new UDPClient(csgReference);
	// // return (String)
	// csgClient.execute("CPManager.getCoordinatorPeerSystem", params);
	// // //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+csgName+" CSG, get reg CP of "+scName+"@"+csgName+" response: "+"\nResponse: "
	// + response+"\n");
	// // //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"</Get Coordinator Peer System Side>");
	// //// return response;
	// //// }
	// //// catch (Exception e){
	// //// //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: GetCoordinator Peer");
	// //// return "[SC NOT FOUND]";
	// //// }
	// // }
	// //
	// // public String getCoordinatorPeer(String name, String csgName,
	// // String scName, String myIP, int listeningPort, String qSignature,
	// String csgReference) {
	// //
	// // String response="ERROR: GetCoordinator Peer\n";
	// // try{
	// // // Make a regular call
	// // Object[] params = new Object[] { new String(csgName), new
	// String(scName), new String(myIP),
	// // new Integer(listeningPort), new String(qSignature)};
	// // //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"<Get Coordinator Peer>");
	// // UDPClient csgClient = new UDPClient(csgReference);
	// // response = (String) csgClient.execute("CPManager.getCoordinatorPeer",
	// params);
	// // //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+csgName+" CSG, get reg CP of "+scName+"@"+csgName+" response: "+"\nResponse: "
	// + response+"\n");
	// // //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"</Get Coordinator Peer>");
	// //
	// //// String result = response;
	// // String result = response.substring(response.indexOf(":")+1,
	// response.length());
	// // if (response.startsWith("ROUTING QUERY THROUGH RING")){
	// // int i=0;
	// // int maxT = psg.config.PSGConfiguration.timeOutForQuery/20;
	// //// while
	// ((((McsAPI)PSGFacade.allPSGs.get(name)).myRequests.getCPResults(qSignature).size()==0)
	// &&(i<maxT)){
	// //// Thread.currentThread().sleep(20);
	// //// i++;
	// //// }
	// // if (i==maxT) {
	// // return "[SC NOT FOUND]:"+result;
	// // }
	// //
	// // return ""; // need to be add in later, Ivan 11 May 2012
	// //// return (String) ((McsAPI) PSGFacade.allPSGs.get(name)).myRequests
	// //// .getCPResults(qSignature).getResults().get(0);
	// // } else { // Most Likely won't reach here
	// // return "[SC NOT FOUND]:"+ result;
	// // }
	// // } catch (Exception e){
	// //
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: GetCoordinator Peer");
	// // return "[SC NOT FOUND]";
	// // }
	// // }
	//
	//
	// public String createSC(String csgName, String scName, String
	// scGenAddress) throws Exception{
	// Object[] params = new Object[] { new String(csgName), new
	// String(scName)};
	// UDPClient scGenClient = new UDPClient(scGenAddress);
	// return (String) scGenClient.execute("PSGManager.createSC", params);
	// }
	//
	// public String createCSG(String csgName, String csgGenAddress) throws
	// Exception{
	// Object[] params = { new String(csgName)};
	// UDPClient csgGenClient = new UDPClient(csgGenAddress);
	// return (String) csgGenClient.execute("CPManager.createCSG", params);
	// }
	//
	// public void registerPSGToCSG(String psgID, String csgName, String
	// psgReference, String csgReference) {
	// Object[] params = new Object[] { new String(psgID), new
	// String(psgReference),
	// new String(csgName) };
	// UDPClient csgClient = new UDPClient(csgReference);
	// csgClient.execute("CPManager.registerPSGToCSG", params);
	// }
	//
	//
	//
	//
	//
	// // Remote method in CSM
	// public String registerCSG(String csgName, String csgReference, String
	// csmReference) {
	// // make the a regular call
	// Object[] params = new Object[] { new String(csgName), new
	// String(csgReference) };
	// UDPClient csmClient = new UDPClient(csmReference);
	// return (String) csmClient.execute("CsgManager.registerCSG", params);
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Register CSG: "
	// + result1);
	// // return result;
	// }
	//
	// // Remote method in CSM
	// public String registerCSG(String parentCSG, String csgName, String
	// csgReference, String csmReference) {
	// // make the a regular call
	// Object[] params = new Object[] { new String(parentCSG), new
	// String(csgName),
	// new String(csgReference) };
	// //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+parentCSG+"; "+csgName
	// + "; "+ requestorIP+":"+requestorPort);
	// UDPClient csmClient = new UDPClient(csmReference);
	// return (String) csmClient.execute("CsgManager.registerCSG", params);
	// //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Register PARENT CSG: "
	// + result1);
	// // return result1;
	// }
	//
	//
	//
	// public void registerSCGen(String scGenReference, String csgGenReference)
	// {
	// // try{
	// // Make a regular call
	// Object[] params = new Object[] { new String(scGenReference)};
	// UDPClient csgGenClient = new UDPClient(csgGenReference);
	// csgGenClient.execute("SCGenManager.registerSCGen", params);
	// //
	// System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+csgName+" CSG, get reg SCGen response: "+"\nResponse: "
	// + psgResponse+"\n");
	// // } catch (Exception e){
	// //
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: Cannot register SCGen");
	// // }
	// }
	//
	// public String registerSC(String csgName, String scName, String
	// scReference, String csgReference){
	// // try{
	// // Make a regular call
	// Object[] params = new Object[] { new String(csgName), new String(scName),
	// new String(scReference)};
	// UDPClient csgClient = new UDPClient(csgReference);
	// return (String) csgClient.execute("CPManager.registerCP", params);
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Request : From "+csgName+" CSG, get reg CP response: "+"\nResponse: "
	// + psgResponse+"\n");
	// // } catch (Exception e){
	// //System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"Error: Cannot register CP");
	// // }
	// }
	//
	// // public String registerPSG(String name, String csgName,
	// // String scName, String requestorIP, int requestorPort, String myName) {
	// // Object[] params = new Object[] { new String(csgName), new
	// String(scName),
	// // new String(requestorIP), new Integer(requestorPort), new
	// String(myName) };
	// //// return (String)
	// myDiscoverer.getCPClient(scName).execute("PSGManager.registerPSG",
	// params);
	// // return (String)
	// myDiscoverer.getSCClient(scName).execute("PSGManager.registerPSG",
	// params);
	// // }
	//
	// // public String registerPSG(String parentCSG, String name, String
	// csgName,
	// // String scName, String requestorIP, int requestorPort, String myName) {
	// // Object[] params = new Object[] { new String(csgName), new
	// String(scName),
	// // new String(requestorIP), new Integer(requestorPort), new
	// String(myName) };
	// //// return (String)
	// myDiscoverer.getCPClient(csgName+"@"+scName).execute("PSGManager.registerPSG",
	// params);
	// // return (String)
	// myDiscoverer.getSCClient(csgName+"@"+scName).execute("PSGManager.registerPSG",
	// params);
	// // }
	//
	//

}
