package com.example.edulearn.DTO.Repetition;

import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class RepetitionDTO {
    private Integer id ;
    private Integer enseignant ;
    private Integer offreRepetition ;
    private Integer montant ;
}
