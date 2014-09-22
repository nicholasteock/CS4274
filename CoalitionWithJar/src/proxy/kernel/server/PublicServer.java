package proxy.kernel.server;

//package org.apache.xmlrpc.demo.webserver;

import java.net.InetAddress;

import kernel.network.server.UDPServer;
import psg.service.manager.*;
import psg.config.PSGConfiguration;
import psg.kernel.connection.ConnectionManager;
//import psg.kernel.psgManager.PSGManager;
import psg.query.processor.*;
import qp.kernel.api.QPManager;


public class PublicServer {
	// data
	private String name;
    private int port;// = 8001;
    private UDPServer server;
    
    // constructor
    public PublicServer(String name, int incomingPort) throws Exception {
    	this.name = name; 
    	this.port = incomingPort;        
      
        // modified by Ivan, 25 Apr 2012
//    	System.out.println("[ "+config.config.resourceName+" ] "+"[ Server ] Preparing Resource.");
//    	System.out.println("[ "+config.config.resourceName+" ] "+"[ Server ] Starting Resource Package on Localhost ["+incomingPort+"]");    	
    	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"[ Server ] Preparing Resource.");
    	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"[ Server ] Starting Resource Package on Localhost ["+incomingPort+"]");    	
    	// end of modification, mainly modify the resource name to default name
    	
    	this.server = new UDPServer(port); 	  	
//        server.addHandler("QueryP2P",
//                psg.kernel.api.McsAPI.class);       
        server.addHandler("RequestsManager",RequestsManager.class);
        server.addHandler("QPManager", QPManager.class);
//        server.addHandler("PSGManager",
//                psg.kernel.psgManager.PSGManager.class);

  	  	
//  	System.out.println("[ "+config.config.resourceName+" ] "+"[ Server ] Ready.\n\n");
  	  	System.out.println("[ "+PSGConfiguration.defaultName+" ] "+"[ Server ] Ready.\n\n");
//        server.start();
    }
    
    //methods
    // actually this method is not used at all, should be deleted, 30 April 2012, Ivan
    public void start() throws Exception{
    	server.start();
    	//webServer.start();	
    }
    
    public void close() {
    	server.stop();
    }

}
