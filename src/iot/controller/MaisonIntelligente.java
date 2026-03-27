//C'est le Contrôleur Principal de l'application. Il fait le lien entre l'interface utilisateur (View) et les données (Model).
package iot.controller; // Définition du package

import iot.model.*; // Importe tout le modèle
import java.util.*; // Importe les collections
import java.text.SimpleDateFormat; // Importe le formateur de date

/**
 * CONTROLLER PRINCIPAL
 * - Gère les appareils IoT
 * - Applique la logique métier
 * - Capture les exceptions du Model
 * - Fournit méthodes pour la vue
 */
public class MaisonIntelligente { // Classe principale du contrôleur

    // Stockage des appareils (POO + polymorphisme)
    private ArrayList<AppareilConnecte> appareils; // Liste hétérogène stockant tous les types d'appareils

    // Historique des mesures (clé = nom appareil)
    private Map<String, ArrayList<String>> historiques; // Map associant le nom d'un appareil à une liste de chaînes (son historique)

    // Journal des logs système
    private ArrayList<String> logs; // Liste simple de chaînes pour les événements globaux (ex: "Appareil ajouté")

    // Service de gestion des fichiers
    private GestionDonnees donnees; // Instance de la classe gérant JSON/Excel

    public MaisonIntelligente() { // Constructeur
        donnees = new GestionDonnees(); // instance pour JSON/XLSX
        historiques = new HashMap<>(); // Initialisation map vide
        logs = new ArrayList<>(); // Initialisation liste vide
        appareils = new ArrayList<>(); // Initialisation liste vide

        try {
            // Charger appareils depuis JSON au démarrage
            appareils.addAll(donnees.chargerJson()); // Remplit la liste avec les données du fichier
            logs.add("Appareils chargés depuis JSON"); // Log de succès
        } catch (ImportExportException e) { // En cas d'erreur de fichier
            logs.add("Erreur chargement JSON : " + e.getMessage()); // Log de l'erreur
        }
    }

    // ====================== GESTION DES APPAREILS ======================
    public AppareilConnecte ajouterAppareil(String type, String nom) throws AppareilException { // Méthode pour ajouter un nouvel appareil

        // Vérification doublon
        for (AppareilConnecte a : appareils) // Parcourt la liste existante
            if (a.getNom().equalsIgnoreCase(nom)) // Compare les noms (insensible à la casse)
                throw new AppareilException("Appareil déjà existant"); // Interdit deux appareils avec le même nom

        AppareilConnecte a = GestionDonnees.factory(type, nom); // factory POO : Délègue la création à la classe utilitaire
        if (a == null) // Si le type est invalide
            throw new AppareilException("Type d’appareil inconnu"); // Erreur

        appareils.add(a); // ajout de l'objet créé dans la liste principale
        logs.add("Appareil ajouté : " + nom + " (" + type + ")"); // Ajout log
        sauvegarder(); // sauvegarde JSON immédiate

        return a; // Retourne l'objet créé (utile pour l'UI)
    }

    public void supprimerAppareil(AppareilConnecte a) throws ImportExportException { // Méthode pour supprimer
        appareils.remove(a); // suppression de la liste
        historiques.remove(a.getNom()); // suppression historique associé
        logs.add("Appareil supprimé : " + a.getNom()); // Log
        sauvegarder(); // sauvegarde la nouvelle liste
    }

    // ====================== CONNEXION / DÉCONNEXION ======================
    public void changerEtatConnexion(AppareilConnecte a, boolean etat) throws AppareilException { // Méthode centralisée pour connecter/déconnecter
        if (etat) a.connecter(); // Appelle connecter() du modèle
        else a.deconnecter(); // Appelle deconnecter() du modèle

        logs.add("Connexion modifiée : " + a.getNom() + " -> " + (etat ? "ON" : "OFF")); // Log
        sauvegarder(); // Sauvegarde l'état modifié
    }

    // ====================== MESURES (POLYMORPHISME) ======================
    public String mesurer(AppareilConnecte a) throws AppareilException { // Méthode pour déclencher une mesure

        // Vérifie si c'est un capteur (seuls les capteurs peuvent "mesurer" des données environnementales aléatoires)
        if (!(a instanceof CapteurTemperature || a instanceof CapteurHumidite ||
              a instanceof CapteurQualiteAir || a instanceof CapteurDebitEau))
            throw new AppareilException("Ce n'est pas un capteur !"); // Erreur si on essaie de mesurer sur une lampe

        String valeur = a.mesurer(); // polymorphisme : chaque capteur exécute sa propre version de mesurer()

        // Sauvegarde historique
        historiques.putIfAbsent(a.getNom(), new ArrayList<>()); // Crée la liste si elle n'existe pas encore pour cet appareil
        String ligne = new SimpleDateFormat("HH:mm:ss").format(new Date()) + " -> " + valeur; // Formate "Heure -> Valeur"
        historiques.get(a.getNom()).add(ligne); // Ajoute à l'historique

        logs.add("Mesure effectuée : " + a.getNom() + " = " + valeur); // Log global

        return valeur; // Retourne le résultat pour l'affichage
    }

