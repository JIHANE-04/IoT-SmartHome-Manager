package iot.model; // Définition du package

public class CameraIP extends AppareilConnecte { // Hérite de AppareilConnecte

    private boolean active = false; // état caméra (false = désactivée par défaut)

    public CameraIP(String nom) { // Constructeur
        super(nom); // Appel du constructeur parent
    }

    @Override // Redéfinition
    public String getType() { return "CameraIP"; } // Retourne le type

    @Override // Redéfinition
    public String mesurer() { // Retourne l'état de la caméra
        return active ? "ACTIVÉE" : "DÉSACTIVÉE"; // Texte selon l'état booléen
    }

    public void activer() { active = true; } // Active la caméra
    public void desactiver() { active = false; } // Désactive la caméra

    public boolean isActive() { return active; } // Getter de l'état
    public void setActive(boolean a) { // Setter de l'état
        this.active = a; // Affectation
        this.valeur = a ? "ACTIVÉE" : "DÉSACTIVÉE"; // Mise à jour de la valeur textuelle
    }
}