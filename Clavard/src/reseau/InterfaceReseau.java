package reseau;

import java.util.Map;

import core.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.InetAddress;


public abstract class InterfaceReseau {

    /*associe les pseudos aux objets Correspondant*/
    protected Map<String, Correspondant> annuaire_;

    //serveur TCP
    protected ServeurTCP serveur_;

    //port d'écoute du serveur TCP
    protected int portServeur_;

    //pseudo du user possédant l'ir
    protected String pseudo_;

    //port d'écoute UDP
    protected int portUDP_;

    //objet Runnable d'écoute UDP
    protected UDPListener udpListener_;

    //classe qui gère les messages UDP (administration du réseau)
    protected Secretaire secretaire_;

    //connected_ passe à vrai quand l'interface réseau a des contacts
    protected boolean connected_;

    /* ce boolean est utile pour terminer le UDPListener : en effet,
       comme son socket est bloqué sur un .receive, appeler sa méthode close
       lèverait une exception. Ainsi, quand termine_ passe à faux, une fois
       que son timeout sera terminé, UDPListener 
       fermera ses sockets, sort de la boucle et termine son activité 
       (c'est aussi utile pour le thread attend connexion de serveur, qui
       reste bloqué sur une fonction accept)*/
    protected boolean termine_;
    
    protected Controller controller_;


    //constructeur pour tester secrétaire
    InterfaceReseau(String pseudo) {
	annuaire_ = new HashMap<>();
	pseudo_=pseudo;
    }

    
    public InterfaceReseau (String pseudo, int portServeur, int portUDP, Controller controller) {
	controller_ = controller;
	
	annuaire_ = new HashMap<>();
	pseudo_=pseudo;

	//config et lancement serveur
	portServeur_=portServeur;
	serveur_ = new ServeurTCP(portServeur_, this);
	serveur_.startServer();

	//booléens d'état
	connected_=false;
	termine_=false;

	/*le secrétaire est l'assistant de l'interface réseau : il s'occupe
	  d'écrire les messages qu'il demande et de traiter son courrier UDP (hello,
	  tchao, changement de pseudo, welcome etc.) */
	secretaire_=new Secretaire(this);

	//config et lancement udpListener
	portUDP_=portUDP;
	udpListener_=new UDPListener(portUDP_, this);
	Thread threadUDPListener = new Thread(udpListener_);
	threadUDPListener.start();
    }

    public void recevoirMessageUDP(InetAddress address, int port, String message) {
	System.out.println("IR : Message UDP reçu, IR le passe au Secrétaire.");
	secretaire_.traiteMessage(address, port, message);
    }

    

    public void envoyerMessage (String pseudoDest, String message) throws CorrespondantException {
	if (annuaire_.containsKey(pseudoDest)) {
	    Correspondant corr = annuaire_.get(pseudoDest);
	    if (corr.coEtablie()) {
		corr.getBBTCP().envoyerMessage(message);
	    } else {
		//il faut créer un client
		System.out.println("Destinataire "+pseudoDest+" trouvé, la co n'est pas établie, on lance le client.");
		ClientTCP client = new ClientTCP(corr.getPort(), corr.getInetAddress(), this, pseudoDest);
		client.startClient();
		//annuaire_.get(pseudoDest).setBBTCP(client.getBBTCP());
		corr.getBBTCP().envoyerMessage(message);
	    }
	} else {
	    //il faut lancer un client
	    throw new CorrespondantException("Ce destinataire est inconnu.");
	}
	printAnnuaire();	
    }


    /* appel� quand message TCP re�u -> TODO lien avec controller qui va update IG + BDD */
    public void recevoirMessage(String pseudoEmetteur, String message) throws CorrespondantException{
	if (annuaire_.containsKey(pseudoEmetteur)) {
		System.out.println(pseudoEmetteur+" : "+message);
		printAnnuaire();
		controller_.recevoirMessage(pseudoEmetteur, message);
	    } else {
		throw new CorrespondantException("Reçu message d'un correspondant inconnu");
	    }
    }

