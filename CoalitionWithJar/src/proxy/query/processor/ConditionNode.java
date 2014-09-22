/**
 *@author Ivan
 *@create data: 20 May 2013
 *@description
 *1) An object to represent the context query plan tree 
 * */
package proxy.query.processor;

public class ConditionNode {
	// data
	private String conditionString;
	private String connector;
	private ConditionNode leftNode;
	private ConditionNode rightNode;
	
	// constructor
	public ConditionNode(String conditionString) {
		this.conditionString = conditionString;
		this.connector = null;
		this.leftNode = null;
		this.rightNode = null;
	}
	
	public ConditionNode(String connector, ConditionNode leftNode,
			ConditionNode rightNode) {
		this.connector = connector;
		this.leftNode = leftNode;
		this.rightNode = rightNode;
		String tempCondition = " ( " + leftNode.getCondition() + " ) " 
					+ connector 
					+ " ( " + rightNode.getCondition() + " ) ";
		this.conditionString = deleteDoubleSpaces(tempCondition);
	}
	
	// methods
	// accessors
	public String getCondition() {
		return this.conditionString;
	}
	
	public String getConnector() {
		return this.connector;
	}
	
	public ConditionNode getLeft() {
		return this.leftNode;
	}
	
	public ConditionNode getRight() {
		return this.rightNode;
	}
	
	// mutator
	public void setCondition(String conditionString) {
		this.conditionString = conditionString;
	}
	
	public void setConnector(String connector) {
		this.connector = connector;
	}
	
	public void setLeft(ConditionNode leftNode) {
		this.leftNode = leftNode;
	}
	
	public void setRight(ConditionNode rightNode) {
		this.rightNode = rightNode;
	}
	
	// delete double spaces and head or tail space
	private String deleteDoubleSpaces(String s) {
		while(s.contains("  ")) {
			s = s.replaceAll("  ", " ");
		}
		return s.trim();
	}
}
