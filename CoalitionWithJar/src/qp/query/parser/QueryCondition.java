/**
 *@author Ivan
 *@Modified data: 16 May 2013
 *@description
 *1) An object to represent the context query plan tree 
 * */
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
package qp.query.parser;

public class QueryCondition {
	// data
//	private String predicate;	// condition in string format
	private String attribute;	// context attribute
	private String dataType;	// the data type of attribute
	private String operator;	// comparison operator
	private String constant;	// constant
	private int type; // 1 -- and, 0 -- or
	
	// constructor
	public QueryCondition(String attribute, String operator, String constant) {
		this.attribute = attribute;
		this.operator = operator;
		this.constant = constant;
	}
	
	public QueryCondition (String predicate) {
		String[]  strA = predicate.split(" ", 3);
		// constant parsing can be added here
		strA[2] = strA[2].replaceAll("\"", ""); // remove the quotation mark in constant string
//		return new QueryCondition(strA[0],strA[1],strA[2]);
		this.attribute = strA[0];
		this.operator = strA[1];
		this.constant = strA[2];		
	}
	
	// methods

	// accessors
	public String getAttribute() {
		return this.attribute;
	}
	
	public String getOperator() {
		return this.operator;
	}
	
	public String getConstant() {
		return this.constant;
	}
	
	public String getDataType() {
		return this.dataType;
	}
	
	public String getPredicate() {
		return attribute + " " + operator + " " + "\"" + constant + "\"";
	}
	
	// mutators
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
	
	public void setPredicate(String predicate) {
		String[]  strA = predicate.split(" ", 3);
		// constant parsing can be added here
		strA[2] = strA[2].replaceAll("\"", ""); // remove the quotation mark in constant string
		this.attribute = strA[0];
		this.operator = strA[1];
		this.constant = strA[2];	
	}
}