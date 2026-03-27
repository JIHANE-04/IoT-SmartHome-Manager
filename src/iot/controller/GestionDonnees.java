//Gestion des fichiers JSON et XLSX, Factory POO
package iot.controller; // Définition du package contrôleur

import iot.model.*; 

// Utilitaires Java
import java.util.*; // Importe les collections (List, Map, etc.)
import java.io.*; // Importe les classes pour la gestion des fichiers (File, FileReader, FileWriter)


import com.google.gson.*; 
import com.google.gson.reflect.TypeToken; 
import java.lang.reflect.Type; 

// Bibliothèque Apache POI pour Excel XLSX
import org.apache.poi.ss.usermodel.*; // Importe les interfaces communes de POI (Workbook, Sheet, Row, Cell)
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // Importe l'implémentation spécifique pour le format XLSX (Excel 2007+)

public class GestionDonnees { // Déclaration de la classe de gestion des données

    // Chemin du fichier JSON pour sauvegarder les appareils
    private final String FILE_JSON  = "assets/appareils.json"; // Constante définissant le chemin du fichier de sauvegarde
    // Chemin du fichier XLSX pour config
    private final String XLSX_FILE = "assets/config.xlsx"; // Constante définissant le chemin du fichier de configuration Excel

    // ======= FACTORY =======
    // Méthode statique pour créer un appareil selon son type
    public static AppareilConnecte factory(String type, String nom) { // Méthode Factory Pattern : retourne une instance spécifique selon le type string
        if(type == null || nom == null) return null; // sécurité si valeurs null : on ne crée rien
        return switch (type) { // Expression Switch (Java récent) pour déterminer quelle classe instancier
            case "CapteurTemperature" -> new CapteurTemperature(nom); // crée un capteur de température
            case "CapteurHumidite" -> new CapteurHumidite(nom); // capteur d'humidité
            case "CapteurQualiteAir" -> new CapteurQualiteAir(nom); // capteur qualité air
            case "CapteurDebitEau" -> new CapteurDebitEau(nom); // capteur débit eau
            case "ActionneurLumiere" -> new ActionneurLumiere(nom); // actionneur lumière
            case "ActionneurVolet" -> new ActionneurVolet(nom); // actionneur volet
            case "ActionneurVentilation" -> new ActionneurVentilation(nom); // ventilation
            case "CameraIP" -> new CameraIP(nom); // caméra IP
            default -> null; // type inconnu : retourne null
        };
    }

    // ======= SAUVEGARDE JSON =======
    public void sauvegarderJson(List<AppareilConnecte> appareils) throws ImportExportException { // Méthode pour écrire la liste des appareils dans le fichier JSON
        try (FileWriter fw = new FileWriter(FILE_JSON)) { // ouverture automatique du flux avec try-with-resources (fermeture garantie)

            List<Map<String, Object>> listeMap = new ArrayList<>(); // liste intermédiaire pour Gson (transforme objets en Maps clé-valeur)

            // Pour chaque appareil, créer une Map avec ses propriétés
            for(AppareilConnecte a : appareils) { // Boucle sur tous les appareils de la liste
                Map<String, Object> map = new HashMap<>(); // Crée une nouvelle map pour l'appareil courant
                map.put("id", a.getId()); // Sauvegarde l'ID
                map.put("nom", a.getNom()); // Sauvegarde le nom
                map.put("connecte", a.isConnecte()); // Sauvegarde l'état de connexion
                map.put("manuel", a.isManuel()); // Sauvegarde le flag manuel
                map.put("type", a.getType()); // type de l'appareil (essentiel pour la reconstruction)

                map.put("valeur", a.getValeurStockee()); // valeur actuelle (dernière mesure ou état)

                // Champs spécifiques selon le type (Cast et vérification de type)
                if(a instanceof ActionneurLumiere l) map.put("allume", l.isAllume()); // Si c'est une lumière, sauvegarde l'état booléen
                if(a instanceof ActionneurVolet v) map.put("ouvert", v.isOuvert()); // Si c'est un volet, sauvegarde ouvert/fermé
                if(a instanceof ActionneurVentilation vent) map.put("niveau", vent.getNiveau()); // Si ventilation, sauvegarde le niveau entier
                if(a instanceof CameraIP c) map.put("active", c.isActive()); // Si caméra, sauvegarde actif/inactif

                listeMap.add(map); // ajouter la map remplie à la liste globale
            }

            // Création de Gson avec indentation
            Gson g = new GsonBuilder().setPrettyPrinting().create(); // Configure Gson pour rendre le JSON lisible (sauts de ligne)
            g.toJson(listeMap, fw); // sérialisation JSON dans le fichier via le FileWriter

        } catch (Exception e) { // Capture toute erreur d'écriture
            throw new ImportExportException("Erreur sauvegarde JSON : " + e.getMessage()); // Relance une exception métier personnalisée
        }
    }

