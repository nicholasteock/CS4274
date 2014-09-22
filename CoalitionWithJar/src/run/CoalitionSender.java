/**
 * @author ivan
 * @date 23 SEP 2013
 * @objective
 * 1) UDP receiver for remote control of PSGs
 * */
package run;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class CoalitionSender {
	// data
	private final int size = 4096;
	
	private InetAddress receiverAddress; //= "locahost";
	private int receiverPort;
	
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private DatagramSocket socket;
	// constructor
	public CoalitionSender(String receiverIP, int receiverPort)  {
		try {
			this.receiverAddress = InetAddress.getByName(receiverIP);
			this.receiverPort = receiverPort;
			this.socket = new DatagramSocket();
		} catch (UnknownHostException e ) {
			e.printStackTrace();
		} catch (SocketException se) {
			se.printStackTrace();
		}
		//send(message);
	}
//	public TempUDPSender(InetAddress receiverAddress, int receiverPort) {
//		try {
//			this.receiverAddress = receiverAddress;
//			this.receiverPort = receiverPort;
//			this.socket = new DatagramSocket();
//		} catch (SocketException se) {
//			se.printStackTrace();
//		}
//		//send(message);
//	}
	// methods
	// sending
	public void send(String message) {
		try {
			byte[] data = message.getBytes();
		
			sendPacket = new DatagramPacket(data, data.length, receiverAddress, receiverPort);
		
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // end of send
	
	// receiving
	public String receiveReply() {		
		byte[] data = new byte[size];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			socket.receive(receivePacket);

		} catch (IOException e) {
			System.err.println("[ "+"UDP CLIENT"+" ] "+"UDP Packet loss: Re-sending..");
			e.printStackTrace();
		} 
		return new String(receivePacket.getData(),0,receivePacket.getLength());
	} // end of receive
	
	public void close() {
		System.out.println("[UDPSender.close].port: " + this.socket.getLocalPort());
		this.socket.close();
	}
	
}


