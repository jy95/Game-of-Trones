
/*CREATION TYPE PERSONNALISE*/
CREATE TYPE DevisReturn
AS (numero INTEGER, montant NUMERIC(10,2), description VARCHAR(255));

CREATE OR REPLACE FUNCTION projet.afficherDevis(INTEGER, INTEGER, INTEGER) RETURNS SETOF DevisReturn AS $$
    DECLARE
        numDemande ALIAS FOR $1;
		quiDemande ALIAS FOR $2;
		numMaison  ALIAS FOR $3;
        sortie DevisReturn;
        operation RECORD;
    BEGIN
	IF(EXISTS(SELECT * FROM projet.demandes_devis WHERE num = numDemande AND devis_retenu IS NULL)) THEN
		-- affichage demandé par maison
		IF (quiDemande = 1) THEN
			-- affiche tout sauf les devis secrets des autres maisons
			FOR operation IN SELECT * FROM projet.devis de WHERE de.supprime != TRUE AND de.demande = numDemande  AND ((de.maison = numMaison ) OR (de.maison != numMaison AND de.secret != TRUE))
					LOOP
					SELECT operation.num ,operation.montant, operation.description 
					INTO sortie;
					RETURN NEXT sortie;
			END LOOP; 
			
		END IF;
		
		-- affichage demandé par client
		IF (quiDemande = 0) THEN
			IF ((SELECT COUNT(*) FROM projet.devis de WHERE de.god_mode AND de.demande = numDemande AND de.supprime != TRUE) = 1) THEN
				SELECT de.num , de.montant, de.description
				FROM projet.devis de
				WHERE de.demande = numDemande AND de.god_mode AND de.supprime != TRUE
				INTO sortie;
				RETURN NEXT sortie;
			ELSE
				FOR operation IN SELECT * FROM projet.devis de WHERE de.demande = numDemande AND de.supprime != TRUE
					LOOP
					SELECT operation.num ,operation.montant, operation.description 
					INTO sortie;
					RETURN NEXT sortie;
				END LOOP; 
			END IF;
		END IF;
	END IF;
	RETURN;
    END;
$$ LANGUAGE plpgsql; 

-- function devis

CREATE OR REPLACE FUNCTION projet.insererDevis(INTEGER,INTEGER,NUMERIC(10,2),VARCHAR (255),INTEGER) RETURNS INTEGER AS $$
DECLARE
    numMaison ALIAS FOR $1;
    numDemande ALIAS FOR $2;
    devisMontant ALIAS FOR $3;
    devisDescription ALIAS FOR $4;
    typeInsertion ALIAS FOR $5;
	valeur INTEGER;
BEGIN
    IF (typeInsertion = 0) THEN
		INSERT INTO projet.devis 
		values(DEFAULT, numMaison, numDemande, devisMontant, devisDescription, devisMontant, false, false,NOW(),false)
		RETURNING num INTO valeur;
    END IF;

    IF (typeInsertion = 1) THEN
		INSERT INTO projet.devis 
		values(DEFAULT, numMaison, numDemande, devisMontant, devisDescription, devisMontant, true, false,NOW(),false)
		RETURNING num INTO valeur;
    END IF;

    IF (typeInsertion = 2) THEN
		INSERT INTO projet.devis 
		values(DEFAULT, numMaison, numDemande, devisMontant, devisDescription, devisMontant	, false, true,NOW(),false)
		RETURNING num INTO valeur;
    END IF;
	RETURN valeur;
END;
$$ LANGUAGE plpgsql;

-- insert new client
CREATE OR REPLACE FUNCTION projet.inscrireClient(VARCHAR(50), VARCHAR(50), VARCHAR(50), VARCHAR(50)) RETURNS INTEGER AS $$
	DECLARE
		pseudoClient ALIAS FOR $1;
		passwordClient ALIAS FOR $2;
		nomClient ALIAS FOR $3;
		prenomClient ALIAS FOR $4;
		numeroClient INTEGER;
	BEGIN
		IF (passwordClient = '') THEN
			RAISE zero_length_character_string;
		END IF;
		INSERT INTO projet.clients values (DEFAULT, pseudoClient, passwordClient, nomClient, prenomClient) RETURNING num INTO numeroClient;
		RETURN numeroClient;
	END;
$$ LANGUAGE plpgsql;


