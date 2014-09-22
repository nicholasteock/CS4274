package qp.query.processor;

import java.util.Hashtable;

import qp.kernel.api.QPManager;

public class DataRetrievalThread implements Runnable {
	// data
	private ProcessorNode pn;
	
	// constructor
	public DataRetrievalThread(ProcessorNode pn) {
		this.pn = pn;
	}
	
	// methods
	@Override
	public void run() {
		// TODO Auto-generated method stub
		QPManager qp = new QPManager();
		Hashtable<String, String> data = qp.retrieveRawContext(pn.getID());
//		pn.get
		pn.reportResult(data);
	}

}
