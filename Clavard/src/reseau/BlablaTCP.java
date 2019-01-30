package reseau;



import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;

public class BlablaTCP {
    private String pseudoCorrespondant_;
    private InterfaceReseau ir_;
    private Socket socket_;
    private PrintWriter out_;

    


    BlablaTCP(String pseudoCo, InterfaceReseau ir, Socket socket) {
	pseudoCorrespondant_=pseudoCo;
	ir_=ir;
	socket_=socket;
	try {
	    out_ = new PrintWriter(socket_.getOutputStream(), true);
	}catch (IOException e) {
	    System.out.println("PB bbTCP: IOException.");
	    e.printStackTrace();
	}
    }




    public void envoyerMessage(String message) {
	out_.println(message);
    }

    public void envoyerPseudoLocal() {
	out_.println(ir_.getPseudo());
    }

    /*requete DC est vrai si l'on doit ordonner au correspondant
      de passer dernierCo (on le fait si l'on est soi même dernierCo)*/
    public void envoyerTchao(boolean requeteDC) {
	if (!requeteDC) {
	    out_.println("tchao");
	    System.out.println("BBTCP : tchao envoyé à "+pseudoCorrespondant_);
	} else {
	    out_.println("tchaoDC");
	    System.out.println("BBTCP : tchaoDC envoyé à "+pseudoCorrespondant_);
	}
    }
    
    
    public void recevoirMessage(String message) {
	System.out.println("TCPListener reçoit le message.");
	try {
	    ir_.recevoirMessage(pseudoCorrespondant_, message);
	} catch (CorrespondantException e) {
	    System.out.println("Le correspondant existe déjà.");
	    e.printStackTrace();
	}
    }

    //fonction appelée quand le correspondant terminer la co
    //ou depuis ir quand bbtcp vient d'envoyer "tchao"
    //b est vraie si cette fonction est appelée par le TCPListener
    //soit si c'est le correspondant qui a terminé la connexion
    //alors si b vraie on enlève l'entrée du correspondant dans l'annuaire
    public void terminerConnexion(boolean b) {
	out_.close();
	try {
	    socket_.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	if (b) {
	    ir_.supprimeCorrespondant(pseudoCorrespondant_,true);
	}
	System.out.println("BBTCP : connexion avec "+pseudoCorrespondant_+" terminée.");
    }

    /* appelé par le listener si il reçoit une requête
       demandant à son IR propriétaire de passer dernierCo */
    public void setDernierCo() {
	ir_.setDernierCo(true);
	System.out.println("BBTCP : dernierCo passé à vrai");
    }

    public void setPseudoCo(String newPseudo) {
	pseudoCorrespondant_=newPseudo;
	System.out.println("BlablaTCP : pseudo changé en "+newPseudo);
    }
    
    public String getPseudoCo() {return pseudoCorrespondant_;}



}