package reseau;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ServeurTCP {
    int port_;
    AttendConnexion attendCo_;
    InterfaceReseau ir_;

    ServeurTCP(int port, InterfaceReseau ir){
	port_=port;
	ir_=ir;
	attendCo_ = new AttendConnexion(port, this);
    }

    public void startServer() {
	System.out.println("Serveur : On lance le serveur");
	Thread threadAttendCo = new Thread(attendCo_);
	threadAttendCo.start();
    }


    public InterfaceReseau getIR() {return ir_;}



    /*ce thread s'occupe d'attendre les connexions des clients
      quand un client se connecte il file le blablaTCP au ServeurTCP*/
    /* on a choisi de créer cette classe pour ne pas mettre ServeurTCP en Runnable : 
       on peut ainsi le configurer plus dynamiquement */
    private static class AttendConnexion implements Runnable {
	private Socket link_=null;
	private ServerSocket srvSock_=null;
	private int port_;
	//incrémenté à chaque co pour attribuer un numéro aux threads crées
	//correspondant à des clients distants
	private int incNumero_;
	private ServeurTCP srv_;

	AttendConnexion(int port, ServeurTCP srv) {
	    port_=port;
	    srv_=srv;
	    incNumero_=0;
	}

	public void termineAC() {
	    try {
		srvSock_.close();
		System.out.println("ServeurTCP(AC) : OVER");
	    }catch(SocketException e) {
		e.printStackTrace();
	    } catch(IOException e) {
		e.printStackTrace();
	    } 
	}

	public void run() {
	    System.out.println("AttendConnexion lanc�.");
	    try {
		srvSock_ = new ServerSocket(port_);
		srvSock_.setSoTimeout(1000*60*10); //on laisse un timeout de 2 minutes
		while (!srv_.getIR().isTermine()) {
		    link_ = srvSock_.accept();
		    System.out.println("Serveur(AC) : Connexion avec client numéro "+incNumero_+" acceptée.");
		
		    //on crée le TCPListener			
		    TCPListener listener = new TCPListener(link_);

		    //on récupère le pseudo du client
		    String pseudoClient = listener.lecturePseudo(); 
	     
		    //on crée le blablaTCP
		    BlablaTCP bbTCP = new BlablaTCP(pseudoClient, srv_.getIR(), link_);
		    listener.setBBTCP(bbTCP);
		    //on lance le listener
		    Thread threadListener = new Thread(listener);
		    threadListener.start();

		    //on informe l'IR de l'arrivée d'un nouveau correspondant
		    srv_.getIR().nouveauCorrespondant(pseudoClient, bbTCP);

		    ++incNumero_;
		}
		//si on est ici, on peut fermer le serversocket
		srvSock_.close();
	    } catch(SocketTimeoutException e) {
		System.out.println("ServeurTCP (AC) : Le timeout a expiré.");
		termineAC();
	    } catch (SocketException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		System.out.println("PB AttendConnexion");
		e.printStackTrace();
	    }

	}
	
    }
   

   
 
	/*
	  public static void main(String[] args) {
	  Srv serveur = new Srv(5000);
	  serveur.startServer();

	  Scanner scan = new Scanner(System.in);
	  String inputNum=null;
	  String inputMsg=null;
	  while(true) {
	  System.out.println("Attente entrée numéro.");
	  if(scan.hasNext()) {
	  inputNum = scan.nextLine();
	  System.out.println("Entrée prise en compte : "+inputNum);
	  if (scan.hasNext()) {
	  System.out.println("Attente entrée message.");
	  inputMsg = scan.nextLine();
	  System.out.println("Entrée prise en compte : "+inputMsg);
	  }
	  serveur.envoyerMessage(inputMsg, Integer.parseInt(inputNum));
	  }
	  }
	
	  }*/


}
