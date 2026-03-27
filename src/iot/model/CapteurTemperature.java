package iot.model;

import java.util.Random;

public class CapteurTemperature extends AppareilConnecte {

    public CapteurTemperature(String nom) {
        super(nom);
    }

    @Override
    public String getType() {
        return "CapteurTemperature";
    }

    @Override
    public String mesurer() throws AppareilException {
        if (!connecte)
            throw new MesureException("Mesure impossible : capteur température non connecté");
        this.valeur = (15 + new Random().nextInt(21)) + " °C";  
        return this.valeur;
    }}
