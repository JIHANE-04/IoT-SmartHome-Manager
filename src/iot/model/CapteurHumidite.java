package iot.model; // Définition du package

import java.util.Random; // Importation outil aléatoire

public class CapteurHumidite extends AppareilConnecte { // Hérite de AppareilConnecte

    public CapteurHumidite(String nom) { // Constructeur
        super(nom); // Appel constructeur parent
    }

    @Override // Redéfinition
    public String getType() {
        return "CapteurHumidite"; // Retourne le type
    }

    @Override // Redéfinition
    public String mesurer() throws AppareilException {
        if (!connecte) // Vérification connexion
            throw new MesureException("Mesure impossible : capteur humidité non connecté"); // Erreur si non connecté
        this.valeur = (30 + new Random().nextInt(61)) + " %"; // Génère valeur entre 30 et 90 (30 + [0..60])
        
        return this.valeur; // Retourne la valeur
    }
}