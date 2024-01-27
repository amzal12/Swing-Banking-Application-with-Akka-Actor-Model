package sd.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.Hashtable;

public class BanqueActor extends AbstractActor {

    private final Hashtable<Integer, ClientInfo> clients;
    private final Hashtable<Integer, ActorRef> banquiers;
    private final Connexion conn;

    // Constructeur privé pour initialiser les données nécessaires
    private BanqueActor(Connexion connexion, Hashtable<Integer, ClientInfo> clients) {
        this.clients = new Hashtable<>();
        this.banquiers = new Hashtable<>();
        this.conn = connexion;
        this.clients.putAll(clients);

        // Création des acteurs Banquier pour chaque banquier disponible
        ArrayList<Integer> bankers = connexion.getBanquiers();
        for (int idBanquier : bankers) {
            ActorRef banquier = getContext().actorOf(BanquierActor.props(connexion, clients), "acteurBanquier_" + idBanquier);
            this.banquiers.put(idBanquier, banquier);
        }
    }

    // Méthode de création des props pour le constructeur
    public static Props props(Connexion connexion, Hashtable<Integer, ClientInfo> clients) {
        return Props.create(BanqueActor.class, () -> new BanqueActor(connexion, clients));
    }

    @Override
    public Receive createReceive() {
        // Définition des comportements en fonction des types de messages reçus
        return receiveBuilder()
                .match(Depot.class, this::depot)
                .match(Retrait.class, this::retrait)
                .match(GetSolde.class, this::getSolde)
                .build();
    }

    // Logique de dépôt d'argent sur un compte
    private void depot(Depot message) {
        int idClient = message.idClient;
        int montant = message.montant;

        if (clients.containsKey(idClient)) {
            ClientInfo clientInfo = clients.get(idClient);
            int idBanquier = clientInfo.getIdBanquier();
            ActorRef banquier = banquiers.get(idBanquier);
            banquier.tell(new BanquierActor.Depot(idClient, montant), getSender());
        } else {
            System.out.println("Il n'y a pas de client avec l'ID : " + idClient);
        }
    }

    // Logique de retrait d'argent d'un compte
    private void retrait(Retrait message) {
        int idClient = message.idClient;
        int montant = message.montant;

        if (clients.containsKey(idClient)) {
            ClientInfo clientInfo = clients.get(idClient);
            int nouveauSolde = conn.getSoldeClient(idClient) - montant;

            if (nouveauSolde < 100) {
                System.out.println(clientInfo.getNomClient() + " Votre solde est de " +
                        conn.getSoldeClient(idClient) + ". Impossible de faire le retrait de " + montant);
            } else {
                int idBanquier = clientInfo.getIdBanquier();
                ActorRef banquier = banquiers.get(idBanquier);
                banquier.tell(new BanquierActor.Retrait(idClient, montant), getSender());
            }
        } else {
            System.out.println("Pas de client avec l'ID : " + idClient);
        }
    }

    // Logique pour récupérer le solde d'un compte
    private void getSolde(GetSolde message) {
        int idClient = message.idClient;
        ClientInfo clientInfo = clients.get(idClient);

        if (clientInfo != null) {
            int idBanquier = clientInfo.getIdBanquier();
            ActorRef banquier = banquiers.get(idBanquier);
            banquier.tell(new BanquierActor.GetSolde(idClient), getSelf());  // Demander au banquier le solde
        } else {
            System.out.println("Pas de client avec l'ID : " + idClient);
        }
    }

    // Classe interne représentant le message de dépôt
    public static class Depot {
        public final int idClient;
        public final int montant;

        public Depot(int idClient, int montant) {
            this.idClient = idClient;
            this.montant = montant;
        }
    }

    // Classe interne représentant le message de retrait
    public static class Retrait {
        public final int idClient;
        public final int montant;

        public Retrait(int idClient, int montant) {
            this.idClient = idClient;
            this.montant = montant;
        }
    }

    // Classe interne représentant le message de récupération du solde
    public static class GetSolde {
        public int idClient;

        public GetSolde(int idClient) {
            this.idClient = idClient;
        }
    }
}
