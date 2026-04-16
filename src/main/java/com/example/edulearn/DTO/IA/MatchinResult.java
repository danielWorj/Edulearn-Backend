package com.example.edulearn.DTO.IA;

import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import lombok.Data;

@Data
public class MatchinResult {
    private Enseignant enseignant;
    private Double score ;

    public MatchinResult(Enseignant enseignant, Double score) {
        this.enseignant = enseignant;
        this.score = score;
    }

    public MatchinResult() {
    }
}
