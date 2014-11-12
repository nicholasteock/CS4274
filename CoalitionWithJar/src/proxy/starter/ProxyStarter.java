package proxy.starter;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import proxy.connectionmanager.TCP_Session_Handler;
import proxy.discovery.SearchSubnet_Proxy;

public class ProxyStarter {

	// Data on proxy socket
	public static String proxyIP = "192.168.173.1";  // IP of proxy 
	static int SERVERPORT = 5000;  // Port where proxy is listening for PSG registration through discovery
	static boolean active = true; // select mode of proxy here; active -> true, passive -> false


	private static int StartDiscoveryProtocol() {
		// Initiate a thread for running proxy discovery in background
		final SearchSubnet_Proxy search_first_try;
		try {
			search_first_try = new SearchSubnet_Proxy(active);
		} catch(Exception e) {
			e.printStackTrace();
			return -1; // if there is exception in enabling discovery, stop proxy 
		}
		Thread startDiscoveryModule = new Thread() {
			public void run() {
				if (active) {
					System.out.println("Starting in active mode");
					search_first_try.startActive();
				} else {
					System.out.println("Starting in passive mode");
					search_first_try.startPassive();
				}
			}
		};
		startDiscoveryModule.start();
		return 0;
	}
	
    private static int StartTCPSessionManager() {
        Socket socket;   
        ServerSocket serverSocket;
        final TCP_Session_Handler tcpsession = new TCP_Session_Handler();
     
        try {
            InetAddress thisIp = InetAddress.getByName(proxyIP);
            serverSocket = new ServerSocket(SERVERPORT, 10, thisIp);
               System.out.println("ServerSocket created on ip " + serverSocket.getInetAddress() + ", listening on port " + SERVERPORT);
              
            while (Boolean.TRUE) {
            	System.out.println("-------------------->>>Waiting for new connections...");
                socket = serverSocket.accept();
                System.out.println("New client joined..");
                // Register the new client in Proxy
                final Socket sock = socket;
                Thread handleNewClient = new Thread() {
                    public void run() {
                        tcpsession.RegisterMPSG(sock);
                    }
                };
                handleNewClient.start();   
                socket = null;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
	
	public static void main(String args[]) {
		int discovery_starter = StartDiscoveryProtocol();
		if (discovery_starter == -1) 
			System.exit(0); 
		 
		int tcp_starter = StartTCPSessionManager();
		if (tcp_starter == -1)
			System.exit(0);
	}
}
