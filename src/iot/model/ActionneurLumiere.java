package iot.model; // Définition du package auquel appartient la classe

public class ActionneurLumiere extends AppareilConnecte { // Déclaration de la classe qui hérite de AppareilConnecte

    private boolean allume = false; // Variable d'instance pour stocker l'état de la lumière (initialisée à false/éteint)

    public ActionneurLumiere(String nom) { // Constructeur prenant le nom de l'appareil en paramètre
        super(nom); // Appel du constructeur de la classe parente (AppareilConnecte) pour initialiser l'ID et le nom
    }

    @Override // Annotation indiquant que nous redéfinissons une méthode de la classe parente
    public String getType() { // Méthode pour obtenir le type spécifique de l'appareil
        return "ActionneurLumiere"; // Retourne la chaîne de caractères identifiant ce type d'appareil
    }

    @Override // Annotation pour la redéfinition de la méthode abstraite 'mesurer'
    public String mesurer() { // Méthode simulant une mesure (ici, l'état de la lumière)
        // retourne l’état actuel comme valeur
        valeur = isAllume() ? "ALLUMÉE" : "ÉTEINTE"; // Mise à jour de la variable 'valeur' selon l'état booléen (Opérateur ternaire)
        return valeur; // Retourne cette valeur textuelle
    }

    // méthodes utilitaires
    public void allumer() { allume = true; } // Méthode pour passer l'état à vrai (allumer)
    public void eteindre() { allume = false; } // Méthode pour passer l'état à faux (éteindre)

    // getter / setter
    public boolean isAllume() { return allume; } // Accesseur pour lire l'état actuel
    public void setAllume(boolean a) { // Mutateur pour modifier l'état actuel
        this.allume = a; // Affecte la nouvelle valeur au booléen
        this.valeur = a ? "ALLUMÉE" : "ÉTEINTE"; // met à jour valeur stockée en conséquence pour l'affichage
    }
}