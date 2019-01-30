package reseau;

/*le secrétaire est l'assistant de l'interface réseau : il s'occupe
  d'écrire les messages qu'il demande et de traiter son courrier UDP (hello,
  tchao, changement de pseudo, welcome etc.) */
import java.util.regex.Pattern;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.DatagramSocket;

public class Secretaire {
    private InterfaceReseau ir_;

    Secretaire() {}
    Secretaire(InterfaceReseau ir) {
	System.out.println("Secrétaire : crée");
	ir_=ir;
    }

    public String construireHelloMessage() {
	//il faut aussi récupérer le port TCP
	//on devrait rajouter une condition ici : si
	//on a pas reçu de welcome après x secondes, on renvoie un hello
	//si, toujours rien reçu, on passe dernierCo
	return (ir_.getPseudo()+":hello:"+ir_.getPort());
    }

    /*si requête dc est vrai, on construit un message avec le
     dernier champ à 1 : il ordonne à celui qui le reçoit de passer dernierCo */
    public String construireTchaoMessage(boolean requeteDC) {
	//il faut aussi récupérer le port TCP
	//on devrait rajouter une condition ici : si
	//on a pas reçu de welcome après x secondes, on renvoie un hello
	//si, toujours rien reçu, on passe dernierCo
	String lastDigit=null;
	if (requeteDC) {
	    lastDigit="1";
	} else {
	    lastDigit="0";
	}
	return (ir_.getPseudo()+":tchao:"+lastDigit);
    }


