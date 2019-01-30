package reseau;

import java.net.InetAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import core.Controller;

public class AgentLAN extends InterfaceReseau {

    //objet pour envoyer des messages UDP
    private UDPTalk udpTalk_;

    /* vrai si on est le dernier connecté. Si on reçoit un message hello,
       on envoie notre annuaire à l'émetteur et dernierCo passe à faux.*/
    private boolean dernierCo_;

    //timer utilisé pour la récéption de Hello
    Timer timerHello_ = new Timer();

    /*TimerTask -> si on dépasse les 2 secondes sans être connecté (ie sans avoir reçu de welcome)
      -> on considère qu'on est le premier connecté -> connected passe à vrai */
    TTHello ttHello_;

    public AgentLAN(String pseudo, int portServeur, int portUDP, Controller controller) {
	super(pseudo, portServeur, portUDP, controller);
	dernierCo_=true;

	udpTalk_ = new UDPTalk(portUDP_);

	//on broadcast un hello message
	udpTalk_.broadcastMessage(secretaire_.construireHelloMessage());
	System.out.println("IR : Hello broadcasté");

	//on lance le timer
	ttHello_ = new TTHello(this);
	timerHello_.scheduleAtFixedRate(ttHello_, 1000, 1000);
    }

    public void envoyerUDP(InetAddress address, String message) {
	udpTalk_.sendMessageUDP(message, address);
    }

    public boolean dernierCo() {return dernierCo_;}
    public void setDernierCo(boolean b) {dernierCo_=b;}

    

     /* fonction appelée depuis le controller quand l'utilisateur change de pseudo.
       Le pseudo est censé être validé en amont, mais si celui-ci existe déjà au moment
       de l'appel de cette fonction, on lance une CorrespondantException */
    public void informerNewPseudo(String newPseudo) {
	if (dejaPris(newPseudo)) {
	    System.out.println("ERREUR PSEUDO");
	} else  {
	    //on broadcaste le message de changement de pseudo
	    udpTalk_.broadcastMessage(secretaire_.construireNewPseudoMessage(newPseudo));

	    //on change le pseudo enregistré dans l'ir
	    pseudo_=newPseudo;
	}
    }

        /*extinction s'occupe de terminer toutes les connexions
      et de l'organisation du départ de cet utilisateur du projet.
      Il se charge des actions suivantes : 
      >extinction parcourt l'annuaire : 
      pour chaque Correspondant, 
      -si coActive, on envoie "tchao"
      en TCP et on ferme le socket ; 
      -sinon envoyer TchaoMessage en
      UDP sur InetAddress que contient Correspondant
      (si celui qui s'éteint est dernier co le premier de l'annuaire 
      avec pas de connexion établie reçoit un tchao message spécial : avec un champ 1
      cela signifie qu'il passe dernier connecté)
      >termine le UDPTalk(fermer les sockets)
      >(terminer les threads)
      >passer termine_ à true, ce qui induira l'arrêt et la destruction
      des sockets de UDPListener et Serveur, si ce n'est pas déjà fait pas
      leurs timeout respéctifs*/
    public void extinction() {
	System.out.println("IR : EXTINCTION");
	for(Map.Entry<String, Correspondant> entry : annuaire_.entrySet()) {
	    String pseudo = entry.getKey();
	    Correspondant corr = entry.getValue();
	    if (corr.coEtablie()) { //on termine les connexions BlablaTCP
		corr.getBBTCP().envoyerTchao(dernierCo());
		corr.getBBTCP().terminerConnexion(false);
		System.out.println("IR : (ext) connexion terminée avec "+pseudo);
	    } else { //on envoie tchaoMessage sur l'InetAddress du Correspondant
		envoyerUDP(corr.getInetAddress(), secretaire_.construireTchaoMessage(dernierCo()));
		System.out.println("IR : (ext) tchao UDP envoyé à "+pseudo);
	    }
	    setDernierCo(false);
	}
	annuaire_.clear();
	System.out.println("IR : ANNUAIRE VIDÉ, CONNEXIONS FERMÉES");
	printAnnuaire();

	//on termine le UDPTalk
	udpTalk_.terminer();
		
	termine_=true;
    }
    
    /*cette classe est utile pour paraméter l'envoi des hello.
    Si on n'a pas reçu de welcome au bout de 3 secondes, on 
    considère que l'on est le premier connecté*/
  private static class TTHello extends TimerTask {
	private int secondes_=0;
	private InterfaceReseau ir_;
	public TTHello(InterfaceReseau ir) {
	    super();
	    ir_=ir;
	}
	public void run() {
	    secondes_++;
	    System.out.println("TTHello secondes : "+secondes_);
	    if (ir_.isCo()) {
		//si ir connecté on arrête la timer task
		System.out.println("TTHello s'arrête car on est co");
		cancel();
	    }
	    if (secondes_>2) {
		//on considère qu'on est les premiers connectés
		//on se set connecté et on arrête la timer task
		System.out.println("TTHello passe ir en co : on est les premiers co");
		ir_.setCo();
		cancel();
	    }
	}
  }

}
