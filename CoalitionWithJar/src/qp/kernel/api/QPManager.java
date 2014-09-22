/**
 * @author Ivan
 * @date 3 Jul 2012
 * @description
 * 1) This class is designed as interface to receive context queries from
 * PSGs and to receive context query results from PSGs.
 * 2) This class also takes charge of managing various QueryProcessor instances.
 * This class create an individual QueryProcessor instance for each query.
 * */
package qp.kernel.api;

import java.util.Map;
import java.util.Hashtable;

import qp.query.parser.QPQueryObject;
import qp.query.processor.PlanManager;

public class QPManager {
	// data
	private static Map<String, QueryProcessor> qpList = new Hashtable<String, QueryProcessor>();
	// constructor
	public QPManager() {}	// dummy constructor, usually, interface class use dummny constructor
	// methods
	// query context
	public String queryContext(String querySignature, String contextQuery, String issuerReference) {
//		System.out.println("[QPManager.queryContext].contextQuery: " + contextQuery);
		PlanManager planManager = new PlanManager(querySignature, contextQuery, issuerReference);
		return planManager.executeQuery();
	}
	
	// this one is not called remotely by other computing module, but as a local QueryProcessor manager for 
	// processor tree leaves
	public String queryRawContext(String querySignature, QPQueryObject contextQuery, String issuerReference) 
			{
		QueryProcessor qpInstance = 
				new QueryProcessor(querySignature, contextQuery, issuerReference);
//		System.out.println("[QPManager.queryRawContext].querySignature: " + querySignature);
		qpList.put(querySignature, qpInstance);
		return qpInstance.queryMCS();
	}
	
	public Hashtable<String, String> retrieveRawContext(String querySignature) {
//		System.out.println("[QPManager.retrieveRawContext]: confirmed!");
		return qpList.get(querySignature).getResult();
	}
	
	// report result
	public String reportContext(String querySignature, String context){
//		System.out.println("[QPManager.reportContext].querySingature: " + querySignature);
		return qpList.get(querySignature).reportResult(context);
	}
	
}
