/**
 * 1) Logically deleted
 * */

package qp.query.processor;

import experiment.data.ExportData;
import qp.kernel.api.QPManager;
import qp.query.parser.QPQueryObject;

public class QPConcurrent implements Runnable {
	// data
	private String querySignature;
	private QPQueryObject contextQuery;
	private String issuerReference;
	
	// constructor
	public QPConcurrent (String querySignature, QPQueryObject contextQuery, String issuerReference) {
		this.querySignature = querySignature;
		this.contextQuery = contextQuery;
		this.issuerReference = issuerReference;
	}
	
	// methods
	@Override
	public void run() {
		QPManager qp = new QPManager();
//		ExportData.writeLog("processor_tree_distribute", "" + System.currentTimeMillis());
		qp.queryRawContext(querySignature, contextQuery, issuerReference);
	}
	
}
