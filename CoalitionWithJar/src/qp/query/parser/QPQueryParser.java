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

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;



/**
 * This class implements a very simple query processor at a PSG.
 */
public class QPQueryParser {
	// data

	// constructor

	// methods
	// Ivan, 19 Jul 2012: rewrite the query parser
	public static QPQueryObject parseQuery(String query) {
		// Step 1: define query plan needed parameters
		// those parameters reflected the key words of query
		boolean hasSELECT = true;
		boolean hasSUBSCRIBE = true; // ignore subscription first
		boolean hasFROM = true;
		boolean hasWHERE = true;
		
		String selectPart = "";
		String subscribePart = "";
		String fromPart = "";
		String wherePart = "";
		
		// Query plan related variables;
		byte queryType; // select or subscribe
		String[] domainType; // which context domain is related, current support only one type of domain
		String[] requiredAttributes;
		QPConditionTree queryConditions;
		String errStr = null;
		
		// Step 2: query string analysis
		// the main idea is to split the string based on space
		// and then re-compose them based on keywords
		query = deleteDoubleSpaces(query.trim());
		String[] tempArray = query.split(" ");
		
		// recompose the string array to extract different part
		// assume key words are in the correct order
		String temp = "";
		for(int i=0; i<tempArray.length; i++) {
			String element = tempArray[i];
			if(element.equalsIgnoreCase("SELECT")) {
				hasSELECT = true;
			} else if(element.equalsIgnoreCase("SUBSCRIBE")) {
				hasSUBSCRIBE = true;
			} else if(element.equalsIgnoreCase("FROM")) {
				hasFROM = true;
				selectPart = temp;
				temp = "";
			} else if(element.equalsIgnoreCase("WHERE")) {
				hasWHERE = true;
				fromPart = temp;
				temp = "";
			} else {
				temp += (element + " ");
			}
		}
		// since where is the last key word, all else will be where
		// if there are other keywords behind where, those key words
		// will be here
		wherePart = temp;
		
		// Step 3: analyze each part
		queryType = parseType(hasSELECT, hasSUBSCRIBE);
//		System.out.println("[QueryParser.parseQuery].queryType: " + queryType);
		domainType = parseFrom(fromPart);
//		System.out.println("[QueryParser.parseQuery].fromPart: " + fromPart);
		requiredAttributes = parseSelect(selectPart);
//		System.out.println("[QueryParser.parseQuery].selectPart: " + selectPart);
		queryConditions = new QPConditionTree(wherePart);
//		System.out.println("[QueryParser.parseQuery].wherePart: " + wherePart);
		// query validity check is a huge problem which we have not fully 
		// covered here, like the domainType check, attribute check
		// and condition check, all those checks are related to a 
		// global schema mechanism
		errStr = parseValidity(query); 
		
		// Step 4: generate the query plan
		QPQueryObject qp = new QPQueryObject(query, queryType, convertList(domainType), 
				convertList(requiredAttributes), queryConditions, errStr);
		return qp;
	}
	
	private static byte parseType(boolean hasSELECT, boolean hasSUBSCRIBE) {
		if(hasSELECT) {
			return QPQueryObject.DATA_ACQUISITION;
		} else {
			return QPQueryObject.EVENT_SUBSCRIPTION;
		}
	}
	
	private static String[] parseFrom(String fromPart) {
		fromPart = deleteSpace(fromPart);
		return fromPart.split(",");
//		return fromPart.toUpperCase();
	}
	
	private static String[] parseSelect(String selectPart) {
		selectPart = deleteSpace(selectPart);
		// delimiter: ","
		return selectPart.toLowerCase().split(",");
	}
	
	// add by Ivan, 8 May 2012
	private static String deleteDoubleSpaces(String s) {
		while(s.contains("  ")) {
			s = s.replaceAll("  ", " ");
		}
		return s;
	}
	
