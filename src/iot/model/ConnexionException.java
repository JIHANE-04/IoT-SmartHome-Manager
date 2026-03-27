package iot.model; // Définition du package

public class ConnexionException extends AppareilException { // Hérite de l'exception métier de base
    public ConnexionException(String m) { // Constructeur
        super(m); // Passe le message au parent
    }
}