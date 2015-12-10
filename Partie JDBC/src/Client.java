import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class Client {
	public static java.util.Scanner scanner = new java.util.Scanner(System.in);
	private static Connection conn;
	private static int identifiant  = -1;
	public static void main(String[] args) {
		// chargement driver
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Driver PostgreSQL manquant !");
			System.exit(1);
		}
		String url = "jdbc:postgresql://localhost/dbmaison";
		String user="maison";
		String password="jw8s0F4";

		// connection à la db
		try{
			conn= DriverManager.getConnection(url,user,password);
		}catch (SQLException e){
			System.out.println("Pas de connexion ");
			System.exit(1);
		}

		menu();
		System.exit(0);
	}

	public static void menu(){
		int reponse = 1;

		do{
			System.out.println(" Vos possibilités : ");
			System.out.println(" 1) Se connecter ou s'inscrire");
			System.out.println(" 2) Déposer une demande de devis");
			System.out.println(" 3) Consulter vos demandes de devis");
			System.out.println(" 4) Obtenir des statistiques");
			System.out.println(" Autre ) Quitter ");
			System.out.println("Faites votre choix");
			System.out.println();
			reponse = scanner.nextInt();
			scanner.nextLine();

			switch(reponse){
			case 1 : seConnecterOuSincrire();
			break;

			case 2 : if(Client.identifiant == -1){
				System.out.println(" Vous devez vous connecter !!");
			} else {
				deposerDemandeDevis();
			}
			break;
			case 3 : if(Client.identifiant == -1){
				System.out.println(" Vous devez vous connecter !!");
			} else {
				consulterDemandeDevis();
			}
			break;
			case 4 : consulterStatistiques();
			default : System.out.println("Au revoir");
			break;
			}
		}while(reponse >= 1 && reponse <5 );
	}

	public static void seConnecterOuSincrire(){
		String login ="";
		String password ="";
		String reponse = "W";
		boolean verif;

		if (Client.identifiant != -1){
			System.out.println("Vous êtez déjà connecté");
		} else {
			try{
				while (reponse.charAt(0) != 'O' && reponse.charAt(0) != 'N'){
					System.out.println("Possédez-vous un compte ? O (Oui) ou N (non)");
					reponse = scanner.nextLine();
				}
					

				if (reponse.charAt(0) == 'O'){
					System.out.println("Entrer votre login");
					login = scanner.nextLine();
					System.out.println("Entrer votre password");
					password = scanner.nextLine();
					verif = login(login, password);
					if (verif == true){
						System.out.println("Connexion avec Succès");
					} else {
						System.out.println("Erreur dans la procédure de connexion");
					}	

				} else {
					String nom = null;
					String prenom = null;
					System.out.println("Entrer votre login");
					login = scanner.nextLine();
					System.out.println("Entrer votre password");
					password = scanner.nextLine();
					System.out.println("Entrer votre nom");
					nom = scanner.nextLine();
					System.out.println("Entrer votre prénom");
					prenom = scanner.nextLine();

					verif = signup(login, password, nom, prenom);
					if (verif == true){
						System.out.println("Inscription avec Succès");
					} else {
						System.out.println("Erreur dans la procédure d'inscription");
					}			
				}
			}catch(SQLException e){
				System.out.println("Une erreur s'est produite");
				e.printStackTrace();
			}
		}
	}

	private static boolean login(String login, String password) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("SELECT num , hash FROM projet.clients WHERE pseudo = ?");
		ResultSet rs;
		int numClient;

		ps.setString(1, login);
		ps.executeQuery();
		rs = ps.getResultSet();
		if(!rs.next()){
			System.out.println("Ce pseudo n'existe pas");
			return false;
		}
		String saltHash = rs.getString(2);
		numClient = rs.getInt(1);
		String salt = saltHash.substring(0, 32);
		String hash = saltHash.substring(32, 64);
		String hashTried = PBKDF2.PBKDF2Hash(password, salt).substring(32, 64);

		if (hashTried.equals(hash)){
			Client.identifiant = numClient;
		}

		return hashTried.equals(hash);
	} 

	private static boolean signup(String login, String password, String nom, String prenom) throws SQLException{
		PreparedStatement psExiste = conn.prepareStatement("SELECT * FROM projet.clients WHERE pseudo = ?");
		PreparedStatement psInsert = conn.prepareStatement("SELECT projet.inscrireClient (?, ?, ?, ?)");
		ResultSet rs;
		psExiste.setString(1, login);
		psExiste.executeQuery();
		rs = psExiste.getResultSet();
		if(rs.next()){
			return false;
		}
		psInsert.setString(1, login);
		String hash = PBKDF2.PBKDF2Hash(password);
		psInsert.setString(2, hash);
		psInsert.setString(3, nom);
		psInsert.setString(4, prenom);
		psInsert.execute();
		rs = psInsert.getResultSet();
		if(rs.next()) {
			Client.identifiant = rs.getInt(1);
			return true;
		}
		return false;
	}

	private static String[][] recupererAdresses(int client)throws SQLException{
		PreparedStatement psAdresses = conn.prepareStatement("SELECT num, rue, numero, ville, pays FROM projet.adresses WHERE client = ?");
		PreparedStatement psNombre = conn.prepareStatement("SELECT count(num) FROM projet.adresses WHERE client = ?");
		psNombre.setInt(1, client);
		ResultSet rs = psNombre.executeQuery();
		rs.next();

		String[][] output = new String[rs.getInt(1)][2];
		if(output.length == 0)return null;
		psAdresses.setInt(1, client);
		rs = psAdresses.executeQuery();

		while(rs.next()){
			output[rs.getRow()-1][0] = rs.getString(2)+", "+rs.getString(3)+", "+rs.getString(4)+", "+rs.getString(5);
			output[rs.getRow()-1][1] = rs.getString(1);
		}
		return output;
	}
	
	private static int choisirAdresse()throws SQLException{
		int adresse = -1;
		String[][] adresses = recupererAdresses(identifiant);
		if(adresses == null){
			adresse = ajouterAdresse(identifiant);
		}
		else{
			for (int i = 0; i < adresses.length; i++) {
				System.out.println((i+1)+") "+adresses[i][0]);
			}
			System.out.println((adresses.length+1)+") Entrer nouvelle adresse");

			int choix = 0;
			while(choix < 1 || choix > adresses.length+1)
				choix = Integer.parseInt(scanner.nextLine());
			if(choix == adresses.length+1){
				adresse = ajouterAdresse(identifiant);
			}
			else
				adresse = Integer.parseInt(adresses[choix-1][1]);
		}
		return adresse;
	}

	public static void deposerDemandeDevis(){

		try {
			System.out.println("Entrez la description: ");
			String description = scanner.nextLine();
			System.out.println("Choisissez l'adresse des travaux:");
			int adresseTravaux = choisirAdresse();

			System.out.println("Choisissez l'adresse de facturation:");
			int adresseFact = choisirAdresse();

			System.out.println("Entrez la date des travaux (aaaa-mm-jj): ");

			String dateTravaux = scanner.nextLine();
			java.sql.Date DtravauxStamp = java.sql.Date.valueOf(dateTravaux);

			PreparedStatement insererDemandeDevis = conn.prepareStatement("SELECT projet.insererDemandeDevis(?,?,?,?,?)");
			insererDemandeDevis.setInt(1, identifiant);
			insererDemandeDevis.setString(2, description);
			insererDemandeDevis.setInt(3, adresseTravaux);
			insererDemandeDevis.setInt(4, adresseFact);
			insererDemandeDevis.setDate(5, DtravauxStamp);
			insererDemandeDevis.execute();
		} catch (SQLException e) {
			System.out.println("Une erreur s'est produite");
			e.printStackTrace();
		}
	}

	private static int ajouterAdresse(int client) throws SQLException {
		PreparedStatement psInsererAdresse = conn.prepareStatement("SELECT projet.insererAdresse(?, ?, ?, ?, ?)");
		System.out.println("Entrez le nom de la rue: ");
		String rue = scanner.nextLine();

		System.out.println("Entrez le numero: ");
		int numero = Integer.parseInt(scanner.nextLine());

		System.out.println("Entrez le nom de la ville: ");
		String ville = scanner.nextLine();

		System.out.println("Entrez le code du pays (deux lettres): ");
		String pays = scanner.nextLine();
		while(pays.length() != 2){
			pays = scanner.nextLine();
			System.out.println("Code pays invalide");
		}
		psInsererAdresse.setString(1, rue);
		psInsererAdresse.setInt(2, numero);
		psInsererAdresse.setString(3, ville);
		psInsererAdresse.setString(4, pays);
		psInsererAdresse.setInt(5, client);
		ResultSet rs = psInsererAdresse.executeQuery();
		if(rs.next())
			return rs.getInt(1);
		return -1;
	}

	public static void consulterDemandeDevis(){
		int demande_devis;
		int devis;
		ResultSet rs;
		
		try{
			PreparedStatement checkDemande = conn.prepareStatement("SELECT (EXISTS(SELECT * FROM projet.demandes_devis WHERE num = ? AND devis_retenu IS NULL))");
			PreparedStatement selectDevis = conn.prepareStatement("SELECT d.num , d.description, age(d.date_introduction + INTERVAL '15 days', NOW()), d.devis_retenu FROM  projet.demandes_devis d WHERE d.client = ? ");
			PreparedStatement afficherDevis = conn.prepareStatement("SELECT * FROM projet.afficherDevis(?,?,?)");
			PreparedStatement accepterDevis = conn.prepareStatement("SELECT projet.accepterDevis(?,?,?)");
			PreparedStatement consulterOptions = conn.prepareStatement("SELECT num, description, montant FROM projet.options WHERE devis = ? AND NOT choisie");
			PreparedStatement accepterOption = conn.prepareStatement("SELECT projet.accepterOption(?,?)");
			selectDevis.setInt(1, Client.identifiant);
			selectDevis.executeQuery();
			rs = selectDevis.getResultSet();
			boolean check = false;

			System.out.println("Vos demandes de devis : ");
			System.out.println(" -----------------------------------------------------------------------");
			System.out.println("|Numero de la demande\t|Description \t|Temps restant\t\t|");
			System.out.println(" -----------------------------------------------------------------------");

			while(rs.next()){
				System.out.println("|"+rs.getInt(1) + "\t\t\t|" + rs.getString(2)  + "\t\t|" + rs.getString(3)+"\t|");
				check = true;
			}
			rs.close();

			if (check != false) {
				System.out.println("0) Aucun devis");
				System.out.println();
				System.out.println(" Quelle demande de devis souhaitez-vous accepter ?");
				demande_devis = scanner.nextInt();
				scanner.nextLine();
				if(demande_devis == 0){
					return;
				}
				checkDemande.setInt(1, demande_devis);
				rs = checkDemande.executeQuery();
				rs.next();
				if(!rs.getBoolean(1)){
					System.out.println("Un devis est déja retenu pour cette demande");
					System.out.println();
				}
				else{
					afficherDevis.setInt(1,demande_devis);
					afficherDevis.setInt(2,0);
					afficherDevis.setInt(3,47);
					rs = afficherDevis.executeQuery();
					System.out.println("|Numéro du devis \t|total\t\t|description\t|");
					System.out.println();
					int last = 0;
					
					while(rs.next()){
						last = rs.getInt(1);
						System.out.println("|"+last+ "\t\t\t|" + rs.getDouble(2) + "\t\t|" + rs.getString(3)+"\t|");
					}
					System.out.println("|"+(last+1)+"\tAucun devis");
					rs.close();

					System.out.println();
	
					do {
						System.out.println("Quel devis voulez-vous accepter? ");
						devis = scanner.nextInt();
						scanner.nextLine();
					}while(devis < 1 || devis > last+1);
					if(devis == last+1)
						return;
					consulterOptions.setInt(1, devis);
					rs = consulterOptions.executeQuery();
					if(rs.next()){
						System.out.println("Voici les options pour ce devis");
						System.out.println();
						System.out.println("|Numéro de l'option\t|Description\t|Montant\t|");
						System.out.println();
						do{
							System.out.println(rs.getInt(1)+"\t|"+rs.getString(2)+"\t|"+rs.getDouble(3)+"\t|");
						}while(rs.next());
						System.out.println();
						System.out.println("Entrez les numéros des options que vous voulez, separés par des ;");
						String[] options = scanner.nextLine().split(";");
						for (String string : options) {
							if(string.equals(""))break;
							accepterOption.setInt(1, devis);
							accepterOption.setInt(2, Integer.parseInt(string));
							accepterOption.execute();
						}
					}
					accepterDevis.setInt(1, Client.identifiant);
					accepterDevis.setInt(2, demande_devis);
					accepterDevis.setInt(3, devis);
					accepterDevis.executeQuery();
					
					
					System.out.println("Devis Accepté avec Succes");
				}
			}
			
		}catch(SQLException e){
			System.out.println(" Une erreur système s'est produite. Nos excuses");
			e.printStackTrace();
		}
	}

	public static void consulterStatistiques(){
		int choix = 1;
		int maison;

		try{
			PreparedStatement psSelectMaison = conn.prepareStatement("SELECT m.num, m.nom FROM projet.maisons m");
			PreparedStatement psChiffreAffaire = conn.prepareStatement("SELECT m.chiffre_affaire FROM projet.maisons m WHERE m.num = ?");
			PreparedStatement psTauxAcceptation = conn.prepareStatement("SELECT m.taux_acceptation FROM projet.maisons m WHERE m.num = ?");
			PreparedStatement psNombreTriche = conn.prepareStatement("SELECT m.nombre_triche FROM projet.maisons m WHERE m.num = ?");
			PreparedStatement psNombreDenonce = conn.prepareStatement("SELECT m.nombre_denonce FROM projet.maisons m WHERE m.num = ?");
			ResultSet rsSelectMaison;
			ResultSet rsChiffreAffaire;
			ResultSet rsTauxAcceptation;
			ResultSet rsNombreTriche;
			ResultSet rsNombreDenonce;

			do {
				System.out.println("Quels statistiques désirez-vous consulter ?");
				System.out.println("1) Les chiffres d’affaire des maisons");
				System.out.println("2) Le taux d’acceptation des devis de chaque maison");
				System.out.println("3) Le nombre de fois qu’une maison s’est fait prendre en train de tricher");
				System.out.println("4) Le nombre de fois qu’une maison a découvert qu’une autre maison trichait");
				System.out.println("5) Retourner au menu principal");
				choix = Integer.parseInt(scanner.nextLine());

				if (choix > 0 && choix < 5){
					System.out.println("Voici la liste des maisons . Choissiez le numéro de la maison que vous souahaitez consulter");
					System.out.println("Numéro \t Maison");
					psSelectMaison.executeQuery();
					rsSelectMaison = psSelectMaison.getResultSet();

					while(rsSelectMaison.next()){
						System.out.println(rsSelectMaison.getInt(1) + "\t" + rsSelectMaison.getString(2));
					}
					rsSelectMaison.close();
					maison = Integer.parseInt(scanner.nextLine());

					if(choix == 1){
						psChiffreAffaire.setInt(1, maison);
						psChiffreAffaire.executeQuery();
						rsChiffreAffaire = psChiffreAffaire.getResultSet();
						while(rsChiffreAffaire.next()){
							System.out.println("Cette maison a un chiffre d'affaire de " + rsChiffreAffaire.getDouble(1) +" Euros");
						}
						rsChiffreAffaire.close();
					} else if (choix == 2){
						psTauxAcceptation.setInt(1, maison);
						psTauxAcceptation.executeQuery();
						rsTauxAcceptation = psTauxAcceptation.getResultSet();
						while(rsTauxAcceptation.next()){
							System.out.println("Cette maison a un taux d'acceptation de " + rsTauxAcceptation.getDouble(1) * 100 +" %");
						}
						rsTauxAcceptation.close();
					} else if (choix == 3){
						psNombreTriche.setInt(1, maison);
						psNombreTriche.executeQuery();
						rsNombreTriche = psNombreTriche.getResultSet();

						while(rsNombreTriche.next()){
							System.out.println("Cette maison s’est fait prendre en train de tricher " + rsNombreTriche.getInt(1) + " fois");
						}
						rsNombreTriche.close();
					} else {
						psNombreDenonce.setInt(1, maison);
						psNombreDenonce.executeQuery();
						rsNombreDenonce = psNombreDenonce.getResultSet();

						while(rsNombreDenonce.next()){
							System.out.println("Cette maison a dénoncé des tricheurs " + rsNombreDenonce.getInt(1) + " fois");
						}
						rsNombreDenonce.close();
					}
				}
			}while(choix > 0 && choix < 5);
		}catch(SQLException e){
			System.out.println("Une erreur système s'est produite. Nos excuses");
			e.printStackTrace();
		}
	}

}