	private static String deleteSpace(String s) {
		return s.replaceAll(" ", "");
	}
	
	
//	private Vector<QueryCondition> parseWhere(String whereClause) {
//		whereClause = deleteDoubleSpaces(whereClause);
//		whereClause = whereClause.trim();
//		Vector<QueryCondition> conditions = new Vector<QueryCondition>();
//
//		Vector<String> connectors = new Vector<String>();
//		Vector<String> phrases = new Vector<String>();
//		
//		// two cases: single condition or multiple condition
//		if(whereClause.contains(" and ") || whereClause.contains(" or ")) {
//			// Idea behind: split the whole phrase by empty space
//			// then, re-compose back different components
//			String[] whereArray = whereClause.split(" ");
//			String queryCondition = "";
//			for(String element : whereArray) {
//				if(element.equals("and") || element.equals("or")) {
//					phrases.addElement(queryCondition);	// use and or or as the trigger
//					connectors.addElement(element);
//					queryCondition = "";	// resent the phrase
//				} else {
//					queryCondition += (element + " "); // empty space is needed
//				}
//			}
//			phrases.addElement(queryCondition);	// add in the last condition phrase
//			
//			// convert the parsed phrases and connectors to query conditions
//			if(connectors.elementAt(0).equals("and")) {
//				conditions.addElement(parseSingleWhere(phrases.elementAt(0),AND));
//			} else {
//				conditions.addElement(parseSingleWhere(phrases.elementAt(0),OR));
//			}
//			
//			for(int i=0; i<connectors.size(); i++) {
//				if(connectors.elementAt(i).equals("and")) {
//					conditions.addElement(parseSingleWhere(phrases.elementAt(i+1),AND));
//				} else {
//					conditions.addElement(parseSingleWhere(phrases.elementAt(i+1),OR));
//				}
//			}
//		} else {
//			conditions.addElement(parseSingleWhere(whereClause,AND));
//		}
//		return conditions;
//	}
	

	
	// end of addition, Ivan 8 May 2012
	
	private static String parseValidity(String query) {
		query = query.toLowerCase();
		String error = null;
		boolean hasSELECT = true;
		boolean hasSUBSCRIBE = true;
		boolean hasFROM = true;
		boolean hasWHERE = true;
		if(query.indexOf("select ") == -1) {
			hasSELECT = false;
		}
		if(query.indexOf("subscribe ") == -1) {
			hasSELECT = false;
		}
		if(query.indexOf(" from ") == -1) {
			hasSELECT = false;
		}
		if(query.indexOf(" where ") == -1) {
			hasSELECT = false;
		}
		if(!((hasSELECT || hasSUBSCRIBE) || hasFROM) ) {
			error = "Invalid query string: '" + query + "' \r\n";
			error += "SELECT or SUBSCRIBE or FROM clause is missing";
			return error;
		}
		// check the sequence of sequence of keywords
		// should be SELECT or SUBSCRIBE < FROM < WHERE 
		boolean rightORDER = true;
		if (hasWHERE && hasSELECT) {
			if (query.indexOf("select ") > query.indexOf(" from ") 
				|| query.indexOf("select ") > query.indexOf(" where ") 
				|| query.indexOf("from ") > query.indexOf(" where ") ) {
				
				rightORDER = false;
			}
		} else if(hasWHERE && hasSUBSCRIBE) {
			if (query.indexOf("subscribe ") > query.indexOf(" from ") 
					|| query.indexOf("subscribe ") > query.indexOf(" where ") 
					|| query.indexOf("from ") > query.indexOf(" where ") ) {
					
					rightORDER = false;
				}
		} else if(!hasWHERE && hasSELECT) {
			if (query.indexOf("select ") > query.indexOf(" from ") ) {
					rightORDER = false;
				}
		} else if(!hasWHERE && hasSUBSCRIBE) {
			if (query.indexOf("subscribe ") > query.indexOf(" from ") ) {
				rightORDER = false;
			}
		}
		
		if(!rightORDER) {
			error = "Invalid query string: '" + query + "' \r\n";
			error += "SELECT or SUBSCRIBE or FROM clauses are not in right order!";
			return error;
		}
		return error;
	}
	
	// Ivan, 20 May 2012: convert array to list
	private static List<String> convertList(String[] arrayString) {
		List<String> listString = new Vector<String>();
		for(String item : arrayString) {
			listString.add(item);
		}
		return listString;
	}
	

	//testing main method
	public static void main(String[] args) {
//		QueryParser parser = new QueryParser();
		String queryString = "select name, location from person where name = \"IVAN\" AND location = \"IDMI\"";
		QPQueryObject qp = QPQueryParser.parseQuery(queryString);
		qp.print();
	}
	

}  // class