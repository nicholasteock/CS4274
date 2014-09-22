/*

* Copyright (c) 2007, National University of Singapore (NUS)
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without 
* modification, are permitted provided that the following conditions 
* are met:
*
*   * Redistributions of source code must retain the above copyright notice, 
*     this list of conditions,the authors and the following disclaimer.
*   * Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions,the authors and the following disclaimer in
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
 * This class represents a context domain that a physical space belongs to.
 */
package psg.service.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.List;

import psg.kernel.server.PublicServer;
import psg.query.processor.ConditionNode;
import psg.query.processor.QueryCondition;
import psg.query.processor.PSGQueryObject;
import kernel.com.reference.NetworkReference;
import kernel.network.server.UDPServer;

public class ContextDomain {
	// data
//	String name; // name of the context domain
	private String domainKey; // format: "domainType@macAddress"
	private String domainName; // name of the context domain, 
							// format: ��http://IP:Port/xmlrpc;��
	private String domainType;
//	private String domainIP;
//	private int domainPort;
	private String domainReference;
//	ContextAttribute[] contextAttrs = null;  // list of context attributes supported in the domain
//	private ContextAttribute[] contextAttributes;  // list of context attributes supported in the domain
//	private List<ContextAttribute> caList = new Vector<ContextAttribute>();
	private Map<String, ContextAttribute> contextAttributes;
	
