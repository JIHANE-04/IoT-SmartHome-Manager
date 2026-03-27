package iot.model; // Définition du package

public class MesureException extends AppareilException { // Hérite de l'exception métier de base
    public MesureException(String m) { // Constructeur
        super(m); // Passe le message au parent
    }
}