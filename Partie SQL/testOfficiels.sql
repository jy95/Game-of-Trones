delete from projet.options;
delete from projet.devis;
delete from projet.demandes_devis;
delete from projet.adresses;
delete from projet.maisons;
delete from projet.clients;

ALTER SEQUENCE projet.maisons_num_seq RESTART WITH 1;
ALTER SEQUENCE projet.clients_num_seq RESTART WITH 1;
ALTER SEQUENCE projet.devis_num_seq RESTART WITH 1;
ALTER SEQUENCE projet.demandes_devis_num_seq RESTART WITH 1;
ALTER SEQUENCE projet.adresses_num_seq RESTART WITH 1;
ALTER SEQUENCE projet.options_num_seq RESTART WITH 1;


insert into projet.maisons values(1, 'st', '33a1ad847b4959c946d210cdfc18b1aade6a4d93bbe8f9d51112060ec056856e', 'Starque',DEFAULT, DEFAULT,  DEFAULT, DEFAULT, DEFAULT,DEFAULT, NULL, NULL, NULL);
insert into projet.maisons values(2, 'bo', 'f8f5b937f8a17fde570316ec3c76bc020ba8c923e87247f8bd4552040b6c437b', 'Boltone',DEFAULT, DEFAULT,  DEFAULT, DEFAULT, DEFAULT, DEFAULT, NULL, NULL, NULL);