    /* cette fonction est appelée quand une connexion TCP est performée.
       Si le correspondant est présent dans l'annuaire, (ce qui est normal, cela veut
       dire que l'on avait reçu son hello), on set juste le blablaTCP pour discuter
       avec lui dans son objet Correspondant.
       Si le correspondant n'est pas présent dans l'annuaire, (cela veut dire
       que l'on avait pas re�u son hello message, ce qui est possible comme
       c'est un message d'administration réseau, donc UDP), on rajoute
       une nouvelle entr�e dans l'annuaire pour ce Correspondant cr�e avec
       l'objet BlablaTCP du nouvel interlocuteur.*/
    public void nouveauCorrespondant(String pseudo, BlablaTCP bbTCP) {
	if (annuaire_.containsKey(pseudo)) {
	    try {
		//le correspondant était déjà présent dans l'annuaire grâce à son Hello
		annuaire_.get(pseudo).setBBTCP(bbTCP);
		System.out.println(pseudo+" mis à jour dans l'annuaire, il y était déjà.");
	    } catch (CorrespondantException e) {
		//System.out.println("PB nouveau correspondant.");
		//e.printStackTrace();
	    }
	} else {
	    Correspondant nouveauCorrespondant = new Correspondant(bbTCP);
	    annuaire_.put(pseudo, nouveauCorrespondant);
	    System.out.println(pseudo+" ajouté à l'annuaire, son Hello n'avait pas été reçu.");
	    
	    //on informe le controller
	    controller_.nouveauConnecte(pseudo);
	}
    }

    //fonction à appeler par Secrétaire quand on reçoit un message Hello
    public void nouveauCorrespondant(String pseudo, InetAddress address, int port) throws CorrespondantException{
		if (annuaire_.containsKey(pseudo)) {
		    /*le pseudo est déjà dans l'annuaire : on lance une erreur, cela veut dire que 
		      l'interlocuteur utilise un pseudo actif ou qu'il a envoyé plusieurs hello. */
		    throw new CorrespondantException(pseudo+" venant d'envoyer un hello est déjà dans l'annuaire.");
		} else {
		    Correspondant nouveauCorrespondant = new Correspondant(address, port);
		    annuaire_.put(pseudo, nouveauCorrespondant);
		    System.out.println(pseudo+" ajout� � l'annuaire gr�ce � son message Hello.");
		    
		    //on informe le controller
		    controller_.nouveauConnecte(pseudo);
		}
    }

    //fonction à appeler depuis bbTCP quand on éteint l'ir ou quand il reçoît un msg TCP "tchao"
    //supprime le Correspondant ayant pour clé le pseudo en argument
    public void supprimeCorrespondant(String pseudo, boolean b) {
	annuaire_.remove(pseudo);
	printAnnuaire();
	
	
	//on informe le controller
	if (b) {controller_.decoContact(pseudo);}
    }

    public String getPseudo() {return pseudo_;}
    public int getPort() {return portServeur_;}
    public int getPortUDP() {return portUDP_;}
    public String getIP() {
    	return secretaire_.retourneIPLocale();
    }
    public boolean isCo() {return connected_;}
    public boolean isTermine() {return termine_;}
    public void setCo() {connected_=true;}
    //à redéfinir dans AgentLAN
    public boolean dernierCo() {return false;}
    //à redéfinir dans AgentLAN
    public void setDernierCo(boolean b) {}
    //à redéfinir dans AgentLAN
    public void envoyerUDP(InetAddress address, String message) {}
    public void setPseudo(String pseudo) {
	System.out.println("IR : je change de pseudo "+pseudo_+" --> "+pseudo);
	pseudo_=pseudo;
    }

    /*cette fonction est appelée par le secrétaire pour construire un message welcome*/
    public String annuaireToString() {
	String result = "";
	for(Map.Entry<String, Correspondant> entry : annuaire_.entrySet()) {
	    Correspondant corr = entry.getValue();
	    if ((corr.getInetAddress()!=null)&&(corr.getPort()!=-1)) {
		String pseudo = entry.getKey();
		String stringAdresse = corr.getInetAddress().toString().replace("/", "");
		result=result+":"+pseudo+";"+stringAdresse+";"+Integer.toString(corr.getPort());
		System.out.println("IR annuaire to String, adresse de "+pseudo+" stockée "+corr.getInetAddress().toString());
	    } 	    
	}
	return result;
    }
    
    /*cette fonction est appelée par le secrétaire pour construire un message welcome*/
    public ArrayList<String> annuaireToPseudoList() {
	ArrayList<String> result = new ArrayList<String>();
	for(Map.Entry<String, Correspondant> entry : annuaire_.entrySet()) {
	    Correspondant corr = entry.getValue();
	    if ((corr.getInetAddress()!=null)&&(corr.getPort()!=-1)) {
		String pseudo = entry.getKey();
		result.add(pseudo);
	    } 
	}
	return result;
    }

   

