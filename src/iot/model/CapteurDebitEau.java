package iot.model; // Définition du package

import java.util.Random; // Importation pour la génération de nombres aléatoires

public class CapteurDebitEau extends AppareilConnecte { // Hérite de AppareilConnecte

    public CapteurDebitEau(String nom) { // Constructeur
        super(nom); // Appel constructeur parent
    }

    @Override // Redéfinition
    public String getType() {
        return "CapteurDebitEau"; // Retourne le type
    }

    @Override // Redéfinition de mesurer avec gestion d'exception
    public String mesurer() throws AppareilException {
        if (!connecte) // Vérifie si l'appareil est connecté
            throw new MesureException("Mesure impossible : capteur débit eau non connecté"); // Lève une erreur sinon
    this.valeur = (1 + new Random().nextInt(20)) + " L/min"; // Génère un nombre entre 1 et 20 et ajoute l'unité
        
        return this.valeur; // Retourne la valeur générée
    }
}