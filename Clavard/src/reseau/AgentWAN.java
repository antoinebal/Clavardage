package reseau;



import java.util.Map;

import core.Controller;

public class AgentWAN extends InterfaceReseau {
	private HttpTalker httpTalker_;
	
	public AgentWAN(String pseudo, int portServeur, int portUDP, Controller controller) {
		super(pseudo, portServeur, portUDP, controller);
		httpTalker_ = new HttpTalker(this);
		
		/* on demande au secr�taire de remplir l'annuaire avec la liste
		 * renvoy�e par le serveur
		 */
		secretaire_.traiteWelcomeMessage(httpTalker_.subscribe());
		
		/* on se passe connect�, ce qui est le signal
		 * pour que le controller puisse r�cup�rer la liste des
		 * connect�s
		 */
		connected_=true;
			
		
	}

	@Override
	public void informerNewPseudo(String newPseudo) {
		//on envoie la requ�te au serveur
		httpTalker_.notifyNewPseudo(newPseudo);
		
		//on change le pseudo enregistr� localement
		pseudo_=newPseudo;
	}

	@Override
	public void extinction() {
		
		//on termine toutes les connexions TCP en cours
		for(Map.Entry<String, Correspondant> entry : annuaire_.entrySet()) {
		    String pseudo = entry.getKey();
		    Correspondant corr = entry.getValue();
		    if (corr.coEtablie()) { //on termine les connexions BlablaTCP
			corr.getBBTCP().envoyerTchao(false);
			//argument faux car appel� depuis agent wan
			corr.getBBTCP().terminerConnexion(false);
			System.out.println("AgentWAN : (ext) connexion termin�e avec "+pseudo);
		    }
		}
		
		annuaire_.clear();
		System.out.println("AW : ANNUAIRE VID�, CONNEXIONS FERM�ES");
		printAnnuaire();
		
		//on envoie une requ�te GET au serveur avec l'attribut deconnexion � 1
		httpTalker_.notifyDeconnexion();
		
		termine_=true;	
	}
	
	public String getIPServer() {
		return controller_.getIPserver();
	}
	
	
		
	
	

}
