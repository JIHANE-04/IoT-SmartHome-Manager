package iot.view;

import iot.controller.MaisonIntelligente; // Import du contrôleur
import iot.model.*; // Import du modèle pour manipuler les objets Appareil

import javax.swing.*; // Import des composants graphiques Swing
import javax.swing.border.*; // Import pour les bordures
import javax.swing.table.*; // Import pour la gestion des tableaux (JTable)
import java.awt.*; // Import pour le layout et les couleurs (AWT)
import java.util.ArrayList; // Listes
import java.util.Map; // Maps pour la config
import java.io.File; // Gestion fichiers pour export
import java.io.FileWriter; // Écriture fichier
import java.io.IOException; // Gestion erreurs E/S

/**
 * Dashboard IoT - Interface principale
 * Affiche Dashboard, Appareils, Actions, Historique et Logs
 * Gère toutes les interactions avec le controller MaisonIntelligente
 */
public class DashboardIoT extends JFrame { // Hérite de JFrame = c'est une fenêtre

    private MaisonIntelligente controller; // Instance du contrôleur (le cerveau)

    // Composants graphiques globaux (accessibles dans toute la classe)
    private JTable table; // Tableau des appareils
    private DefaultTableModel modelTable; // Modèle de données du tableau
    private JTextArea areaLogs; // Zone de texte pour les logs
    private JList<String> listHistorique; // Liste pour l'historique
    private DefaultListModel<String> modelHistorique; // Modèle de la liste historique

    private JTextField txtNomAjout; // Champ texte pour le nom du nouvel appareil
    private JComboBox<String> comboTypeAjout; // Menu déroulant pour choisir le type à ajouter

    private JComboBox<String> comboActionType; // Filtre par type pour les actions
    private JComboBox<AppareilConnecte> comboActionAppareil; // Sélection de l'appareil spécifique
    private JTextArea zoneResultatAction; // Zone pour afficher le retour de l'action

    // Label global pour les statistiques (Total / Actifs)
    private JLabel lblStats; 

    // Constantes de couleurs pour l'uniformité du design
    private final Color CLR_FOND = new Color(255, 240, 245); // Rose très pâle
    private final Color CLR_HEADER = new Color(255, 200, 220); // Rose plus soutenu
    private final Color CLR_BTN = new Color(220, 190, 255); // Violet clair
    private final Color CLR_MANUEL = new Color(220, 190, 255); // Couleur ligne ajout manuel
    private final Color CLR_JSON = new Color(240, 250, 255); // Couleur ligne import JSON

    public DashboardIoT() {
        controller = new MaisonIntelligente(); // Initialisation du controller (charge les données)

        setTitle("IoT Smart Home - Manager"); // Titre de la fenêtre
        setSize(1100, 750); // Dimensions
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Quitter l'appli à la fermeture
        setLocationRelativeTo(null); // Centrer à l'écran
        getContentPane().setBackground(CLR_FOND); // Couleur de fond

        // Création des onglets
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Police des onglets
        tabs.setBackground(CLR_HEADER); // Couleur de fond des onglets

        // Ajout des panneaux dans les onglets via les méthodes d'initialisation
        tabs.addTab("Accueil", initDashboard());
        tabs.addTab("Appareils", initAppareils());
        tabs.addTab("Actions", initActions());
        tabs.addTab("Historique", initHistory());
        tabs.addTab("Logs Système", initLogs());

        add(tabs); // Ajout des onglets à la fenêtre

        // Rafraîchir les données affichées à l'ouverture pour voir les données JSON chargées
        rafraichir();
    }

