package iot.model; // Définition du package

// Exception spécifique Appareil
public class AppareilException extends Exception { // Hérite de la classe standard Exception
    public AppareilException(String m) { // Constructeur prenant un message d'erreur
        super(m); // Transmet le message à la classe parente Exception
    }
}