/**
 *@author Ivan
 *@Modified data: 20 May 2013
 *@description
 *1) An object to represent the context query plan tree 
 * */

/*
 * Copyright (c) 2007, National University of Singapore (NUS)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditionTree 
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, 
 *     this list of conditionTree,the authors and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditionTree,the authors and the following disclaimer in
 *     the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the university nor the names of its 
 *     contributors may be used to endorse or promote products derived from 
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Author: Wenwei Xue (dcsxw@nus.edu.sg)
 *
 */

/**
 * This class implements an evaluation plan for a context query at a PSG. 
 * We assume a query only involves a single physical space 
 * in the current prototype.
 */
package qp.query.parser;

import java.util.List;
import java.util.Vector;

//import psg.servicemanager.*;

public class QPQueryObject {
	// data
	private String queryString; // query string that this plan is parsed from

	public static final byte DATA_ACQUISITION = 0x01; // one-time, pull-based  data acquisition
	public static final byte EVENT_SUBSCRIPTION = 0x02; // continuous, push-based event detection and notification
	private byte type;

	private List<String> attributeList; // list of context attributeList in the  SELECT or SUBSCRIBE clause
//	private List<String> constrainedAttributes; // context attributeList that required, but not shown  in where clause
//	private List<String> unconstrainedAttributes; // attributeList required  that are shown in where clause
	private List<String> domainList; // context domainList of the physical space; NULL if no FROM clause in  query specification
	private QPConditionTree conditionTree;  // instead of this conditionTree, we use a tree to represent the conditions
	private String errStr; // a string indicates whether this query plan is  generated from parsing a valid query string
							// if not, the error message is contained in this  string
	private String whereClause; // the where clause

	// private ServiceManager cds;

	// constructor

	// create the query object based on query string
	public QPQueryObject(String queryString, byte queryType,
			List<String> domainType, List<String> attributeList,
			QPConditionTree conditionTree, String error) {
		this.queryString = queryString;
		this.type = queryType;
		this.domainList = domainType;
		this.attributeList = attributeList;
		this.conditionTree = conditionTree;
		this.errStr = error;

		// internal variables initialization
		if(queryString.contains("where")) {
			this.whereClause = queryString.substring(queryString.indexOf("where"));
		} else {
			this.whereClause = queryString.substring(queryString.indexOf("WHERE"));
		}
//		this.constrainedAttributes = new Vector<String>();
//		this.unconstrainedAttributes = new Vector<String>();
//		for (String attribute : attributeList) {
//			if (whereClause.contains(attribute)) {
//				constrainedAttributes.add(attribute);
//				System.out.println("[QueryObject.constructor].constrainedAttribute: " + attribute);
//			} else {
//				unconstrainedAttributes.add(attribute);
//				System.out.println("[QueryObject.constructor].unConstrainedAttribute: " + attribute);
//			}
//		}
	}
	
	// create query object based on condition tree
	public QPQueryObject(QPQueryObject parentQO, QPConditionTree conditionTree ) {
		this.type = DATA_ACQUISITION;
		this.errStr = parentQO.getErrorString();
		this.conditionTree = conditionTree;
		this.whereClause = conditionTree.getCondition();
		this.domainList = new Vector<String>();
		// cannot let attribute list equals to parent list
		// but have to duplicate the list
		this.attributeList = new Vector<String>();
		for(String attribute : parentQO.getAttributes()) {
			attributeList.add(attribute);
		}
//		this.constrainedAttributes = new Vector<String>();
//		this.unconstrainedAttributes = new Vector<String>();
		
//		List<String> tempUnconstrained = parentQO.getUnconstrainedAttributes();
//		List<String> tempConstrained = parentQO.getConstrainedAttributes();
		List<String> tempDomainList = parentQO.getDomain();
		
 		// update domain list
		for(String domain : tempDomainList) {
//			System.out.println("[QueryObject.update].domain: " + domain);
			if (whereClause.contains(domain)) {
//				System.out.println("[QueryObject.update].domain: " + domain + " is not contained!");
				domainList.add(domain);
			}
		}
		
		// update attribute list
		// update the unconstrained attribute list
		List<String> unrelatedAttributeList = new Vector<String>();
		for(String attribute : attributeList) {
			String domain = attribute.split("\\.")[0];
			if(!domainList.contains(domain)) {
				unrelatedAttributeList.add(attribute);
			}
		}
		
		for(String attribute : unrelatedAttributeList) {
			attributeList.remove(attribute);
		}
		
//		// update attribute list
//		// update the unconstrained attribute list
//		for(String attribute : tempUnconstrained) {
//			String domain = attribute.split("\\.")[0];
//			if(domainList.contains(domain)) {
//				unconstrainedAttributes.add(attribute);
//				attributeList.add(attribute);
//			}
//		}
		// Ivan, 12 SEP 2013: result will returned automatically with a id and no need to add in here
		// add in the primary key identical attribute
//		for(String possibleDomain : domainList) {
//			String idAttribute = possibleDomain + ".id";
//			// assume these two list are synchronized
//			if(!attributeList.contains(idAttribute)) {
//				unconstrainedAttributes.add(idAttribute);
//				attributeList.add(idAttribute);
//			}
//		}
		
//		// update the constrained attribute list
//		for(String attribute : tempConstrained) {
//			if(whereClause.contains(attribute)) {
//				constrainedAttributes.add(attribute);
//				attributeList.add(attribute);
//			}
//		}
		
		// recompose the query string
		this.queryString = recomposeQuery();
	}

