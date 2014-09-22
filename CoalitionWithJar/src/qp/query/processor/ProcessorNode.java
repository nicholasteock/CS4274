/**
 *@author Ivan
 *@create data: 4 SEP 2013
 *@description
 *1) An object to represent the context processor
 * */
package qp.query.processor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;


import qp.query.parser.QPQueryObject;
//import qp.query.parser.QueryObject;
import qp.query.plan.PlanNode;

public class ProcessorNode {
	// data
	private String processorID;
//	private String parentID;
	private ProcessorNode leftChild;
	private ProcessorNode rightChild;
	// Ivan, 12 SEP 2013: Processor Tree should be double linked
	// as data is processed in a bottom up manner
	private ProcessorNode parent; 
	private String operation;
	private PlanNode planNode;
	// input data
	private Hashtable<String, String> leftInput;
	private Hashtable<String, String> rightInput;
	private Hashtable<String, String> output;

	// constructor
	public ProcessorNode(String processorID, ProcessorNode parent, PlanNode planNode) {
		this.processorID = processorID;
		this.parent = parent;
		this.operation = decideOperation(planNode);
//		System.out.println("[ProcessorNode.constructor].operation: " + operation);
		this.leftChild = null;
		this.rightChild = null;
		this.planNode = planNode;
		this.leftInput = new Hashtable<String, String>();
		this.rightInput = new Hashtable<String, String>();
		this.output = new Hashtable<String, String>();
	}

//	public ProcessorNode(String processorID, ProcessorNode parent,
//			ProcessorNode leftChild, ProcessorNode rightChild,
//			String operation, PlanNode planNode) {
//		this.processorID = processorID;
//		this.parent = parent;
//		this.leftChild = leftChild;
//		this.rightChild = rightChild;
//		this.operation = operation;
//		this.planNode = planNode;
//	}

	// methods
	public String getOperation() {
		return operation;
	}

	public ProcessorNode getLeft() {
		return leftChild;
	}

	public ProcessorNode getRight() {
		return rightChild;
	}
	
	public String getID() {
		return processorID;
	}
	
	public QPQueryObject getQueryObject() {
		return planNode.getQueryObject();
	}

	public ProcessorNode getParent() {
		return parent;
	}
	
	public String getParentID() {
		return parent.getID();
	}
	
	public Hashtable<String, String> getResult() {
		return output;
	}
	
	// mutators
	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setLeft(ProcessorNode leftChild) {
		this.leftChild = leftChild;
	}

	public void setRight(ProcessorNode rightChild) {
		this.rightChild = rightChild;
	}
	
