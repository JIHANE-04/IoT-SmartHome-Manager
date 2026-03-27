package iot.model; // Définition du package

public class ImportExportException extends AppareilException { // Hérite de l'exception métier de base
    public ImportExportException(String m) { // Constructeur
        super(m); // Passe le message au parent
    }
}