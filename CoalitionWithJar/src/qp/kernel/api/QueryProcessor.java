/**
 * @modified: Ivan
 * @date 2 Jul 2012
 * @actions
 * 1) remove all PSG, SC and CSG related methods
 * 2) remove unnecessary parameters, such IP and Port
 * @descripiton
 * 1) This class is the place to processor each query, namely parse query,
 * distribute query and consolidate query result.
 * 2) Three parameters are important for this class: Query Signature (Identifier),
 * Query Content and Query Issuer.
 * */
package qp.kernel.api;

import java.util.Hashtable;
import java.util.Map;

import qp.kernel.locator.ComponentsLocator;
import qp.query.parser.QPQueryObject;
//import qp.io.config.Config;
import experiment.data.ExportData;

public class QueryProcessor {
	// data
	private final int RESULT_SIZE = 1;
	private final int TIME_THRESHHOLD = 1000;
	// private String qpReference = QPConfiguration.QP_REFERENCE;
	private ComponentsLocator myDiscoverer;

	private String querySignature;
	private QPQueryObject queryObject;
	private String issuerReference;
	private Hashtable<String, String> queryResult;

	// constructor
	public QueryProcessor(String querySignature, QPQueryObject queryObject,
			String issuerReference) {
		this.myDiscoverer = new ComponentsLocator();
		this.querySignature = querySignature;
		this.queryObject = queryObject;
		this.issuerReference = issuerReference;
		this.queryResult = new Hashtable<String, String>();
	}

	// methods
	// - Hash function for integrating all ValueType
	public double hashValue(String valueType, String Value) {
		// writeFile("Testing Beta 1", "HashValue -> "+
		// " ValueType = "+valueType+"; Value="+Value);
		if (valueType.equals("Integer")) {
			return new Integer(Value).intValue();
		} else if (valueType.equals("Double")) {
			return new Double(Value).doubleValue();
		} else if (valueType.equals("String")) {
			return Value.hashCode();
		} else {
			return -999;
		}
	}

	public String getType(String csgName, String scName) {
		if (csgName.toUpperCase().equals("SHOP")) {
			if (scName.toUpperCase().equals("NAME")) {
				return "String";
			} else if (scName.toUpperCase().equals("LOCATION")) {
				return "String";
			} else {
				return "String";
			}
		} else if (csgName.toUpperCase().equals("PERSON")) {
			if (scName.toUpperCase().equals("NAME")) {
				return "String";
			} else if (scName.toUpperCase().equals("PREFERENCE")) {
				return "String";
			} else if (scName.toUpperCase().equals("LOCATION")) {
				return "String";
			} else {
				return "String";
			}
		} else {
			return "String";
		}
	}

	// Support Cross Domain Query Lookup
	// Support RDF Query Currently
	public String queryMCS() {
		double parseBegin = System.currentTimeMillis();
		// Step 1: parse the query
		// Ivan, 3 Jul 2012: extract parameter values
		if (queryObject.getErrorString() != null) {
			System.out
					.println("[QueryProcessor.queryMCS].queryPlan.ErrorString:"
							+ queryObject.getErrorString());
			return "[INVALID QUERY FORMAT]";
		}

		String csgName = queryObject.getDomain().get(0);
		csgName = csgName.toUpperCase(); // confirm the case of csgName and
											// scName
		// Ivan, 18 Sep 2013:
		// There are two ways to identify the sc name of query
		// One is to use attributes in conditions
		// another one is to use attributes in selected attributes
		// In order to make it easier, we use selected attributes here
		// one assumption is that: only PSGs with selected attributes will be
		// returned
		String scName = queryObject.getAttributes().get(0);
		// String conditionString = queryObject.getQueryConditions()
		// .getCondition();
		// QueryCondition queryCondition = new QueryCondition(conditionString);
		// String scName = queryCondition.getAttribute();
		scName = scName.toLowerCase();
		// String valueType = getType(csgName, scName); //
		// queryPlan.getQueryConditions().get(0).getDataType();
		// String queryValue = queryCondition.getConstant();
		double x = 10.0;// hashValue(getType(csgName, scName), queryValue);

		// Step 2: distribute the query to PSGs
		// Ivan, 25 SEP 2013: distribute context query to corresponding PSGs
		String peerList = myDiscoverer.getPSGList(csgName, scName);
//		System.out.println("[QueryProcessor.queryMCS].peerList:" + peerList);
//		ExportData.writeLog("qp_peer_list", peerList.length() + " " + peerList);
		// String randomPeerList = myDiscoverer.getRandomPSGList(csgName,
		// scName);
		// System.out.println("[QueryProcessor.queryMCS].randomPeerList:" +
		// randomPeerList);

		distributeQuery(scName, peerList, x);

		double parseEnd = System.currentTimeMillis();
		double singelParseDistributeTime = parseEnd - parseBegin;
		ExportData.writeLog("single_parse_distribute_time", ""
				+ singelParseDistributeTime);
		System.out.println("[SINGLE_PARSE_DISTRIBUTE_TIME] : "+singelParseDistributeTime);
		return "";
	}
	