	// methods
	// Ivan, 20 May 2013
	// begin
	
	// recompose the queryString based on new conditions
	private String recomposeQuery() {
		String tempQuery = "";
		if(type == DATA_ACQUISITION ) {
			tempQuery += "SELECT ";
			
			// add in attributes
			for(int i=0; i<attributeList.size()-1; i++) {
				tempQuery += (attributeList.get(i) + ", ");
			}
			// last attribute
			tempQuery += (attributeList.get(attributeList.size()-1) + " ");
			
			// add the from clause
			tempQuery += "FROM ";
			for(int i=0; i<domainList.size()-1; i++) {
				tempQuery += (domainList.get(i) + ", ");
			}
			tempQuery += (domainList.get(domainList.size()-1) + " ");
			
			// addd the where clause
			tempQuery += "WHERE " + conditionTree.getCondition();
			
		} else {
			
		}
		
		return tempQuery;
	}
	
	// end

	// accessors
	public String getQueryString() {
		return this.queryString;
	}

	public byte getType() {
		return this.type;
	}

	public List<String> getAttributes() {
		return this.attributeList;
	}
	
//	public List<String> getUnconstrainedAttributes() {
//		return this.unconstrainedAttributes;
//	}
//	
//	public List<String> getConstrainedAttributes() {
//		return this.constrainedAttributes;
//	}
	
	public List<String> getDomain() {
		return this.domainList;
	}

	public QPConditionTree getQueryConditions() {
		return this.conditionTree;
	}

	public String getErrorString() {
		return this.errStr;
	}

	// public ServiceManager getContextDataServices() {
	// return this.cds;
	// }

	// mutators
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public void setAttributes(List<String> attributeList) {
		this.attributeList = attributeList;
	}

	public void setDomain(List<String> domainList) {
		this.domainList = domainList;
	}

	public void setConditions(QPConditionTree conditionTree) {
		this.conditionTree = conditionTree;
	}

	public void setErrorString(String error) {
		this.errStr = error;
	}

	// public void setContextDataServices(ServiceManager cds) {
	// this.cds = cds;
	// }

	// add or modify
	public void appendError(String s) {
		this.errStr += s;
	}

	// Ivan, 19 Jul 2012: verify the validness of query
	public boolean hasError() {
		if (errStr == null) {
			return false;
		} else {
			System.out.println("[QueryPlan.hasError].errStr:" + errStr);
			return true;
		}
	}

	// Ivan, 18 Sep 2013: check whether two Query Objects have the same domain
	// Begin
	public boolean isSingleDomain(){
		if(domainList.size() == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	// end 
	
	/**
	 * This function prints out information encapsulated in a query plan.
	 */
	public void print() {
		if (errStr != null) {
			System.out.println("Error message: " + errStr);
			return;
		}

		System.out.print("Type of query: ");
		if (type == DATA_ACQUISITION) {
			System.out.println("data acquisition");
		} else {
			System.out.println("event subscription");
		}
		if (domainList != null) {
			System.out.println("Context domainList: " + domainList + " ");
		} else {
			System.out.println("No context domainList specified.");
		}
		System.out.print("List of context attributeList: ");
		int i;
		for (i = 0; i < attributeList.size(); i++) {
			System.out.print(attributeList.get(i) + " ");
		}
		System.out.println();

		if (conditionTree == null) {
			System.out.println("No query predicate.");
		} else {
			System.out.println("List of query predicates: ");
			System.out.println(conditionTree.getCondition());
//			for (i = 0; i < conditionTree.size(); i++) {
//				System.out.println("(" + (i + 1) + ") "
//						+ conditionTree.get(i).getAttribute() + " "
//						+ conditionTree.get(i).getOperator() + " "
//						+ conditionTree.get(i).getConstant());
//			}
		}

		if (errStr == null) {
			System.out.println("No error!");
		} else {
			System.out.println("Error message: " + errStr);
		}
		// System.out.println("Query condition: " + evaluateQueryCondition());
		// System.out.println("Query result: " + getQueryResult());
	} // print

} // class