	// Ivan, 7 Aug 2012: add in the server
	private PublicServer domainServer;
	
	
	// constructor
	public ContextDomain(String domainName, String domainType, String domainReference)  {
		this.domainName = domainName;
		this.domainType = domainType.toUpperCase();
		this.domainReference = domainReference;
		this.contextAttributes = new Hashtable<String, ContextAttribute>();
		try {
			this.domainServer = new PublicServer(getName(), getPort());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ContextDomain(String domainName, String domainType, String domainReference, Map<String, ContextAttribute> contextAttrs) {
		this.domainName = domainName;
		this.domainType = domainType.toUpperCase();
		this.domainReference = domainReference;
		this.contextAttributes = contextAttrs;
		try {
			this.domainServer = new PublicServer(getName(), getPort());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		convertToList();
	}
	// methods
	// accessors
	public String getName() {
		return this.domainName;
	}
	
	public String getType() {
		return this.domainType;
	}
	
	public String getKey() {
		return this.domainKey;
	}
	
	public int getPort() {
		return (new NetworkReference(domainReference)).getPort();
	}
	
	public String getReference() {
		return this.domainReference;
	}
	
	public PublicServer getServer() {
		return this.domainServer;
	}
	
	public Map<String,ContextAttribute> getAllAttributeMap() {
		return this.contextAttributes;
	}
	
	public List<ContextAttribute> getAttributeList() {
		return convertToList();
	}
	
	public ContextAttribute getAttribute(String name) {
		return this.contextAttributes.get(name);
	}
	
	public String getAttributeValue(String attributeName) {
//		System.out.println("[ContextDomain.getAttributeValues].attributeName: " + attributeName);
		return this.contextAttributes.get(attributeName).getValue();
	}
	
	// mutators
	public void setName(String name) {
		this.domainName = name;
	}
	
	// Ivan, 25 Feb 2014: update the reference needs to 
	// close previous server and open new server
	public void setReference(String reference) {
		this.domainReference = reference;
	}
	
//	public void setAttributes(List<ContextAttribute> attrs) {
//		this.contextAttributes = attrs;
//	}
//	
//	public void updateAttribute(ContextAttribute cs, int index) {
//		this.contextAttributes.set(index, cs);
//	}
	public void setAttributes(Map<String, ContextAttribute> attrs) {
		this.contextAttributes = attrs;
	}
	
	public void updateAttribute(ContextAttribute attribute) {
		this.contextAttributes.put(attribute.getName(), attribute);
	}
	
	public void updateAttributeValue(String attributeName, String attributeValue) {
		this.contextAttributes.get(attributeName).setValue(attributeValue);
	}
	
	public void addAttribute(ContextAttribute attribute) {
		this.contextAttributes.put(attribute.getName(), attribute);
	}
	
	public boolean isEmpty() {
		return contextAttributes.isEmpty();
	}
	
	// verifiers
	// check whether a ceitain attribute is contained in this domain
	public boolean containsAttribute(String attrName) {
		return contextAttributes.containsKey(attrName);
	}

	public boolean checkDomain(String name) {
		return this.domainType.equalsIgnoreCase(name);
	}
	
	//convert the Map entries to list
	private List<ContextAttribute> convertToList() {
		Collection<ContextAttribute> collection = contextAttributes.values();
		if(collection instanceof List) {
			return (List<ContextAttribute>) collection;
		} else {
			return new ArrayList<ContextAttribute>(collection);
		}
	}
	
	/**
	 * This function examines whether the condition "name op val" is satisfied. 
	 * @param domain context domain of the attribute
	 * @param name attribute name in the domain
	 * @param op a comparsion operator such as ">" and "<="
	 * @param val a constant value for the attribute
	 * @return TRUE if the condition is satisfied; otherwise FALSE
	 */
	private boolean filterContext(QueryCondition qc) {
		String name = qc.getAttribute();
		String op = qc.getOperator();
		String val = qc.getConstant();
		name = name.toLowerCase();
		if(contextAttributes.containsKey(name)) {
			boolean result =  getAttribute(name).filterValue(op, val);
//			System.out.println("[ContextDomain.filterContext].result: " + result);
			return result;
		} else {
			return false;
		}
		
	}  // filterContext
	
	/**
	 * This function obtains the query result from the PSG.
	 * For a data acquisition query, the result is the current values of a list of context attribute.
	 * For an event subscription query, the result is the Socket Address of the Event Server Socket.
	 * @return string representation of the query result
	 *         for a data acquisition query: (attr1, val1); (attr2, val2); ... (attrN, valN)
	 *         for an event subscription query: gatewayIP:port; (event1, connectionID1); ... (eventN, connectionIDN)
	 */
	public String getQueryResult(PSGQueryObject queryObject) {
//		System.out.println("[ContextDomain.getQueryResult]: position confirmed!");
		// Ivan, 9 SEP 2013: return result value with psgID as index
		// start
		String psgID = getReference(); //getAttributeValue("psgID");
		String result = psgID + "@";
		// end
//		String[] socketAddress = null;
		int i;
//		System.out.println("[ContextDomain.getQueryResult].queryType: " + queryObject.getType());
		if (queryObject.getType() == PSGQueryObject.DATA_ACQUISITION) {
			for (i=0;i<queryObject.getAttributes().size();i++) {
				result += queryObject.getAttributes().get(i) + "=" + getAttributeValue(queryObject.getAttributes().get(i)) + ":";
			}	
			result = result.substring(0,result.length()-1);	
//			System.out.println("[ContextDomain.getQueryResult].result: " + result);
		} else if (queryObject.getType() == PSGQueryObject.EVENT_SUBSCRIPTION) {
//			for (i=0;i<queryObject.getAttributes().length;i++) {
//				String name = queryObject.getAttribute();
//				if (name.indexOf(GlobalContextSchemas.eventsArray[2].toLowerCase())== 0) {
//					int start, end;
//					start = GlobalContextSchemas.eventsArray[2].length();
//					start = name.indexOf("(",start) + 1;
//					end = name.indexOf(")",start);
//					name = name.substring(start,end).trim();
//					socketAddress = cds.getContextChange(domain, name);				
//				} else {
//					socketAddress = cds.subscribeEvent(domain,attributes[i]);
//				}
//				if (i==0) {
//					result += socketAddress[0] + ":" + socketAddress[1] + ";";
//				}
//				result += "(" + queryObject.getAttributes()[i] + ", " + socketAddress[2] + "); ";				
//			} // end of for					
//			result = result.substring(0,result.length()-2);
		}	
//		System.out.println("[ContextDomain.getQueryResult].result: " + result);
		return result;
	}  // getQueryResult
	
	/**
	 * This function evaluates whether the condition of this query is satisfied at this time.
	 * @return TURE if the condition is satisfied; FALSE otherwise
	 */
	public boolean evaluateQueryCondition(PSGQueryObject queryObject) {
		if (queryObject.getQueryConditions() == null) return true;
		System.out.println("[ContextDomain.evaluateQueryCondition].size:" + queryObject.getQueryConditions());
		return evaluateCondition(queryObject.getQueryConditions().getRoot());
		//		for (int i=0; i<queryObject.getQueryConditions().size(); i++)	{
//			System.out.println("[ContextDomain.evaluateQueryCondition].conditions[i]:" + queryObject.getQueryCondition());
//			System.out.println("[ContextDomain.evaluateQueryCondition].cdInstance:" + getName());
//			if (!filterContext(queryObject.getQueryCondition())) {
//				System.out.println("[ContextDomain.evaluateQueryCondition].if: false" );
//				return false;
//				return true;
//			}	
//		}
//		return true;
	}  // evaluateQueryCondition
	
	private boolean evaluateCondition(ConditionNode conditionNode) {
		if(conditionNode.getLeft() == null) {
			return filterContext(new QueryCondition(conditionNode.getCondition()));
		} else {
			Boolean left = evaluateCondition(conditionNode.getLeft());
			Boolean right = evaluateCondition(conditionNode.getRight());
			if(conditionNode.getConnector().equalsIgnoreCase("AND")) {
				return (left && right);
			} else {
				return (left || right);
			}
		}
	}
	
	
	
	// end of class
} // end of class