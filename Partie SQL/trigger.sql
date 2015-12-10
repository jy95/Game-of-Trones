-- nombre devis

CREATE OR REPLACE FUNCTION projet.nombre_devis() RETURNS TRIGGER AS $$
DECLARE
     tauxAcceptation NUMERIC(3,2);
BEGIN  
    UPDATE projet.maisons m
    SET nombre_devis = m.nombre_devis +1
    WHERE m.num = NEW.maison;
    
	SELECT m.nombre_gagnes::NUMERIC(3,2)/m.nombre_devis::NUMERIC(3,2) 
	FROM projet.maisons m
	WHERE m.num = NEW.maison 
	INTO tauxAcceptation;
	
	UPDATE projet.maisons m
	SET taux_acceptation = tauxAcceptation
	WHERE m.num = NEW.maison;
	
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;
 
CREATE TRIGGER nombre_devis_trigger AFTER INSERT
ON projet.devis 
FOR EACH ROW EXECUTE PROCEDURE projet.nombre_devis();

-- verification devis

CREATE OR REPLACE FUNCTION projet.verificationDevis() RETURNS TRIGGER AS $$
DECLARE
	dateSecret TIMESTAMP;
	dateBlock TIMESTAMP;
	dateGod TIMESTAMP;
	autreMaison INTEGER;
BEGIN
    
    SELECT date_denonce
    FROM projet.maisons WHERE num = NEW.maison 
    INTO dateBlock;

    IF ((dateBlock IS NOT NULL) AND ( (SELECT DATE_PART('day',NOW() - (dateBlock + INTERVAL '1 day') )) < 1)) THEN	
	  RAISE insufficient_privilege;
    END IF;

    IF (NEW.secret) THEN
    
    SELECT date_secret 
    FROM projet.maisons WHERE num = NEW.maison 
    INTO dateSecret;

    IF ((dateSecret IS NOT NULL) AND ((DATE_PART('day',NOW() - (dateSecret + INTERVAL '1 day') )) < 1)) THEN	
	  RAISE insufficient_privilege;
    END IF;
	
	UPDATE projet.maisons 
	SET date_secret = NOW()
	WHERE num = NEW.maison;
    
    END IF;

    IF (NEW.god_mode) THEN
         SELECT date_godmode 
	     FROM projet.maisons WHERE num = NEW.maison 
	     INTO dateGod;

	 IF ((dateGod IS NOT NULL) AND ( (SELECT DATE_PART('day',NOW() - (dateGod + INTERVAL '7 day') )) < 7)) THEN	
	    RAISE insufficient_privilege;
	 END IF;
	
	UPDATE projet.maisons 
	SET date_godMode = NOW()
	WHERE num = NEW.maison;
	
	 SELECT maison 
	 FROM projet.devis 
	 WHERE demande = NEW.demande 
	 AND  god_mode AND maison != NEW.maison
	 INTO autreMaison;

        IF ( autreMaison IS NOT NULL) THEN
    
		UPDATE projet.maisons 
		SET nombre_triche = nombre_triche + 1 
		WHERE num = autreMaison;
	
		UPDATE projet.maisons 
		SET nombre_denonce = nombre_denonce + 1
		WHERE num = NEW.maison;
	    
		--Cette autre maison voit les devis de la journée supprimés (logiquement) du système (sauf acceptés)
		
		UPDATE projet.devis d
		SET supprime = TRUE
		WHERE maison = autreMaison AND DATE_PART('day',date_introduction) = DATE_PART('day',NOW())
		AND NOT EXISTS (SELECT * FROM projet.demandes_devis WHERE devis_retenu = d.num);
		
		-- elle ne pourra plus rien soumettre pendant 24h.	
		UPDATE projet.maisons 
		SET date_denonce = NOW()
		WHERE num = autreMaison;
		
		--ce devis qui est soumis se transforme en une soumission normale de devis
		NEW.god_mode := FALSE;
		
	 END IF;
	 
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER devis_insert_trigger BEFORE INSERT ON projet.devis FOR EACH ROW
EXECUTE PROCEDURE projet.verificationDevis();

-- accepter devis

CREATE OR REPLACE FUNCTION projet.accepterDevisTrigger() RETURNS TRIGGER AS $$
DECLARE
     maison_elue INTEGER;
	 CA NUMERIC(10,2);
	 tauxAcceptation NUMERIC(3,2);
BEGIN
	
    SELECT maison 
    FROM projet.devis d
    WHERE d.num = NEW.devis_retenu
    INTO maison_elue;
	
	SELECT total
	FROM projet.devis
	WHERE maison = maison_elue AND num = NEW.devis_retenu
	INTO CA;
	
    UPDATE projet.maisons m
    SET nombre_gagnes = nombre_gagnes + 1
    WHERE m.num = maison_elue;
    
	UPDATE projet.maisons m
    SET chiffre_affaire = chiffre_affaire + CA
    WHERE m.num = maison_elue;
	
	SELECT m.nombre_gagnes::NUMERIC(3,2)/m.nombre_devis::NUMERIC(3,2) 
	FROM projet.maisons m
	WHERE m.num = maison_elue 
	INTO tauxAcceptation;
	
	UPDATE projet.maisons m
	SET taux_acceptation = tauxAcceptation
	WHERE m.num = maison_elue;
	
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER devis_accepter_trigger AFTER UPDATE ON projet.demandes_devis FOR EACH ROW
EXECUTE PROCEDURE projet.accepterDevisTrigger();

CREATE OR REPLACE FUNCTION projet.montantTotalDemande() RETURNS TRIGGER AS $$
DECLARE
     demande_devis INTEGER;
BEGIN

    UPDATE projet.devis
    SET total  = total  + NEW.montant
    WHERE num = NEW.devis;
      
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER demande_total_trigger AFTER UPDATE ON projet.options FOR EACH ROW
EXECUTE PROCEDURE projet.montantTotalDemande();