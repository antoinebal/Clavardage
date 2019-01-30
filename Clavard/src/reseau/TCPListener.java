package reseau;


import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketException;



/* cette classe scrute le socket pour voir
       si on a pas reçu un message. En informe le serveur si oui. */
    public class TCPListener implements Runnable {
	private BlablaTCP bbTCP_;
	private Socket socket_;
	private BufferedReader buffReader_;
	private String recu_;

	//constructeur appelé depuis le client
	TCPListener(BlablaTCP bbTCP, Socket socket) {
	    try {
		bbTCP_=bbTCP;
		socket_=socket;
		buffReader_ = new BufferedReader(new InputStreamReader(socket_.getInputStream()));
		recu_=null;
	    } catch(IOException e) {
		e.printStackTrace();
	    }
	}

	//constructeur appelé depuis le serveur
	TCPListener(Socket socket) {
	    try {
		socket_=socket;
		buffReader_ = new BufferedReader(new InputStreamReader(socket_.getInputStream()));
		recu_=null;
	    } catch(IOException e) {
		e.printStackTrace();
	    }
	}

	public void setBBTCP(BlablaTCP bbTCP) {
	    bbTCP_=bbTCP;
	}

	/* cette fonction est appelée avent le lancement de TCPListener. Elle permet de récupérer
	   le pseudo du nouveau correspondant avant l'échange de messages, pour l'identifier */
	public String lecturePseudo() {
	    try {
		recu_=buffReader_.readLine();
		System.out.println("Pseudo lu "+recu_);
		return recu_;
	    } catch (IOException e) {
		System.out.println("Pb lecturePseudo");
		e.printStackTrace();
	    }
	    return "";
	}

	/*cette fonction est appelée du côté de celui qui 
	  termine la connexion, autrement le socket est closed
	  quand on reçoit tchao */
	public void termineListener() {
	    try {
		buffReader_.close();
		System.out.println("TCPListener : termineListener() et connexion terminée avec "+bbTCP_.getPseudoCo());
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	public void run() {
	    try {
		System.out.println("TCPListener "+bbTCP_.getPseudoCo()+" lancé.");
		while ((recu_=buffReader_.readLine())!=null) {
		    System.out.println("TCPListener "+bbTCP_.getPseudoCo()+" attend récéption message.");
		    if (recu_.equals("tchao")) {
			System.out.println("TCPListener : tchao reçu de "+bbTCP_.getPseudoCo());
			termineListener();
			bbTCP_.terminerConnexion(true);
			System.out.println("TCPListener : tchao reçu et connexion terminée avec "+bbTCP_.getPseudoCo());
			break;
		    } else if (recu_.equals("tchaoDC")) {
			System.out.println("TCPListener : tchaoDC reçu de "+bbTCP_.getPseudoCo());
			termineListener();
			bbTCP_.setDernierCo();
			bbTCP_.terminerConnexion(true);
			System.out.println("TCPListener : tchao reçu et connexion terminée avec "+bbTCP_.getPseudoCo());
			break;
		    } else {
			bbTCP_.recevoirMessage(recu_);
			System.out.println("Message reçu.");
		    }
		}
	    } catch (SocketException e) {
		termineListener();
	    } catch (IOException e) {
		System.out.println("Pb buffReader.readLine");
		e.printStackTrace();
	    }
	}

    }
