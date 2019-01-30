package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import bdd.BDD;
import bdd.Message;
import graphique.Accueil;
import graphique.Connecte;
import reseau.AgentLAN;
import reseau.AgentWAN;
import reseau.CorrespondantException;
import reseau.InterfaceReseau;


public class Controller {
	private ArrayList<String> ListeCo = new ArrayList<String>();
	private InterfaceReseau ir_;
	private Connecte fenetreCo_;
	private BDD connexion;
	private String pseudo;
	private boolean ready_=false;
	private String IPserver;
	
	
	public Controller(boolean test) {}
	public Controller(int tcpPort, int udpPort) {
		
		//fen�tre d'authentification
		Accueil accueil = new Accueil(this);
		
		//on r�cupère le pseudo
		while (!accueil.getLoginaccepte()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		pseudo=accueil.getLog();
		
		//on cr�e l'ir en fonction du choix de communication de l'utilisateur
		if (accueil.isLocal()) {
			System.out.println("Controller : MODE LOCAL SELECTIONNE");
			ir_ = new AgentLAN(pseudo, 6000, 5000, this);
		} else {
			System.out.println("Controller : MODE WAN SELECTIONNE");
			ir_ = new AgentWAN(pseudo, tcpPort, udpPort, this);
		}
		
		//on attend d'�tre connect�
		while (!ir_.isCo()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//on r�cupère la liste des connect�s
		ListeCo = ir_.annuaireToPseudoList();
		
		connexion = new BDD("./src/bdd/Clavard.db");
        connexion.connect();
		
		//on a la liste donc on peut cr�er la fenetre connecte
		fenetreCo_ = new Connecte(pseudo, this);
		ready_=true;
	}
	
	/*fonction appel�e depuis le Chat. Appelle envoyerMessage de
	 * InterfaceReseau
	 */
	public void envoyerMessage(String pseudoDest, String message) {
		try {
			ir_.envoyerMessage(pseudoDest, message);
		} catch (CorrespondantException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getListeCo() {
		return ListeCo;
	}

	public void setListeCo(ArrayList<String> listeCo) {
		ListeCo = listeCo;
	}
	
	public void recevoirMessage(String pseudoEmetteur, String message) {
		ecrireBDD(pseudoEmetteur, pseudo, new Date(), message);
		fenetreCo_.ajoutMessageRecu(pseudoEmetteur, message);
	}
	
	public void ecrireBDD(String login_emet,String login_recept,Date date,String mess) {
		connexion.ecrire(login_emet, login_recept, date, mess);
	}
	
	public ArrayList<Message> lireBDD(String login_emet,String login_recept) {
		try {
			return connexion.lire_mess(login_emet, login_recept);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		return new ArrayList<Message>();
	}
	
	public ArrayList<String> listeloginbdd(){
		ArrayList<String> listeloginbdd = connexion.loginbdd();
		return listeloginbdd;
	}
	
	/*appel�e quand l'utilisateur ferme la fen�tre : 
	 * il faut close la bdd et �teindre l'IR
	 */
	public void fermer() {
		connexion.close();
		ir_.extinction();
	}
	
	/* m�thode appel�e par l'IR quand un contact se
	 * d�connecte
	 */
	
	
	
	/* fonction � appeler depuis l'ir quand il y a
	 * un nouveau connect�. Le controller va notifier 
	 * la fen�tre Connecte de cela
	 */
	public void nouveauConnecte(String pseudo) {
		if (ready_) {
		ListeCo.add(pseudo);
		fenetreCo_.majListeCo();
		}
	}
	
	/* m�thode appel�e par l'IR quand un contact se
	 * d�connecte
	 */
	public void decoContact(String pseudo) {
		if (ready_) {
		ListeCo.remove(pseudo);
		if (pseudo.equals(fenetreCo_.getRecepteur())) {
			fenetreCo_.cacherChat();
		}
		fenetreCo_.majListeCo();
		}
	}
	
	/*fonction appel�e depuis l'IR, on : 
	 * > m�j la BDD
	 * > m�j l'interface graphique
	 */
	public void traiterNewPseudo(String previousPseudo, String newPseudo) {
		System.out.println("Controller : "+previousPseudo+" s'appelle d�sormais "+newPseudo);
		
		//on m�j la bdd
		connexion.changementPseudo(previousPseudo, newPseudo);
		
		//on m�j l'ig
		ListeCo.remove(previousPseudo);
		ListeCo.add(newPseudo);
		fenetreCo_.majListeCo();
		fenetreCo_.setRecepteur(newPseudo);
		try {
			fenetreCo_.afficheChat();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void changementPseudo(String newPseudo) {
		connexion.changementPseudo(pseudo, newPseudo);
		pseudo=newPseudo;
		ir_.informerNewPseudo(newPseudo);
	}
	
	public void setPseudo(String newPseudo) {pseudo=newPseudo;}
	
	public String getIPserver() {
		return IPserver;
	}
	public void setIPserver(String iPserver) {
		IPserver = iPserver;
	}
	
	
	public static void main(String[] args) {
		new Controller(6001, 5001);
	}
	
	
	
	
}
