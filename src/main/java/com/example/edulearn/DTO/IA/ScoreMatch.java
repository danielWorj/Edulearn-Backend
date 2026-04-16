package com.example.edulearn.DTO.IA;

import lombok.Data;

@Data
public class ScoreMatch {
    private String nomEnseignant;
    private String matiere;
    private String descriptionOffre;
    private double scoreRecupere;

    public ScoreMatch() {
    }

    public ScoreMatch(String nomEnseignant, String matiere, String descriptionOffre, double scoreRecupere) {
        this.nomEnseignant = nomEnseignant;
        this.matiere = matiere;
        this.descriptionOffre = descriptionOffre;
        this.scoreRecupere = scoreRecupere;
    }
}
