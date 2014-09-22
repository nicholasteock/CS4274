package run;

public class RunMCS {

//	public static String IP_Address = "localhost";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{


			// - (1) Run CSM
			System.err.println("[ "+csm.config.CSMConfiguration.resourceName+" ] "+"<1> Loading CSM ... <1>");
			csm.deployment.CSMStarter.main(args);
			Thread.sleep(1000);

			// - (2) Run Context Space Gateways Generators
			System.err.println("[ "+csg.config.CSGConfiguration.RESOURNCE_NAME+" ] "+"<2> Loading CSG Gen ... <3>");
			csg.kernel.csg.generator.CSGGeneratorStarter.main(args);
			Thread.sleep(1000);

			// - (3) Run Semantic Cluster Generators
			System.err.println("[ "+sc.config.SCConfiguration.RESOURCE_NAME+" ] "+"<3> Loading SC Gen ... <2>");
			sc.kernel.scgenerator.SCGeneratorStarter.main(args);
			Thread.sleep(1000);


			// - (4) Run Query Processor [Chung]
//			System.err.println("[ QueryProcessor ] "+"<4> Loading Chung QP ... <4>");
//			qp.demo.QPStarter.main(args);
//			Thread.sleep(1000);

			/*
			// - (5) Run Query Processor [Health-care]
			System.err.println("[ "+kernel.udp.config.config.resourceName+" ] "+"<5> Loading Health-Care QP ... <5>");
			healthcare.queryprocessor.demo.demo.main(args);
			Thread.sleep(1000);
			*/


			// - (6) Run Global Schema Server
			//System.err.println("[ "+kernel.udp.config.config.resourceName+" ] "+"<6> Loading Global Schema Server ... <6>");
			//globalSchemaServer.demo.demo.main(args);
			//Thread.sleep(1000);
			/*
			// - (7) Run Registration Manager
			System.err.println("[ "+kernel.udp.config.config.resourceName+" ] "+"<7> Loading Registration Manager ... <7>");
			registrationManager.demo.demo.main(args);
			Thread.sleep(1000);
			*/
			// - (8) PSG Simulator
//			System.err.println("[ "+psg.config.PSGConfiguration.RESOURCE_NAME+" ] "+"<8> Loading PSG Simulator ... <8>");
//			psg.consoleUi.PSGStarter.main(args);
//			Thread.sleep(1000);//
			


		}
		catch (Exception e){
			e.printStackTrace();
		}


	}

}
