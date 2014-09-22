/**
 *@author Ivan
 *@create data: 20 May 2013
 *@description
 *1) An object to represent the context query plan tree 
 * */
package proxy.query.processor;

import java.util.List;
import java.util.Stack;
import java.util.Vector;

import qp.query.plan.PlanNode;



public class PSGConditionTree {
	// data
	private String conditionString;
	private ConditionNode root;
	
//	// for printing 
//	private int[][] printArray;
//	private int index;
//	private List<ConditionNode> nodeList;
	
	// constructor
	public PSGConditionTree(String conditionString) {
		this.conditionString = conditionString;
		this.root = buildConditionTree();
		
//		// initiate printArray
//		this.printArray = new int[100][100]; // default value is 0
//		this.index = 1;
//		this.nodeList = new Vector<ConditionNode>();
	}
	
	public PSGConditionTree(ConditionNode root) {
		this.root = root;
		this.conditionString = root.getCondition();
	}
	
	// methods
	// accessor
	public String getCondition() {
		return this.conditionString;
	}
	
	public ConditionNode getRoot() {
		return this.root;
	}
	// mutator
	public void setCondition(String conditionString) {
		this.conditionString = conditionString;
		this.root = buildConditionTree();
	}
	
	public void setRoot(ConditionNode root) {
		this.root = root;
		this.conditionString = root.getCondition();
	}
	// build tree
	private ConditionNode buildConditionTree() {
		conditionString = deleteDoubleSpaces(conditionString);
		conditionString = conditionString.trim();
		
		Stack<String> connectors = new Stack<String>();
		Stack<ConditionNode> nodes = new Stack<ConditionNode>();
		
		if(conditionString.contains(" and ") 
				|| conditionString.contains(" or ")
				|| conditionString.contains(" AND ")
				|| conditionString.contains(" OR ")) {
			String[] whereArray = conditionString.split(" ");
			
			// identify the constraints first
			Vector<String> whereVector = new Vector<String>();
			String constraint = "";
			for(String item:whereArray) {
				if(item.equals("(") || item.equals(")") 
						|| item.equalsIgnoreCase("and")
						|| item.equalsIgnoreCase("or")) {
					if(!constraint.equals("")) {
						whereVector.add(constraint.trim());
						constraint = "";
					} 
					whereVector.add(item);
				} else {
					constraint += (item + " ");
				}
			}
			
			// if the last one is constraint, put it in the whereVector
			if(!constraint.equals("") ) {
				whereVector.add(constraint.trim());
				constraint = "";
			}
			
			// check the recomposed where clause
//			System.out.print("[ConditionTree.buildConditionTree]: ");
//			for(String element:whereVector) {
//				System.out.print(element + " ");
//			}
//			System.out.println("");
			
//			for(int i=0; i<whereArray.length; i++) {
			for(String item:whereVector) {
				if(item.equalsIgnoreCase("and") 
						|| item.equalsIgnoreCase("or") ) {
					// if the latest connector is "and" or "or", create node and put in the node stack
					while(!connectors.isEmpty() && !connectors.peek().equals("(")) {
						ConditionNode rightNode = nodes.pop();
						ConditionNode leftNode = nodes.pop();
						ConditionNode tempNode = new ConditionNode(connectors.pop(),leftNode,rightNode);
						nodes.add(tempNode);
					}
					connectors.add(item);
				} else if(item.equals("(")) {
					connectors.add(item);
				} else if(item.equals(")")) {
					while(!connectors.peek().equals("(")) {
						ConditionNode rightNode = nodes.pop();
						ConditionNode leftNode = nodes.pop();
						ConditionNode tempNode = new ConditionNode(connectors.pop(),leftNode,rightNode);
						nodes.add(tempNode);
					}
					connectors.pop();
				} else {
					nodes.add(new ConditionNode(item));
				}
			}
			
			// create the remaining tree
			while(!connectors.isEmpty()) {
				ConditionNode rightNode = nodes.pop();
				ConditionNode leftNode = nodes.pop();
				ConditionNode tempNode = new ConditionNode(connectors.pop(),leftNode,rightNode);
				nodes.add(tempNode);
			}
			
			// there should be only one node left in the stack, which is the root
			return nodes.pop();
			
		} else {
			return new ConditionNode(conditionString);
		}
	}
	
	private String deleteDoubleSpaces(String s) {
		while(s.contains("  ")) {
			s = s.replaceAll("  ", " ");
		}
		return s;
	}
	
//	// testing main method
//	public static void main(String[] args) {
//		String whereClause = "( A = 3 and B = 4 ) and ( C = 5 or  D = 6 ) and E = 7 ";
//		ConditionTree ct = new ConditionTree(whereClause);
//		ct.print();
//	}
//	
//	// print the constraint tree
//	public void print() {
//		print(root, 50, 0);
//		for(int i=0; i<10; i++) {
//			for(int j=0; j<100; j++) {
//				if(printArray[i][j] != 0) {
//					System.out.print(printArray[i][j]);
//				} else {
//					System.out.print(" ");
//				}
//			}
//			System.out.println("");
//		}
//		
//		// print out condition string
//		for(int i=0; i<nodeList.size(); i++) {
//			System.out.println("" + (i + 1) + ": " + nodeList.get(i).getCondition());
//		}
//	}
//	private void print(ConditionNode node, int position,  int level) {
////		System.out.println(rootNode.getValue() + " ");
//		nodeList.add(node);
//		printArray[level][position] = index++;
//		if(node.getLeft() != null) {
//			level++;
//			print(node.getLeft(), position -(8-level+1), level);
//			print(node.getRight(), position + (8-level+1), level);
//		}
//	}
}
