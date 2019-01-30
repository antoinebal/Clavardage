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
	
	
	public Controller(boolean test) {}
	public Controller(int tcpPort, int udpPort) {
		
		//fenêtre d'authentification
		Accueil accueil = new Accueil();
		
		//on récupÃ¨re le pseudo
		while ((pseudo=accueil.getLog())==null) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		//on crée l'ir en fonction du choix de communication de l'utilisateur
		if (accueil.isLocal()) {
			System.out.println("Controller : MODE LOCAL SELECTIONNE");
			ir_ = new AgentLAN(pseudo, 6000, 5000, this);
		} else {
			System.out.println("Controller : MODE WAN SELECTIONNE");
			ir_ = new AgentWAN(pseudo, tcpPort, udpPort, this);
		}
		
		//on attend d'être connecté
		while (!ir_.isCo()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//on récupÃ¨re la liste des connectés
		ListeCo = ir_.annuaireToPseudoList();
		
		connexion = new BDD("C:/Users/Mehdi/Desktop/INSA/4IR/POO/Projet/Final-Clavard/Clavard/src/bdd/Clavard.db");
        connexion.connect();
		
		//on a la liste donc on peut créer la fenetre connecte
		fenetreCo_ = new Connecte(pseudo, this);
		ready_=true;
	}
	
	/*fonction appelée depuis le Chat. Appelle envoyerMessage de
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
	
	/*appelée quand l'utilisateur ferme la fenêtre : 
	 * il faut close la bdd et éteindre l'IR
	 */
	public void fermer() {
		connexion.close();
		ir_.extinction();
	}
	
	/* méthode appelée par l'IR quand un contact se
	 * déconnecte
	 */
	
	
	
	/* fonction à appeler depuis l'ir quand il y a
	 * un nouveau connecté. Le controller va notifier 
	 * la fenêtre Connecte de cela
	 */
	public void nouveauConnecte(String pseudo) {
		if (ready_) {
		ListeCo.add(pseudo);
		fenetreCo_.majListeCo();
		}
	}
	
	/* méthode appelée par l'IR quand un contact se
	 * déconnecte
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
	
	/*fonction appelée depuis l'IR, on : 
	 * > màj la BDD
	 * > màj l'interface graphique
	 */
	public void traiterNewPseudo(String previousPseudo, String newPseudo) {
		System.out.println("Controller : "+previousPseudo+" s'appelle désormais "+newPseudo);
		
		//on màj la bdd
		connexion.changementPseudo(previousPseudo, newPseudo);
		
		//on màj l'ig
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
	
	public static void main(String[] args) {
		new Controller(6001, 5001);
	}
	
	
	
	
}
