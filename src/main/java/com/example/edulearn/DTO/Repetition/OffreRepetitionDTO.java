package com.example.edulearn.DTO.Repetition;

import com.example.edulearn.ENTITY.Utilisateur.Parent;
import lombok.Data;

@Data
public class OffreRepetitionDTO {
    private Integer id ;
    private String intitule;
    private String bio;
    private String salaireMin ;
    private String salaireMax ;
    private String dateCreation;
    private Integer frequence;
    private String duree;
    private Integer eleve ;
}
