package sd.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import sd.akka.App;

import java.util.Hashtable;

public class BanquierActor extends AbstractActor {
    private final Connexion connexion;
    private final Hashtable<Integer, ClientInfo> clients;

    // Constructeur privé pour initialiser les données nécessaires
    private BanquierActor(Connexion connexion, Hashtable<Integer, ClientInfo> clients) {
        this.connexion = connexion;
        this.clients = clients;
    }

    // Méthode de création des props pour le constructeur
    public static Props props(Connexion connexion, Hashtable<Integer, ClientInfo> clients) {
        return Props.create(BanquierActor.class, () -> new BanquierActor(connexion, clients));
    }

    @Override
    public AbstractActor.Receive createReceive() {
        // Définition des comportements en fonction des types de messages reçus
        return receiveBuilder()
                .match(Depot.class, this::depot)
                .match(Retrait.class, this::retrait)
                .match(GetSolde.class, this::getSolde)
                .match(SoldeResult.class, this::soldeResult)
                .build();
    }

    // Logique de dépôt d'argent sur un compte
    public void depot(final Depot message){
        int idClient = message.idClient;
        int montant = message.montant;
        this.connexion.modifierSolde(montant, idClient);
    }

    // Logique de retrait d'argent d'un compte
    public void retrait(final Retrait message) {
        int idClient = message.idClient;
        int montant = message.montant * -1;

        if (this.connexion.clientExiste(idClient)) {
            // Si le client existe, effectuez le retrait
            this.connexion.modifierSolde(montant, idClient);
        } else {
            System.out.println("Pas de client numéro : " + idClient);
        }
    }

    // Logique pour récupérer le solde d'un compte
    public void getSolde(final GetSolde message) {
        int idClient = message.idClient;
        if (clients.containsKey(idClient)) {
            int solde = connexion.getSoldeClient(idClient);
            self().tell(new SoldeResult(idClient, solde), getSelf());  // Envoyer le résultat du solde à l'acteur actuel
        }
    }

    // Logique pour traiter le résultat du solde
    public void soldeResult(final SoldeResult message) {
        ClientInfo clientInfo = clients.get(message.idClient);
        int solde = message.solde;
        // Faire quelque chose avec le solde, par exemple, l'afficher
        System.out.println("Le solde du client " + clientInfo.getNomClient() + " est de " + solde + "€");
        // Mettre à jour l'interface graphique avec le nouveau solde
        App.setSoldeText("Le solde du client " + clientInfo.getNomClient() + " est " + solde + "€");
    }

    // Classe interne représentant le message de dépôt
    public static class Depot {
        public int idClient;
        public int montant;

        public Depot(int idClient, int montant) {
            this.idClient = idClient;
            this.montant = montant;
        }
    }

    // Classe interne représentant le message de retrait
    public static class Retrait  {
        public int idClient;
        public int montant;

        public Retrait(int idClient, int montant) {
            this.idClient = idClient;
            this.montant = montant;
        }
    }

    // Classe interne représentant le message de récupération du solde
    public static class GetSolde  {
        public int idClient;

        public GetSolde (int idClient) {
            this.idClient = idClient;
        }
    }

    // Classe interne représentant le résultat du solde
    public static class SoldeResult {
        public final int idClient;
        public final int solde;

        public SoldeResult(int idClient, int solde) {
            this.idClient = idClient;
            this.solde = solde;
        }
    }
}
