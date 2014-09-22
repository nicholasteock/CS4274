/**
 *@author Ivan
 *@create data: 4 SEP 2013
 *@description
 *1) An object to represent the execution plan tree: a tree of processors 
 * */
package qp.query.processor;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import qp.config.QPConfiguration;
import qp.kernel.api.QPManager;
import qp.query.parser.ConditionNode;
import qp.query.parser.QPConditionTree;
import qp.query.parser.QPQueryObject;
import qp.query.plan.PlanNode;
import qp.query.plan.PlanTree;

public class ProcessorTree {
	// data
	private final String queryID;
//	private String qpReference = QPConfiguration.QP_REFERENCE;
	private String qpReference;
	private PlanTree planTree;
	private ProcessorNode root;
	private int indexID; // used to allocate ID to each node
	private Hashtable<String, ProcessorNode> allNodes;
	private List<ProcessorNode> leafNodes;
	
	// constructor
	public ProcessorTree(PlanTree planTree, String queryID, String qpReference) {
		this.queryID = queryID;
		this.qpReference = qpReference;
		this.allNodes = new Hashtable<String, ProcessorNode>();
		this.leafNodes = new Vector<ProcessorNode>();
		this.planTree = planTree;
		this.indexID = 0;
		this.root = buildProcessTree(null,planTree.getRoot());
	}
	// methods
	private ProcessorNode buildProcessTree(ProcessorNode parent, PlanNode planNode) {
//		PlanNode planNode = planTree.getQueryConditions().getRoot();
		indexID++;
		String currentID = queryID + "_" + indexID;
		ProcessorNode processorNode = new ProcessorNode(currentID, parent, planNode);
		// put the processor node into hashtable
		allNodes.put(currentID, processorNode);
		if(planNode.getLeft() == null) {
			processorNode.setLeft(null);
			processorNode.setRight(null);
			// set queries to corresponding PSGs
			leafNodes.add(processorNode);
		} else {
//			ProcessorNode leftPN = new ProcessorNode(planNode.getLeft().getConnector());
			processorNode.setLeft(buildProcessTree(processorNode, planNode.getLeft()));
//			ProcessorNode rightPN = new ProcessorNode(planNode.getRight().getConnector());
			processorNode.setRight(buildProcessTree(processorNode, planNode.getRight()));
		}

		return processorNode;
	}
	
	public void reportResult(String processorID, Hashtable<String, String> resultData) {
		ProcessorNode processorNode = allNodes.get(processorID);
		boolean isDone = processorNode.reportResult(resultData);
		if(processorNode.isRoot() && isDone) {
			// return result to query issuer
		} else if(!processorNode.isRoot() && isDone) {
			/// report result to parent
			Hashtable<String, String> newResultData = processorNode.getResult();
			reportResult(processorNode.getParentID(), newResultData);
		} else {
			// do nothing
		}
		
	}

	// Ivan, 6 Nov 2013: re-write method into concurrent process
//	public void distributeLeafNode() {
//		QPConcurrent[] qpManagerList = new QPConcurrent[leafNodes.size()];
//		for(int j=0; j<leafNodes.size(); j++) {
//			ProcessorNode pn = leafNodes.get(j);
//			QPConcurrent temp = new QPConcurrent(pn.getID(), pn.getQueryObject(), qpReference);
//			qpManagerList[j] = temp;
////			qp.queryRawContext(pn.getID(), pn.getQueryObject(), qpReference);
//		}
//		ExecutorService threadExecutor = Executors.newCachedThreadPool();
//		for(int i=0; i<qpManagerList.length; i++) {
//			threadExecutor.execute(qpManagerList[i]);
//		}
//		threadExecutor.shutdown();
//	}
	
	public void distributeLeafNode() {
		for(ProcessorNode pn:leafNodes) {
			QPManager qp = new QPManager();
			qp.queryRawContext(pn.getID(), pn.getQueryObject(), qpReference);
		}
	}
	
	// Ivan, 4 DEC 2013: re-write method into concurrent process
	public void retrieveLeafData() {
		//			QPConcurrent[] qpManagerList = new QPConcurrent[leafNodes.size()];
		ExecutorService threadExecutor = Executors.newCachedThreadPool();
		for(int j=0; j<leafNodes.size(); j++) {
			ProcessorNode pn = leafNodes.get(j);
			threadExecutor.execute(new DataRetrievalThread(pn));
		}
		threadExecutor.shutdown();

	}	
//	public void retrieveLeafData() {
//		System.out.println("[ProcessorTree.retrieveLeafData]: location!");
//		for(ProcessorNode pn:leafNodes) {
//			QPManager qp = new QPManager();
//			Hashtable<String, String> data = qp.retrieveRawContext(pn.getID());
//			pn.reportResult(data);
//		}
//	} 
	
	public String getResult() {
		return root.getQueryResult();
	}
	
}
