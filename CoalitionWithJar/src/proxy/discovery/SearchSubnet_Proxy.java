package proxy.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;


/**
 * @author Sathiya
 * Proxy code for performing Datagram based subnet search for proxy
 */
public class SearchSubnet_Proxy {
	boolean active = true; // default start in active mode
	byte[] gatewayMac = null;
	InetAddress src = null;
	InetAddress srcBcast = null;
	
	static DatagramSocket startJoinListener_socket = null;
	static DatagramSocket startActive_socket = null;
	static DatagramSocket startPassive_socket = null;
	
	int PSG_AD_LISTENPORT = 12344;
	int PSG_JOIN_LISTENPORT = 12345;
	int PSG_ADPORT = 12346;
	int PSG_JOINOKPORT = 12347;
	
	/**
	 * Initialize the broadcast address required
	 * @param devicename: name of the interface to use; boolean: active or passive mode  
	 */
	@SuppressWarnings({"rawtypes" })
	public
	SearchSubnet_Proxy(boolean active) throws Exception {
		startJoinListener_socket = new DatagramSocket(PSG_JOIN_LISTENPORT);
		startActive_socket = new DatagramSocket();
		startPassive_socket = new DatagramSocket(PSG_AD_LISTENPORT);
		
		Enumeration list = NetworkInterface.getNetworkInterfaces();

        while(list.hasMoreElements()) {
            NetworkInterface iface = (NetworkInterface) list.nextElement();
            if(iface == null) continue;
            if(!iface.isLoopback() && iface.isUp()) {
                Iterator it = iface.getInterfaceAddresses().iterator();
                while (it.hasNext()) {
                    InterfaceAddress address = (InterfaceAddress) it.next();
                    if(address == null) continue;
                    if (!address.getAddress().isAnyLocalAddress() && 
                    		!address.getAddress().isLinkLocalAddress() && 
                    		!address.getAddress().isLoopbackAddress()) {
                    	if (address.getAddress()  instanceof Inet4Address) {
                    		src = address.getAddress();
                    		srcBcast = address.getBroadcast();
                    		break;
                    	}
                    }
                }
            }
        }
        System.out.println("Got local ip and bcast: " + src + "," + srcBcast);
		this.active = active;
	}
	
	/**
	 * Creates a custom UDP packet for the given message string
	 * @parameters message and destination
	 * @return UDP Packet which was created
	 *
	 */
	private DatagramPacket createUDP(String message, InetAddress dst, int port) {
		byte[] sendData = message.getBytes();
		
		DatagramPacket udp = null;
		
		try {
			udp = new DatagramPacket(sendData, sendData.length, dst, port);
		} catch (Exception e) {
			System.out.println("Error in creating UDP packet to send");
			e.printStackTrace();
		}
		//System.out.println("Data sent (1): " + udp.getData()+42);
	//	System.out.println("Data sent (2): " + (udp.getLength() + 42));
        return udp;
	}
	
	/**
	 * Starts the proxy in active mode and runs continuously. 
	 * Sends 1 advertisement per 2 seconds.
	 */
	public void startActive() {
		System.out.println("Starting join listener");
		Thread joinlistener = new Thread() {
			public void run() {
				startJoinListener();
			}
		};
		joinlistener.start();
		System.out.println("Join listener started");
		
		try {
			startActive_socket.setBroadcast(true);
//			System.out.println("Created new socket and set bcast to true");
		} catch (SocketException e) {
			System.out.println("Error in creating socket to send packet");
			e.printStackTrace();
		}
		
		while(true) {
			if (active) {
				DatagramPacket udp = createUDP("MOBILE_REG_REQ_IAMPROXY", srcBcast, PSG_ADPORT);
				try {
//					System.out.println("Sending new advertisement");
					startActive_socket.send(udp);
					//System.out.println("Advertisement sent");
				} catch (IOException e) {
					System.out.println("Error in sending MOBILE_REG_REQ_IAMPROXY");
					e.printStackTrace();
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("Error in thread sleep in Active mode");
					e.printStackTrace();	
				}
			} else {
				break;
			}
		}
	}
	
