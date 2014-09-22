package proxy.connectionmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import experiment.data.ExportData;
import proxy.service.manager.ContextDataService;


public class TestQuery implements Runnable{
	// data
	private String index; // index for the experimental results for different issuers
	private String reference;
	private ContextDataService cds;
	
	private long[] timeList;
	// constructor
	public TestQuery(String defaultIP, int port) {
		this.index = defaultIP;
		this.cds = new ContextDataService();
		this.reference = "http://" + defaultIP + ":" + (13001+port) + "/xmlrpc";
		// Ivan, 16 Dec 2013: initialize the time
		try {
			BufferedReader br = new BufferedReader(new FileReader("./data/0/0.05_" +port+".txt"));
			String line = br.readLine();
			String[] intervalList = line.split(" ");
			
			timeList = new long[intervalList.length];
			timeList[0] = System.currentTimeMillis() + Integer.parseInt(intervalList[0]);
			for(int i=1; i<timeList.length; i++) {
				timeList[i] = timeList[i-1] + Integer.parseInt(intervalList[i]);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// end
	}
	// methods
	@Override
	public void run() {
//		for(int j=1; j<2; j++) {
//			int j = 1;
//			String directory = "D:\\Experiment\\" + index + "\\verion-3.1-"+j;
//			ExportData.setDir(directory);
			long totalBegin = System.currentTimeMillis();
		long currentTime;
		for(int i=0; i<timeList.length; i++) {
			currentTime = System.currentTimeMillis();
			if(currentTime < timeList[i]) {
				try {
					Thread.sleep(timeList[i] - currentTime);
				} catch(InterruptedException ie) {
					ie.printStackTrace();
				}
			} 
//			for(int i=0; i<1*j; i++) {
//				String query = "select name from person where name = \"IVAN\"";
//				String query = "select person.speed, person.action, person.mood, person.location,"
//						+ " office.name, office.isMeeting, office.location"
//						+ " from person, office "
//						+ " where ( person.preference = \"book\" "
//						+ " AND person.location = \"vivo\" "
//						+ " AND person.speed = \"0\" "
//						+ " AND person.isBusy = \"0\" )"
//						+ " OR ( office.lightOn = \"0\""
//						+ " AND office.location = \"NUS\" ) ";	
				// Ivan, 2 Oct 2013: preference or shop.type cannot by "computer"
				// computer may confilict with some keywords in data transmission
				String query = " SELECT person.name, person.preference, shop.name, shop.type "
						+ " FROM person, shop "
						+ " WHERE "
						+ " ( ( person.location = \"vivo\""
						+ " AND person.preference = \"book\" ) "
						+ " AND ( shop.location = \"vivo\""
						+ " AND shop.type = \"book\" ) )"
						+ " OR ( ( person.location = \"jp\""
						+ " AND person.preference = \"pc\" )"
						+ " AND ( shop.location = \"jp\""
						+ " AND shop.type = \"pc\" ) )"
						+ " OR ( ( person.location = \"ion\""
						+ " AND person.preference = \"gift\" )"
						+ " AND ( shop.location = \"ion\""
						+ " AND shop.type = \"gift\" ) )";
//				String query = "SELECT person.name FROM person WHERE person.location = \"vivo\"";
//				long queryBegin = System.currentTimeMillis();
				String result =  cds.queryContext(reference,query,reference);
//				long queryEnd = System.currentTimeMillis();
//				long singleQueryTime = queryEnd - queryBegin;
				long singleQueryTime = System.currentTimeMillis() - timeList[i];
//				System.err.println("[Query Result]: "+result);
				ExportData.writeLog("final_query_result", result);
				System.err.println("[Single Query Time ]: "+singleQueryTime);
				ExportData.writeLog("single_query_time",""+singleQueryTime);
			}
			long totalEnd = System.currentTimeMillis();
			long totalQueryTime = totalEnd - totalBegin;
//			//			System.err.println("[Query Result]: "+result);
			System.err.println("[Total Query Time]: "+totalQueryTime);
			ExportData.writeLog("total_query_time",""+totalQueryTime);
//		}
	}
	// write experiment data to txt
//	public void writeLog(String fileName, double content) {
//		try{
//			FileWriter fstream = new FileWriter("D:\\Experiment\\"+fileName+".txt",true);
//			BufferedWriter out = new BufferedWriter(fstream);
//			out.write("" + content);
//			out.write(System.getProperty( "line.separator" ));
//			out.write(System.getProperty( "line.separator" ));
//			//Close the output stream
//			out.close();
//		}
//		catch (Exception e){//Catch exception if any
//			System.err.println("Error: " + e.getMessage());}
//	}
	
}
