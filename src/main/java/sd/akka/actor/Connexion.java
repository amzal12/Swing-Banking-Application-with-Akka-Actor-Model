package sd.akka.actor;

import java.sql.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Connexion {
    private static final String URL = "jdbc:mysql://localhost:3306/mydb?characterEncoding=utf-8";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    private Hashtable<Integer, ClientInfo> clients = new Hashtable<>();

    LocalDateTime myDateobj = LocalDateTime.now();
    DateTimeFormatter myformaobject = DateTimeFormatter.ofPattern("dd-MM-YYYY HH:mm:ss");
    Connection con;

    // Méthode pour vérifier si un client existe dans la Hashtable clients
    public boolean clientExiste(int idClient) {
        return clients.containsKey(idClient);
    }

    // Méthode pour établir la connexion à la base de données
    public void Connect() {
        try {
            // Étape 1: charger la classe de driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Étape 2: Établir la connexion
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.print("Connexion établie.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour récupérer les clients depuis la base de données
    public Hashtable<Integer, ClientInfo> getClients() {
        try {
            Statement statement = con.createStatement();
            ResultSet resultat = statement.executeQuery("SELECT C.idC, C.nom, C.idBq, B.idBq FROM Client C INNER JOIN Banquier B ON C.idBq = B.idBq");

            int idClient;
            int idBanquier;
            String nomClient;

            while (resultat.next()) {
                idClient = resultat.getInt("idC");
                nomClient = resultat.getString("nom");
                idBanquier = resultat.getInt("idBq");

                ClientInfo clientInfo = new ClientInfo(idClient, nomClient, idBanquier);
                clients.put(idClient, clientInfo);
            }

            // Ajout des clients à la Hashtable clients de la classe Connexion
            this.clients.putAll(clients);

            resultat.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    // Méthode pour récupérer la liste des ID des banquiers depuis la base de données
    public ArrayList<Integer> getBanquiers() {
        ArrayList<Integer> banquiers = new ArrayList<>();
        try {
            Statement statement = con.createStatement();
            ResultSet resultat = statement.executeQuery("SELECT * FROM Banquier");
            int idBanquier;

            // Pour chaque ligne de la table Banquier
            while (resultat.next()) {
                idBanquier = resultat.getInt("idBq");
                banquiers.add(idBanquier);
            }
            resultat.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return banquiers;
    }

    // Méthode pour mettre à jour le solde (dépôt ou retrait) d'un client
    public void modifierSolde(int montant, int idClient) {
        try {
            int solde = getSoldeClient(idClient);
            String nomClient = getNomClient(idClient);  // Récupérer le nom du client

            if (montant < 0) {
                if (solde - montant <= 100) {
                    System.out.print("Retrait impossible");
                } else {
                    PreparedStatement preparedStatement = con.prepareStatement(
                            "UPDATE Client SET solde = (solde + ?) WHERE idC = ?");

                    preparedStatement.setInt(1, montant);
                    preparedStatement.setInt(2, idClient);

                    preparedStatement.executeUpdate();
                    System.out.print(nomClient + " a fait un retrait de " + (-1 * montant) +
                            "€ le " + myDateobj.format(myformaobject) + "\n");
                    preparedStatement.close();
                }
            } else {
                PreparedStatement preparedStatement = con.prepareStatement(
                        "UPDATE Client SET solde = (solde + ?) WHERE idC = ?");
                preparedStatement.setInt(1, montant);
                preparedStatement.setInt(2, idClient);

                preparedStatement.executeUpdate();
                System.out.print(nomClient + " a fait un dépôt de " + montant +
                        "€ le " + myDateobj.format(myformaobject) + "\n");

                preparedStatement.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour récupérer le nom du client à partir de son ID
    public String getNomClient(int idClient) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement(
                    "SELECT nom FROM Client WHERE idC = ?");
            preparedStatement.setInt(1, idClient);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("nom");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Nom inconnu";
    }

    // Méthode pour récupérer le solde d'un client à partir de son ID
    public int getSoldeClient(int idClient) {
        int solde = 0;
        try {
            // Création de la requête préparée
            PreparedStatement preparedStatement = con.prepareStatement("SELECT solde FROM Client where idC=?");

            // Donne les attributs à la requête préparée
            preparedStatement.setInt(1, idClient);

            // Donne les attributs à la requête préparée
            ResultSet resultat = preparedStatement.executeQuery();

            while (resultat.next()) {
                solde = resultat.getInt("solde");
            }

            // Fermeture du ResultSet
            resultat.close();

            // Fermeture du PreparedStatement
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return solde;
    }
}
