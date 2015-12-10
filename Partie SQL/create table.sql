DROP SCHEMA projet CASCADE;
CREATE SCHEMA projet;
 
CREATE TABLE projet.clients(
    num serial PRIMARY KEY,
    pseudo VARCHAR(50) UNIQUE,
    hash CHAR(64),
    nom VARCHAR(50),
    prenom VARCHAR(50),
    CHECK(nom != ''),
    CHECK(prenom != '')
);

CREATE TABLE projet.adresses(
	num serial PRIMARY KEY,
	rue VARCHAR(50),
	numero INTEGER,
	ville VARCHAR(50),
	pays CHAR(2),
	client INTEGER REFERENCES projet.clients(num) NOT NULL
);
CREATE TABLE projet.demandes_devis(
    num serial PRIMARY KEY,
    client INTEGER REFERENCES projet.clients(num) NOT NULL,
    description VARCHAR(50) NOT NULL,
    adresse_travaux INTEGER REFERENCES projet.adresses(num) NOT NULL,
    adresse_facturation INTEGER REFERENCES projet.adresses(num),
    date_travaux DATE NOT NULL,
    date_introduction TIMESTAMP NOT NULL,
    devis_retenu INTEGER,
    CHECK (date_travaux > NOW())
);
 
CREATE TABLE projet.maisons (
    num serial,
    pseudo VARCHAR(50) UNIQUE,
    hash CHAR(64),
    nom varchar(50) NOT NULL,
    chiffre_affaire NUMERIC(10,2) DEFAULT 0,
    nombre_triche INTEGER DEFAULT 0,
    nombre_denonce INTEGER DEFAULT 0,
    nombre_devis INTEGER DEFAULT 0,
    nombre_gagnes INTEGER DEFAULT 0,
	taux_acceptation NUMERIC(3,2) DEFAULT 0,
    date_secret TIMESTAMP,
    date_godMode TIMESTAMP,
    date_denonce TIMESTAMP,
    CONSTRAINT m_pkey PRIMARY KEY (num)
);
 
CREATE TABLE projet.devis(
    num serial PRIMARY KEY,
    maison INTEGER REFERENCES projet.maisons(num) NOT NULL,
    demande INTEGER REFERENCES projet.demandes_devis(num) NOT NULL,
    montant NUMERIC(10,2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    total NUMERIC(10,2),
    secret BOOLEAN NOT NULL,
    god_mode BOOLEAN NOT NULL,
	date_introduction TIMESTAMP,
	supprime BOOLEAN DEFAULT false
);
 
ALTER TABLE projet.demandes_devis
ADD FOREIGN KEY(devis_retenu) REFERENCES projet.devis(num) ON DELETE CASCADE;
 
 
CREATE TABLE projet.options( 
    devis serial REFERENCES projet.devis(num),
    num serial,
	choisie BOOLEAN DEFAULT false,
    description VARCHAR(255),
    montant NUMERIC(10,2) CHECK (montant > 0),
    CONSTRAINT o_pkey PRIMARY KEY(devis, num)
);
