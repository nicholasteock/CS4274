package proxy.kernel.server;

import java.net.Socket;
import java.util.List;
import java.util.Vector;

import proxy.config.MPSGConfiguration;
import proxy.config.MPSGContext;
import proxy.kernel.connection.ConnectionManager;
import proxy.query.processor.PSGQueryObject;
import proxy.query.processor.PSGQueryParser;
import proxy.service.manager.ContextDataService;
import proxy.service.manager.ContextDomain;


// TODO: Auto-generated Javadoc
/**
 * The Class RequestsManager.
 */
public class RequestsManager {
	// data
//	private static Hashtable memory;
//	private static Vector queryQueue;
	/** The memory. */
//	private static Map<String, PSGQueryProcessor> qpManager = new Hashtable<String, PSGQueryProcessor>();
	private static List<String> memory = new Vector<String>();
	
	/** The mcs api. */
	private ConnectionManager mcsAPI;
//	private ContextDataService smTemp;
	// constructor
	public RequestsManager() {
		this.mcsAPI = new ConnectionManager();
	}
	// methods
	/**
	 * Testcase1.
	 *
	 * @param tName the t name
	 * @param s1 the s1
	 * @param s2 the s2
	 * @return the string
	 * @throws Exception the exception
	 */
	private String testcase1(String tName, String s1, String s2) throws Exception{
		System.out.println("<RequestsManager> Testcase 1 </RequestsManager>\n");
		return "Physical Space 1: "+tName;
	}

	/**
	 * Testcase2.
	 *
	 * @param tName the t name
	 * @return the string
	 */
	private String testcase2(String tName){
		return "4fdsaf Physical Space 1: "+tName;
	}

	// Ivan, 13 Jul 2012: begin
	// called by SC to update value
	/**
	 * Request for value.
	 *
	 * @param psgName the psg name
	 * @param scName the sc name
	 * @return the string
	 */
	public String requestForValue(String psgName, String scName) {
		// Step 1: identify Context Domain instance
		ContextDataService cds = new ContextDataService();
		ContextDomain cdInstance = cds.getContextDomain(psgName);
		
		// Step 2: get value
		return cdInstance.getAttributeValue(scName);
	}
	
	// Ivan, 17 Jul 2012:
	public String refreshNeighbors(String psgName, String scName, String neighbors) {
		// Step 1: extract neighbor information
		String[] neighborList = neighbors.split("@");
		
		// Step 2: reset neighbors
		mcsAPI.refreshNeighborList(psgName, scName, neighborList);
		
		// Step 3: return dummy result
		return "dummy";
	}
	
	// Call be other peers
	// Ivan, 13 Jul 2012: this neighbor registration mechanism
	// concerns only for network connection information, so actually
	// should directly call ComponentsLocator.java, but in order to 
	// be consistent with other query calling, we put it here
	// and make the calls all the down to ComponetsLocator.java
	
	// manage network level requests, Ivan, 15 May 2012
	// For registering oneself as a neighbour of the requestor peer.
	/**
	 * Register neighbour.
	 *
	 * @param psgName the psg name
	 * @param csgName the csg name
	 * @param scName the sc name
	 * @param requestorAddress the requestor address
	 */
	public String registerNeighbour(String psgName, String csgName, String scName, String requestorName, String requestorAddress) {
		mcsAPI.registerNeighbor(psgName, csgName, scName, requestorName, requestorAddress);
		
		// return dummy reply\
		return "dummy";
		// Ivan, 11 May 2012, this ID mechanism should be revised
//		String id = PSGConfiguration.defaultMAC;
//		name = "http://"+id+"/xmlrpc";
		// should use CompolentLocator.java to handle this
		// Ivan, 15 May 2012
//		return McsAPI.allNetworkConnections.get(csgName).registerNeighbor(
//				csgName, scName, requestorAddress);		
//		return null;
	}
	
	
	// Querying the local data
	/**
	 * Query local.
	 *
	 * @param psgName the psg name
	 * @param queryString the query string
	 * @return the string
	 */
	private String queryLocal(String psgName, String queryString){
		/**
		 * Added by Sathiya
		 * updateResult has the result of the update of dynamic attributes
		 */
		System.out.println("Invoking updateAttributes");
		boolean updateResult = MPSGContext.updateAttributes(psgName, queryString);
		Socket sock = (Socket) MPSGConfiguration.socketList.get(psgName);
		System.out.println("In query local, after update attrib: " + sock.isClosed());
		String result;
		
		// If update result was false, then return with QUERY MISS
		if (!updateResult) {
			/**
			 * Added by Sathiya
			 */
			// If there was a close request for the MPSG, close it now
			//MPSGContext.removeMPSG(psgName);
			return "[QUERY MISS]";
		}
		
		// Step 1: locate the cd instance
		ContextDataService cds = new ContextDataService();
//		cds.print();
		System.out.println("[RequestsManager.queryLocal].psgName:" + psgName);
		ContextDomain cdInstance = cds.getContextDomain(psgName);
		System.out.println("[RequestsManager.queryLocal].psgName:" + cdInstance.getName());
		
		
		// Step 2: parse the query
//		QueryParser queryProcessor = new QueryParser(queryString);
		double timeBegin = System.currentTimeMillis();
		System.out.println("[RequestsManager.queryLocal].queryString: " + queryString);
		PSGQueryObject queryObject = PSGQueryParser.parseQuery(queryString);
		double timeEnd = System.currentTimeMillis();
//		System.out.println("[RequestsManager.queryLocal].Query parsing time: " + (timeEnd - timeBegin));
		
		// Step 3: generte the result
		if ((!queryObject.hasError() && cdInstance.evaluateQueryCondition(queryObject) ) ) {
			System.out.println("[RequestsManager.queryLocal].if: true");
			result = cdInstance.getQueryResult(queryObject);
		} else {
			System.out.println("[RequestsManager.queryLocal].if: false");
			result =  "[QUERY MISS]";	
//			return null;	
		}
		/**
		 * Added by Sathiya
		 */
		// If there was a close request for the MPSG, close it now
		//MPSGContext.removeMPSG(psgName);
		return result;
	}
	