    // ========================= ACCUEIL =========================
    private JPanel initDashboard() {
        JPanel main = new JPanel(new BorderLayout()); // Layout principal
        main.setBackground(CLR_FOND);
        main.setBorder(new EmptyBorder(30, 30, 30, 30)); // Marges

        JLabel lblTitre = new JLabel("Configuration Globale de la Maison", JLabel.CENTER);
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitre.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Grille pour afficher les infos de config (2 colonnes)
        JPanel pInfos = new JPanel(new GridLayout(0, 2, 10, 10));
        pInfos.setBackground(Color.WHITE);
        pInfos.setBorder(new CompoundBorder( // Bordure double (ligne + vide)
                new LineBorder(CLR_HEADER, 3),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Récupération de la config depuis le controller (lecture fichier Excel)
        Map<String, String> configs = controller.getAllConfig(); 
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            JLabel k = new JLabel(entry.getKey() + " :"); // Clé (ex: "Propriétaire")
            k.setFont(new Font("Segoe UI", Font.BOLD, 16));
            k.setForeground(new Color(100, 50, 100));

            JLabel v = new JLabel(entry.getValue()); // Valeur (ex: "M. Dupont")
            v.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            pInfos.add(k);
            pInfos.add(v);
        }

        JPanel pStats = new JPanel(new FlowLayout());
        pStats.setBackground(CLR_FOND);
        
        // Initialisation du label des statistiques
        lblStats = new JLabel("Chargement...");
        lblStats.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        pStats.add(lblStats);

        main.add(lblTitre, BorderLayout.NORTH);
        main.add(pInfos, BorderLayout.CENTER);
        main.add(pStats, BorderLayout.SOUTH);

        return main;
    }

    // ========================= APPAREILS =========================
    private JPanel initAppareils() {
        JPanel main = new JPanel(new BorderLayout(20, 20)); // Ecart horizontal/vertical de 20
        main.setBackground(CLR_FOND);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Formulaire d'ajout (Haut)
        JPanel form = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(new LineBorder(CLR_HEADER, 2));

        // Liste des types disponibles pour la création
        String[] types = {
                "CapteurTemperature", "CapteurHumidite", "CapteurQualiteAir",
                "CapteurDebitEau", "ActionneurLumiere", "ActionneurVolet",
                "ActionneurVentilation", "CameraIP"
        };

        comboTypeAjout = new JComboBox<>(types); // Menu déroulant
        txtNomAjout = new JTextField(12); // Champ texte
        JButton btnAdd = creerBouton("Ajouter", CLR_BTN);
        btnAdd.addActionListener(e -> actionAjouter()); // Action bouton

        form.add(new JLabel("Type :"));
        form.add(comboTypeAjout);
        form.add(new JLabel("Nom :"));
        form.add(txtNomAjout);
        form.add(btnAdd);

        // Tableau Appareils (Centre)
        String[] cols = {"ID", "Type", "Nom", "État", "Valeur", "Source"};
        modelTable = new DefaultTableModel(cols, 0) { // Modèle de données
            public boolean isCellEditable(int r, int c) { return false; } // Rend le tableau non éditable directement
        };
        table = new JTable(modelTable);
        table.setRowHeight(30); // Hauteur ligne
        table.setDefaultRenderer(Object.class, new RowColorRenderer()); // Applique le rendu couleur personnalisé

        // Barre de boutons (Bas)
        JPanel pBtns = new JPanel(new FlowLayout());
        pBtns.setBackground(CLR_FOND);

        JButton btnCon = creerBouton("Connecter", new Color(180, 255, 180));
        JButton btnDec = creerBouton("Déconnecter", new Color(255, 180, 180));
        JButton btnDel = creerBouton("Supprimer", Color.LIGHT_GRAY);
        JButton btnExp = creerBouton("Exporter TXT", Color.WHITE);

        // Liaison des boutons aux méthodes d'action
        btnCon.addActionListener(e -> actionChangerConnexion(true));
        btnDec.addActionListener(e -> actionChangerConnexion(false));
        btnDel.addActionListener(e -> actionSupprimer());
        btnExp.addActionListener(e -> actionExporter());

        pBtns.add(btnCon);
        pBtns.add(btnDec);
        pBtns.add(btnDel);
        pBtns.add(btnExp);

        main.add(form, BorderLayout.NORTH);
        main.add(new JScrollPane(table), BorderLayout.CENTER); // Tableau avec barre de défilement
        main.add(pBtns, BorderLayout.SOUTH);

        return main;
    }

    // ========================= ACTIONS =========================
    private JPanel initActions() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(CLR_FOND);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Zone de sélection : Type puis Appareil spécifique ---
        JPanel pSel = new JPanel(new GridLayout(2, 2, 5, 5));
        pSel.setOpaque(false);

        String[] types = {
                "-- Filtrer --", "CapteurTemperature", "CapteurHumidite",
                "CapteurQualiteAir", "CapteurDebitEau",
                "ActionneurLumiere", "ActionneurVolet",
                "ActionneurVentilation", "CameraIP"
        };

        comboActionType = new JComboBox<>(types);
        comboActionAppareil = new JComboBox<>();
        comboActionType.addActionListener(e -> updateComboNoms()); // Si on change le type, on met à jour la liste des noms

        pSel.add(new JLabel("Filtre Type :"));
        pSel.add(comboActionType);
        pSel.add(new JLabel("Appareil :"));
        pSel.add(comboActionAppareil);

        // --- Zone des boutons de commande ---
        JPanel pCmd = new JPanel(new FlowLayout());
        pCmd.setOpaque(false);

        JButton btnMesurer = creerBouton("MESURER", CLR_BTN);
        btnMesurer.addActionListener(e -> executerAction("mesure"));

        JButton btnOnOff = creerBouton("ALLUMER / ÉTEINDRE", new Color(200, 255, 200));
        btnOnOff.addActionListener(e -> executerAction("onoff"));

        JButton btnMonterDescendre = creerBouton("MONTER / DESCENDRE", new Color(255, 200, 200));
        btnMonterDescendre.addActionListener(e -> executerAction("monterdescendre"));

        JButton btnNiveau = creerBouton("NIVEAU 1/2/3", new Color(200, 200, 255));
        btnNiveau.addActionListener(e -> executerAction("niveau"));

        main.add(pSel, BorderLayout.NORTH);

        pCmd.add(btnMesurer);
        pCmd.add(btnOnOff);
        pCmd.add(btnMonterDescendre);
        pCmd.add(btnNiveau);

        main.add(pCmd, BorderLayout.CENTER);

        // Zone d'affichage du résultat textuel
        zoneResultatAction = new JTextArea(5, 30);
        zoneResultatAction.setBorder(new TitledBorder("Résultat Action"));
        main.add(new JScrollPane(zoneResultatAction), BorderLayout.SOUTH);

        return main;
    }

