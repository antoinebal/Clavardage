package reseau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpTalker {
	

	
	private AgentWAN aw_;
	
	HttpTalker(AgentWAN aw) {
		aw_=aw;
	}
	
    /*envoie requ�te http au serveur, retourne la liste des connect�s
     * envoy�e par le serveur
     */
    public String subscribe() {
        URL url = construitURL(true, false, null);
        return envoyerRequete(url);
    }
    
    public void notifyDeconnexion() {
    	URL url = construitURL(false, true, null);
    	envoyerRequete(url);
    }
    
    /* � appeler quand l'user change de pseudo */
    public void notifyNewPseudo(String newPseudo) {
    	URL url = construitURL(false, false, newPseudo);
    	envoyerRequete(url);
    }
    
    /* construit l'url en fonction des param�tres */
    public URL construitURL(boolean co, boolean deco, String newpseudo) {
        String stringUrl = "http://"+aw_.getIPServer()+":8080/clavard-serveur/ClavardServlet?pseudo="+aw_.getPseudo();
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
             
             //on r�gle la connexion en output
             connexion.setDoOutput(true);
             
             //on attend une r�ponse
             InputStream in = connexion.getInputStream();
             BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));
             StringBuilder response = new StringBuilder();
             String line;
             while (((line=buffReader.readLine())!=null)) {
                 response.append(line);
                 response.append('\r');
             }
             System.out.println("Message re�u : "+response.toString());
             buffReader.close();
             return response.toString();
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }   
    }
    

	
}
