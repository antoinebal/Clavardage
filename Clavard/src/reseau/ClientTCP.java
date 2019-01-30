package reseau;



import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.Socket;
import java.lang.InterruptedException;

public class ClientTCP {
    private Socket socket_;

    //port d'�coute du srv auquel le client doit se connecter
    private int port_;
    //port d'�coute du srv auquel le client doit se connecter
    private InetAddress address_;
    private TCPListener listener_;
    private InterfaceReseau ir_;
    private BlablaTCP bbTCP_;
    private String pseudoServeur_;

    ClientTCP(int port, InetAddress address, InterfaceReseau ir, String pseudoServeur) {
	address_=address;
	port_=port;
	ir_=ir;
	pseudoServeur_=pseudoServeur;
       	System.out.println("Client : cr�e pour contacter "+pseudoServeur_);
    }
    
    public void startClient() {
	try{
	    System.out.println("Client d�marre.");
	    socket_ = new Socket(address_, port_);
	    //socket_.setSoTimeout(50000);

	    bbTCP_ = new BlablaTCP(pseudoServeur_, ir_, socket_);
	    
	    //on construit et d�marre le listener
	    listener_ = new TCPListener(bbTCP_, socket_);
	    Thread threadListener = new Thread(listener_);
	    threadListener.start();
	     
	    /*on envoie notre pseudo pour être reconnu
	      on fait attendre un peu ce thread
	      pour laisser le temps au serveur d'être en mesure de lire le message*/
	    Thread.sleep(100);
	    //on envoie notre pseudo pour s'identifier
	    bbTCP_.envoyerPseudoLocal();
	    ir_.nouveauCorrespondant(pseudoServeur_, bbTCP_);
	} catch (UnknownHostException e) {
	    System.out.println("PB starClient : hote inconnu.");
	    e.printStackTrace();
	} catch (IOException e) {
	    System.out.println("PB starClient : creation socket.");
	    e.printStackTrace();
	}catch (InterruptedException e) {
	    System.out.println("PB starClient : Thread.sleep().");
	    e.printStackTrace();
	}
				
    }


    public BlablaTCP getBBTCP() {return bbTCP_;}



    public void detruireClient() {
	System.out.println("Destruction Client.");
    }


}
