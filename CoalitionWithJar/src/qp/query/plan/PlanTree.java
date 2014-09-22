/**
 *@author Ivan
 *@create data: 16 May 2013
 *@description
 *1) An object to represent the context query plan tree 
 *2) It is a tree of QueryObject
 *3) In this class, queries from same domain are grouped as one
 * */
package qp.query.plan;

import java.util.List;
import java.util.Stack;
import java.util.Vector;

import qp.query.parser.ConditionNode;
import qp.query.parser.QPConditionTree;

import qp.query.parser.QPQueryObject;
import qp.query.parser.QPQueryParser;

public class PlanTree {
	// data
	private PlanNode head;
	// print puropose only
	private int[][] printArray;
	private int index;
	private List<PlanNode> nodeList;

	// constructor
	public PlanTree(QPQueryObject rootQO) {
		this.head = buildPlanTree(rootQO);

		// print
		this.printArray = new int[100][100];
		this.index = 1;
		this.nodeList = new Vector<PlanNode>();
	}

	// methods
	public PlanNode getRoot() {
		return this.head;
	}

	// build the query object tree using a iterative manner
//	private PlanNode buildPlanTree(QPQueryObject qo) {
//		ConditionNode cn = qo.getQueryConditions().getRoot();
//		PlanNode pn = new PlanNode(cn.getConnector(), qo);
//		if (cn.getLeft() == null) {
//			pn.setLeft(null);
//			pn.setRight(null);
//		} else {
//			QPQueryObject leftQO = new QPQueryObject(qo, new QPConditionTree(
//					cn.getLeft()));
//			pn.setLeft(buildPlanTree(leftQO));
//			QPQueryObject rightQO = new QPQueryObject(qo, new QPConditionTree(
//					cn.getRight()));
//			pn.setRight(buildPlanTree(rightQO));
//		}
//		return pn;
//	}
	//Ivan, 18 Sep 2013: rewrite the planTree with leaves as the 
	// query objects has single domain
	// begin
	private PlanNode buildPlanTree(QPQueryObject qo) {
		ConditionNode cn = qo.getQueryConditions().getRoot();
		PlanNode pn = new PlanNode(cn.getConnector(), qo);
		if (qo.isSingleDomain()) {
			pn.setLeft(null);
			pn.setRight(null);
		} else {
			QPQueryObject leftQO = new QPQueryObject(qo, new QPConditionTree(
					cn.getLeft()));
			pn.setLeft(buildPlanTree(leftQO));
			QPQueryObject rightQO = new QPQueryObject(qo, new QPConditionTree(
					cn.getRight()));
			pn.setRight(buildPlanTree(rightQO));
		}
		return pn;
	}
	// end

	// add by Ivan, 8 May 2012
	private String deleteDoubleSpaces(String s) {
		while (s.contains("  ")) {
			s = s.replaceAll("  ", " ");
		}
		return s;
	}

	// print the constraint tree
	public void print() {
		print(head, 50, 0);
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 100; j++) {
				if (printArray[i][j] > 0) {
					System.out.print(printArray[i][j]);
				} else {
					System.out.print(" ");
				}
			}
			System.out.println("");
		}
		for (int i = 0; i < nodeList.size(); i++) {
			System.out.println("" + (i + 1) + ": "
					+ nodeList.get(i).getQueryObject().getQueryString());
		}
	}

	private void print(PlanNode rootNode, int position, int level) {
		// System.out.println(rootNode.getValue() + " ");
		nodeList.add(rootNode);
		printArray[level][position] = index++;
		if (rootNode.getLeft() != null) {
			level++;
			print(rootNode.getLeft(), position - (8 - level + 1), level);
			print(rootNode.getRight(), position + (8 - level + 1), level);
		}

	}

	// test method
	public static void main(String[] args) {
		// String constraint = "( A and B ) and ( C or  D ) and E "; // or F and
		// G and H or I or J or K";
		// String constraint =
		// "select x.a, y.b, x.c, y.h from x, y where ( x.a = 3 and y.b = 4 ) and ( x.c = 5 or  y.d = 6 ) and x.e = 7 ";
		// // or F and G and H or I or J or K";
//		String constraint = "select x.a, y.b, x.c, y.h from x, y where ( x.a = 3 and y.b = 4 ) and ( x.c = 5 or  y.d = 6 ) and x.e = 7 or F and G and H or I or J or K";
		// Ivan, 12 SEP 2013: when selected attributes are same with constrained attributes, they  will be deleted
		//		String constraint = "select person.name, person.location from person where person.name = \"IVAN\" AND person.location = \"IDMI\"";
		String constraint = "select person.name, person.speed, person.mood, "
				+ "person.location, shop.location, office.name, shop.name, office.location "
				+ "from person, shop, office "
				+ "where ( person.name = \"IVAN\" "
				+ " AND person.location = \"IDMI\" ) "
				+ " AND shop.preference = \"book\""
				+ " AND office.location = \"IDMI\"";
		QPQueryObject qObject = QPQueryParser.parseQuery(constraint);
		PlanTree pt = new PlanTree(qObject);
		pt.print();
	}
}
