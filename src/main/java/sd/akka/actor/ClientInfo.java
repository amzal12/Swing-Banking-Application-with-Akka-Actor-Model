package sd.akka.actor;

public class ClientInfo {
    private int idClient;
    private String nomClient;
    private int idBanquier;

    // Constructeur pour initialiser les données du client
    public ClientInfo(int idClient, String nomClient, int idBanquier) {
        this.idClient = idClient;
        this.nomClient = nomClient;
        this.idBanquier = idBanquier;
    }

    // Méthode pour obtenir l'ID du client
    public int getIdClient() {
        return idClient;
    }

    // Méthode pour obtenir le nom du client
    public String getNomClient() {
        return nomClient;
    }

    // Méthode pour obtenir l'ID du banquier associé au client
    public int getIdBanquier() {
        return idBanquier;
    }
}