	public boolean isRoot() {
//		if(processorID.equalsIgnoreCase(parentID)) {
		if(parent == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isLeave() {
		if(leftChild == null) {
			return true;
		} else {
			return false;
		}
	}
	
	
	// report data and apply operations
	public synchronized boolean reportResult(Hashtable<String, String> inputData) {
		// if the node is leave, directly put the data into output
//		System.out.println("[ProcessorNode.reportResult]: confirm!");
		if(isLeave()) {
//			System.out.println("[ProcessorNode.reqportResult]: leave node!");
			output = inputData;
			// rerport result to parent node
			if(isRoot()) {
				// return result to application
//				System.out.println("[ProcessorNode.reportResult]: the final result is:");
//				printResult();
				
			} else {
//				System.out.println("[ProcessorNode.reportResult]: report to parent!");
				parent.reportResult(output);
			}
			return true;
		}
		
		// if nodes are not leaves
		else if(leftInput.isEmpty()) {
//			System.out.println("[ProcessorNode.reportResult]: left child data!");
			leftInput = inputData;
			return false;
		} else {
//			System.out.println("[ProcessorNode.reportResult]: right child data!");
			rightInput = inputData;
			
//			System.out.println("[ProcessorNode.reportResult].operation: " + operation);
			// apply operation
			if(operation.equalsIgnoreCase("MERGE_AND")) {
				intersect();
			} else if(operation.equalsIgnoreCase("MERGE_OR")) {
				union();
			} else if(operation.equalsIgnoreCase("MATCH")){
				// other types of operations
				match();
			}
//			return true;
			
			// rerport result to parent node
			if(isRoot()) {
				// return result to application
//				System.out.println("[ProcessorNode.reportResult]: the final result is:");
//				printResult();
				
			} else {
//				System.out.println("[ProcessorNode.reportResult]: report to parent!");
				parent.reportResult(output);
			}
			return true;
		} 
	}
	
	// operation AND
	private synchronized void intersect() {
		// from the same and from different domains are different
		// assume same hash key first (same domain)
		for(Enumeration<String> allKeys = leftInput.keys(); allKeys.hasMoreElements();) {
			String key = allKeys.nextElement();
			if(rightInput.containsKey(key)) {
				String value = merge(leftInput.get(key), rightInput.get(key));	// they are supposed to be the same
				output.put(key, value);
			}
		}
	}
	
	// operation OR
	private synchronized void union() {
		// assume for same domain first
		// add leftInput to rightInptu by copy all rightInput to output first 
		output.putAll(rightInput);
//		System.out.println("[ProcessorNode.union].leftInput: " + leftInput.size());
//		System.out.println("[ProcessorNode.union].rightInput: " + rightInput.size());
//		output = leftInput;
		for(Enumeration<String> allKeys = leftInput.keys(); allKeys.hasMoreElements();) {
			String key = allKeys.nextElement();
			if(rightInput.containsKey(key)) {
				// supposed to merge action rather than directly add
				// but they are supposed to be the same	
				String value = merge(leftInput.get(key), rightInput.get(key)); 
				output.put(key, value);
			} else {
				String value = leftInput.get(key);
				output.put(key, value);
			}
		}
	}
	// match
	private synchronized void match() {
		for(Enumeration<String> allLeftKeys = leftInput.keys(); allLeftKeys.hasMoreElements();) {
			String leftKey = allLeftKeys.nextElement();
			for(Enumeration<String> allRightKeys = rightInput.keys(); allRightKeys.hasMoreElements(); ) {
				String rightKey = allRightKeys.nextElement();
				String leftValue = leftInput.get(leftKey);
				String rightValue = rightInput.get(rightKey);
				String newKey = "";
				String newValue = "";
				if(leftKey.compareTo(rightKey) < 0) {
					newKey = leftKey + ":" + rightKey;
					newValue = leftValue + ":" + rightValue;
				} else {
					newKey = rightKey + ":" + leftKey;
					newValue = rightValue + ":" + leftValue;
				}
				output.put(newKey, newValue);
			
			}
		}
	}
	
	// merge
	private String merge(String left, String right) {
		if(left.equalsIgnoreCase(right)) {
			return left;
		}  else {
			String[] leftArray = left.split(":");
			String[] rightArray = right.split(":");
			Set<String> valueList = new HashSet<String>();
			for(String s : leftArray) {
				valueList.add(s);
			}
			for(String s : rightArray) {
				valueList.add(s);
			}
			String result = "";
			for(String s : valueList) {
				result += (s + ":");
			}
			return result;
		}
	}
	
	// return result as a string
	public String getQueryResult() {
		while(output.isEmpty()) {
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		String result = "" + output.size() + "@";
		for(Enumeration<String> allValues = output.elements(); 
				allValues.hasMoreElements();) {
			String value = allValues.nextElement();
			result += (value + "\n");
		}
		return result;
	}
	
	// decide operation
	private String decideOperation(PlanNode planNode) {
//		System.out.println("[ProcessorNode.decideOperation].connector: " + planNode.getConnector());
		String operation = "";
		PlanNode leftChild = planNode.getLeft();
		PlanNode rightChild = planNode.getRight();
		if(leftChild == null) {
			return null;
		} else {
			List<String> leftDomains = leftChild.getQueryObject().getDomain();
			List<String> rightDomains = rightChild.getQueryObject().getDomain();
			if(isSame(leftDomains, rightDomains)) {
				return "MERGE_" + planNode.getConnector();
			} else {
				return "MATCH";
			}
		}
	}
	
	
	private boolean isSame(List<String> listOne, List<String> listTwo) {
		if(listOne.size() != listTwo.size()) {
			return false;
		} else if (listOne.size() == 1) {
			int comparison = listOne.get(0).compareToIgnoreCase(listTwo.get(0));
			if(comparison == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	// print result
	private void printResult() {
		String result = "";
		for(Enumeration<String> allValues = output.elements(); allValues.hasMoreElements();) {
			String value = allValues.nextElement();
			result += (value + ";");
		}
		System.out.println(result);
	}
}