    /*fonction pour retourner l'adresse IP de la machine
    faisant tourner le programme */
    public String retourneIPLocale() {
	try(final DatagramSocket socket = new DatagramSocket()){
	    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
	    String adresseIP = socket.getLocalAddress().getHostAddress();
	    return adresseIP;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return "";
    }


    /* l'argument pseudoAffecte est le pseudo que doit prendre le nouveau co.
       Si celui choisi par le nouveau connecté au début est disponible, il est égal à pseudoAffecte.
       Sinon, pseudoAffecte est un nouveau pseudo (souvent de la forme pseudoOriginel1) que le nouveau
       connecté devra adopter */
    public String construireWelcomeMessage(String pseudoAffecte) {	
	String stringAnnuaire = ir_.annuaireToString();
	String stringLocal = ir_.getPseudo()+";"+retourneIPLocale()+";"+Integer.toString(ir_.getPort());
	return ir_.getPseudo()+":"+pseudoAffecte+":welcome"+stringAnnuaire+":"+stringLocal;
    }

    public String construireNewPseudoMessage(String newPseudo) {
	return ir_.getPseudo()+":newpseudo:"+newPseudo;
    }
    
    

    public void traiteMessage(InetAddress address, int port, String message) {	
	if (Pattern.matches(".+:hello:.+", message)) {	   
	    traiteHelloMessage(message, address);
	} else if (Pattern.matches(".+:tchao:[01]", message)) {
	    String pseudo=message.split(":")[0];
	    System.out.println("Secrétaire : "+message+" est un message Tchao de "+pseudo+".");
	    traiteTchaoMessage(message);
	} else if (Pattern.matches(".+:newpseudo:.+", message)) {
	    String vieuxPseudo=message.split(":")[0];
	    String nouveauPseudo=message.split(":")[2];
	    System.out.println("Secrétaire : "+message+" est un message de changement de pseudo : "+vieuxPseudo+" devient "+nouveauPseudo+".");
	    traiteNewPseudoMessage(message);
	} else if (Pattern.matches(".+:.+:welcome:.+", message)) {
	    String pseudo=message.split(":")[0];
	    System.out.println("Secrétaire : "+message+" est un message Welcome de "+pseudo+".");
	    traiteWelcomeMessage(message);
	} else if (Pattern.matches(".+:newuser:.+:.+", message)) {
		String pseudo=message.split(":")[0];
		System.out.println("Secrétaire : "+message+" est un message newuser de "+pseudo+".");
		traiteNewUserMessage(message);
	} else {
	    System.out.println("Secrétaire : "+message+" est non conforme.");
	}
    }

    /* fonction récursive qui retourne un pseudo disponible */
    public String pseudoDisponible(String pseudo, int tail) {
	if (ir_.dejaPris(pseudo+tail)||(ir_.getPseudo().equals(pseudo+tail))) {
	    return pseudoDisponible(pseudo, tail+1);
	} else {
	    return pseudo+tail;
	}
    }
    
    public void traiteNewUserMessage(String newUserMsg) {
    	String pseudo=newUserMsg.split(":")[0];
		try {
			InetAddress address=InetAddress.getByName(newUserMsg.split(":")[2]);
			int port=Integer.parseInt(newUserMsg.split(":")[3]);
			ir_.nouveauCorrespondant(pseudo, address, port);
			ir_.printAnnuaire();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }


    //hellomsg de la forme pseudo_emetteur:hello:port_srv_tcp_emetteur
    public void traiteHelloMessage(String helloMsg, InetAddress address) {
	try {
	    //rajouter la condition avec le dernier co
	    String pseudo=helloMsg.split(":")[0];
	    /*on rentre dans ce cas si ce message hello est issu de notre propre
	      broadcast */
	    if (pseudo.equals(ir_.getPseudo())&&(!ir_.isCo())) {
		System.out.println("Secrétaire : l'émetteur de ce hello est nous meme, on ignore");
	    } else {
		System.out.println("Secrétaire : "+helloMsg+" est un message Hello de "+pseudo+".");
		/* si le pseudo est déjà pris, on cherche un pseudo disponible à
		   attribuer au nouveau connecté */
		if (ir_.dejaPris(pseudo)) {
		    pseudo=pseudoDisponible(pseudo, 0);
		}
		/*si on est le dernier connecté, on envoie notre annuaire au nouveau co, 
		  et on lui spécifie son nouveau pseudo */
		if (ir_.dernierCo()) {
		    System.out.println("Secrétaire: je suis le dernier co");
		    ir_.envoyerUDP(address, construireWelcomeMessage(pseudo));
		    //on n'est plus le dernier connecté
		    ir_.setDernierCo(false);
		    System.out.println("Secrétaire: welcome envoyé, dernier co = false");
		}
		int port = Integer.parseInt(helloMsg.split(":")[2]);
		ir_.nouveauCorrespondant(pseudo, address, port);
		ir_.printAnnuaire();
	    }
	} catch (CorrespondantException e) {
	    e.printStackTrace();
	}
	
    }

    /*welcomeMsg de la forme : pseudoEmetteur:pseudoAffecte:welcome:pseudo1;adresse1;port1:pseudo2;adresse2;port2 ... */
    //ATTENTION : L'EMETTEUR DE WELCOME DOIT S'INCLURE DANS LA LISTE AU MÊME TITRE QUE LES AUTRES
    public void traiteWelcomeMessage(String welcomeMsg) {
	try {
	    System.out.println("LISTE DES CONNECTES :");
	    String[] firstSplit = welcomeMsg.split(":");
	    String pseudoEm=firstSplit[0];
	    System.out.println("Secrétaire : "+welcomeMsg+" est un message welcome de "+pseudoEm+".");
	    
	    String pseudoAffecte=firstSplit[1];
	    /*si jamais notre pseudo était indispo, on prend pseudoAffecte
	      comme nouveau pseudo */
	    if (!pseudoAffecte.equals(ir_.getPseudo())) {
		System.out.println("Secrétaire : "+ir_.getPseudo()+" indisponible, je m'appelle maintenant "+pseudoAffecte);
		ir_.setPseudo(pseudoAffecte);
	    }

	    //on commence à 3 car les trois premiers champs sont pseudosEm, pseudoAff et welcome
	    for(int NO=3 ; NO < firstSplit.length ; NO++) {
		String[] secondSplit = firstSplit[NO].split(";");
		String pseudo = secondSplit[0];
		InetAddress address = InetAddress.getByName(secondSplit[1]);
		int port = Integer.parseInt(secondSplit[2].trim());
		System.out.println(pseudo+" ; "+secondSplit[1]+" ; "+port);
		ir_.nouveauCorrespondant(pseudo, address, port);
	    }
	    //on passe l'interface à l'état connecté : elle a reçu des contacts
	    ir_.setCo();
	    
	    //TEST on imprime l'annuaire
	    ir_.printAnnuaire();
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	} catch (CorrespondantException e) {
	    e.printStackTrace();
	}
    }

     public void traiteTchaoMessage(String tchaoMsg) {
	 //on l'enlève de l'annuaire
	 System.out.println("Secrétaire : reçu tchao "+tchaoMsg);
	 String pseudoEm = tchaoMsg.split(":")[0];
	 /*logiquement, si on a reçu ce message, c'est que la connexion n'était pas établie
	   avec ce correspondant, on va donc seulement supprimer le correspondant de l'annuaire */
	 ir_.supprimeCorrespondant(pseudoEm, true);

	 String requeteDC=tchaoMsg.split(":")[2];
	 if (requeteDC.equals("1")) {
	     ir_.setDernierCo(true);
	     System.out.println("Secrétaire : on passe dernier co.");
	 }
    }

    /* on informe l'IR de ce changement, qui va 
       > lever une exception si le pseudo existe déjà
       > lever une exception si l'ancien spseudo n'existe pas
       > changer l'annuaire
       > informer le controller. */
    public void traiteNewPseudoMessage(String newPseudoMsg) {
	String previousPseudo=newPseudoMsg.split(":")[0];
	String newPseudo=newPseudoMsg.split(":")[2];
	if (previousPseudo.equals(ir_.getPseudo())||(newPseudo.equals(ir_.getPseudo()))) {
	    System.out.println("Secrétaire : l'émetteur de ce newpseudo est nous meme, on ignore");
	} else {
	    try {
		ir_.traiterNewPseudo(previousPseudo, newPseudo);
	    } catch (CorrespondantException e) {
		e.printStackTrace();
	    }
	}
    }
    


    
    public static void main(String[] args) {
	/*boolean b = Pattern.matches(".+:hello", "maure:hello");
	  if (b) {System.out.println("MATCH");}else{System.out.println("PAS MATCH");}*/
	/*s.traiteMessage("maure:tchao");
	s.traiteMessage("drassius:hello");
	s.traiteMessage("derol:newpseudo:eran");
	s.traiteMessage("mazak:welcome:informations");
	s.traiteMessage("nualia:tcho");*/

	
	//Secretaire s = new Secretaire(new InterfaceReseau("maure"));
	//s.traiteMessage(null,0,"maure:hello");
	//s.traiteMessage(null,0,"mazak:hello:5000");
	//String welcomeMsg = "maure:mazak0:welcome:mazak;255.255.255.255;5000:nualia;255.255.255.255;5001:derol;255.255.255.255;5000:drassius;255.255.255.255;5000";
	//s.traiteMessage(null,0,welcomeMsg);
	/*s.traiteWelcomeMessage(welcomeMsg);
	System.out.println("Testhello");
	s.traiteHelloMessage("maure:hello", null);*/
	}

}