    // ====================== CONTRÔLE ACTIONNEURS ======================
    public String actionOn(AppareilConnecte a) { // Action générique "Allumer/Activer/Augmenter"
        try {
            if (a instanceof ActionneurLumiere) { // Si c'est une lumière
                ((ActionneurLumiere) a).setAllume(true); // Cast et allumage
                logs.add("Lumière allumée : " + a.getNom()); // Log
                return "Lumière allumée"; // Retour utilisateur
            } else if (a instanceof ActionneurVolet) { // Si c'est un volet
                ((ActionneurVolet) a).setOuvert(true); // Cast et ouverture
                logs.add("Volet monté : " + a.getNom());
                return "Volet monté";
            } else if (a instanceof ActionneurVentilation) { // Si c'est une ventil
                ((ActionneurVentilation) a).niveauSuivant(); // Cast et niveau +1
                logs.add("Ventilation niveau augmenté : " + a.getNom());
                return "Ventilation niveau augmenté";
            } else if (a instanceof CameraIP) { // Si c'est une caméra
                ((CameraIP) a).setActive(true); // Cast et activation
                logs.add("Caméra activée : " + a.getNom());
                return "Caméra activée";
            }
            return "Action impossible sur ce type d'appareil"; // Cas par défaut
        } catch (Exception e) {
            logs.add("Erreur action ON : " + e.getMessage()); // Gestion erreur
            return "Erreur : " + e.getMessage();
        }
    }

    public String actionOff(AppareilConnecte a) { // Action générique "Éteindre/Désactiver/Diminuer"
        try {
            if (a instanceof ActionneurLumiere) { // Lumière
                ((ActionneurLumiere) a).setAllume(false); // Éteindre
                logs.add("Lumière éteinte : " + a.getNom());
                return "Lumière éteinte";
            } else if (a instanceof ActionneurVolet) { // Volet
                ((ActionneurVolet) a).setOuvert(false); // Fermer
                logs.add("Volet descendu : " + a.getNom());
                return "Volet descendu";
            } else if (a instanceof ActionneurVentilation) { // Ventilation
                ((ActionneurVentilation) a).niveauPrecedent(); // Niveau -1
                logs.add("Ventilation niveau diminué : " + a.getNom());
                return "Ventilation niveau diminué";
            } else if (a instanceof CameraIP) { // Caméra
                ((CameraIP) a).setActive(false); // Désactiver
                logs.add("Caméra désactivée : " + a.getNom());
                return "Caméra désactivée";
            }
            return "Action impossible sur ce type d'appareil";
        } catch (Exception e) {
            logs.add("Erreur action OFF : " + e.getMessage());
            return "Erreur : " + e.getMessage();
        }
    }

    // ====================== VALEUR COURANTE ======================
    public String getValeur(AppareilConnecte a) { // Méthode pour récupérer la valeur à afficher dans le tableau
        try {
            // Capteurs : lire valeur stockée
            if (a instanceof CapteurTemperature || a instanceof CapteurHumidite ||
                a instanceof CapteurQualiteAir || a instanceof CapteurDebitEau) {
                return a.getValeurStockee(); // Retourne la dernière mesure
            } 
            // Actionneurs : retourner l'état actuel formaté
            else if (a instanceof ActionneurLumiere)
                return ((ActionneurLumiere) a).isAllume() ? "ON" : "OFF";
            else if (a instanceof ActionneurVolet)
                return ((ActionneurVolet) a).isOuvert() ? "↑" : "↓";
            else if (a instanceof ActionneurVentilation)
                return "Niveau " + ((ActionneurVentilation) a).getNiveau();
            else if (a instanceof CameraIP)
                return ((CameraIP) a).isActive() ? "ACTIVÉE" : "DÉSACTIVÉE";
        } catch (Exception e) {
            return "Erreur";
        }
        return "-"; // Valeur par défaut
    }

    // ====================== DASHBOARD / CONFIG ======================
    public Map<String, String> getAllConfig() { // Méthode pour l'UI pour récupérer la config Excel
        try {
            return donnees.chargerXLSX(); // lecture config Excel via GestionDonnees
        } catch (ImportExportException e) {
            logs.add("Erreur lecture config XLSX : " + e.getMessage());
            return new HashMap<>(); // Retourne map vide en cas d'erreur
        }
    }

    // ====================== APPAREILS ACTIFS ======================
    public ArrayList<AppareilConnecte> getAppareilsActifs() { // Filtre pour n'obtenir que les appareils connectés
        ArrayList<AppareilConnecte> actifs = new ArrayList<>();
        for (AppareilConnecte a : appareils)
            if (a.isConnecte()) actifs.add(a); // Ajoute si connecté
        return actifs;
    }

    // ====================== SAUVEGARDE ======================
    private void sauvegarder() { // Méthode privée interne pour déclencher la sauvegarde
        try {
            donnees.sauvegarderJson(appareils); // Appel à GestionDonnees
        } catch (ImportExportException e) {
            logs.add("Erreur sauvegarde JSON : " + e.getMessage());
        }
    }

    // ====================== GETTERS ======================
    public ArrayList<AppareilConnecte> getAppareils() { return appareils; } // Getter liste appareils

    public ArrayList<String> getHistorique(String nom) { // Getter historique spécifique
        return historiques.getOrDefault(nom, new ArrayList<>()); // Retourne liste vide si pas d'historique
    }

    public ArrayList<String> getLogs() { return logs; } // Getter logs globaux

    public void ajouterHistorique(String nom, String action) { // Méthode pour ajouter manuellement une entrée historique
        historiques.putIfAbsent(nom, new ArrayList<>());
        String ligne = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + " -> " + action;
        historiques.get(nom).add(ligne);
    }

    public void sauvegarderAppareils() throws ImportExportException { // Méthode publique pour forcer la sauvegarde
        donnees.sauvegarderJson(appareils); // sérialise la liste complète avec états
    }
}