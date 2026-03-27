package iot.model; // Définition du package

public class ActionneurVentilation extends AppareilConnecte { // Classe héritant de AppareilConnecte

    private int niveau = 0; // Variable pour le niveau actuel de ventilation (défaut 0)
    private final int MAX_NIVEAU = 3; // Constante définissant le niveau maximum autorisé

    public ActionneurVentilation(String nom) { // Constructeur
        super(nom); // Initialisation via le constructeur parent
        this.valeur = "Niveau " + niveau; // Initialisation de la chaîne 'valeur' avec le niveau de départ
    }

    @Override // Redéfinition
    public String getType() { return "ActionneurVentilation"; } // Retourne le type de l'appareil

    @Override // Redéfinition de la méthode de mesure
    public String mesurer() { // Met à jour et retourne la représentation textuelle du niveau
        valeur = "Niveau " + niveau; // Construit la chaîne (ex: "Niveau 2")
        return valeur; // Retourne la chaîne
    }

    public void augmenter() { if(niveau < MAX_NIVEAU) { niveau++; valeur = "Niveau " + niveau; } } // Augmente le niveau si inférieur au max et met à jour la valeur texte
    public void diminuer() { if(niveau > 0) { niveau--; valeur = "Niveau " + niveau; } } // Diminue le niveau si supérieur à 0 et met à jour la valeur texte

    public int getNiveau() { return niveau; } // Getter pour obtenir l'entier du niveau

    public void setNiveau(int n) { // Setter pour définir un niveau spécifique
        if(n < 0) n = 0; // Sécurité : si n est négatif, on force à 0
        if(n > MAX_NIVEAU) n = MAX_NIVEAU; // Sécurité : si n dépasse le max, on force au max
        this.niveau = n; // Affectation du niveau sécurisé
        this.valeur = "Niveau " + niveau; // Mise à jour de la valeur textuelle
    }

    public void niveauSuivant() { augmenter(); } // Alias pour la méthode augmenter
    public void niveauPrecedent() { diminuer(); } // Alias pour la méthode diminuer
}