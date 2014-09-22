/**
 * @Author: Ivan
 * @Date: 12 SEP 2013
 * @Objective:
 * 1) Parse simple query only
 * */
package psg.query.processor;

import java.util.List;
import java.util.Vector;

public class PSGQueryParser {
	// data

	// constructor

	// methods
	// Ivan, 19 Jul 2012: rewrite the query parser
	public static PSGQueryObject parseQuery(String query) {
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
		String domainType; // which context domain is related, current support only one type of domain
		List<String> attributeList;
		PSGConditionTree queryCondition;
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
//		System.out.println("[QueryParser.parseQuery].fromPart: " + fromPart);
		domainType = fromPart.trim();
//		System.out.println("[QueryParser.parseQuery].selectPart: " + selectPart);
		attributeList = parseSelect(selectPart);
//		System.out.println("[QueryParser.parseQuery].requiredAttribute: " + attributeList);
		queryCondition = new PSGConditionTree(wherePart);
//		System.out.println("[QueryParser.parseQuery].wherePart: " + wherePart);
		// query validity check is a huge problem which we have not fully 
		// covered here, like the domainType check, attribute check
		// and condition check, all those checks are related to a 
		// global schema mechanism
		errStr = parseValidity(query); 
		
		// Step 4: generate the query plan
		PSGQueryObject qp = new PSGQueryObject(query, queryType, domainType, 
				attributeList, queryCondition, errStr);
		return qp;
	}
	
	private static byte parseType(boolean hasSELECT, boolean hasSUBSCRIBE) {
		if(hasSELECT) {
			return PSGQueryObject.DATA_ACQUISITION;
		} else {
			return PSGQueryObject.EVENT_SUBSCRIPTION;
		}
	}
	
//	private static String parseFrom(String fromPart) {
//		fromPart = deleteSpace(fromPart);
//		return fromPart.split(",");
////		return fromPart.toUpperCase();
//	}
	
	private static List<String> parseSelect(String selectPart) {
		selectPart = deleteSpace(selectPart);
		// delimiter: ","
		String[] array = selectPart.toLowerCase().split(",");
		List<String> list = new Vector<String>();
		for(String s:array) {
//			String attribute = s.substring(s.indexOf(".")+1);
			list.add(s);
		}
		return list;
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
	

	//testing main method
	public static void main(String[] args) {
//		QueryParser parser = new QueryParser();
//		String queryString = "select person.name, location from person where name = \"IVAN\" AND location = \"IDMI\"";
		String queryString = "select person.name from person where person.name = \"IVAN\"";
		PSGQueryObject qp = PSGQueryParser.parseQuery(queryString);
		qp.print();
	}
	

}  // class