package graphique;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import core.Controller;

import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JPanel;

public class Accueil implements ActionListener, KeyListener{
	private JFrame frmAccueil;
	private JTextField txtfield;
	private JTextField txtLogin;
	private JTextField txtBienvenueSurVotre;
	private JTextField txtVeuillezVousIdentifier;
	private String log;
	private JLabel lblLePseudoNe;
	
	JRadioButton rdbtnLocal;
	
	/**
	 * Launch the application.
	 * @wbp.parser.entryPoint
	 */
	
	
	

	public Accueil() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {
		frmAccueil = new JFrame();
		frmAccueil.getContentPane().setBackground(new Color(0, 0, 51));
		frmAccueil.setForeground(new Color(0, 0, 0));
		frmAccueil.setBackground(new Color(0, 0, 0));
		frmAccueil.setBounds(100, 100, 1080, 720);
		frmAccueil.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAccueil.getContentPane().setLayout(null);
		frmAccueil.setTitle("Accueil");
		
		txtfield = new JTextField();
		txtfield.setFont(new Font("Open Sans", Font.PLAIN, 15));
		txtfield.setMargin(new Insets(2, 10, 2, 2));
		txtfield.setForeground(Color.WHITE);
		txtfield.setBackground(new Color(0, 0, 153));
		txtfield.setBounds(389, 407, 261, 28);
		txtfield.addKeyListener(this);
		frmAccueil.getContentPane().add(txtfield);
		txtfield.setColumns(10);
		//txtfield.addActionListener(l);
		
		txtLogin = new JTextField();
		txtLogin.setBorder(null);
		txtLogin.setEditable(false);
		txtLogin.setForeground(Color.WHITE);
		txtLogin.setBackground(new Color(0, 0, 51));
		txtLogin.setFont(new Font("Source Code Pro", Font.ITALIC, 18));
		txtLogin.setText("login :");
		txtLogin.setBounds(238, 400, 91, 40);
		frmAccueil.getContentPane().add(txtLogin);
		txtLogin.setColumns(10);
		
		txtBienvenueSurVotre = new JTextField();
		txtBienvenueSurVotre.setBorder(null);
		txtBienvenueSurVotre.setHorizontalAlignment(SwingConstants.CENTER);
		txtBienvenueSurVotre.setEditable(false);
		txtBienvenueSurVotre.setForeground(new Color(255, 255, 0));
		txtBienvenueSurVotre.setBackground(new Color(0, 0, 51));
		txtBienvenueSurVotre.setFont(new Font("Dialog", Font.BOLD, 31));
		txtBienvenueSurVotre.setText("Bienvenue sur votre plateforme de clavardage");
		txtBienvenueSurVotre.setBounds(141, 58, 834, 99);
		frmAccueil.getContentPane().add(txtBienvenueSurVotre);
		txtBienvenueSurVotre.setColumns(10);
		
		txtVeuillezVousIdentifier = new JTextField();
		txtVeuillezVousIdentifier.setBorder(null);
		txtVeuillezVousIdentifier.setEditable(false);
		txtVeuillezVousIdentifier.setFont(new Font("Tahoma", Font.PLAIN, 22));
		txtVeuillezVousIdentifier.setForeground(Color.WHITE);
		txtVeuillezVousIdentifier.setBackground(new Color(0, 0, 51));
		txtVeuillezVousIdentifier.setText("Veuillez vous identifier");
		txtVeuillezVousIdentifier.setBounds(403, 171, 247, 28);
		frmAccueil.getContentPane().add(txtVeuillezVousIdentifier);
		txtVeuillezVousIdentifier.setColumns(10);
		
		JButton btnConnexion = new JButton("Connexion");
		btnConnexion.setBounds(701, 409, 118, 25);
		btnConnexion.addActionListener(this);
		//btnConnexion.getInputMap().pu
		//btnConnexion.getInputMap().put(java.awt.event.KeyEvent.VK_ENTER, arg1);
		frmAccueil.getContentPane().add(btnConnexion);
		
		lblLePseudoNe = new JLabel("Le pseudo ne doit ni être vide ni contenir d'accent, ni de \":\" ou de \";\" !");
		lblLePseudoNe.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblLePseudoNe.setForeground(Color.RED);
		lblLePseudoNe.setBounds(253, 482, 574, 48);
		lblLePseudoNe.setVisible(false);
		frmAccueil.getContentPane().add(lblLePseudoNe);
		
		rdbtnLocal = new JRadioButton("Local");
		rdbtnLocal.setFont(new Font("Source Sans Pro", Font.PLAIN, 18));
		rdbtnLocal.setForeground(Color.WHITE);
		rdbtnLocal.setBackground(new Color(0,0, 51));
		rdbtnLocal.setBounds(297, 322, 75, 25);
		rdbtnLocal.setSelected(true); //l'application fonctionne en local par défaut
		frmAccueil.getContentPane().add(rdbtnLocal);
		
		JRadioButton rdbtnEnLigne = new JRadioButton("En ligne");
		rdbtnEnLigne.setFont(new Font("Source Sans Pro", Font.PLAIN, 18));
		rdbtnEnLigne.setForeground(Color.WHITE);
		rdbtnEnLigne.setBackground(new Color(0,0, 51));
		rdbtnEnLigne.setBounds(681, 322, 117, 25);
		frmAccueil.getContentPane().add(rdbtnEnLigne);
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnEnLigne);
		group.add(rdbtnLocal);
		
		JLabel lblNewLabel = new JLabel(new ImageIcon("C:/Users/Mehdi/Desktop/INSA/4IR/POO/Projet/Final-Clavard/Clavard/src/graphique/Benjamin_clavard.png"));
		lblNewLabel.setBounds(34, 196, 136, 320);
		frmAccueil.getContentPane().add(lblNewLabel);
		
		frmAccueil.setVisible(true);
		

		
	}


	public String getLog() {
		return log;
	}
	public void action() {
		log = this.txtfield.getText();
		if (log.contains(":") || log.contains(";") || log.isEmpty() ||  log.contains("é")|| log.contains("è")|| log.contains("ê")|| log.contains("à")){
			lblLePseudoNe.setVisible(true);
		}
		else {
			frmAccueil.setVisible(false);	
		}
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		action();
	}
	
	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == 10) {
			action();
		}
	}
	
	@Override
	public void keyTyped(KeyEvent ke) {
	}
	
	@Override
	public void keyReleased(KeyEvent ke) {}
	
	public boolean isLocal() {return  rdbtnLocal.isSelected();}
	
	public static void main(String[] args) {
		new Controller(6002, 5002);
	}
}
