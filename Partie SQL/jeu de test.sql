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

insert into projet.maisons values(1, 'maison1', '337c7cdc9be7584a3f3603932a5e1d501374716cf0a08fdd231e513ec920adf9', 'maison1',DEFAULT, DEFAULT,  DEFAULT, DEFAULT, DEFAULT, NULL, NULL, NULL);
insert into projet.clients values(1, 'client1', 'b984d2b611033affd718cdc0eda9d099eafc9e49e7d5c1892c7a82df141c2128', 'client1', 'client1');
insert into projet.adresses values(1, 'rue1', 47, 'ville1', 'be', 1);
insert into projet.demandes_devis values(1, 1, 'demande1', 1, 1, '2016/12/23', NOW(), NULL);
insert into projet.devis values(1, 1, 1, 47, 'devis1', DEFAULT, false, false);
insert into projet.devis values(2, 1, 1, 47, 'devis2', DEFAULT, false, false);
insert into projet.devis values(3, 1, 1, 47, 'devis3', DEFAULT, false, false);
insert into projet.options values(1,1, DEFAULT, 'option1.1', 40);
insert into projet.options values(1,2, DEFAULT, 'option1.2', 44);
insert into projet.options values(1,3, DEFAULT, 'option1.3', 24);