	public void startPassive() {
		byte[] recvBuf;
		DatagramPacket udprcvd = null;
		DatagramPacket udpsend = null;
		try {
			startPassive_socket.setBroadcast(true);
			startPassive_socket.setSoTimeout(2000);
		} catch (SocketException e) {
			System.out.println("Error in creating socket to send packet");
			e.printStackTrace();
		}
		
		while (true) {
			if (!active) {
				recvBuf = new byte[15000];
				udprcvd = new DatagramPacket(recvBuf, recvBuf.length);
				if (udprcvd != null) {
					try {
						//System.out.println("Waiting for message from psg");
						startPassive_socket.receive(udprcvd);
						//System.out.println("Received data length (1): " + udprcvd.getData().length + 42);
						System.out.println("Received data length (2): " + (udprcvd.getLength() + 42));
					} catch (IOException e) {
						//System.out.println("IAMPSG Receive: Error in receiving UDP packet");
					//	e.printStackTrace();
					}
					String message = new String(udprcvd.getData()).trim();
					if (message.equalsIgnoreCase("MOBILE_REG_REQ_IAMPSG")) {
						System.out.println("Got IAMPSG message");
						udpsend = createUDP("MOBILE_REG_REQ_IAMPROXY", udprcvd.getAddress(), PSG_ADPORT);
						try {
							startPassive_socket.send(udpsend);
						} catch (IOException e) {
							System.out.println("Error in sending MOBILE_REG_REQ_IAMPROXY");
							e.printStackTrace();
						}
						udpsend = null;
						udprcvd = null;
							
						Thread joinlistener = new Thread() {
							public void run() {
								startJoinListener();
							}
						};
						joinlistener.start();
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("Error in thread sleep in Passive mode");
					e.printStackTrace();
				}
				recvBuf = null;
			} else {
				break;
			}
		}
	}
	
	private void startJoinListener() {
		byte[] recvBuf;
		DatagramPacket udprcvd = null;
		DatagramPacket udpsend = null;

		while (true) {
			recvBuf = new byte[15000];
			udprcvd = new DatagramPacket(recvBuf, recvBuf.length);
			System.out.println("Waiting to receive a new join request");
			if (udprcvd != null) {
				try {
					startJoinListener_socket.receive(udprcvd);
					System.out.println("New data received");
					System.out.println("Received data length (2): " + (udprcvd.getLength() + 42));
				} catch (IOException e) {
					System.out.println("Error in receiving UDP packet");
					e.printStackTrace();
					break;
				}
				String message = new String(udprcvd.getData()).trim();
				System.out.println("Checking if it is join request");
				if (message.equalsIgnoreCase("MOBILE_REG_REQ_JOIN")) {
					System.out.println("Yes. join request. preparing join ok message for " + udprcvd.getAddress());
					udpsend = createUDP("MOBILE_REG_RESP_JOINOK", udprcvd.getAddress(), PSG_JOINOKPORT);
					try {
						startJoinListener_socket.send(udpsend);
						System.out.println("Sent join ok message");
					} catch (IOException e) {
						System.out.println("Error in sending MOBILE_REG_RESP_JOINOK");
						e.printStackTrace();
					}
					udpsend = null;
					udprcvd = null;
					break;
				}
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.out.println("Error in thread sleep in Join listener mode");
				e.printStackTrace();
			}
			recvBuf = null;
		} 
		if (active) {
			Thread joinlistener = new Thread() {
				public void run() {
					startJoinListener();
				}
			};
			joinlistener.start();
		}
	}
	
	public void changeMode() {
		if (active) {
			System.out.println("Currently in active mode");
			active = false;
			Thread startInPassive = new Thread() {
				public void run () {
					startPassive();
				}
			};
			startInPassive.start();
			System.out.println("Changed to passive mode");
		} else {
			System.out.println("Currently in passive mode");
			active = true;
			Thread startInActive = new Thread() {
				public void run () {
					startActive();
				}
			};
			startInActive.start();
			System.out.println("Changed to active mode");
		}
	}
}
