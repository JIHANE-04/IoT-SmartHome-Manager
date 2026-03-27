package iot.model; // Définition du package

public class ActionneurVolet extends AppareilConnecte { // Classe héritant de AppareilConnecte

    private boolean ouvert = false; // Variable d'état du volet (false = fermé par défaut)

    public ActionneurVolet(String nom) { // Constructeur
        super(nom); // Appel du constructeur parent
    }

    @Override // Redéfinition
    public String getType() { return "ActionneurVolet"; } // Retourne le type spécifique

    @Override // Redéfinition
    public String mesurer() { // Retourne l'état visuel du volet
        valeur = ouvert ? "↑" : "↓"; // valeur pour Dashboard (Flèche haut si ouvert, bas si fermé)
        return valeur; // Retourne la flèche
    }

    public void monter() { ouvert = true; } // Action pour ouvrir le volet
    public void descendre() { ouvert = false; } // Action pour fermer le volet

    public boolean isOuvert() { return ouvert; } // Getter de l'état
    public void setOuvert(boolean o) { // Setter de l'état
        this.ouvert = o; // Affectation du booléen
        this.valeur = o ? "↑" : "↓"; // Mise à jour immédiate de la représentation textuelle
    }
}