-- insert new maison
CREATE OR REPLACE FUNCTION projet.inscrireMaison(VARCHAR(50), VARCHAR(50), VARCHAR(50)) RETURNS INTEGER AS $$
	DECLARE
		pseudoMaison ALIAS FOR $1;
		passwordMaison ALIAS FOR $2;
		nomMaison ALIAS FOR $3;
		numeroMaison INTEGER;
	BEGIN
		IF (passwordMaison = '') THEN
			RAISE zero_length_character_string;
		END IF;
		INSERT INTO projet.maisons (pseudo, hash, nom) values (pseudoMaison, passwordMaison, nomMaison) RETURNING num INTO numeroMaison;
		RETURN numeroMaison;
	END;
$$ LANGUAGE plpgsql;

-- accepter devis
CREATE OR REPLACE FUNCTION projet.accepterDevis(INTEGER, INTEGER, INTEGER) RETURNS INTEGER AS $$
	DECLARE
		numClient ALIAS FOR $1;
		numDemande ALIAS FOR $2;
		numDevis ALIAS FOR $3;
	BEGIN
		IF(NOT EXISTS(SELECT * FROM projet.devis WHERE num = numDevis AND demande = numDemande)) THEN
			RAISE foreign_key_violation;
		END IF;
		IF (EXISTS (SELECT * FROM projet.demandes_devis WHERE num = numDemande AND devis_retenu IS NOT NULL)) THEN
			RAISE foreign_key_violation;
		END IF;
		IF(NOT EXISTS(SELECT * FROM projet.demandes_devis WHERE num = numDemande AND client = numClient)) THEN
			RAISE foreign_key_violation;
		END IF;
		
		IF((SELECT DATE_PART('day', NOW() -(date_introduction + INTERVAL '15 days')) FROM projet.demandes_devis WHERE num = numDemande) >= 0) THEN
			RAISE insufficient_privilege;
		END IF;

		UPDATE projet.demandes_devis d
		SET devis_retenu = numDevis
		WHERE d.num = numDemande;

		RETURN 0;
	END;

$$ LANGUAGE plpgsql;

-- inserer adresse
-- le numéro est encore un INTEGER, le temps que tu puisses changer son type par exemple en VARCHAR(5)
CREATE OR REPLACE FUNCTION projet.insererAdresse(VARCHAR(50), INTEGER, VARCHAR(50), CHAR(2), INTEGER) RETURNS INTEGER AS $$
	DECLARE
		Rue ALIAS FOR $1;
		Numero ALIAS FOR $2;
		Ville ALIAS FOR $3;
		Pays ALIAS FOR $4;
		Client ALIAS FOR $5;
		numAdresse INTEGER;
	BEGIN
		 INSERT INTO projet.adresses (rue , numero, ville, pays, client) values (Rue,Numero,Ville,Pays, Client) RETURNING num INTO numAdresse;
		 RETURN numAdresse;
	END;

$$ LANGUAGE plpgsql;

-- insérer demande de devis
-- on laisse l'insert lancer les erreurs (grace aux contraites de la tables)
CREATE OR REPLACE FUNCTION projet.insererDemandeDevis(INTEGER, VARCHAR(50) , INTEGER ,INTEGER , DATE ) RETURNS INTEGER AS $$
	DECLARE
		Client ALIAS FOR $1;
		Description  ALIAS FOR $2;
		Adresse_travaux ALIAS FOR $3;
		Adresse_facturation ALIAS FOR $4;
		Date_travaux ALIAS FOR $5;
		numDemande INTEGER;
	BEGIN
		 INSERT INTO projet.demandes_devis (client, description, adresse_travaux, adresse_facturation , date_travaux, date_introduction) 
		 values (Client,Description,Adresse_travaux,Adresse_facturation , Date_travaux, NOW()) 
		 RETURNING num 
		 INTO numDemande;
		 
		 RETURN numDemande;
	END;

$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION projet.insererOption(INTEGER, VARCHAR(255) , NUMERIC(10,2)) RETURNS INTEGER AS $$
	DECLARE
		numDevis ALIAS FOR $1;
		Description ALIAS FOR $2;
		Montant ALIAS FOR $3;
		Retour INTEGER;
	BEGIN
		 INSERT INTO projet.options (devis , num , description , montant ) 
		 values (numDevis, DEFAULT, Description, Montant) 
		 RETURNING num 
		 INTO Retour;
		 
		 RETURN Retour;
	END;

$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION projet.accepterOption(INTEGER, INTEGER) RETURNS INTEGER AS $$
	DECLARE
		numOption ALIAS FOR $2;
		numDevis ALIAS FOR $1;
	BEGIN
		UPDATE projet.options SET choisie = TRUE WHERE devis = numDevis AND num = numOption ;
		RETURN NULL;
	END;
	

$$ LANGUAGE plpgsql;
