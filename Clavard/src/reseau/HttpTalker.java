package reseau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpTalker {
	
	private static String ADRESSE_IP_SERVEUR_TOMCAT = "localhost";
	
	private AgentWAN aw_;
	
	HttpTalker(AgentWAN aw) {
		aw_=aw;
	}
	
    /*envoie requête http au serveur, retourne la liste des connectés
     * envoyée par le serveur
     */
    public String subscribe() {
        URL url = construitURL(true, false, null);
        return envoyerRequete(url);
    }
    
    public void notifyDeconnexion() {
    	URL url = construitURL(false, true, null);
    	envoyerRequete(url);
    }
    
    /* à appeler quand l'user change de pseudo */
    public void notifyNewPseudo(String newPseudo) {
    	URL url = construitURL(false, false, newPseudo);
    	envoyerRequete(url);
    }
    
    /* construit l'url en fonction des paramètres */
    public URL construitURL(boolean co, boolean deco, String newpseudo) {
        String stringUrl = "http://"+ADRESSE_IP_SERVEUR_TOMCAT+":8080/clavard-serveur/ClavardServlet?pseudo="+aw_.getPseudo();
        if (co) {
            stringUrl+="&connexion=1&ip="+aw_.getIP()+"&ptcp="+aw_.getPort()+"&pudp="+aw_.getPortUDP();
        }
        if (deco) {
            stringUrl+="&deconnexion=1";
        }
        if (newpseudo!=null) {
            stringUrl+="&newpseudo="+newpseudo;
        }

        URL url=null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    
    public String envoyerRequete(URL url) {
    	 try {
             HttpURLConnection connexion = (HttpURLConnection) url.openConnection();
             connexion.setRequestMethod("GET");
             
             //on n'utilise pas le cache
             connexion.setUseCaches(false);
             
             //on règle la connexion en output
             connexion.setDoOutput(true);
             
             //on attend une réponse
             InputStream in = connexion.getInputStream();
             BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));
             StringBuilder response = new StringBuilder();
             String line;
             while (((line=buffReader.readLine())!=null)) {
                 response.append(line);
                 response.append('\r');
             }
             System.out.println("Message reçu : "+response.toString());
             buffReader.close();
             return response.toString();
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }   
    }
    

	
}
