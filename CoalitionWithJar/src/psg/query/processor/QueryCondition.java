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
package psg.query.processor;

public class QueryCondition {
	// data
	private String predicate; 
	private String attribute;	// context attribute
	private String operator;	// comparison operator
	private String constant;	// constant
//	private String domain;
//	private int type; // 1 -- and, 0 -- or
	
	// constructor
	public QueryCondition(String predicate) {
		// predicate format: domain.attribute = constant
//		System.out.println("[QueryCondition.constructor].predicate: " + predicate);
		this.predicate = predicate;
		String[] array = predicate.split(" ");
//		String rawAttribute = array[0];
//		this.domain = rawAttribute.substring(0, rawAttribute.indexOf("."));
//		this.attribute = rawAttribute.substring(rawAttribute.indexOf(".")+1);
		this.attribute = array[0];
		this.operator = array[1];
		String rawConstant = array[2];
		this.constant = rawConstant.substring(1, rawConstant.length()-1);
	}
	public QueryCondition(String attribute, String operator, String constant) {
//		this.domain = domain;
		this.attribute = attribute;
		this.operator = operator;
		this.constant = constant;
		this.predicate = attribute + " " + operator + " " + "\"" + "";
	}
//	public QueryCondition(String attribute, String operator, String constant, int type) {
//		this.attribute = attribute;
//		this.operator = operator;
//		this.constant = constant;
////		this.type = type;
//	}
	
	// methods
	public String getAttribute() {
		return this.attribute;
	}
	
	public String getOperator() {
		return this.operator;
	}
	
	public String getConstant() {
		return this.constant;
	}
	
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	} 
	
	public void setOperator(String operator) {
		this.operator = operator;
	} 
	
	public void setConstant(String constant) {
		this.constant = constant;
	}
	
	public void appendConstant(String s) {
		this.constant += s;
	}
	
	public String toString() {
		String s = "";
//		s += "Domain-" + domain + "\n\r";
		s += "Attribute-" + attribute + "\n\r";
		s += "Operator-" + operator + "\n\r";
		s += "Constant-" + constant + "\n\r";
		return s;
	}
}