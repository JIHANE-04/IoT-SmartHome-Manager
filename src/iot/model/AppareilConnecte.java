package iot.model; // Définition du package

import java.io.Serializable; // Importation de l'interface pour permettre la sauvegarde (sérialisation) de l'objet

public abstract class AppareilConnecte implements Serializable { // Classe abstraite (ne peut être instanciée directement) et sérialisable

    private static int compteur = 0; // compteur global statique pour générer des IDs uniques auto-incrémentés
    protected int id; // ID unique de l'instance
    protected String nom; // Nom de l'appareil
    protected boolean connecte; // État de la connexion réseau simulée
    protected boolean manuel = false; // si créé manuellement (drapeau pour distinguer les ajouts utilisateurs)
    protected String valeur = "-"; // valeur actuelle (stockage par défaut)

    public AppareilConnecte(String nom) { // Constructeur principal
        this.id = ++compteur; // Incrémente le compteur global et assigne le nouvel ID à cet objet
        this.nom = nom; // Assigne le nom
        this.connecte = false; // Par défaut, l'appareil n'est pas connecté
    }

    public void setId(int i) { this.id = i; } // Setter pour l'ID (utile lors du chargement de données)
    public static void setCompteur(int c) { compteur = c; } // Setter pour le compteur statique (utile après un chargement pour éviter les doublons d'ID)

    public String afficher() { // Méthode pour obtenir une description complète de l'appareil
        return "[" + getType() + "] " + nom + " (" + (connecte ? "Connecté" : "Déconnecté") + ")"; // Formatage de la chaîne
    }

    public abstract String getType(); // Méthode abstraite : les enfants DOIVENT définir leur type
    public abstract String mesurer() throws AppareilException; // Méthode abstraite : les enfants DOIVENT définir comment ils mesurent/réagissent

    public void connecter() throws AppareilException { // Méthode pour connecter l'appareil
        if (connecte) throw new ConnexionException("Déjà connecté !"); // Erreur si déjà connecté
        this.connecte = true; // Change l'état à connecté
    }

    public void deconnecter() throws AppareilException { // Méthode pour déconnecter l'appareil
        if (!connecte) throw new ConnexionException("Déjà déconnecté !"); // Erreur si déjà déconnecté
        this.connecte = false; // Change l'état à déconnecté
    }

    // --- Getter / Setter ---
    public int getId() { return id; } // Retourne l'ID
    public String getNom() { return nom; } // Retourne le nom
    public void setValeur(String valeur) { this.valeur = valeur; } // Force une valeur manuellement

    public boolean isConnecte() { return connecte; } // Retourne l'état de connexion
    public void setConnecte(boolean c) { this.connecte = c; } // Définit l'état de connexion

    public boolean isManuel() { return manuel; } // Vérifie si c'est un appareil manuel
    public void setManuel(boolean m) { this.manuel = m; } // Définit si c'est un appareil manuel

    @Override // Redéfinition de toString
    public String toString() { return nom; } // Retourne simplement le nom (utile pour l'affichage dans des listes simples)

    public String getValeurStockee() { // Récupère la dernière valeur connue sans déclencher de nouvelle mesure
       if (this.valeur == null) return "-"; // Gestion du cas null
       return this.valeur; // Retourne la valeur stockée
    }
}