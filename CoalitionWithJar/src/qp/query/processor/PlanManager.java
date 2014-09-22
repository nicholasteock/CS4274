/**
 *@author Ivan
 *@create data: 4 SEP 2013
 *@description
 *1) An object to manage the various processing trees
 * */
package qp.query.processor;

import experiment.data.ExportData;
import qp.query.parser.QPQueryObject;
import qp.query.parser.QPQueryParser;
import qp.query.plan.PlanTree;

public class PlanManager {
	// data
	private String querySignature;
	private String contextQuery;
	private String queryIssuer;
	
	// constructor
	public PlanManager(String signature, String query, String issuer) {
		this.querySignature = signature;
		this.contextQuery = query;
		this.queryIssuer = issuer;
	}
	
	// methods
	// methods
	// methods
	public String executeQuery() {
		System.out.println("Into executeQuery in PlanManager");
		long beginOne = System.currentTimeMillis();
		
		long subOneBegin = System.currentTimeMillis();
		QPQueryObject queryObject = QPQueryParser.parseQuery(contextQuery);
		long subOneEnd = System.currentTimeMillis();
		long queryObjectTime = subOneEnd - subOneBegin;
		ExportData.writeLog("query_object_time",""+queryObjectTime);
		System.out.println("[QUERY_OBJECT_TIME] : " + queryObjectTime);
		
//		System.out.println("[PlanManager.executeQuery].queryObject: " + queryObject.getQueryString());
		
		long subTwoBegin = System.currentTimeMillis();
		PlanTree planTree = new PlanTree(queryObject);
		long subTwoEnd = System.currentTimeMillis();
		long planTreeTime = subTwoEnd - subTwoBegin;
		ExportData.writeLog("plan_tree_time",""+planTreeTime);
		System.out.println("[PLAN_TREE_TIME] : " + planTreeTime);
		
		long subThreeBegin = System.currentTimeMillis();
		ProcessorTree processorTree = new ProcessorTree(planTree,querySignature, queryIssuer);
//		ProcessorTree processorTree = new ProcessorTree(planTree,querySignature);
		long subThreeEnd = System.currentTimeMillis();
		long processorTreeTime = subThreeEnd - subThreeBegin;
		ExportData.writeLog("processor_tree_time",""+processorTreeTime);
		System.out.println("[PROCESSOR_TREE_TIME] : " + processorTreeTime);
		
		long subFourBegin = System.currentTimeMillis();
		processorTree.distributeLeafNode();
		long subFourEnd = System.currentTimeMillis();
		long distributionTime = subFourEnd - subFourBegin;
		ExportData.writeLog("distribution_time",""+distributionTime);
		System.out.println("[DISTRIBUTION_TIME] : " + distributionTime);
		
		
		long endOne = System.currentTimeMillis();
		long parseDistributionTime = endOne - beginOne;
		ExportData.writeLog("parse_distribute_time",""+parseDistributionTime);
		System.out.println("[PARSE_DISTRIBUTE_TIME] : " +parseDistributionTime);
		
		long beginTwo = System.currentTimeMillis();
		
//		long subFiveBegin = System.currentTimeMillis();
//		// wait for 5 seconds
//		try {
////			Thread.sleep(15000);
////			System.out.println("[PlanManager.executeQuery]: finished sleeping!");
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		long subFiveEnd = System.currentTimeMillis();
//		long dataReportingTime = subFiveEnd - subFiveBegin;
//		ExportData.writeLog("data_reporting_time",""+dataReportingTime);
		
		// retrieve result		
		long subSixBegin = System.currentTimeMillis();
//		System.out.println("[PlanManager.executeQuery]: begin to retrieve data!");
		processorTree.retrieveLeafData();
		String result =  processorTree.getResult();
		long subSixEnd = System.currentTimeMillis();
		long dataProcessTime = subSixEnd - subSixBegin;
		ExportData.writeLog("data_process_time",""+dataProcessTime);
		System.out.println("[DATA_PROCESS_TIME] : "+dataProcessTime);
		
		long endTwo = System.currentTimeMillis();
		long reportProcessTime = endTwo - beginTwo;
		ExportData.writeLog("report_process_time",""+reportProcessTime);
		System.out.println("[REPORT_PROCESS_TIME] : " + reportProcessTime);
		
		return result;
	}
//	public String executeQuery() {
//		long beginOne = System.currentTimeMillis();
//		
////		QPQueryObject queryObject = QPQueryParser.parseQuery(contextQuery);
////		System.out.println("[PlanManager.executeQuery].queryObject: " + queryObject.getQueryString());
////		PlanTree planTree = new PlanTree(queryObject);
////		ProcessorTree processorTree = new ProcessorTree(planTree,querySignature, queryIssuer);
////		processorTree.distributeLeafNode();
//		
//		
//		long endOne = System.currentTimeMillis();
//		long parseDistributeTime = endOne - beginOne;
//		ExportData.writeLog("parse_distribute_time",parseDistributeTime);
//		// wait for 5 seconds
//		try {
////			Thread.sleep(500);
//			System.out.println("[PlanManager.executeQuery]: finished sleeping!");
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		// retrieve result
//		long beginTwo = System.currentTimeMillis();
//		
//		System.out.println("[PlanManager.executeQuery]: begin to retrieve data!");
//		processorTree.retrieveLeafData();
//		String result = processorTree.getResult();
//		
//		long endTwo = System.currentTimeMillis();
//		long reportProcessTime = endTwo - beginTwo;
//		ExportData.writeLog("report_process_time",reportProcessTime);
//		return result;
//	}
}
