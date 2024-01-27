# Akka

Gestion de comptes bancaires


On cherche à développer un système de gestion de comptes bancaires à l’aide d’Akka. Un banquier
est en charge de plusieurs comptes, et travaille dans une banque. Lorsqu’un client se présente à la
banque pour déposer ou retirer de l’argent, la banque doit transmettre la demande du client au
banquier responsable de son compte.
Un mécanisme de persistance doit être implémenté en liaison avec un SGBD, afin de pouvoir
retrouver l’état des comptes des clients mme en cas d’arrêt du système.

Exemple de programme en Java pour créer et utiliser des Acteurs Akka. 

Pour compiler et exécuter, utiliser Maven, avec les commandes suivantes :

```
mvn compile
mvn exec:java -Dexec.mainClass="sd.akka.App"
```
