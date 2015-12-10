
-- user client

GRANT CONNECT ON DATABASE dbmaison TO client;
GRANT USAGE ON SCHEMA projet TO client;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA projet TO client;
GRANT INSERT, SELECT, UPDATE ON projet.demandes_devis TO client;
GRANT UPDATE,SELECT ON projet.devis TO client;
GRANT UPDATE,SELECT ON projet.options TO client;
GRANT UPDATE,SELECT ON projet.maisons TO client;
GRANT INSERT,SELECT ON projet.clients TO client;

GRANT INSERT,SELECT ON projet.adresses TO client;


-- user maison


GRANT CONNECT ON DATABASE dbmaison TO maison;
GRANT SELECT ON projet.demandes_devis TO maison;

GRANT USAGE ON SCHEMA projet TO maison;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA projet TO maison;
GRANT SELECT,INSERT,DELETE,UPDATE ON projet.devis TO maison;
GRANT SELECT,INSERT,UPDATE,DELETE ON projet.options TO maison;
GRANT INSERT,SELECT,UPDATE ON projet.maisons TO maison;
GRANT SELECT ON projet.devis_en_cours_view TO maison;