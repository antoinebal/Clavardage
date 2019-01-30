package bdd;

import java.util.ArrayList;
//import java.sql.ResultSet;
import java.util.Date;
import java.text.ParseException;
public class Testbdd {

	public static void main(String[] args) {
		//InterfaceGraphique IG = new InterfaceGraphique();
		/*String mess = "I'll be watching you 'k 'o";
		System.out.println("Dans la fonction ecrire, avant le chmgt : "+mess);
    	String messrap = mess.replace("'", "\\");
    	System.out.println("Dans la fonction ecrire, apr�s le chmgt : "+messrap);
    	String rechange = messrap.replace("\\","'");
    	System.out.println("Dans la fonction ecrire, apr�s le rechmgt : "+rechange);*/
		
		
		BDD connexion = new BDD("C:/Users/Mehdi/Desktop/INSA/4IR/POO/Projet/Final-Clavard/Clavard/src/clavard/Clavard.db");
        connexion.connect();
        Date date = new Date();
        String pseudo1 = "Mehdi";
        String pseudo2 = "Le Balayssac FR";
        ArrayList<String> test=connexion.loginbdd();
        System.out.println(test);
        /*connexion.ecrire(pseudo1,pseudo2, date, "Enorme");
        try {
			ArrayList<Message> liste_mess = connexion.lire_mess(pseudo1, pseudo2);
			//ArrayList<Message> liste_mess = connexion.lire_mess("antoine", "th�o");
			if (liste_mess.isEmpty()){
				System.out.println("Aucun message");
			}
			else {
				for (Message obj : liste_mess) {
					System.out.println("Le "+obj.getDate().toString()+" : "+obj.getContenu());
				}
			}
		} 
        catch (ParseException e) {
			// TODO Auto-generated catch block
			//System.out.println("Aucun message");
		}*/
        connexion.close();

	}

}