	/**
	 * Query p2 p.
	 *
	 * @param psgName the psg name
	 * @param querySignature the query signature
	 * @param queryString the query string
	 * @param destinationReference the destination reference
	 * @param scName the sc name
	 */
//	public String queryP2P(String psgName, String querySignature,
//			String queryString, String destinationReference, String scName) {
//		// Step 1: check occurence of the query
//		String localQueryKey = psgName + "#" + querySignature;
//		if(memory.contains(localQueryKey)) {
//			// Same query has been here
//			return "[Same query has been here!]";
//		} else {
//			try {
//				return "dummy";
//			} catch(Exception e) {
//				e.printStackTrace();
//				return "dummy";
//			} finally {
//				// Step 2: add in memory
//				memory.add(localQueryKey);
//
//				// Step 3: forward to others
//				mcsAPI.queryP2P(psgName, querySignature, queryString, destinationReference, scName);
//				
//				// Step 4: process query and report result
//				
//				long beginOne = System.currentTimeMillis();
//				String queryResult = queryLocal(psgName, queryString);
//				long endOne = System.currentTimeMillis();
//				long psgLocalProcessingTime = endOne - beginOne;
//
//				ExportData.writeLog("psg_p2p_local_time",""+psgLocalProcessingTime);
////				System.out.println("[RequestsManager.queryP2P].queryResult: " + queryResult);
//				
//				if(queryResult != null && (!queryResult.equals("[QUERY MISS]"))) {
//					mcsAPI.reportResult(querySignature, queryResult, destinationReference);
//				}
//
//				
//			}
//			//			return "dummy";
//		}
//	}
//	
//	// end
	

	// Ivan, 25 SEP 2013: instead of P2P query, query directly 
	public String queryPSG(String psgName, String querySignature,
			String queryString, String destinationReference, String scName) {
		
		System.out.println("Got a incoming query. Arguments: psgName:" + psgName + "querSignature:" + querySignature
						+ "queryString:" + queryString + "destinationReference:" + destinationReference + "scName:"+ scName);
		
		// Step 1: check occurence of the query
		String localQueryKey = psgName + "#" + querySignature;
//		ExportData.writeLog("psg_first_query", "" + System.currentTimeMillis());
		if(memory.contains(localQueryKey)) {
			// Same query has been here
			return "[Same query has been here!]";
		} else {
			// Step 2.5 : return and then finally
			try {
				return "dummy";
			} catch(Exception e) {
				e.printStackTrace();
				return "dummy";
			} finally {

				// Step 2: add in memory
				memory.add(localQueryKey);
				
				// Step 3: forward to others
//				mcsAPI.queryP2P(psgName, querySignature, queryString, destinationReference, scName);
				
				// Step 4: process query and report result
				long beginOne = System.currentTimeMillis();
				System.out.println("Calling queryLocal with parameters, psgName:" + psgName + "queryString:" + queryString);
				String queryResult = queryLocal(psgName, queryString);
				long endOne = System.currentTimeMillis();
				long psgLocalProcessingTime = endOne - beginOne;

//				ExportData.writeLog("psg_local_time",""+psgLocalProcessingTime);
				System.out.println("[RequestsManager.queryPSG].queryResult: " + queryResult);
				
				// Ivan, 4 Dec 2013: synchronous transmission in application level
				if(queryResult != null && (!queryResult.equals("[QUERY MISS]"))) {
//					ExportData.writeLog("psg_real_result", queryResult);
					boolean resend = true;
					while(resend) {
						String result = mcsAPI.reportResult(querySignature, queryResult, destinationReference);
						if(result.equals("[confirmed]")) {
							resend = false;
//							ExportData.writeLog("psg_report_reply_result", psgName + "___" + querySignature + "__" + result);
						}
					}
				}
				
//				if(queryResult != null && (!queryResult.equals("[QUERY MISS]"))) {
//					String mcsAPI.reportResult(querySignature, queryResult, destinationReference);
//				}

				
			}
			//		return "dummy";
		}
	}
	
	// end
	
	// Ivan, 18 Jul 2012: test method
	public static void main(String[] args) {
		RequestsManager rm = new RequestsManager();
//		http://localhost:13001/xmlrpc
//		http://localhost:13002/xmlrpc
//		String psgName = "http://localhost:13002/xmlrpc";
//		String queryString = "select person.name, person.location from person where person.location = \"IDMI\"";
//		rm.queryLocal(psgName, queryString);
//		ContextDataService cds = new ContextDataService();
//		cds.print();
	}
	
	

	
	

	
	
	// end of file
}
