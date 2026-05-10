package com.example.edulearn.DTO.IA;

import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class MatchinResult {
    @JsonIgnoreProperties("hibernateLazyInitializer")
    private Enseignant enseignant;
    private Double score ;

    public MatchinResult(Enseignant enseignant, Double score) {
        this.enseignant = enseignant;
        this.score = score;
    }

    public MatchinResult() {
    }
}
