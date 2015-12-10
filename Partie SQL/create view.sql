CREATE OR REPLACE VIEW projet.Devis_en_cours_view AS
SELECT COUNT(*) AS nombre , maison  
FROM projet.devis d
WHERE d.supprime = FALSE AND NOT EXISTS (SELECT * FROM  projet.demandes_devis WHERE d.num = devis_retenu)
GROUP BY maison;      
