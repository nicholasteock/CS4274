/**
 *@author Ivan
 *@create data: 16 May 2013
 *@description
 *1) An object to represent the context query plan tree 
 * */
package qp.query.plan;

import qp.query.parser.QPQueryObject;

public class PlanNode {
	// data
	private String operation;
	private PlanNode leftChild;
	private PlanNode rightNode;
	private QPQueryObject queryObject;
	
	// constructor
	public PlanNode(String operation, QPQueryObject queryObject) {
		this.operation = operation;
		this.queryObject = queryObject;
		this.leftChild = null;
		this.rightNode = null;
	}
	
	public PlanNode(String operation, QPQueryObject queryObject, PlanNode leftChild, PlanNode rightChild) {
		this.operation = operation;
		this.queryObject = queryObject;
		this.leftChild = leftChild;
		this.rightNode = rightChild;
	}
	
	// methods
	// mutators
	public void setLeft(PlanNode leftChild) {
		this.leftChild = leftChild;
	}

	public void setQueryObject(QPQueryObject queryObject) {
		this.queryObject = queryObject;
	}
	
	public void setRight(PlanNode rightChild) {
		this.rightNode = rightChild;
	}
	
	public void setConnector(String operation) {
		this.operation = operation;
	}
	
	
	// accessors
	public PlanNode getLeft() {
		return this.leftChild;
	}
	
	public QPQueryObject getQueryObject() {
		return this.queryObject;
	}
	
	public PlanNode getRight() {
		return this.rightNode;
	}
	
	public String getConnector() {
		return this.operation;
	}
}
