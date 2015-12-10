import java.sql.*;
import java.util.Scanner;

public class Maison {
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
		String password="jw8s06F";

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
			System.out.println(" 2) Ajouter un devis");
			System.out.println(" 3) Consulter les devis");
			System.out.println(" 4) Obtenir des statistiques");
			System.out.println(" Autre ) Quitter ");
			System.out.println("Faites votre choix");
			System.out.println();
			reponse = scanner.nextInt();
			scanner.nextLine();

			switch(reponse){
			case 1 : seConnecterOuSincrire();
			break;

			case 2 : if(identifiant == -1){
					System.out.println(" Vous devez vous connecter !!");
				} else {
					insererDevis();
				}
			break;
			case 3 : if(Maison.identifiant == -1){
					System.out.println(" Vous devez vous connecter !!");
				} else {
					consulterDevis();
			}
			break;
			case 4 : consulterStatistiques();
			default : System.out.println("Au revoir");
			break;
			}

		}while(reponse >= 1 && reponse <5 );
	}

	private static int afficherDemandes(){
		try {
			PreparedStatement psDemandes = conn.prepareStatement("SELECT num, description, date_travaux from projet.demandes_devis where DATE_PART('day', NOW() -(date_introduction + INTERVAL '15 days')) <0 AND devis_retenu IS NULL");
			System.out.println("Voici les demandes de devis:");
			ResultSet rsDemandes = psDemandes.executeQuery();
			System.out.println("Numero\t|Description\t\t|Date Travaux\t|");
			while(rsDemandes.next()){
				System.out.println(rsDemandes.getInt(1)+"\t|"+rsDemandes.getString(2)+"\t|"+rsDemandes.getDate(3)+"\t|");
			}
			System.out.println("0) Quitter");
			int demande = scanner.nextInt();
			scanner.nextLine();
			return demande;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Une erreur s'est produite");
			return -1;
		}
	}

	private static void insererDevis() {
		int demande = afficherDemandes();
		if(demande < 1)
			return;
		try{
			PreparedStatement psDevis = conn.prepareStatement("SELECT * FROM projet.insererDevis(?,?,?,?,?)");
	
			System.out.println("Entrez la description du devis:");
			String description = scanner.nextLine();
			System.out.println("Entrez le montant du devis:");
			java.math.BigDecimal montant = java.math.BigDecimal.valueOf(scanner.nextDouble());
			scanner.nextLine();
			System.out.println("1) Devis normal");
			System.out.println("2) Devis caché");
			System.out.println("3) Devis cachant");
			System.out.println("Choisissez le type du devis:");
			int type = scanner.nextInt()-1;
			scanner.nextLine();
			if(type < 0 || type > 2)
				type = 0;
			psDevis.setInt(1, identifiant);
			psDevis.setInt(2, demande);
			psDevis.setBigDecimal(3, montant);
			psDevis.setString(4, description);
			psDevis.setInt(5, type);
			ResultSet rs = psDevis.executeQuery();
			int devis = -1;
			if(rs.next()){
				devis = rs.getInt(1);
			}
			System.out.println();
			System.out.println("Voulez-vous une option? (O/N)");
			while(scanner.nextLine().charAt(0) == 'O'){
				insererOption(devis);
				System.out.println("Voulez-vous une option? (O/N)");
			}
			System.out.println("Devis inséré avec succès!");
		}
		catch(SQLException e){
			int state = Integer.parseInt(e.getSQLState());
			if(state == 42501){
				System.out.println("INSUFFICIENT PRIVILEGE MADAFAKA!!!");
			}
			else

				e.printStackTrace();
		}
	}

	private static void insererOption(int devis) throws SQLException {
		PreparedStatement psOption = conn.prepareStatement("SELECT projet.insererOption(?,?,?)");
		System.out.println("Entrez la description de l'option:");
		String description = scanner.nextLine();
		System.out.println("Entrez le prix de l'option:");
		java.math.BigDecimal montant = java.math.BigDecimal.valueOf(scanner.nextDouble());
		scanner.nextLine();
		psOption.setInt(1, devis);
		psOption.setString(2, description);
		psOption.setBigDecimal(3, montant);
		psOption.execute();
	}

	public static void seConnecterOuSincrire(){
		String login ="";
		String password ="";
		char reponse = 'N';
		boolean verif;
		
		if (identifiant != -1){
			System.out.println("Vous êtez déjà connecté");
		} else {
			try{
				do{
					System.out.println("Possédez-vous un compte ? O (Oui) ou N (non)");
					reponse = scanner.nextLine().charAt(0);
				}while(reponse != 'O' && reponse != 'N');

				if (reponse == 'O'){
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
					
					verif = signup(login, password, nom);
					if (verif == true){
						System.out.println("Inscription avec Succès");
					} else {
						System.out.println("Erreur dans la procédure d'inscription");
					}			
				}
			}catch(SQLException e){
				e.printStackTrace();
				System.out.println("Une erreur s'est produite");
			}
		}
	}
	
	private static boolean login(String login, String password) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("SELECT num , hash FROM projet.maisons WHERE pseudo = ?");
		ResultSet rs;
		int numMaison;
		
		ps.setString(1, login);
		ps.executeQuery();
		rs = ps.getResultSet();
		while (rs.next()){
			String saltHash = rs.getString(2);
		
		numMaison = rs.getInt(1);
		String salt = saltHash.substring(0, 32);
		String hash = saltHash.substring(32, 64);
		String hashTried = PBKDF2.PBKDF2Hash(password, salt).substring(32, 64);
		
		if (hashTried.equals(hash)){
			Maison.identifiant = numMaison;
		}
		
		return hashTried.equals(hash);
		}return false;
	} 
	
	private static boolean signup(String login, String password, String nom) throws SQLException{
		PreparedStatement psExiste = conn.prepareStatement("SELECT * FROM projet.maisons WHERE pseudo = ?");
		PreparedStatement psInsert = conn.prepareStatement("SELECT projet.inscrireMaison (?, ?, ?)");
		ResultSet rs;
		psExiste.setString(1, login);
		psExiste.executeQuery();
		rs = psExiste.getResultSet();
		if(rs.next()){
			System.out.println("HERE");
			return false;
		}
		psInsert.setString(1, login);
		String hash = PBKDF2.PBKDF2Hash(password);
		psInsert.setString(2, hash);
		psInsert.setString(3, nom);
		psInsert.execute();
		rs = psInsert.getResultSet();
		if(rs.next()) {
			Maison.identifiant = rs.getInt(1);
			return true;
		}
		return false;
	}
	
	private static void consulterDevis(){
		int demande = afficherDemandes();
		if(demande < 1)
			return;
		try {
			PreparedStatement psDevis = conn.prepareStatement("SELECT * FROM projet.afficherDevis(?,?,?)");
			psDevis.setInt(1, demande);
			psDevis.setInt(2, 1);
			psDevis.setInt(3, identifiant);
			ResultSet rs = psDevis.executeQuery();

			System.out.println("|Numéro du devis \t|total\t\t|description\t|");
			System.out.println();
			while(rs.next()){
				System.out.println("|"+rs.getInt(1)+ "\t\t\t|" + rs.getDouble(2) + "\t\t|" + rs.getString(3)+"\t|");
			}
			System.out.println();
			
		
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
			PreparedStatement psNombreDevisEnCours = conn.prepareStatement("SELECT nombre FROM projet.Devis_en_cours_view WHERE maison = ?");
			ResultSet rsSelectMaison;
			ResultSet rsChiffreAffaire;
			ResultSet rsTauxAcceptation;
			ResultSet rsNombreTriche;
			ResultSet rsNombreDenonce;
			ResultSet rsNombreDevisEnCours;

			do {

				System.out.println();
				System.out.println("Quels statistiques désirez-vous consulter ?");
				System.out.println("1) Les chiffres d’affaire des maisons");
				System.out.println("2) Le taux d’acceptation des devis de chaque maison");
				System.out.println("3) Le nombre de fois qu’une maison s’est fait prendre en train de tricher");
				System.out.println("4) Le nombre de fois qu’une maison a découvert qu’une autre maison trichait");
				System.out.println("5) Nombre de devis en cours");
				System.out.println("6) Retourner au menu principal");

				System.out.println();
				choix = Integer.parseInt(scanner.nextLine());

				if (choix > 0 && choix < 6){
					System.out.println("Voici la liste des maisons . Choissiez le numéro de la maison que vous souahaitez consulter");
					System.out.println();
					System.out.println("Numéro \t Maison");
					psSelectMaison.executeQuery();
					rsSelectMaison = psSelectMaison.getResultSet();

					System.out.println();
					while(rsSelectMaison.next()){
						System.out.println(rsSelectMaison.getInt(1) + "\t" + rsSelectMaison.getString(2));
					}
					rsSelectMaison.close();
					System.out.println();
					maison = Integer.parseInt(scanner.nextLine());

					if(choix == 1){
						psChiffreAffaire.setInt(1, maison);
						psChiffreAffaire.executeQuery();
						rsChiffreAffaire = psChiffreAffaire.getResultSet();
						while(rsChiffreAffaire.next()){
							System.out.println("Cette maison a un chiffre d'affaire de " + rsChiffreAffaire.getDouble(1) +" Euros");
						}
						System.out.println();
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
					} else if (choix == 4){
						psNombreDenonce.setInt(1, maison);
						psNombreDenonce.executeQuery();
						rsNombreDenonce = psNombreDenonce.getResultSet();

						while(rsNombreDenonce.next()){
							System.out.println("Cette maison a dénoncé des tricheurs " + rsNombreDenonce.getInt(1) + " fois");
						}
						rsNombreDenonce.close();
					}else if (choix == 5){
						psNombreDevisEnCours.setInt(1, maison);
						psNombreDevisEnCours.executeQuery();
						rsNombreDevisEnCours = psNombreDevisEnCours.getResultSet();
						int nombre = 0;
						if(rsNombreDevisEnCours.next()){
							nombre = rsNombreDevisEnCours.getInt(1);
						}
						System.out.println("Cette maison a " + nombre + " devis en cours");
						rsNombreDevisEnCours.close();
					}
				}
			}while(choix > 0 && choix < 6);
		}catch(SQLException e){
			System.out.println("Une erreur système s'est produite. Nos excuses");
			e.printStackTrace();
		}
	}

}