    //ici fonctions pour le TEST
    public void addAnnuaire(String pseudo, InetAddress address, int port) {
	annuaire_.put(pseudo, new Correspondant(address, port));
    }
    public void printAnnuaire() {
	System.out.println("Annuaire de "+pseudo_+" : ");
	for(Map.Entry<String, Correspondant> entry : annuaire_.entrySet()) {
	    String pseudo = entry.getKey();
	    Correspondant corr = entry.getValue();
	    System.out.println("Pseudo : "+pseudo);
	    corr.print();
	}
    }
    
	    /*fonction utilisée par le secrétaire pour vérifier si un pseudo appartient
	    à l'annuaire */
	  public boolean dejaPris(String pseudo) {
		return annuaire_.containsKey(pseudo);
	  }


    /* fonction appelée depuis le controller quand l'utilisateur change de pseudo.
       Le pseudo est censé être validé en amont, mais si celui-ci existe déjà au moment
       de l'appel de cette fonction, on lance une CorrespondantException */
    public abstract void informerNewPseudo(String newPseudo);

    /* fonction appelée depuis le secrétaire, l'IR doit : 
       > lever une exception si le pseudo existe déjà
       > lever une exception si l'ancien pseudo n'existe pas
       > changer l'annuaire
       > informer le controller. */
    public void traiterNewPseudo(String previousPseudo, String newPseudo) throws CorrespondantException{
	if (dejaPris(newPseudo)||newPseudo.equals(getPseudo())) {
	    throw new CorrespondantException("Le nouveau pseudo "+newPseudo+" est déjà pris.");
	} else if (!dejaPris(previousPseudo)) {
	    throw new CorrespondantException("Le précédent pseudo "+previousPseudo+" est inconnu.");
	} else {
	    Correspondant corr = annuaire_.get(previousPseudo);
	    corr.setPseudo(newPseudo);
	    supprimeCorrespondant(previousPseudo, false);
	    annuaire_.put(newPseudo, corr);
	    System.out.println("IR : ANNUAIRE APRES SUPPRESSION");
	    printAnnuaire();
	    controller_.traiterNewPseudo(previousPseudo, newPseudo);
	}
    }
    



    /* fonction pour appelée par le Controller
     * quand l'utilisateur ferme le programme. Elle
     * permet de quitter le réseau proprement
     * (informer, terminer les threads d'écoute, 
     * terminer les connexions, fermer les sockets)
     */
    public abstract void extinction();


    /*public static class CorrespondantException extends Exception {
	public CorrespondantException(String s) {
	    System.out.println(s);
	}
	}*/
    static class Correspondant {
	//vrai si il y a une connexion établie avec le client (blablaTCP valide)
	private boolean coEtablie_;
	private InetAddress address_;
	private int port_;
	private BlablaTCP bbTCP_;

	/* constructeur à appeler si le Correspondant
	   n'est pas dans l'annuaire lors de son ajout */
	Correspondant(BlablaTCP bbTCP) {
	    bbTCP_=bbTCP;
	    coEtablie_=true;
	    port_=-1; //port non rempli
	}

	/* constructeur à appeler quand on reçoit le Hello
	   du correspondant, on quand on est informé de la table
	   des connectés à notre arrivée */
	Correspondant(InetAddress address, int port) {
	    address_=address;
	    port_=port;
	    coEtablie_=false;
	}

	public void setBBTCP(BlablaTCP bbTCP) throws CorrespondantException{
	    if (coEtablie_) {
		throw new CorrespondantException("Connexion déjà établie");
	    } else {
		bbTCP_ = bbTCP;
		coEtablie_=true;
	    }
	}

	public void setPseudo(String newPseudo) {
	    if (coEtablie_) {
		bbTCP_.setPseudoCo(newPseudo);
	    }
	}

	public BlablaTCP getBBTCP() {return bbTCP_;}
	public boolean coEtablie() {return coEtablie_;}
	public InetAddress getInetAddress() {return address_;}
	public int getPort() {return port_;}
	public void print() {
	    if (coEtablie_) {
		System.out.println("Co etablie avec "+bbTCP_.getPseudoCo());
	    } else {
		System.out.println("Pas de co etablie sur port "+port_);
	    }
	}
    }


 
		

 
	


}