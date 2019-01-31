package reseau;


import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class UDPListener implements Runnable {
    private int port_;
    private InterfaceReseau ir_;
    private DatagramSocket dgramSocket_;
    
    UDPListener(int port, InterfaceReseau ir) {
	port_=port;
	ir_=ir;
    }

    public void run() {
	System.out.println("UDPListener : lance");
	try {
	    dgramSocket_ = new DatagramSocket(port_);
	    dgramSocket_.setSoTimeout(60*1000*60); //on laisse un timeout de 1 heure
	    byte[] buffer = new byte[256];
	    DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
	    while(!ir_.isTermine()) {
		System.out.println("UDPListener : attend de recevoir un message");
		dgramSocket_.receive(inPacket);
		//System.out.println("UDP Listener a re�u un message");
		InetAddress clientAddress = inPacket.getAddress();
		int clientPort = inPacket.getPort();
		String message = new String(inPacket.getData(), 0, inPacket.getLength());

		if ((message.equals(ir_.getPseudo()+":tchao:0"))||(message.equals(ir_.getPseudo()+":tchao:1"))) {
		    dgramSocket_.close();
		    System.out.println("UDPListener : socket ferme.");
		    break;
		}
		
		ir_.recevoirMessageUDP(clientAddress, clientPort, message);

		//PARTIE ACK SUPPRIMEE
		/*
		String response = "UDPListener : Bien recu ton message UDP : "+message;
		DatagramPacket outPacket = new DatagramPacket(response.getBytes(), response.length(), clientAddress, clientPort);
		dgramSocket.send(outPacket);*/
	    }
	    //si on est sorti de la boucle, on peut close le socket
	    termine();
	} catch(SocketTimeoutException e) {
	    System.out.println("UDPListener : Le timeout de UDPListener a expire on close le socket.");
	    termine();
	} catch(SocketException e) {
	    System.out.println("PB dans startUDPListener.");
	    e.printStackTrace();
	} catch(IOException e) {
	    System.out.println("PB dans startUDPListener.");
	    e.printStackTrace();
	}
    }

    public void termine() {
    	dgramSocket_.close();
    	System.out.println("UDPListener : socket closed");	
    }


}