    // ======= CHARGEMENT JSON =======
    public List<AppareilConnecte> chargerJson() throws ImportExportException { // Méthode pour lire le fichier JSON et recréer les objets
        List<AppareilConnecte> liste = new ArrayList<>(); // Liste vide pour accueillir les résultats
        File f = new File(FILE_JSON); // Objet fichier pointant vers le JSON

        if (!f.exists()) // Vérifie si le fichier existe physiquement
            throw new ImportExportException("Fichier JSON introuvable"); // sécurité si fichier absent : lève une exception

        try (Reader reader = new FileReader(f)) { // ouverture flux lecture avec try-with-resources
            Gson gson = new Gson(); // Instance de Gson

            // On définit le type générique pour Gson (liste de maps) car Gson ne peut pas deviner le type List<Map> seul
            Type typeListe = new TypeToken<ArrayList<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> data = gson.fromJson(reader, typeListe); // Désérialise le fichier en une structure de données Java brute
            if (data == null) return liste; // Si le fichier était vide ou invalide, retourne liste vide

            int maxId = 0; // pour mettre à jour le compteur global et éviter les conflits d'ID futurs

            // Parcours des données JSON
            for (Map<String, Object> map : data) { // Boucle sur chaque élément récupéré
                if (map == null) continue; // Ignore les éléments nuls

                Object idObj = map.get("id"); // Récupère l'objet ID
                Object nomObj = map.get("nom"); // Récupère l'objet Nom
                Object typeObj = map.get("type"); // Récupère l'objet Type
                Object actifObj = map.get("connecte"); // Récupère l'état connecté
                Object valObj = map.get("valeur"); // Récupère la valeur

                if (typeObj == null || nomObj == null) continue; // sécurité : si données critiques manquantes, on passe

                int id = idObj != null ? ((Double) idObj).intValue() : 0; // conversion Double->int (Gson lit les nombres comme Double par défaut)
                String nom = (String) nomObj; // Cast en String
                String type = (String) typeObj; // Cast en String
                boolean actif = actifObj != null && (Boolean) actifObj; // Cast en boolean avec vérification null

                // Création appareil via factory
                AppareilConnecte a = factory(type, nom); // Appelle la Factory pour obtenir la bonne instance de classe
                if (a != null) { // Si la factory a réussi
                    a.setId(id); // on remet l'ID JSON (restauration de l'état précédent)

                    if (valObj != null) {
                        a.setValeur((String) valObj); // valeur stockée
                    }

                    // Restaurer champs spécifiques (Cast de l'appareil ET récupération de la valeur spécifique dans la map)
                    if (a instanceof ActionneurLumiere l && map.get("allume") != null)
                        l.setAllume((Boolean) map.get("allume")); // Restaure état lumière
                    if (a instanceof ActionneurVolet v && map.get("ouvert") != null)
                        v.setOuvert((Boolean) map.get("ouvert")); // Restaure état volet
                    if (a instanceof ActionneurVentilation vent && map.get("niveau") != null)
                        vent.setNiveau(((Double) map.get("niveau")).intValue()); // Restaure niveau ventilation (Double -> int)
                    if (a instanceof CameraIP c && map.get("active") != null)
                        c.setActive((Boolean) map.get("active")); // Restaure état caméra

                    if (actif) a.connecter(); // connecter si actif dans le fichier
                    liste.add(a); // Ajoute l'appareil reconstruit à la liste finale

                    if (id > maxId) maxId = id; // mettre à jour le compteur max trouvé
                }
            }

            AppareilConnecte.setCompteur(maxId); // mise à jour compteur global statique dans la classe mère

        } catch (Exception e) { // Capture erreurs lecture/parsing
            throw new ImportExportException("Erreur lecture JSON : " + e.getMessage()); // Relance exception métier
        }

        return liste; // Retourne la liste complète
    }

    // ======= CHARGEMENT XLSX =======
    public Map<String, String> chargerXLSX() throws ImportExportException { // Méthode pour lire la config Excel
        Map<String, String> config = new HashMap<>(); // Map pour stocker Clé/Valeur de config
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(XLSX_FILE))) { // ouverture fichier Excel en lecture
            Sheet sheet = wb.getSheetAt(0); // accès à la première feuille du classeur
            for (Row row : sheet) { // parcours des lignes de la feuille
                Cell key = row.getCell(0); // première colonne (Clé)
                Cell val = row.getCell(1); // deuxième colonne (Valeur)
                if (key != null && val != null) // Si la ligne est valide
                    config.put(key.getStringCellValue(), val.toString()); // clé/valeur ajoutée à la map
            }
        } catch (Exception e) { // Capture erreurs POI
            throw new ImportExportException("Erreur lecture XLSX : " + e.getMessage()); // Relance exception métier
        }
        return config; // Retourne la configuration chargée
    }
}