    // ========================= HISTORIQUE =========================
    private JPanel initHistory() {
        JPanel p = new JPanel(new BorderLayout());
        modelHistorique = new DefaultListModel<>(); // Modèle pour la JList
        listHistorique = new JList<>(modelHistorique);
        listHistorique.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Police monospaced pour alignement

        JButton btnLoad = creerBouton("Afficher Historique Tout Appareil", CLR_BTN);
        btnLoad.addActionListener(e -> {
            modelHistorique.clear(); // Vider la liste
            for (AppareilConnecte a : controller.getAppareils()) { // Parcourir tous les appareils
                ArrayList<String> hist = controller.getHistorique(a.getNom()); // Récupérer l'historique
                for (String h : hist) modelHistorique.addElement("[" + a.getNom() + "] " + h); // Ajouter ligne
            }
            if (modelHistorique.isEmpty()) modelHistorique.addElement("Aucune mesure enregistrée.");
        });

        p.add(btnLoad, BorderLayout.NORTH);
        p.add(new JScrollPane(listHistorique), BorderLayout.CENTER);

        return p;
    }

    // ========================= LOGS =========================
    private JPanel initLogs() {
        JPanel p = new JPanel(new BorderLayout());
        areaLogs = new JTextArea();
        areaLogs.setBackground(new Color(30, 30, 30)); // Fond sombre
        areaLogs.setForeground(Color.GREEN); // Texte style "Matrix"
        areaLogs.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaLogs.setEditable(false); // Lecture seule

        p.add(new JLabel("Journal Système (Temps Réel)"), BorderLayout.NORTH);
        p.add(new JScrollPane(areaLogs), BorderLayout.CENTER);

        return p;
    }

    // ========================= LOGIQUE MÉTIER / INTERACTION =========================
    
