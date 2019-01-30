package bdd;

import java.util.Date;

public class Message {
	String emetteur;
	String recepteur;
	String contenu;
	Date date;
	public Message(String lo_emet, String lo_recept,Date dat,String mess) {
		emetteur = lo_emet;
		recepteur = lo_recept;
		contenu = mess;
		date = dat;
	}
	public String getEmetteur() {
		return emetteur;
	}
	public void setEmetteur(String emetteur) {
		this.emetteur = emetteur;
	}
	public String getRecepteur() {
		return recepteur;
	}
	public void setRecepteur(String recepteur) {
		this.recepteur = recepteur;
	}
	public String getContenu() {
		return contenu;
	}
	public void setContenu(String contenu) {
		this.contenu = contenu;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	

}