	// Ivan, 13 Dec 2013: distributed the query 
	// distributed context query to different PSGs in different RCs
	private void distributeQuery(String scName, String rcInformation, double x) {
		// Map<String, String> rcList = processRCList(rcInformation);
		// Ivan, 29 Aug 2012: parse the rcInformation
//		rcInformation = rcInformation.substring(rcInformation.indexOf("="), rcInformation.indexOf("_"));
//		ExportData.writeLog("query_processor_distribut_query", rcInformation);
		String[] referenceList = rcInformation.split("@");
		System.out.println("Input to distributeQuery : scName " + scName + ", rcInformation:" +rcInformation );

		for (int i = 0; i < referenceList.length; i++) {
//				ExportData.writeLog("qp_distribute_time",
//						"" + System.currentTimeMillis());
			System.out.println("[QueryProcessor.distributeQuery].reference: " + referenceList[i]);
			String[] psgInfor = referenceList[i].split("_");
			String psgName = psgInfor[0];
			String psgReference = psgInfor[1];
				myDiscoverer.forwardQuery(querySignature,
						queryObject.getQueryString(), issuerReference,
						psgName, psgReference, scName);
		} // end for
	}
	// end

//	// distributed context query to different PSGs in different RCs
//	private void distributeQuery(String scName, String rcInformation, double x) {
//		// Map<String, String> rcList = processRCList(rcInformation);
//		// Ivan, 29 Aug 2012: parse the rcInformation
//		String[] rcArray = rcInformation.split("@");
//		List<String> referenceList = new Vector<String>();
//		List<String> rangeList = new Vector<String>();
//		for (int i = 0; i < rcArray.length; i++) {
//			String element = rcArray[i];
//			if ((i % 2) == 0) {
//				String newValue = element.substring(element.indexOf("=") + 1,
//						element.length());
//				referenceList.add(newValue);
//			} else {
//				String newValue = element.substring(element.indexOf("=") + 1,
//						element.length());
//				rangeList.add(newValue);
//			}
//			// String newVar = element.substring(0, element.indexOf("="));
//		}
//
//		// Ivan, 4 Jul 2012: the reason of factored by 2 is that
//		// rcList contains all <RCi, reference> and <Range i, range>
//		// pairs, so should be factored by 2
//		// System.out.println("[QueryProcessor.distributedQuery].rcListSize:" +
//		// rcList.size()/2);
//		for (int i = 0; i < referenceList.size(); i++) {
//			String randomPeerAddress = referenceList.get(i);
//			String range = rangeList.get(i);
//			String minValueStr = range.substring(0, range.indexOf("|"));
//			String maxValueStr = range.substring(range.indexOf("|") + 1);
//
//			double max;
//			double min;
//			if (minValueStr.equals("4.9E-324")) {
//				min = Double.MIN_VALUE;
//
//			} else
//				min = new Double(minValueStr).doubleValue();
//
//			if (maxValueStr.equals("1.7976931348623157E308"))
//				max = Double.MAX_VALUE;
//			else
//				max = new Double(maxValueStr).doubleValue();
//
//			// Shubhabrata - Identify the range cluster
//
//			// Ivan, 4 Jul 2012: ?? not sure the correctness of this if
//			// condition
//			// Ivan, 21 Aug 2012: if x = -9999, send query to any one
//			if (x == -9999) {
//				myDiscoverer.forwardQuery(querySignature,
//						queryObject.getQueryString(), issuerReference,
//						randomPeerAddress, scName);
//			} else if (((i == 0) || (x >= min)) && (x <= max)
//					&& (!randomPeerAddress.equals("[NO PEER]"))) {
//				// Ivan, 14 Aug 2012: key point is change the qpReference to
//				// isserReference
//				// myDiscoverer.forwardQuery(querySignature, contextQuery,
//				// qpReference, randomPeerAddress, scName);
//				ExportData.writeLog("qp_distribute_time",
//						"" + System.currentTimeMillis());
//				myDiscoverer.forwardQuery(querySignature,
//						queryObject.getQueryString(), issuerReference,
//						randomPeerAddress, scName);
////				System.out
////						.println("[QueryProcessor.distributedQuery].Query TO PSG:"
////								+ randomPeerAddress);
//			}
//		} // end for
//	}