    // Action : Ajouter un appareil
    private void actionAjouter() {
        try {
            String n = txtNomAjout.getText().trim();
            if (n.isEmpty()) return; // Ignorer si vide
            // Appel au contrôleur pour créer l'objet
            AppareilConnecte app = controller.ajouterAppareil((String) comboTypeAjout.getSelectedItem(), n);
            app.setManuel(true);  // Marque comme Manuel (pour la couleur dans le tableau)
            rafraichir(); // Mise à jour UI
            txtNomAjout.setText(""); // Vider champ
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage()); // Affiche erreur
        }
    }

    // Action : Supprimer un appareil sélectionné
    private void actionSupprimer() {
        int r = table.getSelectedRow(); // Récupère l'index de ligne sélectionnée
        if (r >= 0) {
            String nom = (String) modelTable.getValueAt(r, 2); // Récupère le nom dans la colonne 2
            try {
                AppareilConnecte a = trouverAppareil(nom); // Trouve l'objet
                if(a != null) controller.supprimerAppareil(a); // Demande suppression au controller
                rafraichir();
            } catch (ImportExportException e) {
                JOptionPane.showMessageDialog(this, "Erreur suppression : " + e.getMessage());
            }
        }
    }

    // Action : Connecter ou Déconnecter
    private void actionChangerConnexion(boolean etat) {
        int r = table.getSelectedRow();
        if (r >= 0) {
            try {
                // Change l'état via le controller
                controller.changerEtatConnexion(trouverAppareil((String) modelTable.getValueAt(r, 2)), etat);
                rafraichir();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    // Action complexe : Exécute une commande spécifique sur un appareil
    private void executerAction(String typeAction) {
        AppareilConnecte a = (AppareilConnecte) comboActionAppareil.getSelectedItem(); // Récupère l'objet sélectionné
        if (a == null) return;

        String res = ""; // Message résultat
        String valeurTableau = ""; // Valeur à mettre dans le tableau

        try {
            switch (typeAction) {

                // --- CAS : MESURE (Pour capteurs uniquement) ---
                case "mesure":
                    if (a instanceof CapteurTemperature || a instanceof CapteurHumidite ||
                        a instanceof CapteurQualiteAir || a instanceof CapteurDebitEau) {
                        res = controller.mesurer(a); // Appelle la méthode polymorphique mesurer()
                        valeurTableau = res;
                    } else {
                        res = "Action impossible : ce n'est pas un capteur !";
                        valeurTableau = controller.getValeur(a);
                    }
                    break;

                // --- CAS : ALLUMER / ÉTEINDRE (Lumière ou Caméra) ---
                case "onoff":
                    if (a instanceof ActionneurLumiere || a instanceof CameraIP) {
                        if (a instanceof ActionneurLumiere) {
                            ActionneurLumiere l = (ActionneurLumiere) a; // Cast
                            if (l.isAllume()) l.eteindre(); else l.allumer(); // Bascule
                        } else if (a instanceof CameraIP c) {
                            c.setActive(!c.isActive()); // Bascule
                        }
                        res = controller.getValeur(a);
                        valeurTableau = res;
                    } else {
                        res = "Action impossible sur cet appareil";
                        valeurTableau = controller.getValeur(a);
                    }
                    break;

                // --- CAS : MONTER / DESCENDRE (Volets) ---
                case "monterdescendre":
                    if (a instanceof ActionneurVolet v) { // Pattern matching (Java 16+) ou Cast classique
                        if (v.isOuvert()) v.descendre(); else v.monter();
                        res = controller.getValeur(a);
                        valeurTableau = res;
                    } else {
                        res = "Action impossible : ce n'est pas un volet !";
                        valeurTableau = controller.getValeur(a);
                    }
                    break;

                // --- CAS : NIVEAU (Ventilation) ---
                case "niveau":
                    if (a instanceof ActionneurVentilation vent) {
                        int niveau = (vent.getNiveau() % 3) + 1; // cycle mathématique 1 -> 2 -> 3 -> 1
                        vent.setNiveau(niveau);
                        res = "Ventilation : Niveau " + niveau;
                        valeurTableau = controller.getValeur(a);
                    } else {
                        res = "Action impossible : ce n'est pas une ventilation !";
                        valeurTableau = controller.getValeur(a);
                    }
                    break;

                default:
                    res = "Action inconnue";
                    valeurTableau = controller.getValeur(a);
            }

            // --- Enregistrement Historique ---
            controller.ajouterHistorique(a.getNom(), res);
            controller.sauvegarderAppareils(); // Sauvegarde l'état JSON

            // --- Mise à jour Interface ---
            zoneResultatAction.setText(res);

            // Mise à jour ciblée dans le tableau (évite de tout redessiner)
            for (int i = 0; i < modelTable.getRowCount(); i++) {
                if (modelTable.getValueAt(i, 2).equals(a.getNom())) {
                    modelTable.setValueAt(valeurTableau, i, 4); // colonne Valeur
                    modelTable.setValueAt(a.isConnecte() ? "ON" : "OFF", i, 3); // colonne État
                    break;
                }
            }

            rafraichir(); // stats et logs

        } catch (Exception e) {
            zoneResultatAction.setText("Erreur : " + e.getMessage());
        }
    }

    // ------------------ Rafraichir tableau et logs ------------------
    private void rafraichir() {
        modelTable.setRowCount(0); // Efface le tableau
        for (AppareilConnecte a : controller.getAppareils()) { // Récupère la liste fraiche
            String src = a.isManuel() ? "MANUEL" : "JSON";
            String val = "-";

            try {
                // Utilise getValeur() (lecture seule) pour ne pas régénérer d'aléatoire sur les capteurs
                val = controller.getValeur(a); 
            } catch (Exception e) {
                val = "Erreur";
            }

            // Ajoute la ligne au modèle du tableau
            modelTable.addRow(new Object[]{
                    a.getId(), a.getType(), a.getNom(),
                    a.isConnecte() ? "ON" : "OFF",
                    val, src
            });
        }

        // Mise à jour des Logs dans la console visuelle
        areaLogs.setText("");
        for (String s : controller.getLogs()) areaLogs.append(s + "\n");
        
        updateComboNoms(); // Met à jour les listes déroulantes

        // Calcul des Stats
        int total = controller.getAppareils().size();
        int capteursActifs = 0, actionneursActifs = 0;
        
        // Parcours pour compter capteurs vs actionneurs
        for (AppareilConnecte a : controller.getAppareilsActifs()) {
            if (a instanceof CapteurTemperature || a instanceof CapteurHumidite ||
                a instanceof CapteurQualiteAir || a instanceof CapteurDebitEau)
                capteursActifs++;
            else
                actionneursActifs++;
        }

        if (lblStats != null) {
            lblStats.setText("Système ACTIF | Total : " + total +
                    " | Capteurs actifs : " + capteursActifs +
                    " | Actionneurs actifs : " + actionneursActifs);
        }
    }

    // Met à jour la 2ème combobox selon le type choisi dans la 1ère
    private void updateComboNoms() {
        comboActionAppareil.removeAllItems();
        String t = (String) comboActionType.getSelectedItem();
        if (t == null || t.startsWith("--")) return;

        for (AppareilConnecte a : controller.getAppareils())
            if (a.getClass().getSimpleName().equals(t)) // vérifie le nom de classe
                comboActionAppareil.addItem(a);
    }

    // Utilitaire pour trouver un objet Appareil par son nom
    private AppareilConnecte trouverAppareil(String nom) {
        for (AppareilConnecte a : controller.getAppareils())
            if (a.getNom().equals(nom)) return a;
        return null;
    }

    // Export des données du tableau vers un fichier texte
    private void actionExporter() {
        try {
            File dir = new File("assets");
            if (!dir.exists()) dir.mkdirs(); // crée le dossier si absent

            File file = new File(dir, "export.txt");
            try (FileWriter fw = new FileWriter(file)) {
                // Parcours des lignes et colonnes du tableau
                for (int i = 0; i < modelTable.getRowCount(); i++) {
                    for (int c = 0; c < modelTable.getColumnCount(); c++) {
                        fw.write(modelTable.getValueAt(i, c).toString()); // Ecriture valeur
                        if (c < modelTable.getColumnCount() - 1) fw.write(" | "); // Séparateur
                    }
                    fw.write("\n"); // Saut de ligne
                }
            }
            JOptionPane.showMessageDialog(this, "Export réussi dans : " + file.getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur export : " + e.getMessage());
        }
    }

    // Helper pour créer des boutons stylisés uniformément
    private JButton creerBouton(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setFocusPainted(false); // Enlève le cadre de focus au clic
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15) // Padding interne
        ));
        return b;
    }

    // CLASSE INTERNE : Renderer pour colorer les lignes du tableau selon la source
    class RowColorRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean s, boolean f, int r, int c) {
            Component cp = super.getTableCellRendererComponent(t, v, s, f, r, c); // Récupère le composant par défaut
            if (!s) { // Si la ligne n'est PAS sélectionnée
                String src = t.getModel().getValueAt(r, 5).toString(); // Regarde la colonne "Source"
                cp.setBackground("MANUEL".equals(src) ? CLR_MANUEL : CLR_JSON); // Applique la couleur
            }
            setHorizontalAlignment(CENTER); // Centre le texte
            return cp;
        }
    }
}