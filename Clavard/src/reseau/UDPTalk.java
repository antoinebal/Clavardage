package reseau;


import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.InetAddress;


public class UDPTalk {
    private int port_;
    private DatagramSocket dgramSocket_;
    private DatagramSocket broadcastSocket_;

    UDPTalk(int port) {
	port_=port;
	try {
	    dgramSocket_ = new DatagramSocket();
	    broadcastSocket_ = new DatagramSocket();
	    broadcastSocket_.setBroadcast(true);
	} catch(SocketException e) {
	    System.out.println("PB dans startUDPTalk.");
	    e.printStackTrace();
	}
	System.out.println("UDPTalk : crée");
    }

    public void sendMessageUDP(String message, InetAddress address) {
	try {
	    DatagramPacket outPacket = new DatagramPacket(message.getBytes(), message.length(), address, port_);
	    dgramSocket_.send(outPacket);

	    //récéption
	    //byte[] buffer = new byte[256];
	    //DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
	    //dgramSocket_.receive(inPacket);
	    //String response = new String(inPacket.getData(), 0, inPacket.getLength());
	    //System.out.println(response);
	    //dgramSocket_.close();
	} catch(Exception e) {
	    e.printStackTrace();
	} 
    }


      public void broadcastMessage(String message) {
	try {
	    InetAddress address = InetAddress.getByName("255.255.255.255");
	    DatagramPacket outPacket = new DatagramPacket(message.getBytes(), message.length(), address, port_);
	    dgramSocket_.send(outPacket);

	     System.out.println("UDPTalk : "+message+" broadcasté.");

	     //PARTIE ACK SUPPRIMEE
	    //récéption
	    /*byte[] buffer = new byte[256];
	    DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
	    dgramSocket_.receive(inPacket);
	    String response = new String(inPacket.getData(), 0, inPacket.getLength());
	    System.out.println("UDPTalk : ACK reçu : "+response);*/
	    //dgramSocket_.close();
	} catch(Exception e) {
	    e.printStackTrace();
	} 
    }

    /*fonction appelée quand on éteint l'IR.
      On ferme les deux sockets */
    public void terminer() {
	dgramSocket_.close();
	broadcastSocket_.close();
	System.out.println("UDPTalk:OVER");
    }
    

    /*
    public static void main(String[] args) {
	UDPTalk udpt = new UDPTalk(1234);
	Scanner scan = new Scanner(System.in);
	String input=null;
	try {
	while (true) {
	    System.out.println("Attente entrée message.");
	    if (scan.hasNext()) {
		input = scan.nextLine();
		System.out.println("Entrée prise en compte : "+input);
	    }
	    udpt.sendHello(InetAddress.getLocalHost());
	}
	} catch(UnknownHostException e) {
	     System.out.println("PB dans main.");
	     e.printStackTrace();
	}
	}*/
    
    
}