	// // distributed context query to different PSGs in different RCs
	// private void distributeQuery(String scName, String rcInformation, double
	// x) {
	// Map<String, String> rcList = processRCList(rcInformation);
	// // Ivan, 4 Jul 2012: the reason of factored by 2 is that
	// // rcList contains all <RCi, reference> and <Range i, range>
	// // pairs, so should be factored by 2
	// for (int i = 0; i < (rcList.size()) / 2; i++) {
	// String randomPeerAddress = rcList.get("RC" + i);
	// String range = rcList.get("Range" + i);
	// String minValueStr = range.substring(0, range.indexOf("|"));
	// String maxValueStr = range.substring(range.indexOf("|") + 1);
	//
	// double max;
	// double min;
	// if (minValueStr.equals("4.9E-324")) {
	// min = Double.MIN_VALUE;
	//
	// } else
	// min = new Double(minValueStr).doubleValue();
	//
	// if (maxValueStr.equals("1.7976931348623157E308"))
	// max = Double.MAX_VALUE;
	// else
	// max = new Double(maxValueStr).doubleValue();
	//
	// // Shubhabrata - Identify the range cluster
	// // Ivan, 4 Jul 2012: ?? not sure the correctness of this if
	// // condition
	// // Ivan, 21 Aug 2012: if x = -9999, send query to any one
	// if(x == -9999){
	// myDiscoverer.forwardQuery(querySignature, contextQuery, issuerReference,
	// randomPeerAddress, scName);
	// } else if
	// (((i==0)||(x>=min))&&(x<=max)&&(!randomPeerAddress.equals("[NO PEER]"))){
	// // Ivan, 14 Aug 2012: key point is change the qpReference to
	// isserReference
	// // myDiscoverer.forwardQuery(querySignature, contextQuery, qpReference,
	// randomPeerAddress, scName);
	// myDiscoverer.forwardQuery(querySignature, contextQuery, issuerReference,
	// randomPeerAddress, scName);
	// System.out.println("[QueryProcessor.distributedQuery].Query TO PSG:" +
	// randomPeerAddress);
	// }
	//
	// if (((i == 0) || (x >= min)) && (x <= max)
	// && (!randomPeerAddress.equals("[NO PEER]"))) {
	// myDiscoverer.forwardQuery(querySignature,
	// queryObject.getQueryString(), issuerReference,
	// randomPeerAddress, scName);
	// }
	// } // end for
	// }

	// get result
	public Hashtable<String, String> getResult() {
		// Step 3: consolidate the result
		// Sleep a period to wait for results to be reported
		 double processBegin = System.currentTimeMillis();
	
		int waitTime = 0;
		while (queryResult.size() < RESULT_SIZE && waitTime < TIME_THRESHHOLD) {
//			ExportData.writeLog("qp_get_result",
//					"" + System.currentTimeMillis() + " " + queryResult.size());
			try {
				Thread.sleep(20);
				waitTime += 20;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ExportData.writeLog("qp_simple_query_result", querySignature + "___" +queryResult.size());
		//
		// // return result
		// // String reply = consolidateResult();
		 double processEnd = System.currentTimeMillis();
		 double processTime = processEnd - processBegin;
		 ExportData.writeLog("qp_get_result_time", ""+processTime);
		 System.out.println("[QP_GET_RESULT_TIME] : " + processTime);
		return queryResult;
	}

	// consolidate context query result
	private String consolidateResult() {
		// Ivan, 3 Jul 2012: seems the following statemetents are used
		// for consolidating context query result
		int size = queryResult.size();
		// ExportData.writeLog("resultSize", size);
		if (size == 0) {
			return "[QUERY MISS]";
		} else {
			String result = "Results ";
			// for (String element : queryResult) {
			// if (!element.contains("MISS")) {
			// result += element;
			// }
			// }
			return "" + size + "" + result;
		}
	}

	// Ivan, 4 Jul 2012: process the returning string from RC for retrieving a
	// random PSG
	private Map<String, String> processRCList(String rcInformation) {
		Map<String, String> rcList = new Hashtable<String, String>();
		String[] rcArray = rcInformation.split("@");
		for (String element : rcArray) {
			String newVar = element.substring(0, element.indexOf("="));
			String newValue = element.substring(element.indexOf("=") + 1,
					element.length());
			rcList.put(newVar, newValue);
		}
		return rcList;
	}

	// Ivan, 12 SEP 2013: report result by PSGs
	public String reportResult(String answer) {
		System.out.println("[QueryProcessor.reportResult].answer: " + answer);
		if (answer.contains("[QUERY MISS]")) {
			System.out.println("[QueryProcessor.reportResult]: Query Miss!");
		} else {
			// Ivan, 20 SEP 2013: processor returning result
			String[] resultArray = answer.split(";");
			for (String answerValue : resultArray) {
				String[] result = answerValue.split("@");
				String key = result[0];
				String value = result[1];
				this.queryResult.put(key, value);
			}
//			ExportData.writeLog("result_size", "" + System.currentTimeMillis()
//					+ " " + queryResult.size());
		}
		return "[confirmed]";
	}

}
