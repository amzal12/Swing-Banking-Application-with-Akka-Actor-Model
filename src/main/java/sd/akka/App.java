package sd.akka;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import sd.akka.actor.BanqueActor;
import sd.akka.actor.ClientInfo;
import sd.akka.actor.Connexion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class App {

    private static JTextField soldeTextField;

    public static void main(String[] args) {
        try {
            // Connexion au serveur bancaire
            Connexion c = new Connexion();
            c.Connect();

            // Récupération des informations des clients depuis le serveur
            Hashtable<Integer, ClientInfo> clients = c.getClients();

            // Création du système d'acteurs Akka
            ActorSystem actorSystem = ActorSystem.create();
            ActorRef banqueActor = actorSystem.actorOf(BanqueActor.props(c, clients), "banque");

            // Interface graphique Swing
            SwingUtilities.invokeLater(() -> {
                createAndShowGUI(banqueActor, clients);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createAndShowGUI(ActorRef banqueActor, Hashtable<Integer, ClientInfo> clients) {
        // Création de la fenêtre Swing
        JFrame frame = new JFrame("Gestion de Comptes Bancaires");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Chargement du logo
        ImageIcon icon = new ImageIcon("logo.jpeg");

        // Création du panneau principal
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Création des boutons et champs de texte
        JButton retraitButton = new JButton("Retrait");
        JButton depotButton = new JButton("Dépôt");
        JButton soldeButton = new JButton("Voir le solde");

        JTextField compteTextField = new JTextField();
        JTextField montantTextField = new JTextField();

        soldeTextField = new JTextField();
        soldeTextField.setEditable(false);

        // Ajout des composants au panneau
        panel.add(new JLabel("Numéro de Compte: "));
        panel.add(compteTextField);
        panel.add(new JLabel("Montant: "));
        panel.add(montantTextField);
        panel.add(retraitButton);
        panel.add(depotButton);
        panel.add(soldeButton);
        panel.add(new JLabel("Solde: "));
        panel.add(soldeTextField);

        // Gestion des événements pour les opérations bancaires
        retraitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int compte = Integer.parseInt(compteTextField.getText());
                int montant = Integer.parseInt(montantTextField.getText());
                banqueActor.tell(new BanqueActor.Retrait(compte, montant), ActorRef.noSender());
            }
        });

        depotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int compte = Integer.parseInt(compteTextField.getText());
                int montant = Integer.parseInt(montantTextField.getText());
                banqueActor.tell(new BanqueActor.Depot(compte, montant), ActorRef.noSender());
            }
        });

        soldeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int compte = Integer.parseInt(compteTextField.getText());
                banqueActor.tell(new BanqueActor.GetSolde(compte), ActorRef.noSender());
            }
        });

        /******************************************************************************/
        /********** afficher le solde de 5 différents comptes en même temps ***********/
        /******************************************************************************/
            /*
                for (int compte = 1; compte <= 5; compte++) {
                    banqueActor.tell(new BanqueActor.GetSolde(compte), ActorRef.noSender());
                }
            */
        /******************************************************************************/
        /***** faire un retrait de 10€ pour 5 comptes différents en parrallèle ********/
        /******************************************************************************/
            /*
                for (int compte = 1; compte <= 5; compte++) {
                    banqueActor.tell(new BanqueActor.Retrait(compte, 10), ActorRef.noSender());
                }
            */
        /******************************************************************************/
        /********** faire un dépot de 200€ pour 5 comptes différents en parrallèle ***********/
        /******************************************************************************/
/*
                for (int compte = 1; compte <= 5; compte++) {
                    banqueActor.tell(new BanqueActor.Depot(compte, 200), ActorRef.noSender());
                }
*/


        // Configuration de la fenêtre Swing
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setSize(320, 220);
        frame.setLocation(500,200);
        frame.setIconImage(icon.getImage());
        frame.setVisible(true);
    }

    // Méthode pour mettre à jour le champ de texte du solde
    public static void setSoldeText(String solde) {
        soldeTextField.setText(solde);
    }
}

