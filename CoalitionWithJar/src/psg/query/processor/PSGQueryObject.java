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
package psg.query.processor;

import java.util.List;
import java.util.Vector;

//import psg.servicemanager.*;

public class PSGQueryObject {
	// data
	private String queryString; // query string that this plan is parsed from

	public static final byte DATA_ACQUISITION = 0x01; // one-time, pull-based  data acquisition
	public static final byte EVENT_SUBSCRIPTION = 0x02; // continuous, push-based event detection and notification
	private byte type;

	private List<String> attributeList; // list of context attributeList in the  SELECT or SUBSCRIBE clause
	private String domain; // context domain of the physical space; NULL if no FROM clause in  query specification
	private PSGConditionTree conditionTree;  // instead of this conditionTree, we use a tree to represent the conditions
	private String errStr; // a string indicates whether this query plan is  generated from parsing a valid query string
							// if not, the error message is contained in this  string
//	private String whereClause; // the where clause
	// constructor
	// create the query object based on query string
	public PSGQueryObject(String queryString, byte queryType,
			String domainType, List<String> attributeList,
			PSGConditionTree conditionTree, String error) {
		this.queryString = queryString;
		this.type = queryType;
		this.domain = domainType;
		this.attributeList = attributeList;
		this.conditionTree = conditionTree;
		this.errStr = error;

		// internal variables initialization
//		if(queryString.contains("where")) {
//			this.whereClause = queryString.substring(queryString.indexOf("where"));
//		} else {
//			this.whereClause = queryString.substring(queryString.indexOf("WHERE"));
//		}
	}

	// methods
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
	
	public String getDomain() {
		return this.domain;
	}

	public PSGConditionTree getQueryConditions() {
		return this.conditionTree;
	}

	public String getErrorString() {
		return this.errStr;
	}

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

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setConditions(PSGConditionTree conditionTree) {
		this.conditionTree = conditionTree;
	}

	public void setErrorString(String error) {
		this.errStr = error;
	}

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
		if (domain != null) {
			System.out.println("Context domain: " + domain + " ");
		} else {
			System.out.println("No context domain specified.");
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