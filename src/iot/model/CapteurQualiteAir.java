package iot.model; // Définition du package

import java.util.Random; // Importation outil aléatoire

public class CapteurQualiteAir extends AppareilConnecte { // Hérite de AppareilConnecte

    public CapteurQualiteAir(String nom) { // Constructeur
        super(nom); // Appel constructeur parent
    }
    
    @Override // Redéfinition
    public String getType() {
        return "CapteurQualiteAir"; // Retourne le type
    }

    @Override // Redéfinition
    public String mesurer() throws AppareilException {
        if (!connecte) // Vérification connexion
            throw new MesureException("Mesure impossible : capteur qualité air non connecté"); // Erreur si non connecté
    this.valeur = new Random().nextInt(101) + " AQI"; // Génère valeur entre 0 et 100
        
        return this.valeur; // Retourne la valeur
    }
}